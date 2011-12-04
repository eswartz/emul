/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine.memory;

import v9t9.base.utils.Check;
import v9t9.common.memory.MemoryDomain;
import v9t9.engine.client.ISoundHandler;
import v9t9.engine.hardware.ISpeechChip;
import v9t9.engine.machine.IMachine;
import v9t9.engine.speech.ISpeechDataSender;
import v9t9.engine.speech.TMS5220;

/** 
 * Speech chip
 * @author ejs
 */
public class SpeechMmio implements IConsoleMmioWriter, IConsoleMmioReader {


	private final ISpeechChip speech;

	public SpeechMmio(final ISpeechChip speech) {
		Check.checkArg(speech);
		this.speech = speech;
    }

    public byte read(int addrMask) {
    	return speech.read();
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        speech.write(val);
    }

}
