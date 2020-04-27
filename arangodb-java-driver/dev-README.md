# dev-README


## compile

```shell script
mvn clean compile
```

It triggers:
- pmd checks
- spotbugs checks
- checkstyle validations


## test

Run tests:
```shell script
mvn clean test
```

To skip resiliency tests (which are slower): `-DexcludedGroups="resiliency"`

Code coverage report is generated here: [target/site/jacoco/index.html](target/site/jacoco/index.html)

### docker image

To specify the docker image to use in tests:
```shell script
mvn test -Dtest.docker.image="docker.io/arangodb/arangodb:3.6.2"
```

### enterprise license

When testing against an enterprise docker image, a license key must be specified (also an evaluation one is fine):

```shell script
mvn test -Dtest.docker.image="docker.io/arangodb/enterprise:3.6.2" -Darango.license.key="<ARANGO_LICENSE_KEY>"
```

### reuse test containers

Test containers used in API tests can be reused. To enable it:
- append `testcontainers.reuse.enable=true` to `~/.testcontainers.properties`
- add the option `-Dtestcontainers.reuse.enable=true` when running tests


## GH Actions

To trigger GH Actions:
```shell script
./bin/mirror.sh
```

Check results [here](https://github.com/ArangoDB-Community/mirror-arangodb-java-driver/actions).


## SonarCloud

Check results [here](https://sonarcloud.io/dashboard?id=ArangoDB-Community_mirror-arangodb-java-driver).


## check dependecies updates

```shell script
mvn versions:display-dependency-updates
```


## snapshot release

Tagging a git revision with `v**-SNAPSHOT` will automatically trigger a snapshot release in [Github Packages](https://github.com/ArangoDB-Community/mirror-arangodb-java-driver/packages).
To import such package in another project:

```xml
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>com.arangodb</groupId>
            <artifactId>arangodb-java-driver</artifactId>
            <version>7.0.0-20200203.102705-1</version>
        </dependency>    
    </dependencies>
    <repositories>
        <repository>
            <id>mirror-arangodb-java-driver</id>
            <name>mirror-arangodb-java-driver</name>
            <url>https://maven.pkg.github.com/ArangoDB-Community/mirror-arangodb-java-driver</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```
