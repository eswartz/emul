/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package proto;

import v9t9.MemoryDomain;
import v9t9.cpu.CompiledCode;
import v9t9.cpu.Executor;


/**
 * @author ejs
 */
public class ProtoCompiledCode extends CompiledCode {

    /* (non-Javadoc)
     * @see v9t9.cpu.CompiledCode#run(v9t9.cpu.Executor)
     */
    public boolean run() {
        short pc;
        pc = cpu.getPC();
        switch (pc) {
        case 0x100:
            pc++;
            break;
        case 0x102:
            break;
        }
        cpu.setPC(pc);
        return false;
    }
    
    ProtoCompiledCode(Executor exec) {
        super(exec);
    }
    
    public static void main(String args[]) {
        v9t9.cpu.Executor exec = null;
        ProtoCompiledCode foo = new ProtoCompiledCode(exec);
    }
    
}
