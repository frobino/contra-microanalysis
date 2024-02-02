package org.frobino.contra.kernel;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

public class App {
	public static void main(String[] args) {
	    final String lttngUstTracePath = "resources/traces/lttng-ust";
	    CTFTrace trace;
	    CTFTraceReader traceReader;
	    
	    try {
			trace = new CTFTrace(lttngUstTracePath);
			traceReader = new CTFTraceReader(trace);
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

		try {
			trace = new CTFTrace(lttngUstTracePath);
			CtfTmfEventFactory factory = CtfTmfEventFactory.instance();
			traceReader = new CTFTraceReader(trace);
			CtfTmfTrace tmfTrace = new CtfTmfTrace();
			// Configure state provider, including State System
			MyStateProvider sp = new MyStateProvider(tmfTrace);
			IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend("Test", 0L);
			ITmfStateSystemBuilder stateSystem = StateSystemFactory.newStateSystem(backend);
			sp.assignTargetStateSystem(stateSystem);
			// Read events from real trace
			while (traceReader.hasMoreEvents()) {
				IEventDefinition def = traceReader.getCurrentEventDef();
				CtfTmfEvent e = factory.createEvent(tmfTrace, def, "");
				sp.myEventHandle(e);
				traceReader.advance();
			}
			// Close the reader
			traceReader.close();
		} catch (CTFException e) {
			// Issues when creating new CTFTrace
			e.printStackTrace();
		}
	    
	}
}
