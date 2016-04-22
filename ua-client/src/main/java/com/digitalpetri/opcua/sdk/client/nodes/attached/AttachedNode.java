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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.NodeCache;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaNode;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.ReadResponse;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;
import com.digitalpetri.opcua.stack.core.types.structured.WriteValue;

import static com.google.common.collect.Lists.newArrayList;

public class AttachedNode implements UaNode {

    protected final NodeCache nodeCache;

    protected final OpcUaClient client;
    protected final NodeId nodeId;

    public AttachedNode(OpcUaClient client, NodeId nodeId) {
        this.client = client;
        this.nodeId = nodeId;

        nodeCache = client.getNodeCache();
    }

    @Override
    public CompletableFuture<DataValue> readNodeId() {
        return readAttribute(AttributeId.NodeId);
    }

    @Override
    public CompletableFuture<DataValue> readNodeClass() {
        return readAttribute(AttributeId.NodeClass);
    }

    @Override
    public CompletableFuture<DataValue> readBrowseName() {
        return readAttribute(AttributeId.BrowseName);
    }

    @Override
    public CompletableFuture<DataValue> readDisplayName() {
        return readAttribute(AttributeId.DisplayName);
    }

    @Override
    public CompletableFuture<DataValue> readDescription() {
        return readAttribute(AttributeId.Description);
    }

    @Override
    public CompletableFuture<DataValue> readWriteMask() {
        return readAttribute(AttributeId.WriteMask);
    }

    @Override
    public CompletableFuture<DataValue> readUserWriteMask() {
        return readAttribute(AttributeId.UserWriteMask);
    }

    protected CompletableFuture<DataValue> readAttribute(AttributeId attributeId) {
        Optional<DataValue> opt =
                attributeId == AttributeId.Value ?
                        Optional.empty() :
                        nodeCache.getAttribute(nodeId, attributeId);

        return opt.map(CompletableFuture::completedFuture).orElseGet(() -> {
            ReadValueId readValueId = new ReadValueId(
                    nodeId, attributeId.uid(), null, QualifiedName.NULL_VALUE);

            CompletableFuture<ReadResponse> future =
                    client.read(0.0, TimestampsToReturn.Neither, newArrayList(readValueId));

            return future.thenApply(response -> {
                DataValue value = response.getResults()[0];

                if (attributeId != AttributeId.Value) {
                    nodeCache.putAttribute(nodeId, attributeId, value);
                }

                return value;
            });
        });
    }

    @Override
    public CompletableFuture<StatusCode> writeNodeId(DataValue value) {
        return writeAttribute(AttributeId.NodeId, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeNodeClass(DataValue value) {
        return writeAttribute(AttributeId.NodeClass, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeBrowseName(DataValue value) {
        return writeAttribute(AttributeId.BrowseName, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeDisplayName(DataValue value) {
        return writeAttribute(AttributeId.DisplayName, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeDescription(DataValue value) {
        return writeAttribute(AttributeId.Description, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeWriteMask(DataValue value) {
        return writeAttribute(AttributeId.WriteMask, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeUserWriteMask(DataValue value) {
        return writeAttribute(AttributeId.UserWriteMask, value);
    }

    protected CompletableFuture<StatusCode> writeAttribute(AttributeId attributeId, DataValue value) {
        WriteValue writeValue = new WriteValue(
                nodeId, attributeId.uid(), null, value);

        return client.write(newArrayList(writeValue)).thenApply(response -> {
            StatusCode statusCode = response.getResults()[0];

            if (statusCode.isGood()) {
                nodeCache.invalidate(nodeId, attributeId);
            }

            return statusCode;
        });
    }

}
