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

package com.arangodb.next.entity.model;

import com.arangodb.next.entity.GeneratePackagePrivateBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Michele Rastelli
 */
@GeneratePackagePrivateBuilder
public abstract class ClusterEndpointsEntry {

    @JsonCreator
    public static ClusterEndpointsEntry of(@JsonProperty("endpoint") final String endpoint) {
        return new ClusterEndpointsEntryBuilder().endpoint(endpoint).build();
    }

    public abstract String getEndpoint();

}
