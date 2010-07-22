/**
 * 
 */
package v9t9.emulator.hardware.speech;

import java.io.IOException;
import java.io.PrintWriter;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.utils.BinaryUtils;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.speech.LPCSpeech.Fetcher;
import v9t9.emulator.hardware.speech.LPCSpeech.Sender;
import v9t9.emulator.runtime.Logging;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.timer.FastTimer;
import v9t9.engine.timer.FastTimerTask;

/**
 * @author ejs
 *
 */
public class TMS5220 implements Fetcher, Sender {

	private final int SPEECH_TIMEOUT = 25+9;
	
	private int status;		/* as returned via read-status */
	/** talk status */
	final int SS_TS	= 0x80;
	
	/** buffer low */
	final int SS_BL	= 0x40;
	/** buffer empty */
	final int SS_BE	= 0x20;	
	/** internal flag */
	final int SS_SPEAKING = 1;	

	private int	gate;		/* how do we route Writes and Reads? */
	/** write -> command */
	final int GT_WCMD	= 0x1;		
	/** write -> speech external data */
	final int GT_WDAT	= 0x2;		
	/** read -> status */
	final int GT_RSTAT  = 0x4;	
	/** read -> data */
	final int GT_RDAT	= 0x8;			

	private	int addr;		/* address -- 20 bits */

	private byte fifo[];	/* fifo buffer */
	private int out,in;		/* ptrs.  out==in --> empty */
	private byte len;		/* # bytes in buffer */
		
	private int bit;		/* bit offset of whatever we're reading */
		
	private byte command;	/* last command */
	private byte data;		/* data register for reading */	
		
	private int timeout;	

	private int addr_pos;	/* for debugger: position of address (0=complete) */

	private DiskMemoryEntry speechRom;

	int speech_hertz = 8000;
	int speech_length = 200;

	private FastTimer speechTimer;

	private FastTimerTask speechTimerTask;

	private LPCSpeech lpc;
	
	public static final SettingProperty settingLogSpeech = new SettingProperty("LogSpeech",
			new Integer(1));

	private Sender sender;

	private Machine machine;
	
