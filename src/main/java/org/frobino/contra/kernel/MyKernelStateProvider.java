package org.frobino.contra.kernel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class MyKernelStateProvider extends KernelStateProvider{

    private PostgreSSBuilder fSsbPg;
    private IKernelAnalysisEventLayout fLayout;
    
    public MyKernelStateProvider(ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, layout);
        fSsbPg  = new PostgreSSBuilder();
        fLayout = layout;
    }
    
    @Override
    public KernelStateProvider getNewInstance() {
        return new MyKernelStateProvider(getTrace(), fLayout);
    }
    
    @Override
    protected @Nullable ITmfStateSystemBuilder getStateSystemBuilder() {
        return fSsbPg;
    }
    
    // Make eventHandle visible (I am breaking the API, to avoid dragging in a entire analysis module)
    public void myEventHandle(ITmfEvent event) {
        this.eventHandle(event);
    }
    
    @Override
    public void done() {
        super.done();
        fSsbPg.closeHistory(fSsbPg.getCurrentEndTime());
    }
    
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        super.dispose();
        fSsbPg.closeConnection();
    }
}
