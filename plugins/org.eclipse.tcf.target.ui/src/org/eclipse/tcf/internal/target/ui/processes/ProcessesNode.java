package org.eclipse.tcf.internal.target.ui.processes;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.services.IProcesses;

public class ProcessesNode {

	private final ITarget target;
	private final ProcessesNode parent;
	private final String contextId;
	private IProcesses.ProcessContext context;
	
	public ProcessesNode(ITarget target, String contextId) {
		this(target, null, contextId);
	}
	
	public ProcessesNode(ITarget target, ProcessesNode parent, String contextId) {
		this.target = target;
		this.parent = parent;
		this.contextId = contextId;
	}
	
	public String getName() {
		if (context != null)
			return context.getName() + " (" + contextId + ")";
		else
			return contextId;
	}
	
	public String getContextId() {
		return contextId;
	}
	
	public void setContext(IProcesses.ProcessContext context) {
		this.context = context;
	}
	
	public IProcesses.ProcessContext getContext() {
		return context;
	}
	
	public Object[] getChildren(final TreeViewer viewer) {
		return new Object[0];
	}
	
	public Object getParent() {
		return parent != null ? parent : target;
	}
	
}
