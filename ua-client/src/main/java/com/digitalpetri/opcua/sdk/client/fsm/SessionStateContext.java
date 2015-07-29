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

package com.digitalpetri.opcua.sdk.client.fsm;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.states.Active;
import com.digitalpetri.opcua.sdk.client.fsm.states.Inactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

public class SessionStateContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<SessionStateListener> listeners = newCopyOnWriteArrayList();

    private final AtomicReference<SessionState> state = new AtomicReference<>(new Inactive());

    private final OpcUaClient client;

    public SessionStateContext(OpcUaClient client) {
        this.client = client;
    }

    public synchronized void handleEvent(SessionStateEvent event) {
        SessionState prevState = state.get();
        SessionState nextState = prevState.transition(event, this);

        logger.debug("S({}) x E({}) = S'({})",
                prevState.getClass().getSimpleName(), event, nextState.getClass().getSimpleName());

        if (nextState != prevState) {
            state.set(nextState);
            nextState.activate(event, this);
        }

        if (!isActive(prevState) && isActive(nextState)) {
            listeners.forEach(l -> l.onSessionActive(event));
        } else if (isActive(prevState) && !isActive(nextState)) {
            listeners.forEach(l -> l.onSessionInactive(event));
        }
    }

    private boolean isActive(SessionState state) {
        return state instanceof Active;
    }

    public synchronized CompletableFuture<UaSession> getSession() {
        if (!isActive()) {
            handleEvent(SessionStateEvent.SESSION_REQUESTED);
        }

        return state.get().getSessionFuture();
    }

    public boolean isActive() {
        return state.get() instanceof Active;
    }

    public OpcUaClient getClient() {
        return client;
    }

    public void addListener(SessionStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SessionStateListener listener) {
        listeners.remove(listener);
    }

    public interface SessionStateListener {

        /**
         * An activated {@link UaSession} is now available.
         *
         * @param event the {@link SessionStateEvent} that caused the state transition.
         */
        void onSessionActive(SessionStateEvent event);

        /**
         * The previously activated {@link UaSession} is no longer available.
         *
         * @param event the {@link SessionStateEvent} that caused the state transition.
         */
        void onSessionInactive(SessionStateEvent event);

    }

}
