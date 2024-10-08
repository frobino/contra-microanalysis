# CONtainerized TRace Analysis

<div align="center">
<img src="doc/contra-logo.jpg" height="372"></br>
WELCOME from Conrado Travagli, CONTRAs official mascot</br></br>
</div>

CONTRAs are applications that *interpret [traces][tracing]*, *derive information*,
and *store the derived information into a database* for further analysis.
CONTRAs can be used as services in a microservice architecture.

The following flow chart gives an overview of how CONTRAs are used:

```mermaid
flowchart LR
    t>Trace] --> con[CONTRA]
    con --> db[(Database)]
    db -.- q[Queries]
    subgraph consumers
        q --> st[Streamlit]
        q --> jup[Jupiter Notebook]
        q --> g[Grafana]
        q --> any[...]
    end
```

## Goals of the project

Initially the main goals of the project were:

- create a *container-izable* application that can interpret (CTF) traces;
- show how to create a simple (maven) project using [Trace Compass libraries][tc-libs],
  mainly the CTF parser;

With time, it evolved to something more, and the following goals were added:

- show how to reuse more features provided by the [Trace Compass libraries][tc-libs],
  namely the Trace Compass analysis, to derive information from the traces;
- show how to store the derived information into a DB for further analysis;

Future work can include the following goals:

- add [views][sql-views] on top of the DB to enable more intuitive queries
- improve performances of the DB filling procedure (producer-consumer)
- indexing of the generated DB to improve queries performances
- propose integration in TC framework (create a new backend type)

## How to build and run a CONTRA

The easiest way to get a feeling of what a CONTRA is and how to use it
is to run docker-compose in one of the CONTRAs in this repo.
The docker-compose command will:

- build the selected CONTRA and create a docker image out of it
- instantiate a CONTRA container, a database and some consumers
- run the CONTRA container with an example trace so that it can fill the
  database and consequently fill the consumers with data

Currently we have 2 CONTRAs:

- [**contra-kernel**](contra-kernel/README.md), which reuses the
  Trace Compass Kernel Analysis to derive information from a lttng
  kernel trace in CTF format;
- [**contra-ust**](contra-ust/README.md), which reuses a generic
  Trace Compass Analysis to derive information from a lttng userspace
  trace in CTF format;

See the README in each subproject for more detailed instructions.
As of now, **contra-kernel is more complete and tested, so we suggest
to start from there**.

### Configure CONTRAs

It is possible to configure the CONTRAs using the following
environment variables:

```
CONTRA_DB_URL
Set the postgreSQL URL.
Default: jdbc:postgresql://pg_data_wh:5432/
```

```
CONTRA_DB_USER:
Set the postgreSQL user name.
Default: postgres
```

```
CONTRA_DB_PWD:
Set the postgreSQL password.
Default: postgrespw
```

```
CONTRA_DB_NAME:
Set the postgreSQL database name that CONTRA will create.
Default: intervals
```

```
CONTRA_TABLE_NAME:
Set the table name that CONTRA will create.
Default: intervalsV2
```

```
CONTRA_TRACE_PATH:
Set the path to the trace that CONTRA will analyze.
Default: resources/traces/lttng-kernel/wget-first-call
```

## NOTEs

- Trace Compass Libraries can be downloaded from [here][tc-libs]
- Java code follows the [google java format][google-java]
- The docker-compose setup has been inspired by [this blog][dcompose]

[tc-libs]:https://download.eclipse.org/tracecompass/stable/repository/plugins/
[dcompose]:https://blog.devgenius.io/how-to-setup-grafana-with-postgresql-database-using-docker-compose-a-step-by-step-guide-e5a9cce90ba3
[google-java]:https://github.com/google/google-java-format
[sql-views]:https://www.postgresql.org/docs/current/sql-createview.html
[tracing]:https://en.wikipedia.org/wiki/Tracing_(software)
