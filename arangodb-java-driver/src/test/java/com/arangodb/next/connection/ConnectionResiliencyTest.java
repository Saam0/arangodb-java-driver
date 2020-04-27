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

import deployments.ProxiedContainerDeployment;
import deployments.ProxiedHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.Exceptions;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.arangodb.next.connection.ConnectionTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
@Tag("resiliency")
@Testcontainers
// "Thread.sleep" should not be used in tests
@SuppressWarnings("squid:S2925")
class ConnectionResiliencyTest {

//    static {
//        Hooks.onOperatorDebug();
//    }

    @Container
    private static final ProxiedContainerDeployment deployment = ProxiedContainerDeployment.ofSingleServer();
    private final ImmutableConnectionConfig.Builder config;

    ConnectionResiliencyTest() {
        config = ConnectionConfig.builder();
    }

    @BeforeEach
    void restore() {
        deployment.getProxiedHosts().forEach(it -> {
            it.enableProxy();
            it.getProxy().setConnectionCut(false);
        });
    }

    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void requestTimeout(ArangoProtocol protocol) throws InterruptedException {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config.timeout(1000).build();
        ArangoConnection connection = new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                .create(host, deployment.getAuthentication()).block();
        assertThat(connection).isNotNull();

        performRequest(connection);

        deployment.getProxiedHosts().forEach(it -> it.getProxy().setConnectionCut(true));
        Throwable thrown = catchThrowable(() -> performRequest(connection));
        assertThat(Exceptions.unwrap(thrown)).isInstanceOf(TimeoutException.class);

        // wait for errorHandling fallback to set the connection to disconnected
        Thread.sleep(100);
        assertThat(connection.isConnected().block()).isFalse();

        deployment.getProxiedHosts().forEach(it -> it.getProxy().setConnectionCut(false));
        performRequest(connection);

        connection.close().block();
    }


    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void VstConnectionTimeout(ArangoProtocol protocol) {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config.timeout(1000).build();
        deployment.getProxiedHosts().forEach(it -> it.getProxy().setConnectionCut(true));
        Throwable thrown = catchThrowable(() ->
                new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                        .create(host, deployment.getAuthentication()).block());
        assertThat(Exceptions.unwrap(thrown)).isInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void closeConnection(ArangoProtocol protocol) throws InterruptedException {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config.build();
        ArangoConnection connection = new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                .create(host, deployment.getAuthentication()).block();
        assertThat(connection).isNotNull();
        assertThat(connection.isConnected().block()).isTrue();
        performRequest(connection);
        assertThat(connection.isConnected().block()).isTrue();
        connection.close().block();

        // wait for errorHandling fallback to set the connection to disconnected
        Thread.sleep(100);
        assertThat(connection.isConnected().block()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void closeConnectionTwice(ArangoProtocol protocol) throws InterruptedException {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config.build();
        ArangoConnection connection = new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                .create(host, deployment.getAuthentication()).block();
        assertThat(connection).isNotNull();
        assertThat(connection.isConnected().block()).isTrue();

        connection.execute(VERSION_REQUEST);
        connection.close().block();
        connection.close().block();

        // wait for errorHandling fallback to set the connection to disconnected
        Thread.sleep(100);
        assertThat(connection.isConnected().block()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void requestWhenDisconnected(ArangoProtocol protocol) throws InterruptedException {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config.build();
        ArangoConnection connection = new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                .create(host, deployment.getAuthentication()).block();
        assertThat(connection).isNotNull();
        deployment.getProxiedHosts().forEach(ProxiedHost::disableProxy);
        Throwable thrown = catchThrowable(() -> performRequest(connection));
        assertThat(Exceptions.unwrap(thrown)).isInstanceOfAny(IOException.class, TimeoutException.class);

        // wait for errorHandling fallback to set the connection to disconnected
        Thread.sleep(100);
        assertThat(connection.isConnected().block()).isFalse();
        connection.close().block();
        assertThat(connection.isConnected().block()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ArangoProtocol.class)
    void reconnect(ArangoProtocol protocol) throws InterruptedException {
        HostDescription host = deployment.getHosts().get(0);
        ConnectionConfig testConfig = config
                .timeout(1000)
                .build();
        ArangoConnection connection = new ConnectionFactoryImpl(testConfig, protocol, DEFAULT_SCHEDULER_FACTORY)
                .create(host, deployment.getAuthentication()).block();
        assertThat(connection).isNotNull();
        assertThat(connection.isConnected().block()).isTrue();

        for (int i = 0; i < 100; i++) {
            performRequest(connection);
            assertThat(connection.isConnected().block()).isTrue();
            deployment.getProxiedHosts().forEach(ProxiedHost::disableProxy);
            Throwable thrown = catchThrowable(() -> performRequest(connection));
            assertThat(Exceptions.unwrap(thrown)).isInstanceOfAny(IOException.class, TimeoutException.class);
            deployment.getProxiedHosts().forEach(ProxiedHost::enableProxy);
            performRequest(connection, 2);
            assertThat(connection.isConnected().block()).isTrue();
        }

        connection.close().block();

        // wait for errorHandling fallback to set the connection to disconnected
        Thread.sleep(100);
        assertThat(connection.isConnected().block()).isFalse();
    }

}