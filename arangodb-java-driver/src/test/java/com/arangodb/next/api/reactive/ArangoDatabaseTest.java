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

package com.arangodb.next.api.reactive;

import com.arangodb.next.api.utils.ArangoDBProvider;
import com.arangodb.next.api.utils.TestContext;
import com.arangodb.next.entity.model.Engine;
import com.arangodb.next.entity.model.ServerRole;
import com.arangodb.next.entity.model.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michele Rastelli
 */
class ArangoDatabaseTest {


    @Test
    void shutdown() {
        // TODO
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBProvider.class)
    void getAccessibleDatabasesFor(TestContext ctx, ArangoDB arangoDB) {
        List<String> databases = arangoDB.db().getAccessibleDatabasesFor("root").collectList().block();
        assertThat(databases).isNotNull();
        assertThat(databases).contains("_system");
    }


    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBProvider.class)
    void getVersion(TestContext ctx, ArangoDB arangoDB) {
        Version version = arangoDB.db().getVersion().block();
        assertThat(version).isNotNull();
        assertThat(version.getServer()).isNotNull();
        assertThat(version.getVersion()).isNotNull();
    }


    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBProvider.class)
    void getEngine(TestContext ctx, ArangoDB arangoDB) {
        Engine engine = arangoDB.db().getEngine().block();
        assertThat(engine).isNotNull();
        assertThat(engine.getName()).isEqualTo(Engine.StorageEngineName.ROCKSDB);
    }


    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBProvider.class)
    void getRole(TestContext ctx, ArangoDB arangoDB) {
        ServerRole role = arangoDB.db().getRole().block();
        assertThat(role).isNotNull();
    }

}
