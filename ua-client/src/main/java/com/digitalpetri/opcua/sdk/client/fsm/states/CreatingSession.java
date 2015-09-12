/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.client.fsm.states;

import java.security.cert.CertificateEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.SessionState;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateEvent;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateFsm;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.structured.CreateSessionRequest;
import com.digitalpetri.opcua.stack.core.types.structured.CreateSessionResponse;
import com.digitalpetri.opcua.stack.core.util.NonceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatingSession implements SessionState {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile CreateSessionResponse csr;

    private final CompletableFuture<UaSession> future;

    public CreatingSession(CompletableFuture<UaSession> future) {
        this.future = future;
    }

    @Override
    public CompletableFuture<Void> activate(SessionStateEvent event, SessionStateFsm fsm) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        createSession(fsm).whenComplete((csr, ex) -> {
            if (csr != null) {
                this.csr = csr;
                fsm.handleEvent(SessionStateEvent.CreateSucceeded);
            } else {
                fsm.handleEvent(SessionStateEvent.ErrCreateFailed);
                future.completeExceptionally(ex);
            }

            f.complete(null);
        });

        return f;
    }

    private CompletableFuture<CreateSessionResponse> createSession(SessionStateFsm fsm) {
        OpcUaClient client = fsm.getClient();
        UaTcpStackClient stackClient = client.getStackClient();

        String serverUri = stackClient.getEndpoint().flatMap(e -> {
            String gatewayServerUri = e.getServer().getGatewayServerUri();
            if (gatewayServerUri != null && !gatewayServerUri.isEmpty()) {
                return Optional.ofNullable(e.getServer().getApplicationUri());
            } else {
                return Optional.empty();
            }
        }).orElse(null);

        ByteString clientNonce = NonceUtil.generateNonce(32);

        ByteString clientCertificate = stackClient.getConfig().getCertificate().map(c -> {
            try {
                return ByteString.of(c.getEncoded());
            } catch (CertificateEncodingException e) {
                return ByteString.NULL_VALUE;
            }
        }).orElse(ByteString.NULL_VALUE);

        CreateSessionRequest request = new CreateSessionRequest(
                client.newRequestHeader(),
                stackClient.getApplication(),
                serverUri,
                stackClient.getEndpointUrl(),
                client.getConfig().getSessionName().get(),
                clientNonce,
                clientCertificate,
                client.getConfig().getSessionTimeout().doubleValue(),
                client.getConfig().getMaxResponseMessageSize());

        logger.debug("Sending CreateSessionRequest...");

        return stackClient.sendRequest(request);
    }


    @Override
    public SessionState transition(SessionStateEvent event, SessionStateFsm fsm) {
        switch (event) {
            case CreateSucceeded:
                return new ActivatingSession(future, csr);

            case ErrCreateFailed:
            case DisconnectRequested:
                return new Inactive();
        }

        return this;
    }

    @Override
    public CompletableFuture<UaSession> getSessionFuture() {
        return future;
    }

    @Override
    public String toString() {
        return "CreatingSession{}";
    }

}