	public TMS5220(MemoryDomain speech) {
		
		Logging.registerLog(settingLogSpeech, 
				new PrintWriter(System.out, true));
		
		try {
			speechRom = DiskMemoryEntry.newByteMemoryFromFile(0, 0x8000, "Speech ROM",
					speech, "spchrom.bin", 0, false);
			speechRom.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fifo = new byte[16];
		lpc = new LPCSpeech();
		reset();
	}
	
	public int getAddr() {
		return addr;
	}

	public void setAddr(int addr) {
		this.addr = addr & 0xffff;
		addr_pos = 0;
	}

	public int getAddrPos() {
		return addr_pos;
	}

	public void write(byte val) {
		Logging.writeLogLine(2, settingLogSpeech, "speech write: " + HexUtils.toHex2((val&0xff)));
		if ((gate & GT_WCMD) != 0)
			command(val);
		else
			writeFIFO(val);
	}

	public byte read() {
		byte ret;
		if ((gate & GT_RSTAT) != 0) {
			byte stat = (byte) (status | 
				((status & SS_SPEAKING) != 0 ? SS_TS : 0));
			ret = stat;
		} else {
			gate = (gate & ~GT_RDAT) | GT_RSTAT;
			ret = data;
		}
		Logging.writeLogLine(3, settingLogSpeech, "Speech read: " + HexUtils.toHex2(ret));
		return ret;
	}

	public void command(byte cmd) {
		command = (byte) (cmd & 0x70);
		if (Logging.getLog(3, settingLogSpeech) != null) {
			Logging.writeLogLine(3, settingLogSpeech,
				"Cmd="+HexUtils.toHex2(cmd)+"  Status: " + 
				HexUtils.toHex2(status));
		}
		switch (command) {
		case 0x00:
		case 0x20:
			loadFrameRate(cmd & 0x7);
			break;
		case 0x10:
			readMemory();
			gate = (gate & ~GT_RSTAT) | GT_RDAT;
			break;
		case 0x30:
			readAndBranch();
			break;
		case 0x40:
			loadAddress(cmd & 0xf);
			break;
		case 0x50:
			speak();
			break;
		case 0x60:
			speakExternal();
			break;
		case 0x70:
			reset();
			break;
			/* default: ignore */
		}		
	}

	private int readMemory()
	{
		addr_pos = 0;
		if (speechRom == null || addr >= speechRom.size)
			data = 0;
		else
			data = speechRom.readByte(addr);
		addr++;
		//
		Logging.writeLogLine(2, settingLogSpeech,
				"Speech memory "+HexUtils.toHex4(addr)+" = " + HexUtils.toHex2(data));
		return data;
	}

	private int peek()
	{
		addr_pos = 0;
		if (speechRom == null || addr >= speechRom.size)
			data = 0;
		else
			data = speechRom.readByte(addr);
		//gate = (gate & ~GT_RSTAT) | GT_RDAT;
		return data & 0xff;
	}

	private void loadAddress(int nybble)
	{
		addr_pos = (addr_pos + 1) % 5;
		addr = (addr >> 4) | (nybble << 16);
		Logging.writeLogLine(3, settingLogSpeech,
				"Speech addr: "+HexUtils.toHex4(addr));
	}

	private void readAndBranch()
	{
		int         addr;

		addr_pos = 0;
		addr = (readMemory() << 8) + readMemory();
		addr = (addr & 0xc000) | (addr & 0x3fff);
		gate = (gate & ~GT_RDAT) | GT_RSTAT;
	}


	/*	Undocumented and unknown function... */
	private void loadFrameRate(int val) {
		if ((val & 0x4) != 0) {
			/* variable */
		} else {
			/* frameRate = val & 0x3; */
		}
	}

	private void speak()
	{
		Logging.writeLogLine(1, settingLogSpeech,
				"Speaking phrase at "+HexUtils.toHex4(addr));

		//demo_record_event(demo_type_speech, demo_speech_starting);

		// wait for previous sample to end, or else we
		// can end up stacking tons of digitized data
		speech_wait_complete(1);

//		SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);

		gate = (gate & ~GT_WDAT) | GT_WCMD;	/* just in case */
		bit = 0;					/* start on byte boundary */
		status |= SS_SPEAKING | SS_TS;

		/*
		while ((status & SS_SPEAKING) != 0)
			generateSpeech();			// not scheduled
		lpc.init();
		*/
		SpeechOn();
		
	}
	
	private void speakExternal()
	{
		Logging.writeLogLine(1, settingLogSpeech,
				"Speaking external data");
		//demo_record_event(demo_type_speech, demo_speech_starting);

		gate = (gate & ~GT_WCMD) | GT_WDAT;	/* accept data from I/O */
		purgeFIFO();
		SpeechOn();					/* call speech_intr every 25 ms */
		status |= SS_SPEAKING;
		timeout = SPEECH_TIMEOUT;
	}

	
	private void writeFIFO(byte val) {
		fifo[in] = BinaryUtils.swapbits(val);
		in = (byte) ((in + 1) & 15);
		Logging.writeLogLine(3, settingLogSpeech,
				"FIFO write: "+HexUtils.toHex2(val)+"; len = " +len);

		//logger(_L | L_3, _("FIFO write: %02X  len=%d\n"), val, len);
		if (len < 16)
			len++;
		timeout = SPEECH_TIMEOUT;
		status &= ~SS_BE;
		if (len > 8) {
			status &= ~SS_BL;
			generateSpeech();
		}		
	}


	private int readFIFO()
	{
		int         ret = fifo[out] & 0xff;

		Logging.writeLogLine(3, settingLogSpeech,
				"FIFO read: "+HexUtils.toHex2(ret)+"; len = " + len);

		if (len == 0) {
			status |= SS_BE;
			status &= ~SS_TS;
			reset();
			SpeechOff();
			Logging.writeLogLine(1, settingLogSpeech,
					"Speech timed out");
		}

		if (len > 0) {
			out = (out + 1) & 15;
			len--;
		}
		if (len < 8)
			status |= SS_BL;
		if (len == 0) {
			status |= SS_BE;
			status &= ~SS_TS;
		}
		return ret;
	}

	private int peekFIFO()
	{
		return fifo[out] & 0xff;
	}


	void speech_wait_complete(int seconds)
	{

		// TODO: how to wait?
		/*
		int ret;
		int tm;
		// wait for existing sample to finish
		tm = TM_GetTicks();
		while (TM_GetTicks() < tm + seconds * TM_HZ)
		{
			SPEECHPLAYING(ret, vms_Speech);
			if (!ret) break;
		}
		*/
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}

	private void purgeFIFO()
	{
		bit = 0;
		out = in = len = 0;
	}

	private void reset()
	{
		Logging.writeLogLine(1, settingLogSpeech,
				"Speech reset");
		status = SS_BE | SS_BL;
		purgeFIFO();
		command = 0x70;
		data = 0;
		addr = 0; addr_pos = 0;
		gate = GT_RSTAT | GT_WCMD;
		SpeechOff();
		status &= ~SS_SPEAKING;
		timeout = SPEECH_TIMEOUT;
		lpc.init();
		//in_speech_intr = 0;

		// flush existing sample
		//SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);
	}
	
	private void SpeechOn()
	{
		int hz;

		if (speechTimer == null) {
			speechTimer = new FastTimer();

			hz = speech_hertz / speech_length;
			speechTimerTask = new FastTimerTask() {
	
				@Override
				public void run() {
					if (machine != null) {
						if (!machine.isAlive())
							SpeechDone();
						if (Machine.settingPauseMachine.getBoolean())
							return;
					}
					generateSpeech();
				}
				
			};
			speechTimer.scheduleTask(speechTimerTask, hz);
		}
	}

	protected void generateSpeech() {
		boolean do_frame = false;

		Logging.writeLogLine(2, settingLogSpeech,
				"Speech generating");
		//logger(_L | L_2, _("Speech Interrupt\n"));

		if ((gate & GT_WDAT) != 0) {	/* direct data */
			if (0 == (status & SS_TS)) {	/* not talking yet... we're waiting for */
				if (0 == (status & SS_BL)) {	/* enough data in the buffer? */
					status |= SS_TS;		/* whee!  Start talking */
					do_frame = true;
				} else {
					if (timeout-- <= 0) {
						//speech_wait_complete(1);

						reset();
						//demo_record_event(demo_type_speech, demo_speech_terminating);

						// this apparently happens in normal cases
						Logging.writeLogLine(1, settingLogSpeech,
							"Speech timed out");
					}
				}
			}
			else {
				if ((status & SS_BL) != 0) {
					if (timeout-- <= 0) {
						//speech_wait_complete(1);

						reset();
						//demo_record_event(demo_type_speech, demo_speech_terminating);

						// this apparently happens in normal cases
						Logging.writeLogLine(1, settingLogSpeech,
							"Speech timed out");
					}
				}
				else
					do_frame = true;
			}
		}
		else {		/* vocab data */
			if ((status & SS_TS) != 0)
				do_frame = true;
		}

		if (do_frame) {
			boolean last = !lpc.frame(this, this, speech_length);

			//SPEECHPLAY(vms_Speech, speech_data, speech_length, speech_hertz);	
			if (last) {
				SpeechDone();
			}
		}


	  //out:
		  //logger(_L | L_4, _("Out of speech interrupt\n"));
	}

	private void SpeechDone() {
		Logging.writeLogLine(1, settingLogSpeech,
				"Done with speech phrase");
		SpeechOff();			/* stop interrupting */
		//demo_record_event(demo_type_speech, demo_speech_stopping);
		if ((gate & GT_WDAT) == 0)
			lpc.init();
		status &= ~(SS_TS | SS_SPEAKING);
		gate = (gate & ~GT_WDAT) | GT_WCMD;
	}

	private void SpeechOff()
	{
		if (speechTimer != null)
			speechTimer.cancel();
		speechTimer = null;
	}
	
	/*
	Fetch so many bits.
	
	This differs if we're reading from vocabulary or FIFO, in terms
	of where a byte comes from, but that's all.
	
	When reading from the FIFO, we only execute ...readFIFO when we
	are finished with the byte.
	*/
	public int fetch(int bits)
	{
		int         cur;
	
		if ((gate & GT_WDAT) != 0) {	/* from FIFO */
			if (bit + bits >= 8) {	/* we will cross into the next byte */
				cur = readFIFO();
				//demo_record_event(demo_type_speech, demo_speech_adding_byte, cur);
	
				cur <<= 8;
				cur |= peekFIFO();	/* we can't read more than 6 bits,
											   so no poss of crossing TWO bytes */
			} else
				cur = peekFIFO() << 8;
		} else {					/* from vocab */
	
			if (bit + bits >= 8) {
				cur = readMemory();
				//demo_record_event(demo_type_speech, demo_speech_adding_byte, cur);
	
				cur <<= 8;
				cur |= peek();
			} else
				cur = peek() << 8;
		}
	
		/*  Get the bits we want.  */
		cur = (cur << bit + 16) >>> (32 - bits);
	
		/*  Adjust bit ptr  */
		bit = (bit + bits) & 7;
	
		bits += bits;
		return cur;
	}

	public void send(short val, int pos, int length) {
		if (sender != null)
			sender.send(val, pos, length);
	}
	
	public void setSender(Sender sender) {
		this.sender = sender;
		
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
		
	}
}
