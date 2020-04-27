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

package deployments;


import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.client.HttpClient;

import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class ContainerUtils {

    private static final String DEFAULT_DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.6.2";
    private static final Logger log = LoggerFactory.getLogger(ContainerUtils.class);

    static String getImage() {
        String dockerImageFromProperties = System.getProperty("test.docker.image");
        String dockerImage = dockerImageFromProperties != null ? dockerImageFromProperties : DEFAULT_DOCKER_IMAGE;
        log.info("Using docker image: {}", dockerImage);
        return dockerImage;
    }

    static String getLicenseKey() {
        String arangoLicenseKeyFromProperties = System.getProperty("arango.license.key");
        String arangoLicenseKey = arangoLicenseKeyFromProperties != null ? arangoLicenseKeyFromProperties : "";
        log.info("Using arango license key: {}", arangoLicenseKey.replaceAll(".", "*"));
        return arangoLicenseKey;
    }

    static void waitForAuthenticationUpdate(ContainerDeployment deployment) {
        boolean authErrors;
        do {
            authErrors = deployment.getHosts().stream()
                    .map(h -> HttpClient.create()
                            .headers(headers -> headers.add("Authorization", deployment.getBasicAuthentication()))
                            .get()
                            .uri("http://" + h.getHost() + ":" + h.getPort() + "/_api/version")
                            .response()
                            .block())
                    .anyMatch(response -> Objects.requireNonNull(response).status() != HttpResponseStatus.OK);

            if (authErrors) {
                log.warn("Authentication Error: retrying in 1s ...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (authErrors);
    }

    static public ArangoVersion getVersion() {
        String[] parts = getImage().split(":")[1].split("\\.");
        return ImmutableArangoVersion.of(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2].split("-")[0])
        );
    }

}
