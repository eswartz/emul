/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.cpu;

import v9t9.Machine;
import v9t9.Memory;
import v9t9.MemoryDomain;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu {
    public Cpu(Machine machine) {
        this.machine = machine;
        this.memory = machine.getMemory();
        this.console = memory.CPU;
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

    /** status register */
    private short ST;

    /** expanded status register */

    /* interrupt level */
    private byte intlevel;

    final int interruptTick = 1000 / 30;    // TODO: non-reduced rate
    long lastInterrupt;
    
    public void changeintmask(int mask) {
        intlevel = (byte) (mask & 0xf);
    }

    /* interrupt pins */
    public static final int INTPIN_RESET = 1;

    public static final int INTPIN_LOAD = 2;

    public static final int INTPIN_INTREQ = 4;

    public void holdpin(int mask) {
        intpins |= (byte) mask;
        // TODO stateflag |= ST_INTERRUPT;
    }

    private byte intpins;

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
        short oldwp = WP;
        short oldpc = PC;
        setWP(newwp);
        setPC(newpc);
        memory.CPU.writeWord(newwp + 13 * 2, oldwp);
        memory.CPU.writeWord(newwp + 14 * 2, oldpc);
        memory.CPU.writeWord(newwp + 15 * 2, getST());
   }

    public void contextSwitch(int addr) {
        contextSwitch(memory.CPU.readWord(addr), memory.CPU.readWord(addr+2));
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
     *  
     */
    public void ping() {

        /*long now = System.currentTimeMillis(); 
        //lastInterrupt = System.currentTimeMillis();
        if (machine.allowInterrupts && now >= lastInterrupt + interruptTick) { 
            holdpin(Cpu.INTPIN_INTREQ);
            lastInterrupt += interruptTick;
        }*/

        if (status.getIntMask() != 0 && intpins != 0) {
            intlevel = (byte) status.getIntMask();
            handleInterrupt();
        }
    }

    /**
     *  
     */
    private void handleInterrupt() {
        // TODO Auto-generated method stub
        //      any sort of interrupt that sets intpins9900

        // non-maskable
        if ((intpins & INTPIN_LOAD) != 0) {
            intpins &= ~INTPIN_LOAD;
            //logger(_L | 0, "**** NMI ****");
            System.out.println("**** NMI ****");
            contextSwitch(0xfffc);
            //instcycles += 22;
            //execute_current_inst();
        } else
        // non-maskable (?)
        if ((intpins & INTPIN_RESET) != 0) {
            intpins &= ~INTPIN_RESET;
            //logger(_L | 0, "**** RESET ****\n");
            System.out.println("**** RESET ****");
            contextSwitch(0);
            //instcycles += 26;
            //execute_current_inst();
        } else
        // maskable
        if ((intpins & INTPIN_INTREQ) != 0) {
            short highmask = (short) (1 << (intlevel + 1));

            // 99/4A console hardcodes all interrupts as level 1,
            // so if any are available, level 1 is it.
            if (intlevel != 0
            // TODO: && read9901int() &&
            //(!(stateflag & ST_DEBUG) || (allow_debug_interrupts))
            ) {
                System.out.println("**** INT ****");
                intpins &= ~INTPIN_INTREQ;
                contextSwitch(0x4);
                intlevel--;
                machine.getClient().timerTick();
                //instcycles += 22;
                //execute_current_inst();
            }
        } else
            intpins = 0; // invalid

        //if (intpins == 0)
        //	stateflag &= ~ST_INTERRUPT;
    }

}