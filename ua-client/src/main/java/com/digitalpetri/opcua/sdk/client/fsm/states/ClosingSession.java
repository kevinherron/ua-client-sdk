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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.SessionState;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateEvent;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateFsm;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.structured.CloseSessionRequest;
import com.digitalpetri.opcua.stack.core.types.structured.RequestHeader;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ClosingSession implements SessionState {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Runnable> listeners = Lists.newCopyOnWriteArrayList();

    private final UaSession session;

    public ClosingSession(UaSession session) {
        this.session = session;
    }

    public void addCompletionListener(Runnable listener) {
        logger.debug("Adding completion listener: {}", listener);
        listeners.add(listener);
    }

    public void removeCompletionListener(Runnable listener) {
        logger.debug("Removing completion listener: {}", listener);
        listeners.remove(listener);
    }

    @Override
    public CompletableFuture<Void> activate(SessionStateEvent event, SessionStateFsm fsm) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        OpcUaClient client = fsm.getClient();
        UaTcpStackClient stackClient = client.getStackClient();

        RequestHeader requestHeader = new RequestHeader(
                session.getAuthenticationToken(),
                DateTime.now(),
                client.nextRequestHandle(),
                uint(0),
                null,
                uint(5000),
                null
        );

        CloseSessionRequest request = new CloseSessionRequest(requestHeader, true);

        stackClient.sendRequest(request).whenComplete((r, t) -> {
            if (r != null) {
                logger.debug("Session closed.");
            } else {
                logger.debug("Error closing session: {}", t.getMessage(), t);
            }
            disconnect(fsm, stackClient, f);
        });

        return f;
    }

    private void disconnect(SessionStateFsm fsm, UaTcpStackClient stackClient, CompletableFuture<Void> f) {
        stackClient.disconnect().whenComplete((c, ex) -> {
            logger.debug("Stack client disconnect complete.");
            fsm.handleEvent(SessionStateEvent.DisconnectSucceeded);
            f.complete(null);
        });
    }

    @Override
    public CompletableFuture<Void> deactivate(SessionStateEvent event, SessionStateFsm fsm) {
        listeners.forEach(r -> {
            logger.debug("Notifying completion listeners: {}", r);
            r.run();
        });

        return CF_VOID_COMPLETED;
    }

    @Override
    public SessionState transition(SessionStateEvent event, SessionStateFsm fsm) {
        switch (event) {
            case DisconnectSucceeded:
                return new Inactive();

            case SessionRequested:
                return new CreatingSession(new CompletableFuture<>());
        }

        return this;
    }

    @Override
    public CompletableFuture<UaSession> getSessionFuture() {
        CompletableFuture<UaSession> f = new CompletableFuture<>();
        f.completeExceptionally(new UaException(StatusCodes.Bad_SessionClosed, "session is closed"));
        return f;
    }

    @Override
    public String toString() {
        return "ClosingSession{}";
    }

}
