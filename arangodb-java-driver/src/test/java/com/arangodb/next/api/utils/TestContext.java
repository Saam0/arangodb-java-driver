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

package com.arangodb.next.api.utils;


import com.arangodb.next.communication.ArangoTopology;
import com.arangodb.next.communication.CommunicationConfig;
import com.arangodb.next.connection.ArangoProtocol;
import com.arangodb.next.connection.ContentType;
import deployments.ContainerDeployment;

import java.util.AbstractMap;
import java.util.stream.Stream;

/**
 * @author Michele Rastelli
 */
public class TestContext {

    private final ContainerDeployment deployment;
    private final CommunicationConfig config;

    public static Stream<TestContext> createContexts(final ContainerDeployment deployment) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>(ArangoProtocol.VST, ContentType.VPACK),
                new AbstractMap.SimpleEntry<>(ArangoProtocol.HTTP, ContentType.VPACK),
                new AbstractMap.SimpleEntry<>(ArangoProtocol.HTTP, ContentType.JSON)
        )
                .map(it -> CommunicationConfig.builder()
                        .protocol(it.getKey())
                        .contentType(it.getValue())
                        .addAllHosts(deployment.getHosts())
                        .authenticationMethod(deployment.getAuthentication())
                        .topology(deployment.getTopology())
                        .build()
                )
                .map(config -> new TestContext(deployment, config));
    }

    private TestContext(final ContainerDeployment deployment, final CommunicationConfig config) {
        this.deployment = deployment;
        this.config = config;
    }

    public CommunicationConfig getConfig() {
        return config;
    }

    public boolean isEnterprise() {
        return deployment.isEnterprise();
    }

    public boolean isCluster() {
        return deployment.getTopology().equals(ArangoTopology.CLUSTER);
    }

    public boolean isAtLeastVersion(int major, int minor) {
        return deployment.isAtLeastVersion(major, minor);
    }

    @Override
    public String toString() {
        return deployment.getTopology() + ", " +
                config.getProtocol() + ", " +
                config.getContentType();
    }

}
