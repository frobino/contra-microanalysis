# CONTRA Kernel

A CONTRA which reuses the Trace Compass Kernel Analysis to derive
information from a lttng kernel trace in CTF format and store the
information in a postgreSQL database.

## Build and start with docker-compose

### Build and start CONTRA-kernel + postgres DB + Grafana

```
docker-compose up -d
```

The command will start:

- a CONTRA container, whose goal is to *interpret a linux kernel CTF trace*,
*derive information (using one of Trace Compass kernel analysis)*, and
*store the analysis results to a postgres DB*;
- a postgres DB where the analysis results are stored;
- a grafana instance, togheter with a own postgres DB to store grafana configurations

The CONTRA container will start reading a trace and fill the DB as soon
as the container is started. So at this point you should be able to:

1. browse the generated DB
2. configure Grafana to read the DB and create a chart

### Browse the generated DB

[bkeeper][bkeeper] is a fancy UI to browse different dbs.
It can be used to connect ot the postgres db configured in the previous
steps. Use the user, pwd, localhost, and port 5488 as specified in
the docker-compose.yaml for the pg\_data\_wh service.

### Configure Grafana and create visualization

Open the browser and access Grafana:

```
http://<ip_of_the_host_machine>:3111
```

Login with with the default user *admin* and pwd *admin*.

Choose *Data Source* as *PostgreSQL*, then fill with the following params
(which can be found in docker-compose.yaml):

```
Host URL: pg_data_wh:5432
Database name: intervals
Username: postgres
Password: postgrespw
TLS/SSL Mode: disable
PostgreSQL version: 15
```

From the *Home > Explore* try to run a query:

```
SELECT * FROM intervalsv2 WHERE attribute = 'CPUs/0/Current_thread' LIMIT 50
```

From the *Home > Dashboard > Create Dashboard > Add Visualization*,
then select the (only) data source (which is our postgres database)
and then try to create a visualization of type *State Timeline*.

Add the following queries to create one timeline for each CPU.
The following is for CPU0:

```
SELECT
    to_timestamp((lower(duration) - 1570653866296619000)) AS "Start time",
    to_timestamp(upper(duration) - 1570653866296619000) AS "End time",
    (SELECT value FROM intervalsv2 WHERE attribute = 'Threads/' || intervals.value || '/Exec_name' LIMIT 1) AS "CPU 0"
FROM 
    intervalsv2 intervals
WHERE
    attribute = 'CPUs/0/Current_thread'
LIMIT 50
```

The following is for CPU1:

```
SELECT
    to_timestamp((lower(duration) - 1570653866296619000)) AS "Start time",
    to_timestamp(upper(duration) - 1570653866296619000) AS "End time",
    (SELECT value FROM intervalsv2 WHERE attribute = 'Threads/' || intervals.value || '/Exec_name' LIMIT 1) AS "CPU 1"
FROM 
    intervalsv2 intervals
WHERE
    attribute = 'CPUs/1/Current_thread'
LIMIT 50
```

Note the ugly subtraction. That is a quick shortcut to handle the
bigInt (nanoseconds) representation, which is not supported in grafana.

## Build and start locally

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

### Build and run CONTRA Kernel

```
# to install locally the tc libs and deps needed
mvn initialize
# to build and get a runnable jar
mvn install
# configure the path to the DB where the info will be stored
# NOTE: substitute 172.17.0.2 with the ip returned by docker inspect
export CONTRA_DB_URL="jdbc:postgresql://172.17.0.2:5432/"
# run CONTRA Kernel
java -jar target/contra-kernel-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Browse the DB filled with information

Use [bkeeper][bkeeper] to browse the DB named *intervals*, and the
table *intervalsV2*.

[bkeeper]:https://github.com/beekeeper-studio/beekeeper-studio
