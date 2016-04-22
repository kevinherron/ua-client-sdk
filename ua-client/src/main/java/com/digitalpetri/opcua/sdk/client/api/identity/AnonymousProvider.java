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

import java.util.Arrays;

import com.digitalpetri.opcua.stack.core.types.builtin.ByteString;
import com.digitalpetri.opcua.stack.core.types.enumerated.UserTokenType;
import com.digitalpetri.opcua.stack.core.types.structured.AnonymousIdentityToken;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.SignatureData;
import com.digitalpetri.opcua.stack.core.types.structured.UserIdentityToken;
import com.digitalpetri.opcua.stack.core.types.structured.UserTokenPolicy;
import org.jooq.lambda.tuple.Tuple2;

/**
 * An {@link IdentityProvider} that will choose the first available anonymous {@link UserTokenPolicy}.
 */
public class AnonymousProvider implements IdentityProvider {

    @Override
    public Tuple2<UserIdentityToken, SignatureData> getIdentityToken(EndpointDescription endpoint,
                                                                     ByteString serverNonce) throws Exception {
        String policyId = Arrays.stream(endpoint.getUserIdentityTokens())
                .filter(t -> t.getTokenType() == UserTokenType.Anonymous)
                .findFirst()
                .map(policy -> {
                    String id = policy.getPolicyId();

                    // treat a null id as empty string
                    // else this becomes an empty Optional.
                    return id == null ? "" : id;
                })
                .orElseThrow(() -> new Exception("no anonymous token policy found"));

        return new Tuple2<>(new AnonymousIdentityToken(policyId), new SignatureData());
    }

    @Override
    public String toString() {
        return "AnonymousProvider{}";
    }

}
