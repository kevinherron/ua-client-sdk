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

package com.digitalpetri.opcua.sdk.client.nodes.attached;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;

public class AttachedVariableNode extends AttachedNode implements UaVariableNode {

    public AttachedVariableNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    @Override
    public CompletableFuture<DataValue> readValue() {
        return readAttribute(AttributeId.Value);
    }

    @Override
    public CompletableFuture<DataValue> readDataType() {
        return readAttribute(AttributeId.DataType);
    }

    @Override
    public CompletableFuture<DataValue> readValueRank() {
        return readAttribute(AttributeId.ValueRank);
    }

    @Override
    public CompletableFuture<DataValue> readArrayDimensions() {
        return readAttribute(AttributeId.ArrayDimensions);
    }

    @Override
    public CompletableFuture<DataValue> readAccessLevel() {
        return readAttribute(AttributeId.AccessLevel);
    }

    @Override
    public CompletableFuture<DataValue> readUserAccessLevel() {
        return readAttribute(AttributeId.UserAccessLevel);
    }

    @Override
    public CompletableFuture<DataValue> readMinimumSamplingInterval() {
        return readAttribute(AttributeId.MinimumSamplingInterval);
    }

    @Override
    public CompletableFuture<DataValue> readHistorizing() {
        return readAttribute(AttributeId.Historizing);
    }

    @Override
    public CompletableFuture<StatusCode> writeValue(DataValue value) {
        return writeAttribute(AttributeId.Value, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeDataType(DataValue value) {
        return writeAttribute(AttributeId.DataType, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeValueRank(DataValue value) {
        return writeAttribute(AttributeId.ValueRank, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeArrayDimensions(DataValue value) {
        return writeAttribute(AttributeId.ArrayDimensions, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeAccessLevel(DataValue value) {
        return writeAttribute(AttributeId.AccessLevel, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeUserAccessLevel(DataValue value) {
        return writeAttribute(AttributeId.UserAccessLevel, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeMinimumSamplingInterval(DataValue value) {
        return writeAttribute(AttributeId.MinimumSamplingInterval, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeHistorizing(DataValue value) {
        return writeAttribute(AttributeId.Historizing, value);
    }

}
