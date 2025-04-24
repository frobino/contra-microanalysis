# CONTRA Kernel

A CONTRA which reuses the Trace Compass Kernel Analysis to derive
information from a lttng kernel trace in CTF format and store the
information in a postgreSQL database.

## Build and start with docker-compose

This section shows how to use docker to create a full fledged application
using the CONTRA kernel.
From this folder execute the command below:

```
docker-compose up -d
```

The command will start:

- a CONTRA container, whose goal is to *interpret a linux kernel CTF
  trace*, *derive information (using one of Trace Compass kernel
  analysis)*, and *store the analysis results to a postgres DB*;
- a postgres DB where the analysis results are stored;
- some consumers of the data stored in the DB, namely:
  - a streamlit application;
  - a grafana instance, togheter with a own postgres DB to store
    grafana configurations;

The CONTRA container will start reading a trace and fill the DB as soon
as the container is started. So at this point you should be able to:

1. browse the DB filled by the CONTRA kernel
2. access the streamlit application and see how it used the DB data to
   fill some charts
3. configure Grafana to read the DB and create a chart

### 1. Browse the generated DB

[bkeeper][bkeeper] is a fancy UI to browse different dbs.
It can be used to connect ot the postgres db configured in the previous
steps. Use the user, pwd, localhost, and port 5488 as specified in
the docker-compose.yaml for the pg\_data\_wh service.

### 2. Access the streamlit application

Open the browser and access the streamlit application:

```
http://localhost:8501/
```

### 3. Configure Grafana and create visualization

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

### Setup a DB (postgres)

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

# Notes for future work

Current tables:

**Intervals table**:

| duration | quark | attribute | value | type |
|----------|-------|-----------|-------|------|

**Quark to attribute table**:

| quark | attribute |
|-------|-----------|

Independently on the technology used,
to create tables that are "easy to interpret",
we need to decide the tables layout at priori,
and then create them.
Somehow we already do this with an the attribute tree,
but we need more info e.g. 
which one are containers, which ones are leaf, etc.

An attribute tree similar to this:
```
 * |- CPUs
 * |  |- <CPU number> -> CPU Status
 * |  |  |- CURRENT_THREAD
 * |  |  |- SOFT_IRQS
 * |  |  |  |- <Soft IRQ number> -> Soft IRQ Status
 * |  |  |- IRQS
 * |  |  |  |- <IRQ number> -> IRQ Status
 * |- IRQs / SOFT_IRQs
 * |  |- <IRQ number> -> Aggregate Status
 * |- THREADS
 * |  |- <Thread number> -> Thread Status
 * |  |  |- PPID -> The thread ID of the parent, can be a process or a thread
 * |  |  |- EXEC_NAME
 * |  |  |- PRIO
 * |  |  |- SYSTEM_CALL
 * |  |  |- CURRENT_CPU_RQ
 * |  |  |- PID -> The process ID. If absent, the thread is a process
 ```

 Could generate the following tables:

**CPUs Tables**:

 | duration | CPU number | CPU status |
 |----------|------------|------------|
 | (0, 100) | 0          | 0          |
 | (0, 100) | 1          | 1          |

 | duration | CPU number | Current thread |
 |----------|------------|----------------|
 | (0, 100) | 0          | 0              |
 | (0, 100) | 1          | 1              |

 | duration | CPU number | Soft IRQ number | IRQ status |
 |----------|------------|----------------|--|
 | (0, 100) | 0          | 0              |  |
 | (0, 100) | 1          | 1              |  |

 | duration | CPU number | IRQ number | IRQ status |
 |----------|------------|----------------|--|
 | (0, 100) | 0          | 0              |  |
 | (0, 100) | 1          | 1              |  |

**IRQs Tables**:

TBD

**THREADS Tables**:

TBD

Ideas:

- The "attribute tree description" could be passed to the db creator to setup tables, using the syntax we consider appropriate and easy to parse.

[bkeeper]:https://github.com/beekeeper-studio/beekeeper-studio
