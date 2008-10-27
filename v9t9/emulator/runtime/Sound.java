/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.emulator.runtime;

import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.Client;

/** Sound chip entry
 * @author ejs
 */
public class Sound implements ConsoleMmioWriter {

    private Client client;

    /**
     * @param machine
     */
    public Sound(Client client) {
        this.client = client;
    }

    /**
     * @see v9t9.engine.memory.Memory.ConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        client.getSoundHandler().writeSound(val);
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
}
