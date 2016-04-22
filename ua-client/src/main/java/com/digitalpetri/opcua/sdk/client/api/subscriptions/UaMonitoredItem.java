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

package com.digitalpetri.opcua.sdk.client.api.subscriptions;

import java.util.function.Consumer;

import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.ExtensionObject;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;

public interface UaMonitoredItem {

    /**
     * Get the client-assigned id.
     * <p>
     * This handle is used in the subscription to match incoming values to the corresponding monitored item.
     *
     * @return the client-assigned id.
     */
    UInteger getClientHandle();

    /**
     * Get the server-assigned id.
     *
     * @return the server-assigned id.
     */
    UInteger getMonitoredItemId();

    /**
     * Get the {@link ReadValueId}.
     *
     * @return the {@link ReadValueId}.
     */
    ReadValueId getReadValueId();

    /**
     * Get the {@link StatusCode} of the last operation.
     *
     * @return the {@link StatusCode} of the last operation.
     */
    StatusCode getStatusCode();

    /**
     * Get the revised sampling interval.
     *
     * @return the revised sampling interval.
     */
    double getRevisedSamplingInterval();

    /**
     * Get the revised queue size.
     *
     * @return the revised queue size.
     */
    UInteger getRevisedQueueSize();

    /**
     * Get the filter result {@link ExtensionObject}.
     *
     * @return the filter result {@link ExtensionObject}.
     */
    ExtensionObject getFilterResult();

    /**
     * Get the {@link MonitoringMode}.
     *
     * @return the {@link MonitoringMode}.
     */
    MonitoringMode getMonitoringMode();

    /**
     * Set the {@link Consumer} that will receive values as they arrive from the server.
     *
     * @param valueConsumer the {@link Consumer} that will receive values as they arrive from the server.
     */
    void setValueConsumer(Consumer<DataValue> valueConsumer);

    /**
     * Set the {@link Consumer} that will receive events as they arrive from the server.
     *
     * @param eventConsumer the {@link Consumer} that will receive events as they arrive from the server.
     */
    void setEventConsumer(Consumer<Variant[]> eventConsumer);

}
