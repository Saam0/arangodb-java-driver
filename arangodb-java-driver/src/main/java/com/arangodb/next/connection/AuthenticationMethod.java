/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.next.connection;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.ValueType;
import io.netty.buffer.ByteBuf;
import org.immutables.value.Value;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Michele Rastelli
 */
public interface AuthenticationMethod {

    static AuthenticationMethod ofJwt(final String user, final String jwt) {
        return ImmutableJwtAuthenticationMethod.of(user, jwt);
    }

    static AuthenticationMethod ofBasic(final String user, final String password) {
        return ImmutableBasicAuthenticationMethod.of(user, password);
    }

    String getUser();

    String getHttpAuthorizationHeader();

    ByteBuf getVstAuthenticationMessage();

    /**
     * @see <a href="https://github.com/arangodb/velocystream#authentication">API</a>
     */
    @Value.Immutable(builder = false)
    abstract class JwtAuthenticationMethod implements AuthenticationMethod {

        @Value.Parameter(order = 1)
        public abstract String getUser();

        @Value.Parameter(order = 2)
        abstract String getJwt();

        @Override
        public String getHttpAuthorizationHeader() {
            return "Bearer " + getJwt();
        }

        @Override
        public ByteBuf getVstAuthenticationMessage() {
            final VPackBuilder builder = new VPackBuilder();
            builder.add(ValueType.ARRAY);
            builder.add(1);
            builder.add(1000);
            builder.add("jwt");
            builder.add(getJwt());
            builder.close();
            return VPackUtils.extractBuffer(builder.slice());
        }

    }

    @Value.Immutable(builder = false)
    abstract class BasicAuthenticationMethod implements AuthenticationMethod {

        @Value.Parameter(order = 1)
        public abstract String getUser();

        @Value.Parameter(order = 2)
        abstract String getPassword();

        @Override
        public String getHttpAuthorizationHeader() {
            final String plainAuth = getUser() + ":" + getPassword();
            final String encodedAuth = Base64.getEncoder().encodeToString(plainAuth.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encodedAuth;
        }

        @Override
        public ByteBuf getVstAuthenticationMessage() {
            final VPackBuilder builder = new VPackBuilder();
            builder.add(ValueType.ARRAY);
            builder.add(1);
            builder.add(1000);
            builder.add("plain");
            builder.add(getUser());
            builder.add(getPassword());
            builder.close();
            return VPackUtils.extractBuffer(builder.slice());
        }

    }

}
