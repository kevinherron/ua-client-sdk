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

package com.digitalpetri.opcua.sdk.client.api.nodes.attached;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;

public interface UaVariableNode extends UaNode {

    /**
     * Read the Value attribute {@link DataValue}.
     *
     * @return the Value attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readValue();

    /**
     * Read the Value attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readValue()}.
     *
     * @return the Value attribute value.
     */
    default CompletableFuture<Object> readValueAttribute() {
        return readValue().thenApply(v -> v.getValue().getValue());
    }

    /**
     * Read the DataType attribute {@link DataValue}.
     *
     * @return the DataType attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readDataType();

    /**
     * Read the DataType attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readDataType()}.
     *
     * @return the DataType attribute value.
     */
    default CompletableFuture<NodeId> readDataTypeAttribute() {
        return readDataType().thenApply(v -> (NodeId) v.getValue().getValue());
    }

    /**
     * Read the ValueRank attribute {@link DataValue}.
     *
     * @return the ValueRank attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readValueRank();

    /**
     * Read the ValueRank attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readValueRank()}.
     *
     * @return the ValueRank attribute value, if present.
     */
    default CompletableFuture<Integer> readValueRankAttribute() {
        return readValueRank().thenApply(v -> (Integer) v.getValue().getValue());
    }

    /**
     * Read the ArrayDimensions attribute {@link DataValue}.
     *
     * @return the ArrayDimensions attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readArrayDimensions();

    /**
     * Read the ArrayDimensions attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readArrayDimensions()}.
     *
     * @return the ArrayDimensions attribute value, if present.
     */
    default CompletableFuture<Optional<UInteger[]>> readArrayDimensionsAttribute() {
        return readArrayDimensions().thenApply(v -> {
            StatusCode statusCode = v.getStatusCode();

            if (statusCode.getValue() == StatusCodes.Bad_AttributeIdInvalid) {
                return Optional.empty();
            } else {
                return Optional.ofNullable((UInteger[]) v.getValue().getValue());
            }
        });
    }

    /**
     * Read the AccessLevel attribute {@link DataValue}.
     *
     * @return the AccessLevel attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readAccessLevel();

    /**
     * Read the AccessLevel attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readAccessLevel()}.
     *
     * @return the AccessLevel attribute value.
     */
    default CompletableFuture<UByte> readAccessLevelAttribute() {
        return readAccessLevel().thenApply(v -> (UByte) v.getValue().getValue());
    }

    /**
     * Read the UserAccessLevel attribute {@link DataValue}.
     *
     * @return the UserAccessLevel attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readUserAccessLevel();

    /**
     * Read the UserAccessLevel attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readUserAccessLevel()}.
     *
     * @return the UserAccessLevel attribute value.
     */
    default CompletableFuture<UByte> readUserAccessLevelAttribute() {
        return readUserAccessLevel().thenApply(v -> (UByte) v.getValue().getValue());
    }

    /**
     * Read the MinimumSamplingInterval attribute {@link DataValue}.
     *
     * @return the MinimumSamplingInterval attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readMinimumSamplingInterval();

    /**
     * the MinimumSamplingInterval attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readMinimumSamplingInterval()}.
     *
     * @return the MinimumSamplingInterval attribute value, if present.
     */
    default CompletableFuture<Optional<Double>> readMinimumSamplingIntervalAttribute() {
        return readMinimumSamplingInterval().thenApply(v -> {
            StatusCode statusCode = v.getStatusCode();

            if (statusCode.getValue() == StatusCodes.Bad_AttributeIdInvalid) {
                return Optional.empty();
            } else {
                return Optional.ofNullable((Double) v.getValue().getValue());
            }
        });
    }

    /**
     * Read the Historizing attribute {@link DataValue}.
     *
     * @return the Historizing attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readHistorizing();

    /**
     * Read the Historizing attribute value.
     * <p>
     * If quality and timestamps are required, see {@link #readHistorizing()}.
     *
     * @return the Historizing attribute value.
     */
    default CompletableFuture<Boolean> readHistorizingAttribute() {
        return readHistorizing().thenApply(v -> (Boolean) v.getValue().getValue());
    }

    /**
     * Write a {@link DataValue} to the Value attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeValue(DataValue value);

    /**
     * Write an {@link Object} to the Value attribute.
     *
     * @param value the {@link Object} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeValueAttribute(Object value) {
        return writeValue(new DataValue(new Variant(value)));
    }

    /**
     * Write a {@link DataValue} to the DataType attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeDataType(DataValue value);

    /**
     * Write a {@link NodeId} to the DataType attribute.
     *
     * @param dataType the {@link NodeId} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeDataTypeAttribute(NodeId dataType) {
        return writeDataType(new DataValue(new Variant(dataType)));
    }

    /**
     * Write a {@link DataValue} to the ValueRank attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeValueRank(DataValue value);

    /**
     * Write an {@link Integer} to the ValueRank attribute.
     *
     * @param valueRank the {@link Integer} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeValueRankAttribute(Integer valueRank) {
        return writeValueRank(new DataValue(new Variant(valueRank)));
    }

    /**
     * Write a {@link DataValue} to the ArrayDimensions attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeArrayDimensions(DataValue value);

    /**
     * Write a {@link UInteger}[] to the ArrayDimensions attribute.
     *
     * @param arrayDimensions the {@link UInteger}[] to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeArrayDimensionsAttribute(UInteger[] arrayDimensions) {
        return writeArrayDimensions(new DataValue(new Variant(arrayDimensions)));
    }

    /**
     * Write a {@link DataValue} to the AccessLevel attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeAccessLevel(DataValue value);

    /**
     * Write a {@link UByte} to the AccessLevel attribute.
     *
     * @param accessLevel the {@link UByte} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeAccessLevelAttribute(UByte accessLevel) {
        return writeAccessLevel(new DataValue(new Variant(accessLevel)));
    }

    /**
     * Write a {@link DataValue} to the UserAccessLevel attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeUserAccessLevel(DataValue value);

    /**
     * Write a {@link UByte} to the UserAccessLevel attribute.
     *
     * @param userAccessLevel the {@link UByte} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeUserAccessLevelAttribute(UByte userAccessLevel) {
        return writeUserAccessLevel(new DataValue(new Variant(userAccessLevel)));
    }

    /**
     * Write a {@link DataValue} to the MinimumSamplingInterval attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeMinimumSamplingInterval(DataValue value);

    /**
     * Write a {@link Double} to the MinimumSamplingInterval attribute.
     *
     * @param minimumSamplingInterval the {@link Double} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeMinimumSamplingIntervalAttribute(Double minimumSamplingInterval) {
        return writeMinimumSamplingInterval(new DataValue(new Variant(minimumSamplingInterval)));
    }

    /**
     * Write a {@link DataValue} to the Historizing attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeHistorizing(DataValue value);

    /**
     * Write a {@link Boolean} to the Historizing attribute.
     *
     * @param historizing the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeHistorizingAttribute(Boolean historizing) {
        return writeHistorizing(new DataValue(new Variant(historizing)));
    }

}
