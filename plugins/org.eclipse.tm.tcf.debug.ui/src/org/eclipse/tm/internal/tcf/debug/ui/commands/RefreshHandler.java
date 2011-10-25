package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Object input = null;
        final IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part instanceof IDebugView) {
            IWorkbenchPartSite site = part.getSite();
            if (site != null && IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
                ISelection selection = HandlerUtil.getCurrentSelection(event);
                if (selection instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)selection).getFirstElement();
                    if (obj instanceof TCFNode) input = ((TCFNode)obj).getModel().getRootNode();
                }
            }
            else {
                input = ((IDebugView)part).getViewer().getInput();
            }
        }
        if (input instanceof TCFNode) {
            final TCFNode node = (TCFNode)input;
            return new TCFTask<Object>(node.getChannel()) {
                public void run() {
                    node.refresh(part);
                    if (node.getModel().clearLock(part)) {
                        node.getModel().setLock(part);
                    }
                    done(null);
                }
            }.getE();
        }
        return null;
    }
}
