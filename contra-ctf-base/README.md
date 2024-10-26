# CONtainerized TRace Analysis

A service to interpret CTF traces, and simply print them out.

## How to build and run

```
# To install locally the tc libs and deps needed
mvn initialize
# To build and get a runnable jar
mvn install
# To run
java -jar target/contra-ctf-base-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## NOTEs

- Trace Compass Libraries can be downloaded from [here][tc-libs].

[tc-libs]:https://download.eclipse.org/tracecompass/stable/repository/plugins/
