# CONtainerized TRace Analysis

A service to interpret CTF traces, derive information, and store it to
a DB for further analysis.

## How to build and run

See docker image.

```
# To install locally the tc libs and deps needed
mvn initialize
# To build and get a runnable jar
mvn install
# To run
cd /app/target
java -jar contra-kernel-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## How to setup a DB (postgres)

```
docker pull postgres:15.3
docker run --name test-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgrespw -e POSTGRES_DB=postgres -d postgres:15.3
```

[bkeeper][bkeeper] seems a fancy UI to browse different dbs.

## NOTEs

- Trace Compass Libraries can be downloaded from [here][tc-libs].

[tc-libs]:https://download.eclipse.org/tracecompass/stable/repository/plugins/
[bkeeper]:https://github.com/beekeeper-studio/beekeeper-studio
