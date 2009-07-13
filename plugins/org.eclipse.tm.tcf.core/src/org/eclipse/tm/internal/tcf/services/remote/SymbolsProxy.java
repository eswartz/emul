package org.eclipse.tm.internal.tcf.services.remote;

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.ISymbols;

public class SymbolsProxy implements ISymbols {

    private final IChannel channel;
    
    private class Context implements Symbol {
        
        private final byte[] value;
        private final Map<String,Object> props;
        
        Context(Map<String,Object> props) {
            this.props = props;
            value = JSON.toByteArray(props.get(PROP_VALUE));
        }

        public Number getAddress() {
            return (Number)props.get(PROP_ADDRESS);
        }

        public String getBaseTypeID() {
            return (String)props.get(PROP_BASE_TYPE_ID);
        }

        public String getExeContextID() {
            return (String)props.get(PROP_EXE_ID);
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getIndexTypeID() {
            return (String)props.get(PROP_INDEX_TYPE_ID);
        }

        public int getLength() {
            Number n = (Number)props.get(PROP_LENGTH);
            if (n == null) return 0;
            return n.intValue();
        }

        public String getName() {
            return (String)props.get(PROP_NAME);
        }

        public int getOffset() {
            Number n = (Number)props.get(PROP_OFFSET);
            if (n == null) return 0;
            return n.intValue();
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public int getSize() {
            Number n = (Number)props.get(PROP_SIZE);
            if (n == null) return 0;
            return n.intValue();
        }

        public SymbolClass getSymbolClass() {
            Number n = (Number)props.get(PROP_SYMBOL_CLASS);
            if (n != null) {
                switch (n.intValue()) {
                case 1: return SymbolClass.value;
                case 2: return SymbolClass.reference;
                case 3: return SymbolClass.function;
                case 4: return SymbolClass.type;
                }
            }
            return SymbolClass.unknown;
        }

        public TypeClass getTypeClass() {
            Number n = (Number)props.get(PROP_TYPE_CLASS);
            if (n != null) {
                switch (n.intValue()) {
                case 1: return TypeClass.cardinal;
                case 2: return TypeClass.integer;
                case 3: return TypeClass.real;
                case 4: return TypeClass.pointer;
                case 5: return TypeClass.array;
                case 6: return TypeClass.composite;
                case 7: return TypeClass.enumeration;
                case 8: return TypeClass.function;
                }
            }
            return TypeClass.unknown;
        }

        public String getTypeID() {
            return (String)props.get(PROP_TYPE_ID);
        }

        public byte[] getValue() {
            return value;
        }
    }

    public SymbolsProxy(IChannel channel) {
        this.channel = channel;
    }
    
    public String getName() {
        return NAME;
    }

    public IToken getContext(String id, final DoneGetContext done) {
        return new Command(channel, this, "getContext", new Object[]{ id }) {
            @SuppressWarnings("unchecked")
            @Override
            public void done(Exception error, Object[] args) {
                Context ctx = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    if (args[1] != null) ctx = new Context((Map<String,Object>)args[1]);
                }
                done.doneGetContext(token, error, ctx);
            }
        }.token;
    }

    public IToken getChildren(String parent_context_id, final DoneGetChildren done) {
        return new Command(channel, this, "getChildren", new Object[]{ parent_context_id }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] lst = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    lst = toStringArray(args[1]);
                }
                done.doneGetChildren(token, error, lst);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
}
