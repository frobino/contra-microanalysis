# CONtainerized TRace Analysis

CONTRAs are services to *interpret CTF traces*, *derive information*, and
*store it to a DB* for further analysis.

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
is to run docker or docker-compose in one of the CONTRAs in this repo.

Currently we have 2 CONTRAs:

- [**contra-kernel**](contra-kernel/README.md), which reuses the
  Trace Compass Kernel Analysis to derive information from a lttng
  kernel trace in CTF format;
- [**contra-ust**](contra-ust/README.md), which reuses a generic
  Trace Compass Analysis to derive information from a lttng userspace
  trace in CTF format;

See the README in each subproject for more detailed instructions.
As of now, **contra-kernel** is more complete and tested, so we suggest
to start from there.

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
