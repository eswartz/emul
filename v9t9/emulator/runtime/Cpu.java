/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime;

import v9t9.emulator.Machine;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu {
    public Cpu(Machine machine) {
        this.machine = machine;
        this.memory = machine.getMemory();
        this.console = machine.CPU;
        this.status = new Status();
    }

    public short getPC() {
        return PC;
    }

    public void setPC(short pc) {
        PC = pc;
    }

    Status status;

    public short getST() {
        return status.flatten();
    }

    public void setST(short st) {
        status.expand(st);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public short getWP() {
        return WP;
    }

    public void setWP(short wp) {
        // TODO: verify
        WP = wp;
    }

    Machine machine;

    public Memory memory;

    MemoryDomain console;

    /** program counter */
    private short PC;

    /** workspace pointer */
    private short WP;

    /** current handled interrupt level */
    private byte intlevel;

    long lastInterrupt;
    
    /* interrupt pins */
    public static final int INTPIN_RESET = 1;

    public static final int INTPIN_LOAD = 2;

    public static final int INTPIN_INTREQ = 4;

    public void holdpin(int mask) {
        intpins |= (byte) mask;
        //abortIfInterrupted();
    }

    private byte intpins;

    //public static Object executionToken;

    /**
     * @return
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * 
     */
    public void contextSwitch(short newwp, short newpc) {
    	//System.out.println("contextSwitch from " + 
    	//Utils.toHex4(WP)+"/"+Utils.toHex4(PC) +
    	//" to " + Utils.toHex4(newwp)+"/"+Utils.toHex4(newpc));
        short oldwp = WP;
        short oldpc = PC;
        setWP(newwp);
        setPC(newpc);
        console.writeWord(newwp + 13 * 2, oldwp);
        console.writeWord(newwp + 14 * 2, oldpc);
        console.writeWord(newwp + 15 * 2, getST());
   }

    public void contextSwitch(int addr) {
        contextSwitch(console.readWord(addr), console.readWord(addr+2));
        if (addr == 0) {
            /*
             * this mimics the behavior where holding down fctn-quit keeps the
             * program going
             */
            // TODO
            //trigger9901int(M_INT_VDP);
            holdpin(INTPIN_INTREQ);
        }
    }

    /**
     * Called by compiled code to see if it's time to stop
     * running.  All this can do is throw AbortedException().
     */
    public void abortIfInterrupted() {
        if (status.getIntMask() != 0 && intpins != 0) {
            intlevel = (byte) status.getIntMask();
        }
        if (intlevel != 0) {
           throw new AbortedException();
        }
            
    }
    
    public void handleInterrupts() {
        if (intlevel != 0) {
            handleInterrupt();
        }
    }

    /**
     *  
     */
    private void handleInterrupt() {
        //      any sort of interrupt that sets intpins9900

        // non-maskable
        if ((intpins & INTPIN_LOAD) != 0) {
            intpins &= ~INTPIN_LOAD;
            //logger(_L | 0, "**** NMI ****");
            System.out.println("**** NMI ****");
            contextSwitch(0xfffc);
            //instcycles += 22;
            //execute_current_inst();
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else
        // non-maskable (?)
        if ((intpins & INTPIN_RESET) != 0) {
            intpins &= ~INTPIN_RESET;
            //logger(_L | 0, "**** RESET ****\n");
            System.out.println("**** RESET ****");
            contextSwitch(0);
            //instcycles += 26;
            //execute_current_inst();
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else
        // maskable
        if ((intpins & INTPIN_INTREQ) != 0) {
            //short highmask = (short) (1 << (intlevel + 1));

            // 99/4A console hardcodes all interrupts as level 1,
            // so if any are available, level 1 is it.
            if (intlevel != 0
            // TODO: && read9901int() &&
            //(!(stateflag & ST_DEBUG) || (allow_debug_interrupts))
            ) {
                //System.out.println("**** INT ****");
                intpins &= ~INTPIN_INTREQ;
                contextSwitch(0x4);
                intlevel = 0;
                machine.getClient().timerInterrupt();
                //instcycles += 22;
                //execute_current_inst();
                
                // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
                machine.getExecutor().interpretOneInstruction();
                //machine.getExecutor().execute();
                //throw new AbortedException();
            }
        } else {
			intpins = 0; // invalid
		}

        //if (intpins == 0)
        //	stateflag &= ~ST_INTERRUPT;
    }

    public int getRegister(int reg) {
        return console.readWord(WP + reg*2);
    }

}