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

package com.digitalpetri.opcua.sdk.client.api.identity;

import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.SignatureData;
import com.digitalpetri.opcua.stack.core.types.structured.UserIdentityToken;
import org.jooq.lambda.tuple.Tuple2;

public interface IdentityProvider {

    /**
     * Return the {@link UserIdentityToken} and {@link SignatureData} (if applicable for the token) to use when
     * activating a session.
     *
     * @param endpoint the {@link EndpointDescription} being connected to.
     * @return a {@link Tuple2} containing the {@link UserIdentityToken} and {@link SignatureData}.
     */
    Tuple2<UserIdentityToken, SignatureData> getIdentityToken(EndpointDescription endpoint,
                                                              ByteString serverNonce) throws Exception;

}
