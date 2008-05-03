package org.eclipse.tm.internal.tcf.dsf.services;

import java.math.BigInteger;
import java.util.Map;

public class TCFDSFRunControlState {

    public boolean is_suspended;
    public boolean is_running;
    public String suspend_pc;
    public String suspend_reason;
    public Map<String,Object> suspend_params;
    
    public TCFAddress getPC() {
        if (suspend_pc == null) return null;
        return new TCFAddress(new BigInteger(suspend_pc));
    }
}

