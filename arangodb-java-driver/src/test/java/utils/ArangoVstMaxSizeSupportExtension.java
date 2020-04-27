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

package utils;

import deployments.ArangoVersion;
import deployments.ContainerUtils;
import deployments.ImmutableArangoVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Michele Rastelli
 */
public class ArangoVstMaxSizeSupportExtension implements ExecutionCondition {

    private static final ArangoVersion maxVersion = ImmutableArangoVersion.of(3, 6, 0);

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        ArangoVersion version = ContainerUtils.getVersion();

        if (version.compareTo(maxVersion) < 0) {
            return ConditionEvaluationResult.enabled("Enabled on DB version < " + maxVersion);
        } else {
            return ConditionEvaluationResult.disabled("Disabled on DB version >= " + maxVersion);
        }

    }
}