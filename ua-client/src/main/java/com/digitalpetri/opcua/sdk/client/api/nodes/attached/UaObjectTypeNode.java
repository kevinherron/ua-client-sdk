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

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;

public interface UaObjectTypeNode extends UaNode {

    /**
     * Read the IsAbstract attribute {@link DataValue}.
     *
     * @return the IsAbstract attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readIsAbstract();

    /**
     * Read the IsAbstract attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readIsAbstract()}.
     *
     * @return the IsAbstract attribute.
     */
    default CompletableFuture<Boolean> readIsAbstractAttribute() {
        return readIsAbstract().thenApply(v -> (Boolean) v.getValue().getValue());
    }

    /**
     * Write a {@link DataValue} to the IsAbstract attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeIsAbstract(DataValue value);

    /**
     * Write a {@link Boolean} to the IsAbstract attribute.
     *
     * @param isAbstract the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeIsAbstractAttribute(boolean isAbstract) {
        return writeIsAbstract(new DataValue(new Variant(isAbstract)));
    }

}
