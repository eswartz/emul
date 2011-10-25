package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

public class UpdatePolicyMenu extends CompoundContributionItem {

    private static final String[] policy_names = {
        "Automatic",
        "Manual",
        //"Breakpoint Hit",
    };

    @Override
    protected IContributionItem[] getContributionItems() {
        IContributionItem[] items = new IContributionItem[policy_names.length];
        for (int i = 0; i < items.length; i++) {
            final int n = i;
            items[i] = new ContributionItem() {
                @Override
                public void fill(final Menu menu, int index) {
                    final MenuItem item = new MenuItem(menu, SWT.RADIO);
                    item.setText(policy_names[n]);
                    final MenuListener menu_listener = new MenuListener() {
                        public void menuShown(MenuEvent e) {
                            item.setSelection(getPolicy() == n);
                        }
                        public void menuHidden(MenuEvent e) {
                        }
                    };
                    menu.addMenuListener(menu_listener);
                    item.addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e) {
                            menu.removeMenuListener(menu_listener);
                        }
                    });
                    item.addSelectionListener(new SelectionListener() {
                        public void widgetSelected(SelectionEvent e) {
                            if (item.getSelection()) setPolicy(n);
                        }
                        public void widgetDefaultSelected(SelectionEvent e) {
                        }
                    });
                }
            };
        }
        return items;
    }

    private IWorkbenchPart getPart() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
    }

    private TCFNode getRootNode(IWorkbenchPart part) {
        IWorkbenchPartSite site = part.getSite();
        if (site == null || IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
            return null;
        }
        if (part instanceof IDebugView) {
            Object input = ((IDebugView)part).getViewer().getInput();
            if (input instanceof TCFNode) return (TCFNode)input;
        }
        return null;
    }

    private int getPolicy() {
        final IWorkbenchPart part = getPart();
        if (part == null) return 0;
        final TCFNode node = getRootNode(part);
        if (node == null) return 0;
        return new TCFTask<Integer>(node.getChannel()) {
            public void run() {
                if (!node.getModel().isLocked(part)) done(0);
                else done(1);
            }
        }.getE();
    }

    private void setPolicy(final int n) {
        final IWorkbenchPart part = getPart();
        if (part == null) return;
        final TCFNode node = getRootNode(part);
        if (node == null) return;
        new TCFTask<Object>(node.getChannel()) {
            public void run() {
                if (n == 0) {
                    node.getModel().clearLock(part);
                }
                else {
                    node.getModel().setLock(part);
                }
                done(null);
            }
        }.getE();
    }
}
