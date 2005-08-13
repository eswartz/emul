/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package sound;

import v9t9.Machine;

/** Sound chip entry
 * @author ejs
 */
public class Sound implements v9t9.Memory.ConsoleMmioWriter {

    private Machine machine;
    
    /**
     * @param machine
     */
    public Sound(Machine machine) {
        this.machine = machine;
    }

    /**
     * @see v9t9.Memory.ConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        machine.getClient().getSound().writeSound(val);
    }
    
}
