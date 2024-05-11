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
# To start a container at localhost:5432
docker run --name test-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgrespw -e POSTGRES_DB=postgres -d postgres:15.3
# Check the container local ip address
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' test-postgres
```

[bkeeper][bkeeper] is a fancy UI to browse different dbs.
It can be used to connect ot the postgres db configured in the previous
steps. Use the user,pwd configured above, the ip address returned by
```docker inspect```, and port 5432.

## How to setup DB (postgres) and Grafana

```
docker-compose up -d
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pg_data_wh
```

See [this blog][dcompose].

### Open browser and access the Grafana

```
http://<ip_of_the_host_machine>:3111
```

Login with with the default user *admin* and pwd *admin*.

Choose *Data Source* as *PostgreSQL*, then fill with the following params:

```
Host URL: pg_data_wh:5432
Database name: postgres
Username: postgres
Password: postgrespw
TLS/SSL Mode: disable
PostgreSQL version: 15
```

From the *Home > Explore* try to run a query:

```
SELECT *  FROM intervalsv2 WHERE attribute = 'CPUs/0/Current_thread' LIMIT 50
```



## NOTEs

- Trace Compass Libraries can be downloaded from [here][tc-libs].

[tc-libs]:https://download.eclipse.org/tracecompass/stable/repository/plugins/
[bkeeper]:https://github.com/beekeeper-studio/beekeeper-studio
[dcompose]:https://blog.devgenius.io/how-to-setup-grafana-with-postgresql-database-using-docker-compose-a-step-by-step-guide-e5a9cce90ba3
