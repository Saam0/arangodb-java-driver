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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class ArangoDBTest {

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBProvider.class)
    void alreadyExistingConversation(TestContext ctx, ArangoDB arango) {
        Throwable thrown = catchThrowable(() ->
                arango.getConversationManager().requireConversation(
                        arango.getConversationManager().requireConversation(
                                Mono.just("hello")
                        )
                ).block()
        );

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

}
