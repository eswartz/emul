/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.windriver.debug.tcf.core.model.ITCFBreakpointListener;
import com.windriver.debug.tcf.core.model.TCFBreakpoint;
import com.windriver.debug.tcf.core.model.TCFBreakpointsStatus;
import com.windriver.debug.tcf.core.model.TCFLaunch;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IBreakpoints;

public class TCFModelPresentation implements IDebugModelPresentation {
    
    private final Collection<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();
    
    private IWorkbenchWindow active_window;
    private TCFLaunch launch_selection;
    
    private final TCFLaunch.Listener launch_listener = new TCFLaunch.Listener() {

        public void onConnected(TCFLaunch launch) {
            updateLaunchSelection();
        }

        public void onDisconnected(TCFLaunch launch) {
            updateLaunchSelection();
        }
    };
    
    private final ISelectionListener selection_listener = new ISelectionListener() {
        
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateLaunchSelection();
        }
    };
    
    private final IWindowListener window_listener = new IWindowListener() {

        public void windowActivated(IWorkbenchWindow window) {
            if (active_window != null) {
                active_window.getSelectionService().removeSelectionListener(
                        IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
                active_window = null;
            }
            window.getSelectionService().addSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            active_window = window;
            updateLaunchSelection();
        }

        public void windowClosed(IWorkbenchWindow window) {
            if (window == active_window) {
                active_window.getSelectionService().removeSelectionListener(
                        IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
                active_window = null;
            }
        }

        public void windowDeactivated(IWorkbenchWindow window) {
        }

        public void windowOpened(IWorkbenchWindow window) {
        }
    };
    
    private final ITCFBreakpointListener breakpoint_status_listener = new ITCFBreakpointListener() {

        public void breakpointStatusChanged(String id) {
            refreshBreakpointView();
        }

        public void breakpointRemoved(String id) {
            refreshBreakpointView();
        }
    };
    
    public TCFModelPresentation() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                TCFLaunch.addListener(launch_listener);
            }
        });
        PlatformUI.getWorkbench().addWindowListener(window_listener);
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (w != null) window_listener.windowActivated(w); 
    }
    
    private void updateLaunchSelection() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                TCFLaunch launch = null;
                IAdaptable adaptable = DebugUITools.getDebugContext();
                if (adaptable != null) {
                    ILaunch x = (ILaunch)adaptable.getAdapter(ILaunch.class);
                    if (x instanceof TCFLaunch) {
                        final TCFLaunch l = (TCFLaunch)x;
                        final boolean[] b = new boolean[1];
                        Protocol.invokeAndWait(new Runnable() {
                            public void run() {
                                IChannel channel = l.getChannel();
                                b[0] = channel != null && channel.getState() == IChannel.STATE_OPEN;
                            }
                        });
                        if (b[0]) launch = l;
                    }
                }
                if (launch_selection != launch) {
                    setBreakpointStatusListener(launch_selection, launch);
                    launch_selection = launch;
                    refreshBreakpointView();
                }
            }
        });
    }
    
    private void refreshBreakpointView() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (active_window != null) {
                    final IDebugView view = (IDebugView)active_window.getActivePage().findView(
                            IDebugUIConstants.ID_BREAKPOINT_VIEW);
                    if (view != null) {
                        view.getViewer().refresh();
                    }
                }
            }
        });
    }
    
    private void setBreakpointStatusListener(final TCFLaunch prev, final TCFLaunch next) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                if (prev != null && prev.getBreakpointsStatus() != null) {
                    prev.getBreakpointsStatus().removeListener(breakpoint_status_listener);
                }
                if (next != null && next.getBreakpointsStatus() != null) {
                    next.getBreakpointsStatus().addListener(breakpoint_status_listener);
                }
            }
        });
    }
    
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    public void dispose() {
        if (launch_selection != null) {
            setBreakpointStatusListener(launch_selection, null);
            launch_selection = null;
        }
        if (active_window != null) {
            active_window.getSelectionService().removeSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            active_window = null;
        }
        PlatformUI.getWorkbench().removeWindowListener(window_listener);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                TCFLaunch.removeListener(launch_listener);
            }
        });
    }

    public void computeDetail(IValue value, IValueDetailListener listener) {
    }

    public Image getImage(Object element) {
        return null;
    }

    public String getText(Object element) {
        if (element instanceof TCFBreakpoint) {
            final TCFBreakpoint breakpoint = (TCFBreakpoint)element;
            final TCFLaunch launch = launch_selection;
            final String[] text = new String[1];
            text[0] = breakpoint.getText();
            if (launch != null) {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                        if (bs != null) {
                            Map<String,Object> map = bs.getStatus(breakpoint);
                            if (map != null) {
                                String status = null;
                                String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                                Object planted = map.get(IBreakpoints.STATUS_PLANTED);
                                if (error != null) status = error;
                                else if (planted != null) status = "Planted";
                                if (status != null) text[0] += " (" + status + ")";
                            }
                        }
                    }
                });
            }
            return text[0];
        }
        return null;
    }

    public void setAttribute(String attribute, Object value) {
    }

    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    public String getEditorId(IEditorInput input, Object element) {
        return null;
    }

    public IEditorInput getEditorInput(Object element) {
        return null;
    }
}
