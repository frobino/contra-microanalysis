# CONtainerized TRace Analysis

A service to interpret CTF traces, derive information, and store it to
a DB for further analysis.

## Setup a DB (postgres)

```
docker pull postgres:15.3
# To start a container at localhost:5432
docker run --name test-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgrespw -e POSTGRES_DB=postgres -d postgres:15.3
# Check the container local ip address
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' test-postgres
```

[bkeeper][bkeeper] is a fancy UI to browse different dbs.
It can be used to connect ot the postgres db configured in the previous
steps. Use the user,pwd configured above, the ip address returned by
```docker inspect```, and port 5432.

## How to build and run

See docker image.

```
# To install locally the tc libs and deps needed
mvn initialize
# To build and get a runnable jar
mvn install
# configure the path to the DB where the info will be stored
# NOTE: substitute 172.17.0.2 with the ip returned by docker inspect
export CONTRA_DB_URL="jdbc:postgresql://172.17.0.2:5432/"
# To run
java -jar target/contra-ust-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Browse the DB filled with information

Use [bkeeper][bkeeper] to browse the DB named *intervals*, and the
table *intervals*.

## NOTEs

- Trace Compass Libraries can be downloaded from [here][tc-libs].

[tc-libs]:https://download.eclipse.org/tracecompass/stable/repository/plugins/
[bkeeper]:https://github.com/beekeeper-studio/beekeeper-studio
