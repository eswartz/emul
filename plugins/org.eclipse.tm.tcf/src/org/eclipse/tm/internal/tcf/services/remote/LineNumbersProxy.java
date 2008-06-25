package org.eclipse.tm.internal.tcf.services.remote;

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.ILineNumbers;


public class LineNumbersProxy implements ILineNumbers {
    
    private final IChannel channel;

    public LineNumbersProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken mapToSource(String context_id, Number start_address,
            Number end_address, final DoneMapToSource done) {
        return new Command(channel, this, "mapToSource", new Object[]{ context_id,
                start_address, end_address }) {
            @Override
            public void done(Exception error, Object[] args) {
                CodeArea[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toTextAreaArray(args[1]);
                }
                done.doneMapToSource(token, error, arr);
            }
        }.token;
    }
    
    private static int getInteger(Map<String,Object> map, String name, int def) {
        Number n = (Number)map.get(name);
        if (n == null) return def;
        return n.intValue();
    }
    
    private static String getString(Map<String,Object> map, String name, String def) {
        String s = (String)map.get(name);
        if (s == null) return def;
        return s;
    }
    
    private static boolean getBoolean(Map<String,Object> map, String name) {
        Boolean b = (Boolean)map.get(name);
        if (b == null) return false;
        return b.booleanValue();
    }
    
    @SuppressWarnings("unchecked")
    private CodeArea[] toTextAreaArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        int n = 0;
        CodeArea[] arr = new CodeArea[c.size()];
        String directory = null;
        String file = null;
        for (Map<String,Object> area : c) {
            directory = getString(area, "Dir", directory);
            file = getString(area, "File", file);
            arr[n++] = new CodeArea(directory, file,
                    getInteger(area, "SLine", 0), getInteger(area, "SCol", 0),
                    getInteger(area, "ELine", 0), getInteger(area, "ECol", 0),
                    (Number)area.get("SAddr"), (Number)area.get("EAddr"),
                    getInteger(area, "ISA", 0),
                    getBoolean(area, "IsStmt"), getBoolean(area, "BasicBlock"),
                    getBoolean(area, "PrologueEnd"), getBoolean(area, "EpilogueBegin"));
        }
        return arr;
    }
}
