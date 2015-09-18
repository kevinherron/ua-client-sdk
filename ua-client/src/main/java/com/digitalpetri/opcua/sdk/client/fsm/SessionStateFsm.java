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
import com.digitalpetri.opcua.stack.core.util.AsyncSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

public class SessionStateFsm {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<SessionStateListener> listeners = newCopyOnWriteArrayList();

    private final AsyncSemaphore semaphore = new AsyncSemaphore(1);

    private final AtomicReference<SessionState> state = new AtomicReference<>(new Inactive());

    private final OpcUaClient client;

    public SessionStateFsm(OpcUaClient client) {
        this.client = client;
    }

    public synchronized CompletableFuture<SessionState> handleEvent(SessionStateEvent event) {
        logger.debug("handleEvent({})", event);
        CompletableFuture<SessionState> future = new CompletableFuture<>();

        semaphore.acquire().thenAccept(permit -> {
            logger.debug("semaphore acquired - handleEvent({})", event);
            CompletableFuture<SessionState> f = handleEvent0(event);

            f.whenCompleteAsync((s, t) -> {
                SessionState p = state.getAndSet(s);

                maybeNotifyListeners(p, s, event);

                future.complete(s);
                permit.release();

                logger.debug("semaphore released - handleEvent({})", event);
            }, client.getConfig().getExecutor());
        });

        return future;
    }

    private CompletableFuture<SessionState> handleEvent0(SessionStateEvent event) {
        final SessionState currState = state.get();
        final SessionState nextState = currState.transition(event, this);

        logger.debug("S({}) x E({}) = S'({})", currState, event, nextState);

        if (currState != nextState) {
            logger.debug("deactivating S({})", currState);

            return currState.deactivate(event, this).thenCompose(vd -> {
                logger.debug("deactivated S({})", currState);
                logger.debug("activating S({})", nextState);

                return nextState.activate(event, this);
            }).thenApply(vd -> {
                logger.debug("activated S({})", nextState);

                return nextState;
            });
        } else {
            return CompletableFuture.completedFuture(currState);
        }
    }

    public synchronized CompletableFuture<UaSession> getSession() {
        return handleEvent(SessionStateEvent.SessionRequested)
                .thenCompose(SessionState::getSessionFuture);
    }

    public synchronized SessionState getState() {
        return state.get();
    }

    public OpcUaClient getClient() {
        return client;
    }

    private void maybeNotifyListeners(SessionState prevState, SessionState nextState, SessionStateEvent event) {
        if (isNotActive(prevState) && isActive(nextState)) {
            logger.debug("notifying SessionStateListeners state is active...");
            listeners.forEach(l -> l.onSessionActive(event));
        }
        if (isActive(prevState) && isNotActive(nextState)) {
            logger.debug("notifying SessionStateListeners state is not active...");
            listeners.forEach(l -> l.onSessionInactive(event));
        }
    }

    private boolean isActive(SessionState sessionState) {
        return sessionState instanceof Active;
    }

    private boolean isNotActive(SessionState sessionState) {
        return !isActive(sessionState);
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
