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

package com.arangodb.next.entity.serde;

import com.arangodb.next.api.collection.entity.*;
import com.arangodb.next.api.database.entity.Sharding;
import com.arangodb.next.api.entity.NumericReplicationFactor;
import com.arangodb.next.api.entity.SatelliteReplicationFactor;
import com.arangodb.velocypack.VPackSerializer;

/**
 * @author Mark Vollmary
 */
public final class VPackSerializers {

    private VPackSerializers() {
    }

    public static final VPackSerializer<SatelliteReplicationFactor> SATELLITE_REPLICATION_FACTOR =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());

    public static final VPackSerializer<NumericReplicationFactor> NUMERIC_REPLICATION_FACTOR =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());

    //region DatabaseApi
    public static final VPackSerializer<Sharding> SHARDING =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());
    //endregion

    //region CollectionApi
    public static final VPackSerializer<ShardingStrategy> SHARDING_STRATEGY =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());

    public static final VPackSerializer<KeyType> KEY_TYPE =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());

    public static final VPackSerializer<CollectionType> COLLECTION_TYPE =
            (builder, attribute, value, context) -> builder.add(attribute, value.getValue());
    //endregion

}
