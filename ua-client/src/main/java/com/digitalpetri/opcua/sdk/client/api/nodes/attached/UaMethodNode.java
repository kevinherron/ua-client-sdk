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

public interface UaMethodNode extends UaNode {

    /**
     * Read the Executable attribute {@link DataValue}.
     *
     * @return the Executable attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readExecutable();

    /**
     * Read the Executable attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readExecutable()}.
     *
     * @return the Executable attribute.
     */
    default CompletableFuture<Boolean> readExecutableAttribute() {
        return readExecutable().thenApply(v -> (Boolean) v.getValue().getValue());
    }

    /**
     * Read the UserExecutable attribute {@link DataValue}.
     *
     * @return the UserExecutable attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readUserExecutable();

    /**
     * Read the UserExecutable attribute.
     * <p>
     * If quality and timestamps are required, see {@link #readUserExecutable()}.
     *
     * @return the UserExecutable attribute.
     */
    default CompletableFuture<Boolean> readUserExecutableAttribute() {
        return readUserExecutable().thenApply(v -> (Boolean) v.getValue().getValue());
    }

    /**
     * Write a {@link DataValue} to the Executable attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeExecutable(DataValue value);

    /**
     * Write a {@link Boolean} to the Executable attribute.
     *
     * @param executable the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeExecutableAttribute(Boolean executable) {
        return writeExecutable(new DataValue(new Variant(executable)));
    }

    /**
     * Write a {@link DataValue} to the UserExecutable attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeUserExecutable(DataValue value);

    /**
     * Write a {@link Boolean} to the UserExecutable attribute.
     *
     * @param userExecutable the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeUserExecutableAttribute(Boolean userExecutable) {
        return writeExecutable(new DataValue(new Variant(userExecutable)));
    }

}
