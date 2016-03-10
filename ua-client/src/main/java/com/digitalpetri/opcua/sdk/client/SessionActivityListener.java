package com.digitalpetri.opcua.sdk.client;

import com.digitalpetri.opcua.sdk.client.api.UaSession;

public interface SessionActivityListener {

    /**
     * An activated {@link UaSession} is now available.
     * <p>
     * Holding a reference to this session is not necessary or advised;
     * it is provided merely for informational purposes.
     */
    default void onSessionActive(UaSession session) {}

    /**
     * The previously activated {@link UaSession} is no longer available.
     * <p>
     * Holding a reference to this session is not necessary or advised;
     * it is provided merely for informational purposes.
     */
    default void onSessionInactive(UaSession session) {}

}
