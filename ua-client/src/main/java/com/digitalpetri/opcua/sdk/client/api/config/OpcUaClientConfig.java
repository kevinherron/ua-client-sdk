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

package com.digitalpetri.opcua.sdk.client.api.config;

import java.util.function.Supplier;

import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.stack.client.config.UaTcpStackClientConfig;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.structured.PublishRequest;

public interface OpcUaClientConfig extends UaTcpStackClientConfig {

    /**
     * @return a {@link Supplier} for the session name.
     */
    Supplier<String> getSessionName();

    /**
     * @return the session timeout, in milliseconds, to request.
     */
    UInteger getSessionTimeout();

    /**
     * @return the timeout, in milliseconds, before failing a request due to timeout.
     */
    UInteger getRequestTimeout();

    /**
     * @return the maximum size for a response from the server.
     */
    UInteger getMaxResponseMessageSize();

    /**
     * @return the maximum number of outstanding {@link PublishRequest}s allowed at any given time.
     */
    UInteger getMaxPendingPublishRequests();

    /**
     * @return an {@link IdentityProvider} to use when activating a session.
     */
    IdentityProvider getIdentityProvider();

    static OpcUaClientConfigBuilder builder() {
        return new OpcUaClientConfigBuilder();
    }

}
