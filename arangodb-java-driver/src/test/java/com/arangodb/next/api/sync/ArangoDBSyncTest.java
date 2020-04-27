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

package com.arangodb.next.api.sync;

import com.arangodb.next.api.utils.ArangoDBSyncProvider;
import com.arangodb.next.api.utils.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * @author Michele Rastelli
 */
class ArangoDBSyncTest {

    @Test
    void shutdown() {
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBSyncProvider.class)
    void alreadyExistingThreadConversation(TestContext ctx, ArangoDBSync arango) {
        try (ThreadConversation tc = arango.getConversationManager().requireConversation()) {
            Throwable thrown = catchThrowable(() -> arango.getConversationManager().requireConversation());
            assertThat(thrown).isInstanceOf(IllegalStateException.class);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ArangoDBSyncProvider.class)
    void wrongThreadClosingThreadConversation(TestContext ctx, ArangoDBSync arango) {
        try (ThreadConversation tc = arango.getConversationManager().requireConversation()) {
            Throwable thrown = catchThrowable(() -> CompletableFuture.runAsync(tc::close).join());
            assertThat(thrown).isInstanceOf(CompletionException.class);
            assertThat(thrown.getCause()).isInstanceOf(ConcurrentModificationException.class);
        }
    }

}
