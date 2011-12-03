/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine.memory;

import v9t9.engine.client.ISoundHandler;
import v9t9.engine.machine.IMachine;
import v9t9.engine.speech.TMS5220;
import v9t9.engine.speech.LPCSpeech.Sender;

/** 
 * Speech chip
 * @author ejs
 */
public class SpeechMmio implements IConsoleMmioWriter, IConsoleMmioReader {

	private TMS5220 sp;

	public SpeechMmio(final IMachine machine) {
		sp = new TMS5220(machine.getMemory().getDomain("SPEECH"));
		sp.setSender(new Sender() {

			private ISoundHandler soundHandler;

			public void send(short val, int pos, int length) {
				if (soundHandler == null)
					soundHandler = machine.getSound().getSoundHandler();
				if (soundHandler != null)
					soundHandler.speech(val);
			}
			
		});
		sp.setMachine(machine);
    }

    public byte read(int addrMask) {
    	return sp.read();
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        sp.write(val);
    }

    public int getAddr() {
    	return sp.getAddr();
    }
    
    public void setAddr(int addr) {
    	sp.setAddr(addr);
    }
    
    public boolean isAddrComplete() {
    	return sp.getAddrPos() == 0 || sp.getAddrPos() == 5;
    }
    
}
