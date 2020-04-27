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


package com.arangodb.next.api.collection.entity;


import com.arangodb.velocypack.annotations.VPackPOJOBuilder;
import org.immutables.value.Value;

import javax.annotation.Nullable;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
@Value.Immutable
public interface KeyOptions {

    @VPackPOJOBuilder
    static ImmutableKeyOptions.Builder builder() {
        return ImmutableKeyOptions.builder();
    }

    /**
     * @return if set to true, then it is allowed to supply own key values in the _key attribute of a document. If set
     * to false, then the key generator will solely be responsible for generating keys and supplying own key values in
     * the _key attribute of documents is considered an error.
     */
    @Nullable
    Boolean getAllowUserKeys();

    /**
     * @return specifies the type of the key generator
     */
    @Nullable
    KeyType getType();

    /**
     * @return increment value for autoincrement key generator. Not used for other key generator types.
     */
    @Nullable
    Integer getIncrement();

    /**
     * @return Initial offset value for autoincrement key generator. Not used for other key generator types.
     */
    @Nullable
    Integer getOffset();

}
