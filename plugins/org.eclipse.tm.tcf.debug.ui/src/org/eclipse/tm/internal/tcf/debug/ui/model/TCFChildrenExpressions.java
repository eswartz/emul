package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.HashMap;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IExpressions;

public class TCFChildrenExpressions extends TCFChildren {

    private final TCFNodeStackFrame node;
    private final HashMap<IExpression,TCFNodeExpression> map =
        new HashMap<IExpression,TCFNodeExpression>();
    
    private final IExpressionsListener listener = new IExpressionsListener() {
        
        public void expressionsAdded(IExpression[] expressions) {
            expressionsRemoved(expressions);
        }

        public void expressionsChanged(IExpression[] expressions) {
            expressionsRemoved(expressions);
        }

        public void expressionsRemoved(IExpression[] expressions) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    reset();
                    node.parent.addModelDelta(IModelDelta.CONTENT);
                }
            });
        }
    };

    TCFChildrenExpressions(TCFNodeStackFrame node) {
        super(node.model.getLaunch().getChannel(), 32);
        this.node = node;
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        m.addExpressionListener(listener);
    }

    @Override
    void dispose(String id) {
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        m.removeExpressionListener(listener);
        TCFNode n = node.model.getNode(id);
        super.dispose(id);
        if (n instanceof TCFNodeExpression) {
            map.remove(((TCFNodeExpression)n).getExpression());
        }
    }
    
    void add(TCFNode n) {
        super.add(n);
        TCFNodeExpression e = (TCFNodeExpression)n;
        assert map.get(e.getExpression()) == null;
        map.put(e.getExpression(), e);
    }
    
    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended();
    }

    @Override
    protected boolean startDataRetrieval() {
        IExpressions exps = node.model.getLaunch().getService(IExpressions.class);
        if (exps == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        for (final IExpression e : m.getExpressions()) {
            TCFNodeExpression ne = map.get(e);
            if (ne == null) {
                assert command == null;
                command = exps.create(node.id, null, e.getExpressionText(), new IExpressions.DoneCreate() {
                    public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                        if (isDisposed()) {
                            IExpressions exps = channel.getRemoteService(IExpressions.class);
                            exps.dispose(context.getID(), new IExpressions.DoneDispose() {
                                public void doneDispose(IToken token, Exception error) {
                                    if (error == null) return;
                                    if (channel.getState() != IChannel.STATE_OPEN) return;
                                    Activator.log("Error disposing remote expression evaluator", error);
                                }
                            });
                            return;
                        }
                        add(new TCFNodeExpression(node, error, e, context));
                        if (command != token) return;
                        command = null;
                        run();
                    }
                });
                return false;
            }
            assert ne.getExpression() == e;
            data.put(ne.id, ne);
        }
        set(null, null, data);
        return true;
    }
}
