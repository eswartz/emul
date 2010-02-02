/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine;
import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.emulator.hardware.speech.TMS5220;
import v9t9.emulator.hardware.speech.LPCSpeech.Sender;
import v9t9.engine.SoundHandler;

/** 
 * Speech chip
 * @author ejs
 */
public class SpeechMmio implements ConsoleMmioWriter, v9t9.emulator.Machine.ConsoleMmioReader {

	private TMS5220 sp;

	public SpeechMmio(final Machine machine) {
		sp = new TMS5220(machine.getMemory().getDomain("SPEECH"));
		sp.setSender(new Sender() {

			private SoundHandler soundHandler;

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
     * @see v9t9.engine.memory.Memory.ConsoleMmioWriter#write 
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
