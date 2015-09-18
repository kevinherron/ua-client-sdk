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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.SessionState;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateEvent;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateFsm;
import com.digitalpetri.opcua.stack.core.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactivateDelay implements SessionState {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CompletableFuture<UaSession> sessionFuture = new CompletableFuture<>();

    private volatile ScheduledFuture<?> scheduledFuture;

    private final UaSession previousSession;
    private final long delaySeconds;

    public ReactivateDelay(UaSession previousSession, long delaySeconds) {
        this.previousSession = previousSession;
        this.delaySeconds = delaySeconds;
    }

    @Override
    public CompletableFuture<Void> activate(SessionStateEvent event, SessionStateFsm fsm) {
        if (scheduledFuture == null || (scheduledFuture != null && scheduledFuture.cancel(false))) {
            logger.debug("Scheduling reactivate in {} seconds...", delaySeconds);

            scheduledFuture = Stack.sharedScheduledExecutor().schedule(() -> {
                logger.debug("{} seconds elapsed; requesting reactivate.", delaySeconds);

                fsm.handleEvent(SessionStateEvent.ReactivateRequested);
            }, delaySeconds, TimeUnit.SECONDS);
        }

        return CF_VOID_COMPLETED;
    }

    @Override
    public SessionState transition(SessionStateEvent event, SessionStateFsm fsm) {
        switch (event) {
            case DisconnectRequested:
                return new ClosingSession(previousSession);

            case ReactivateRequested:
                return new ReactivateExecute(sessionFuture, previousSession, delaySeconds);
        }

        return this;
    }

    @Override
    public CompletableFuture<UaSession> getSessionFuture() {
        return sessionFuture;
    }

    @Override
    public String toString() {
        return "Reactivating{" +
                "delaySeconds=" + delaySeconds +
                '}';
    }

}
