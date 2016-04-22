/*
 * Copyright 2016 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.opcua.sdk.client;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.client.api.identity.UsernameProvider;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscription;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscriptionManager.SubscriptionListener;
import com.digitalpetri.opcua.sdk.server.OpcUaServer;
import com.digitalpetri.opcua.sdk.server.api.config.OpcUaServerConfig;
import com.digitalpetri.opcua.sdk.server.identity.UsernameIdentityValidator;
import com.digitalpetri.opcua.server.ctt.CttNamespace;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.UaServiceFaultException;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoringParameters;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;
import com.digitalpetri.opcua.stack.core.types.structured.UserTokenPolicy;
import com.digitalpetri.opcua.stack.server.tcp.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class OpcUaClientIT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OpcUaClient client;
    private OpcUaServer server;

    @BeforeTest
    public void startClientAndServer() throws Exception {
        logger.info("startClientAndServer()");

        UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(
            true, // allow anonymous access
            challenge -> {
                String user0 = "user";
                String pass0 = "password";

                char[] cs = new char[1000];
                Arrays.fill(cs, 'a');
                String user1 = new String(cs);
                String pass1 = new String(cs);

                boolean match0 = user0.equals(challenge.getUsername()) &&
                    pass0.equals(challenge.getPassword());

                boolean match1 = user1.equals(challenge.getUsername()) &&
                    pass1.equals(challenge.getPassword());

                return match0 || match1;
            }
        );

        List<UserTokenPolicy> userTokenPolicies = newArrayList(
            OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS,
            OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME
        );

        KeyStoreLoader loader = new KeyStoreLoader().load();

        TestCertificateManager certificateManager = new TestCertificateManager(
            loader.getServerKeyPair(),
            loader.getServerCertificate()
        );

        TestCertificateValidator certificateValidator = new TestCertificateValidator(
            loader.getClientCertificate()
        );

        OpcUaServerConfig serverConfig = OpcUaServerConfig.builder()
            .setApplicationName(LocalizedText.english("digitalpetri opc-ua server"))
            .setApplicationUri("urn:digitalpetri:opcua:server")
            .setBindAddresses(newArrayList("localhost"))
            .setBindPort(12686)
            .setCertificateManager(certificateManager)
            .setCertificateValidator(certificateValidator)
            .setSecurityPolicies(EnumSet.of(SecurityPolicy.None, SecurityPolicy.Basic128Rsa15))
            .setProductUri("urn:digitalpetri:opcua:sdk")
            .setServerName("test-server")
            .setUserTokenPolicies(userTokenPolicies)
            .setIdentityValidator(identityValidator)
            .build();

        server = new OpcUaServer(serverConfig);

        // register a CttNamespace so we have some nodes to play with
        server.getNamespaceManager().registerAndAdd(
            CttNamespace.NAMESPACE_URI,
            idx -> new CttNamespace(server, idx));

        server.startup();

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:12686/test-server").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
            .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
            .setApplicationUri("urn:digitalpetri:opcua:client")
            .setEndpoint(endpoint)
            .setRequestTimeout(uint(60000))
            .build();

        client = new OpcUaClient(clientConfig);
    }

    @AfterTest
    public void stopClientAndServer() {
        logger.info("stopClientAndServer()");

        try {
            client.disconnect().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Error disconnecting client.", e);
        }
        server.shutdown();
        SocketServer.shutdownAll();
    }

    @Test
    public void testRead() throws Exception {
        logger.info("testRead()");

        UaVariableNode currentTimeNode = client.getAddressSpace()
            .getVariableNode(Identifiers.Server_ServerStatus_CurrentTime);

        assertNotNull(currentTimeNode.readValueAttribute().get());
    }

    @Test
    public void testWrite() throws Exception {
        logger.info("testWrite()");

        NodeId nodeId = new NodeId(2, "/Static/AllProfiles/Scalar/Int32");

        UaVariableNode variableNode = client.getAddressSpace().getVariableNode(nodeId);

        // read the existing value
        Object valueBefore = variableNode.readValueAttribute().get();
        assertNotNull(valueBefore);

        // write a new random value
        DataValue newValue = new DataValue(new Variant(new Random().nextInt()));
        StatusCode writeStatus = variableNode.writeValue(newValue).get();
        assertTrue(writeStatus.isGood());

        // read the value again
        Object valueAfter = variableNode.readValueAttribute().get();
        assertNotNull(valueAfter);

        assertNotEquals(valueBefore, valueAfter);
    }

    @Test
    public void testSubscribe() throws Exception {
        logger.info("testSubscribe()");

        // create a subscription and a monitored item
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        ReadValueId readValueId = new ReadValueId(
            Identifiers.Server_ServerStatus_CurrentTime,
            AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

        MonitoringParameters parameters = new MonitoringParameters(
            uint(1),    // client handle
            1000.0,     // sampling interval
            null,       // no (default) filter
            uint(10),   // queue size
            true);      // discard oldest

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
            readValueId, MonitoringMode.Reporting, parameters);

        List<UaMonitoredItem> items = subscription
            .createMonitoredItems(TimestampsToReturn.Both, newArrayList(request)).get();

        // do something with the value updates
        UaMonitoredItem item = items.get(0);

        CompletableFuture<DataValue> f = new CompletableFuture<>();
        item.setValueConsumer(f::complete);

        assertNotNull(f.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testTransferSubscriptions() throws Exception {
        logger.info("testTransferSubscriptions()");

        // create a subscription and a monitored item
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        NodeId nodeId = new NodeId(2, "/Static/AllProfiles/Scalar/Int32");

        ReadValueId readValueId = new ReadValueId(
            nodeId, AttributeId.Value.uid(),
            null, QualifiedName.NULL_VALUE);

        MonitoringParameters parameters = new MonitoringParameters(
            uint(1),    // client handle
            100.0,      // sampling interval
            null,       // no (default) filter
            uint(10),   // queue size
            true);      // discard oldest

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
            readValueId, MonitoringMode.Reporting, parameters);

        List<UaMonitoredItem> items = subscription
            .createMonitoredItems(TimestampsToReturn.Both, newArrayList(request)).get();

        // do something with the value updates
        UaMonitoredItem item = items.get(0);

        AtomicInteger updateCount = new AtomicInteger(0);

        item.setValueConsumer(v -> {
            int count = updateCount.incrementAndGet();
            logger.info("updateCount={}", count);
        });

        AtomicBoolean subscriptionTransferred = new AtomicBoolean(true);

        client.getSubscriptionManager().addSubscriptionListener(new SubscriptionListener() {
            @Override
            public void onKeepAlive(UaSubscription subscription, DateTime publishTime) {

            }

            @Override
            public void onStatusChanged(UaSubscription subscription, StatusCode status) {

            }

            @Override
            public void onPublishFailure(UaException exception) {

            }

            @Override
            public void onNotificationDataLost(UaSubscription subscription) {

            }

            @Override
            public void onSubscriptionTransferFailed(UaSubscription subscription, StatusCode statusCode) {
                subscriptionTransferred.set(false);
            }
        });

        logger.info("killing the session...");
        UaSession uaSession = client.getSession().get();
        server.getSessionManager().killSession(uaSession.getSessionId(), false);

        logger.info("sleeping while waiting for an update");
        Thread.sleep(5000);

        // one update for the initial subscribe, another after transfer
        assertEquals(updateCount.get(), 2);

        assertTrue(subscriptionTransferred.get());

        client.disconnect().get();
    }

    @Test
    public void testUsernamePassword() throws Exception {
        logger.info("testUsernamePassword()");

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:12686/test-server").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
            .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        KeyStoreLoader loader = new KeyStoreLoader().load();

        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
            .setApplicationUri("urn:digitalpetri:opcua:client")
            .setCertificate(loader.getClientCertificate())
            .setKeyPair(loader.getClientKeyPair())
            .setEndpoint(endpoint)
            .setRequestTimeout(uint(60000))
            .setIdentityProvider(new UsernameProvider("user", "password"))
            .build();

        OpcUaClient client = new OpcUaClient(clientConfig);

        client.connect().get();
    }

    /**
     * Test using a username and password long enough that the encryption requires multiple ciphertext blocks.
     *
     * @throws Exception
     */
    @Test
    public void testUsernamePassword_MultiBlock() throws Exception {
        logger.info("testUsernamePassword_MultiBlock()");

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:12686/test-server").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
            .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        char[] cs = new char[1000];
        Arrays.fill(cs, 'a');
        String user = new String(cs);
        String pass = new String(cs);

        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
            .setApplicationUri("urn:digitalpetri:opcua:client")
            .setEndpoint(endpoint)
            .setRequestTimeout(uint(60000))
            .setIdentityProvider(new UsernameProvider(user, pass))
            .build();

        OpcUaClient client = new OpcUaClient(clientConfig);

        client.connect().get();
    }

    @Test
    public void testConnectAndDisconnect() throws Exception {
        logger.info("testConnectAndDisconnect()");

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:12686/test-server").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
            .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        class ConnectDisconnect implements Runnable {
            private final int threadNumber;

            private ConnectDisconnect(int threadNumber) {
                this.threadNumber = threadNumber;
            }

            private OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
                    .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
                    .setApplicationUri("urn:digitalpetri:opcua:client")
                    .setEndpoint(endpoint)
                    .setRequestTimeout(uint(10000))
                    .build();

            private OpcUaClient client = new OpcUaClient(clientConfig);

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        client.connect().get();

                        client.readValues(
                                0.0,
                                TimestampsToReturn.Both,
                                newArrayList(Identifiers.Server_ServerStatus_CurrentTime)
                        ).get();

                        client.disconnect().get();

                        Thread.sleep(10);
                    } catch (InterruptedException | ExecutionException e) {
                        fail(e.getMessage(), e);
                    }
                }
                logger.info("Thread {} done.", threadNumber);
            }
        }

        Thread[] threads = new Thread[4];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new ConnectDisconnect(i));
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    public void testReactivate() throws Exception {
        logger.info("testReactivate()");

        EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints("opc.tcp://localhost:12686/test-server").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
            .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
            .setApplicationUri("urn:digitalpetri:opcua:client")
            .setEndpoint(endpoint)
            .setRequestTimeout(uint(10000))
            .build();

        OpcUaClient client = new OpcUaClient(clientConfig);

        UaVariableNode currentTimeNode = client.getAddressSpace()
            .getVariableNode(Identifiers.Server_ServerStatus_CurrentTime);

        assertNotNull(currentTimeNode.readValueAttribute().get());

        // Kill the session. Client can't and won't be notified of this.
        logger.info("killing session...");
        UaSession session = client.getSession().get();
        server.getSessionManager().killSession(session.getSessionId(), true);

        // Expect the next action to fail because the session is no longer valid.
        try {
            logger.info("reading, expecting failure...");
            currentTimeNode.readValueAttribute().get();
        } catch (Throwable t) {
            StatusCode statusCode = UaServiceFaultException.extract(t)
                .map(UaException::getStatusCode)
                .orElse(StatusCode.BAD);

            assertEquals(statusCode.getValue(), StatusCodes.Bad_SessionIdInvalid);
        }

        Thread.sleep(1000);

        // Force a reactivate and read.
        logger.info("reconnecting...");
        client.connect().get();

        logger.info("reading, expecting success...");
        assertNotNull(currentTimeNode.readValueAttribute().get());

        client.disconnect().get();
    }

}
