package com.digitalpetri.opcua.sdk.client.fsm.states;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.OpcUaSession;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.SessionState;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateEvent;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateFsm;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.channel.ClientSecureChannel;
import com.digitalpetri.opcua.stack.core.security.SecurityAlgorithm;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.structured.ActivateSessionRequest;
import com.digitalpetri.opcua.stack.core.types.structured.ActivateSessionResponse;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.SignatureData;
import com.digitalpetri.opcua.stack.core.types.structured.SignedSoftwareCertificate;
import com.digitalpetri.opcua.stack.core.types.structured.UserIdentityToken;
import com.digitalpetri.opcua.stack.core.util.SignatureUtil;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactivateExecute implements SessionState {

    private static final int MAX_REACTIVATE_DELAY_SECONDS = 16;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile OpcUaSession session;

    private final CompletableFuture<UaSession> sessionFuture;
    private final UaSession previousSession;
    private final long delaySeconds;

    public ReactivateExecute(CompletableFuture<UaSession> sessionFuture,
                             UaSession previousSession,
                             long delaySeconds) {

        this.sessionFuture = sessionFuture;
        this.previousSession = previousSession;
        this.delaySeconds = delaySeconds;
    }

    @Override
    public CompletableFuture<Void> activate(SessionStateEvent event, SessionStateFsm fsm) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        logger.debug("Reactivating session...");

        activateSession(fsm, previousSession).whenComplete((asr, ex) -> {
            if (asr != null) {
                session = new OpcUaSession(
                        previousSession.getAuthenticationToken(),
                        previousSession.getSessionId(),
                        fsm.getClient().getConfig().getSessionName().get(),
                        previousSession.getSessionTimeout(),
                        previousSession.getMaxRequestSize(),
                        previousSession.getServerCertificate(),
                        previousSession.getServerSoftwareCertificates());

                session.setServerNonce(asr.getServerNonce());

                fsm.handleEvent(SessionStateEvent.ReactivateSucceeded);
            } else {
                StatusCode statusCode = UaException.extract(ex)
                        .map(UaException::getStatusCode)
                        .orElse(StatusCode.BAD);

                if (statusCode.getValue() == StatusCodes.Bad_SessionIdInvalid ||
                        statusCode.getValue() == StatusCodes.Bad_SessionClosed ||
                        statusCode.getValue() == StatusCodes.Bad_SessionNotActivated ||
                        statusCode.getValue() == StatusCodes.Bad_SecurityChecksFailed) {

                    // Treat any session-related errors as re-activate failed.
                    fsm.handleEvent(SessionStateEvent.ErrReactivateInvalid);
                } else {
                    fsm.handleEvent(SessionStateEvent.ErrReactivateFailed);
                }

                sessionFuture.completeExceptionally(ex);
            }

            f.complete(null);
        });

        return f;
    }

    private CompletableFuture<ActivateSessionResponse> activateSession(SessionStateFsm context, UaSession session) {
        OpcUaClient client = context.getClient();
        UaTcpStackClient stackClient = client.getStackClient();

        return stackClient.getChannelFuture().thenCompose(secureChannel -> {
            try {
                EndpointDescription endpoint = stackClient.getEndpoint()
                        .orElseThrow(() -> new Exception("cannot create session with no endpoint configured"));

                Tuple2<UserIdentityToken, SignatureData> tuple =
                        client.getConfig().getIdentityProvider().getIdentityToken(endpoint, session.getServerNonce());

                UserIdentityToken userIdentityToken = tuple.v1();
                SignatureData userTokenSignature = tuple.v2();

                SignatureData clientSignature = buildClientSignature(
                        secureChannel,
                        session.getServerCertificate(),
                        session.getServerNonce());

                ActivateSessionRequest request = new ActivateSessionRequest(
                        client.newRequestHeader(session.getAuthenticationToken()),
                        clientSignature,
                        new SignedSoftwareCertificate[0],
                        new String[0],
                        ExtensionObject.encode(userIdentityToken),
                        userTokenSignature);

                logger.debug("Sending ActivateSessionRequest...");

                return stackClient.sendRequest(request);
            } catch (Exception e) {
                CompletableFuture<ActivateSessionResponse> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        });
    }

    private SignatureData buildClientSignature(ClientSecureChannel secureChannel,
                                               ByteString serverCertificate,
                                               ByteString serverNonce) {

        byte[] serverNonceBytes = Optional.ofNullable(serverNonce.bytes()).orElse(new byte[0]);
        byte[] serverCertificateBytes = Optional.ofNullable(serverCertificate.bytes()).orElse(new byte[0]);

        // Signature is serverCert + serverNonce signed with our private key.
        byte[] signature = new byte[serverCertificateBytes.length + serverNonceBytes.length];
        System.arraycopy(serverCertificateBytes, 0, signature, 0, serverCertificateBytes.length);
        System.arraycopy(serverNonceBytes, 0, signature, serverCertificateBytes.length, serverNonceBytes.length);

        SecurityAlgorithm signatureAlgorithm = secureChannel.getSecurityPolicy().getAsymmetricSignatureAlgorithm();

        if (secureChannel.getSecurityPolicy() != SecurityPolicy.None) {
            try {
                PrivateKey privateKey = secureChannel.getKeyPair().getPrivate();

                signature = SignatureUtil.sign(
                        signatureAlgorithm,
                        privateKey,
                        ByteBuffer.wrap(signature)
                );
            } catch (Throwable t) {
                logger.warn("Asymmetric signing failed: {}", t.getMessage(), t);
            }
        }

        return new SignatureData(signatureAlgorithm.getUri(), ByteString.of(signature));
    }

    @Override
    public SessionState transition(SessionStateEvent event, SessionStateFsm fsm) {
        switch (event) {
            case DisconnectRequested:
                return new ClosingSession(session);

            case ReactivateSucceeded:
                return new Active(session, sessionFuture);

            case ErrReactivateFailed:
                return new ReactivateDelay(previousSession, nextDelay());

            case ErrReactivateInvalid:
                return new CreatingSession(new CompletableFuture<>());
        }

        return this;
    }

    private long nextDelay() {
        if (delaySeconds == 0) {
            return 1;
        } else {
            return Math.min(delaySeconds << 1, MAX_REACTIVATE_DELAY_SECONDS);
        }
    }

    @Override
    public CompletableFuture<UaSession> getSessionFuture() {
        return sessionFuture;
    }

}
