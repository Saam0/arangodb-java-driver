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

package com.arangodb.next.api.collection.impl;


import com.arangodb.next.api.collection.CollectionApi;
import com.arangodb.next.api.collection.entity.*;
import com.arangodb.next.api.reactive.ArangoDatabase;
import com.arangodb.next.api.reactive.impl.ArangoClientImpl;
import com.arangodb.next.connection.ArangoRequest;
import com.arangodb.next.connection.ArangoResponse;
import com.arangodb.next.exceptions.server.CollectionOrViewNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

import static com.arangodb.next.api.util.ArangoResponseField.RESULT;

/**
 * @author Michele Rastelli
 */
public final class CollectionApiImpl extends ArangoClientImpl implements CollectionApi {

    private static final String PATH_API = "/_api/collection";

    public static final Type ITERABLE_OF_COLLECTION_ENTITY = new com.arangodb.velocypack.Type<Iterable<SimpleCollectionEntity>>() {
    }.getType();

    private final String dbName;

    public CollectionApiImpl(final ArangoDatabase arangoDatabase) {
        super((ArangoClientImpl) arangoDatabase);
        dbName = arangoDatabase.name();
    }

    @Override
    public Flux<SimpleCollectionEntity> getCollections(final CollectionsReadParams params) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.GET)
                                .path(PATH_API)
                                .putQueryParams(
                                        CollectionsReadParams.EXCLUDE_SYSTEM_PARAM,
                                        params.getExcludeSystem().map(String::valueOf)
                                )
                                .build()
                )
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().<Iterable<SimpleCollectionEntity>>deserializeField(RESULT, bytes, ITERABLE_OF_COLLECTION_ENTITY))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<DetailedCollectionEntity> createCollection(
            final CollectionCreateOptions options,
            final CollectionCreateParams params
    ) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.POST)
                                .body(getSerde().serialize(options))
                                .path(PATH_API)
                                .putQueryParams(
                                        "enforceReplicationFactor",
                                        params.getEnforceReplicationFactor().map(it -> it ? "1" : "0")
                                )
                                .putQueryParams(
                                        "waitForSyncReplication",
                                        params.getWaitForSyncReplication().map(it -> it ? "1" : "0")
                                )
                                .build()
                )
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<Void> dropCollection(final String name, final CollectionDropParams params) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.DELETE)
                                .path(PATH_API + "/" + name)
                                .putQueryParams(
                                        CollectionDropParams.IS_SYSTEM_PARAM,
                                        params.getIsSystem().map(String::valueOf)
                                )
                                .build()
                )
                .then();
    }

    @Override
    public Mono<SimpleCollectionEntity> getCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name)
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, SimpleCollectionEntity.class));
    }

    @Override
    public Mono<Boolean> existsCollection(final String name) {
        return getCollection(name)
                .thenReturn(true)
                .onErrorReturn(CollectionOrViewNotFoundException.class, false);
    }

    @Override
    public Mono<DetailedCollectionEntity> getCollectionProperties(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/properties")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<DetailedCollectionEntity> changeCollectionProperties(final String name, final CollectionChangePropertiesOptions options) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/properties")
                        .body(getSerde().serialize(options))
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<SimpleCollectionEntity> renameCollection(final String name, final CollectionRenameOptions options) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/rename")
                        .body(getSerde().serialize(options))
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, SimpleCollectionEntity.class));
    }


    @Override
    public Mono<Long> getCollectionCount(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/count")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeField("count", bytes, Long.class));
    }

    @Override
    public Mono<CollectionChecksumEntity> getCollectionChecksum(String name, CollectionChecksumParams params) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/checksum")
                        .putQueryParams(
                                CollectionChecksumParams.WITH_REVISIONS,
                                params.getWithRevisions().map(String::valueOf)
                        )
                        .putQueryParams(
                                CollectionChecksumParams.WITH_DATA,
                                params.getWithData().map(String::valueOf)
                        )
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, CollectionChecksumEntity.class));
    }

    @Override
    public Mono<SimpleCollectionEntity> truncateCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/truncate")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, SimpleCollectionEntity.class));
    }

}
