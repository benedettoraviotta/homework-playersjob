# io.playersjob

## Run configuration

The jar application can have the following environment variables set:

  - CLUB_ID : This variable specifies the club ID for the job. If CLUB_ID is not provided, it defaults to 5.
  - FORCE_INSERT_INTERRUPT=[yes|no]: This variable controls whether an interruption is forced. It is set to no by default, meaning no interruption is simulated unless explicitly set otherwise.

In the .run folder, there are two IntelliJ run configurations to run the jar application:
  - *fetch-club5-default.run*: This is the standard run configuration that starts the application normally without simulating any interruption.

  - *fetch-club5-default-force-interruption.run*: This configuration simulates an interruption during the job execution, for testing how the system behaves in case of an interruption.
## Postgresql container
Used to store players retrieve from transfermark api and for job state.

Run
```shell script
 docker-compose -f src/main/resources/docker-compose-postgresql17.yml up -d
```
to create an instance of postgre17 and initialize the db.

### Interact with db 
From container bash 
```shell script
 docker exec -it resources-postgres-1 bash
```
use
```shell script
 psql -U myuser -d playersdb
```



## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/io.playersjob-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.
