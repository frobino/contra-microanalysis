package org.frobino.contra.kernel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class MyStateProvider extends AbstractTmfStateProvider {

  private PostgreSSBuilder fSsbPg;

  public MyStateProvider(ITmfTrace trace) {
    super(trace, "org.frobino.ctf2json.MyStateProvider");
    fSsbPg = new PostgreSSBuilder();
  }

  @Override
  public ITmfStateProvider getNewInstance() {
    return new MyStateProvider(getTrace());
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  protected @Nullable ITmfStateSystemBuilder getStateSystemBuilder() {
    return fSsbPg;
  }

  @Override
  protected void eventHandle(ITmfEvent event) {
    System.out.println(
        "Handling event with name: "
            + event.getName()
            + " at time: "
            + event.getTimestamp().toNanos()
            + " with content: "
            + event.getContent());

    // TODO:
    // - create a own class implementing ITmfStateSystemBuilder
    // - create in this state provider a singleton of that object which is returned here
    //
    // or (to make it more TC framework friendly):
    //
    // - override assignTargetStateSystem(ITmfStateSystemBuilder ssb)
    // - override getBackEndType so it returns SQL
    // - override executeAnalysis so it runs the super.executeAnalysis, etc.

    final ITmfStateSystemBuilder ssb = getStateSystemBuilder();
    String cpuNr = event.getContent().getField("context.cpu_id").getFormattedValue();
    int cpuQuark = ssb.getQuarkAbsoluteAndAdd(cpuNr);

    if (event.getContent().getField("my_string_field").getFormattedValue().contains("starts")) {
      ssb.modifyAttribute(event.getTimestamp().toNanos(), 1, cpuQuark);
    } else if (event
        .getContent()
        .getField("my_string_field")
        .getFormattedValue()
        .contains("ends")) {
      ssb.modifyAttribute(event.getTimestamp().toNanos(), null, cpuQuark);
    }
    // Yee! We have put something in the SS!
  }

  // Make eventHandle visible (I am breaking the API, to avoid dragging in a entire analysis module)
  public void myEventHandle(ITmfEvent event) {
    this.eventHandle(event);
  }

  @Override
  public void done() {
    super.done();
    fSsbPg.closeConnection();
  }
}
