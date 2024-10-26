package org.frobino.contra.ust;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;

public class App {
  public static void main(String[] args) {
    String lttngUstTracePath = "resources/traces/lttng-ust";

    String envTracePath = System.getenv("CONTRA_TRACE_PATH");
    if (envTracePath != null) {
      lttngUstTracePath = envTracePath;
    }
    readTrace(lttngUstTracePath);
  }

  /*
   * Simple method to read a CTF trace and use the CTF parser of trace compass to
   * interpret and print out
   */
  private static void readTrace(final String tracePath) {
    CTFTrace trace;
    CTFTraceReader traceReader;

    try {
      trace = new CTFTrace(tracePath);
      traceReader = new CTFTraceReader(trace);
      int counter = 0;
      while (traceReader.hasMoreEvents()) {
        System.out.println("Event " + counter + ":");
        System.out.println(
            "Event name: " + traceReader.getCurrentEventDef().getDeclaration().getName());
        System.out.println("Timestamp: " + traceReader.getCurrentEventDef().getTimestamp());
        System.out.println("Events fields: " + traceReader.getCurrentEventDef().getFields());
        System.out.println();
        traceReader.advance();
        counter++;
      }
      // Close the reader
      traceReader.close();
    } catch (CTFException e) {
      // Issues when creating new CTFTrace
      e.printStackTrace();
    }
  }
}
