package org.frobino.contra.kernel;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;

public class App {
	public static void main(String[] args) {
	    final String lttngUstTracePath = "resources/traces/lttng-ust";
	    try {
			CTFTrace trace = new CTFTrace(lttngUstTracePath);
			CTFTraceReader traceReader = new CTFTraceReader(trace);
			int counter = 0;
			while (traceReader.hasMoreEvents()) {
				System.out.println("Event " + counter + ":");
				System.out.println("Event name: " + traceReader.getCurrentEventDef().getDeclaration().getName());
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
