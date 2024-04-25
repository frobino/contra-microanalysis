package org.frobino.contra.kernel;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

public class App {
    public static void main(String[] args) {
        final String lttngUstTracePath = "resources/traces/lttng-ust";

        readTrace(lttngUstTracePath);

        interpretTraceWithMyStateProvider(lttngUstTracePath);
    }

    private static void interpretTraceWithMyStateProvider(final String tracePath) {
        CTFTrace trace;
        CTFTraceReader traceReader;
        try {
            trace = new CTFTrace(tracePath);
            CtfTmfEventFactory factory = CtfTmfEventFactory.instance();
            traceReader = new CTFTraceReader(trace);

            /*
             * To use the trace in the Trace Compass framework, it is needed to "bridge"
             * CtfTrace to CtfTmfTrace
             */
            CtfTmfTrace tmfTrace = new CtfTmfTrace();
            try {
                /*
                 * This init is needed to enable tmf to create a CtfTmfEvent with correct timing
                 * 
                 * (see CtfTmfEvent e = factory.createEvent)
                 * 
                 * TODO: check if it is really needed to initialize fTrace in
                 * CtfTmfTrace.initTrace, or if it can be initialized in the constructor
                 */
                tmfTrace.initTrace(null, tracePath, ITmfEvent.class);
            } catch (TmfTraceException e1) {
                // Issues when initializing tmfTrace, expecting problems when creating the
                // CtfTmfEvent
                e1.printStackTrace();
            }

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
