package org.frobino.contra.kernel;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
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
    String lttngKernelTracePath = "resources/traces/lttng-kernel/wget-first-call";

    String envTracePath = System.getenv("CONTRA_TRACE_PATH");
    if (envTracePath != null) {
      lttngKernelTracePath = envTracePath;
    }
    interpretTraceWithMyKernelStateProvider(lttngKernelTracePath);
  }

  private static void interpretTraceWithMyKernelStateProvider(final String tracePath) {
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
      MyKernelStateProvider sp;
      if (trace instanceof IKernelTrace) {
        sp = new MyKernelStateProvider(tmfTrace, ((IKernelTrace) trace).getKernelEventLayout());
      } else {
        /* Fall-back to the base LttngEventLayout */
        sp = new MyKernelStateProvider(tmfTrace, DefaultEventLayout.getInstance());
      }
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
      sp.dispose();
      // Close the reader
      traceReader.close();
    } catch (CTFException e) {
      // Issues when creating new CTFTrace
      e.printStackTrace();
    }
  }
}
