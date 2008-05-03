package org.eclipse.tm.internal.tcf.dsf.launch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFBreakpoints;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFMemory;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRegisters;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControl;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFStack;

class TCFDSFShutdownSequence extends Sequence {

    private final String session_id;
    private final Step[] steps;
    private DsfServicesTracker tracker;

    TCFDSFShutdownSequence(final DsfSession session, final TCFDSFLaunch launch, RequestMonitor monitor) {
        super(session.getExecutor(), monitor);
        session_id = session.getId();
        steps = new Step[] {
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        // Initialize services tracker.
                        tracker = new DsfServicesTracker(Activator.getBundleContext(), session_id);
                        monitor.done();
                    }
            
                    @Override
                    public void rollBack(RequestMonitor monitor) {
                        // In case the shutdown sequence aborts,
                        // ensure that the tracker is properly disposed.
                        tracker.dispose();
                        tracker = null;
                        monitor.done();
                    }
                }, 
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(TCFDSFBreakpoints.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(TCFDSFRegisters.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(TCFDSFMemory.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(TCFDSFStack.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(StepQueueManager.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        shutdownService(TCFDSFRunControl.class, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        tracker.dispose();
                        tracker = null;
                        monitor.done();
                    }
                } 
        };
    }

    private void shutdownService(Class<?> clazz, final RequestMonitor requestMonitor) {
        IDsfService service = (IDsfService)tracker.getService(clazz);
        if (service != null) {
            service.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleCompleted() {
                    if (!isSuccess()) {
                        Activator.getDefault().getLog().log(getStatus());
                    }
                    requestMonitor.done();
                }
            });
        }
        else {
            requestMonitor.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
                "Service '" + clazz.getName() + "' not found.", null)); //$NON-NLS-1$//$NON-NLS-2$
            requestMonitor.done();
        }
    }

    @Override
    public Step[] getSteps() {
        return steps;
    }
}
