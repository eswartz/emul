/*
  SpeechTMS5220.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

import static v9t9.common.speech.TMS5220Consts.GT_RDAT;
import static v9t9.common.speech.TMS5220Consts.GT_RSTAT;
import static v9t9.common.speech.TMS5220Consts.GT_WCMD;
import static v9t9.common.speech.TMS5220Consts.GT_WDAT;
import static v9t9.common.speech.TMS5220Consts.SS_BE;
import static v9t9.common.speech.TMS5220Consts.SS_BL;
import static v9t9.common.speech.TMS5220Consts.SS_SPEAKING;
import static v9t9.common.speech.TMS5220Consts.SS_TS;

import java.io.IOException;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.common.speech.ISpeechDataSender;
import v9t9.common.speech.ISpeechPhraseListener;
import v9t9.engine.demos.actors.OldSpeechDemoActor;
import v9t9.engine.demos.actors.SpeechDemoActor;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.speech.IFifoLpcDataFetcher.IFifoStatusListener;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import ejs.base.timer.FastTimer;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 * 
 */
public class SpeechTMS5220 implements ISpeechChip {
	public final static SettingSchema settingSpeechRomFileName = new SettingSchema(
			ISettingsHandler.MACHINE, "SpeechRomFileName", "spchrom.bin");

	public static MemoryEntryInfo speechRomInfo = MemoryEntryInfoBuilder
			.byteMemoryEntry().withDomain(IMemoryDomain.NAME_SPEECH)
			.withFilenameProperty(settingSpeechRomFileName)
			.withDescription("Speech Synthesizer Vocabulary ROM")
			.withSize(-0x10000).withFileMD5("7ADCAF64272248F7A7161CFC02FD5B3F")
			.create("Speech ROM");

	private int status; /* as returned via read-status */

	private int gate; /* how do we route Writes and Reads? */

	private int addr; /* address -- 20 bits */

	private byte command; /* last command */
	private byte data; /* data register for reading */

	private int timeout;

	private int addr_pos; /* for debugger: position of address (0=complete) */

	private IMemoryEntry speechRom;

	final int speechHertz = 8000;
	//final int speechLength = speechHertz / 5;		/* 40 */
	//private final int SPEECH_TIMEOUT = 25 + 9;
	
	private LPCSpeech lpc;

	private ListenerList<ISpeechDataSender> senderList;
	private ListenerList<ISpeechPhraseListener> phraseListeners;
	private ListenerList<ILPCParametersListener> paramListeners;

	private boolean speechOn;

	private IProperty logSpeech;

	private final IMachine machine;

	private String complainedMissingFilename;


	private FastTimer speechTimer;
	private Runnable speechTimerTask;
	
	private FifoLpcDataFetcher fifoFetcher;
	private RomLpcDataFetcher romFetcher;

	private IProperty demoPlaying;
	private IProperty talkRate;

	private IFifoStatusListener fifoStatusListener;

	private ILPCEquationFetcher equationFetcher;

	private LPCParameters currentParams = new LPCParameters();

	private byte lastRead;
	
	public SpeechTMS5220(final IMachine machine,
			final ISettingsHandler settings, final IMemoryDomain speech) {
		this.machine = machine;
		logSpeech = settings.get(ISpeechChip.settingLogSpeech);
		talkRate = settings.get(ISpeechChip.settingTalkSpeed);
		demoPlaying = settings.get(IDemoHandler.settingPlayingDemo);
		
		IPropertyListener speechRomFilenameListener = new IPropertyListener() {

			@Override
			public void propertyChanged(IProperty property) {
				// load later
				if (speechRom != null)
					speech.unmapEntry(speechRom);
				speechRom = null;
			}

		};

		Settings.get(machine, settingSpeechRomFileName).addListener(
				speechRomFilenameListener);

		senderList = new ListenerList<ISpeechDataSender>();
		phraseListeners = new ListenerList<ISpeechPhraseListener>();
		paramListeners = new ListenerList<ILPCParametersListener>();
		
		lpc = new LPCSpeech(settings);
		lpc.setSenderList(senderList);
		lpc.setParamListeners(paramListeners);

		fifoStatusListener = new FifoLpcDataFetcher.IFifoStatusListener() {
			
			@Override
			public void lengthChanged(int length) {
				if (length == 0) {
					status |= SS_BE | SS_BL;
					status &= ~SS_TS;
				}
				else if (length < 8) {
					status |= SS_BL;
					status &= ~SS_BE;
				} else {
					status &= ~(SS_BE + SS_BL);
				}
			}
			
			/* (non-Javadoc)
			 * @see v9t9.engine.speech.FifoLpcDataFetcher.IFifoStatusListener#fetchedEmpty()
			 */
			@Override
			public void fetchedEmpty() {
				timedOut();
			}
		};
		
		
		fifoFetcher = new FifoLpcDataFetcher();
		fifoFetcher.setLogProperty(logSpeech);
		fifoFetcher.setListener(fifoStatusListener);
		
		romFetcher = new RomLpcDataFetcher(new ILPCByteFetcher() {

			public byte read() {
				final byte cur = (byte) readMemory();

				phraseListeners.fire(new IFire<ISpeechPhraseListener>() { 

					@Override
					public void fire(ISpeechPhraseListener listener) {
						listener.phraseByteAdded(cur);
					}

				});

				return cur;
			}
			
			public byte peek() {
				addr_pos = 0;
				if (speechRom == null || addr >= speechRom.getSize())
					data = (byte) 0xff;	// ensure it will eventually terminate
				else
					data = speechRom.readByte(addr);
				return data;
			}

		});
		
		equationFetcher = new BuiltinEquationFetcher();
		
		machine.getDemoManager().registerActorProvider(new SpeechDemoActor.Provider());
		machine.getDemoManager().registerActorProvider(new OldSpeechDemoActor.Provider());

		speechTimerTask = new Runnable() {

			@Override
			public void run() {
				generateSpeech();
			}
			
		};
		

		speechTimer = new FastTimer("Speech");
		
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

	public void write(final byte val) {
		ILPCDataFetcher fetcher;
		synchronized (this) {
			fetcher = getDataFetcher();
		}
		if ((gate & GT_WCMD) != 0) {
			Logging.writeLogLine(2, logSpeech,
					"Speech command write: " + HexUtils.toHex2((val & 0xff)));
			command(val);
		} else {
			Logging.writeLogLine(2, logSpeech,
					"Speech data write: " + HexUtils.toHex2((val & 0xff)));
			IFifoLpcDataFetcher fifoFetcher = (IFifoLpcDataFetcher) fetcher;
			if (fifoFetcher.isFull()) {
				waitForBufferRoom(5000);
			}
			timeout = getNumberTimeoutFrames();
			fifoFetcher.write(val);

			phraseListeners.fire(new IFire<ISpeechPhraseListener>() { 

				@Override
				public void fire(ISpeechPhraseListener listener) {
					listener.phraseByteAdded(val);
				}

			});
		}
	}

	/**
	 * @return
	 */
	private ILPCDataFetcher getDataFetcher() {
		return (gate & GT_WDAT) != 0 ? fifoFetcher : romFetcher;
	}

	/**
	 * @param equationFetcher the equationFetcher to set
	 */
	public void setEquationFetcher(ILPCEquationFetcher equationFetcher) {
		if (equationFetcher == null) {
			this.equationFetcher = new BuiltinEquationFetcher();
		} else {
			this.equationFetcher = equationFetcher;
		}
	}
	
	public byte read() {
		byte ret;
		if ((gate & GT_RSTAT) != 0) {
			byte stat = (byte) (status & ~SS_SPEAKING);
			if ((status & SS_SPEAKING) != 0)
				stat |= SS_TS;
			ret = stat;
			
			if (ret != lastRead) {
				Logging.writeLogLine(2, logSpeech,
						"Speech status read: " + HexUtils.toHex2(ret) + " (" + statusToString(stat) + ")");
				lastRead = ret;
			}
		} else {
			// no more reading data
			gate = (gate & ~GT_RDAT) | GT_RSTAT;
			ret = data;
			
			Logging.writeLogLine(2, logSpeech,
					"Speech data read: " + HexUtils.toHex2(ret));
			lastRead = ret;
		}
		return ret;
	}

	private String statusToString(byte stat) {
		StringBuilder sb = new StringBuilder();
		if ((stat & SS_TS) != 0)
			sb.append("TS ");
		if ((stat & SS_BL) != 0)
			sb.append("BL ");
		if ((stat & SS_BE) != 0)
			sb.append("BE ");
		
		return sb.toString().trim();
	}

	/**
	 * @return 
	 * 
	 */
	protected IMemoryEntry getSpeechRom() {
		if (speechRom == null) {
			try {
				speechRom = machine.getMemory().getMemoryEntryFactory()
						.newMemoryEntry(speechRomInfo);
				speechRom.load();
				speechRom.getDomain().mapEntry(speechRom);
			} catch (IOException e) {
				String filename = speechRomInfo.getResolvedFilename(Settings
						.getSettings(machine));
				if (!filename.equals(complainedMissingFilename)) {
					complainedMissingFilename = filename;
					machine.notifyEvent(Level.WARNING,
							"Did not find Speech ROM: " + filename
									+ "; speech may not work");
				}
			}

		}
		return speechRom;
	}

	/**
	 * Get the number of samples per frame
	 * @return
	 */
	private int getSpeechRate() {
		return speechHertz / getSamplesPerFrame();
	}

	public int getSamplesPerFrame() {
		int length = (int) (speechHertz / 40 / talkRate.getDouble());
		if (length < 1)
			length = 1;
		else if (length > speechHertz)
			length = speechHertz;
		return length;
	}

	private int getNumberTimeoutFrames() {
		int base = getSpeechRate();
		int frames = (1000 / base) + (360 / base);
		return frames;
	}


	
	public void command(byte cmd) {
		command = (byte) (cmd & 0x70);
		if (Logging.isSettingEnabled(3, logSpeech)) {
			Logging.writeLogLine(3, logSpeech, "Cmd=" + HexUtils.toHex2(cmd)
					+ "  Status: " + HexUtils.toHex2(status));
		}
		
		if (command != 0x70) {
			// per section 6 of manual -- cannot send commands while others are executing.
			// speak external & speak are the only ones that can "remain executing"
			waitSpeechComplete(5000);
		}
		
		switch (command) {
		case 0x00:
		case 0x20:
			loadFrameRate(cmd & 0x7);
			break;
		case 0x10:
			readMemory();
			// next read will be data, not status
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

	private int readMemory() {
		addr_pos = 0;
		
		IMemoryEntry speech = getSpeechRom();
		if (speech == null || addr >= speech.getSize())
			data = 0;
		else
			data = speech.readByte(addr);
		addr++;
		//
		Logging.writeLogLine(3, logSpeech,
				"Speech memory " + HexUtils.toHex4(addr - 1) + " = "
						+ HexUtils.toHex2(data));
		return data;
	}

	private void loadAddress(int nybble) {
		addr_pos = (addr_pos + 1) % 5;
		addr = (addr >> 4) | (nybble << 16);
		Logging.writeLogLine(3, logSpeech,
				"Speech addr: " + HexUtils.toHex4(addr));
	}

	private void readAndBranch() {
		int addr;

		addr_pos = 0;
		addr = (readMemory() << 8) + readMemory();
		addr = (addr & 0xc000) | (addr & 0x3fff);
		
		// no more reading data
		gate = (gate & ~GT_RDAT) | GT_RSTAT;
	}

	/* Undocumented and unknown function... */
	private void loadFrameRate(int val) {
		if ((val & 0x4) != 0) {
			/* variable */
		} else {
			/* frameRate = val & 0x3; */
		}
	}

	private void speak() {
		
		// SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);

		Logging.writeLogLine(1, logSpeech,
				"Speaking phrase at " + HexUtils.toHex4(addr));

		phraseListeners.fire(new IFire<ISpeechPhraseListener>() {

			@Override
			public void fire(ISpeechPhraseListener listener) {
				listener.phraseStarted();
			}

		});


		gate = (gate & ~GT_WDAT) | GT_WCMD; /* just in case */
		getDataFetcher().reset();
		
		romFetcher.reset();
		fifoFetcher.reset();
		
		status |= SS_SPEAKING | SS_TS;

		SpeechOn();
		
		//machine.getFastMachineTimer().scheduleTask(awaitSpeechCompletion, 10);
		
	}

	private void speakExternal() {
		Logging.writeLogLine(1, logSpeech, "Speaking external data");

		gate = (gate & ~GT_WCMD) | GT_WDAT; /* accept data from I/O */
		fifoFetcher.purge();		// but not userDataFetcher
		
		phraseListeners.fire(new IFire<ISpeechPhraseListener>() {
			
			@Override
			public void fire(ISpeechPhraseListener listener) {
				listener.phraseStarted();
			}
			
		});
		
		SpeechOn(); /* call speech_intr every 25 ms */
		
		status |= SS_SPEAKING;
		timeout = getNumberTimeoutFrames();
	}



	private void waitSpeechComplete(int maxMs) {

		int ms = 50;
		
		int elapsed = 0;
		while (elapsed < maxMs) {
			synchronized (this) {
				if (!speechOn && (status & SS_TS) == 0)
					return;
			}
			
			try {
				timeout += ms * getSpeechRate() / 1000;
				Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
			
			elapsed += ms;
		}
		
		// ran out of time
		timedOut();
	}


	private void waitForBufferRoom(int maxMs) {

		int ms = 5;
		
		int elapsed = 0;
		while (elapsed < maxMs) {
			synchronized (this) {
				if (!speechOn || (status & SS_BL) != 0)
					return;
			}
			
			try {
				timeout += ms * getSpeechRate() / 1000;
				Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
			
			elapsed += ms;
		}
		
		// ran out of time
		timedOut();
	}
	
	@Override
	public synchronized void reset() {
		Logging.writeLogLine(1, logSpeech, "Speech reset");
		lastRead = 0;
		status = SS_BE | SS_BL;
		if (getDataFetcher() instanceof IFifoLpcDataFetcher)
			((IFifoLpcDataFetcher) getDataFetcher()).purge();
		fifoFetcher.purge();
		command = 0x70;
		data = 0;
		addr = 0;
		addr_pos = 0;
		gate = GT_RSTAT | GT_WCMD;
		SpeechOff();
		status &= ~SS_SPEAKING;
		timeout = getNumberTimeoutFrames();
		lpc.init();
		// in_speech_intr = 0;

		// flush existing sample
		// SPEECHPLAY(vms_Speech, NULL, 0L, speech_hertz);
	}

	private synchronized void SpeechOn() {
		speechOn = true;
		//machine.getFastMachineTimer().scheduleTask(speechTimerTask, speechHertz / speechLength);
		speechTimer.scheduleTask(speechTimerTask, getSpeechRate());
	}

	private synchronized void generateSpeech() {
		if (!speechOn)
			return;

		boolean do_frame = false;

		// logger(_L | L_2, _("Speech Interrupt\n"));

		if ((gate & GT_WDAT) != 0) { /* direct data */
			if (0 == (status & SS_TS)) { /* not talking yet... we're waiting for */
				if (0 == (status & SS_BL)) { /* enough data in the buffer? */
					status |= SS_TS; /* whee! Start talking */
					do_frame = true;
				} else {
					if (timeout-- < 0) {
						// speech_wait_complete(1);

						timedOut();
					}
				}
			} else {
				if ((status & SS_BE) != 0) {
					if (timeout-- < 0) {
						// speech_wait_complete(1);
						timedOut();
					}
				} else
					do_frame = true;
			}
		} else { /* vocab data */
			if ((status & SS_TS) != 0)
				do_frame = true;
		}

		if (do_frame) {
			Logging.writeLogLine(3, logSpeech, "Speech generating");
		
			equationFetcher.fetchEquation(getDataFetcher(), currentParams);
			boolean last = currentParams.isLast();
			
			lpc.frame(currentParams, getSamplesPerFrame());
			
//			boolean last = !lpc.frame(getDataFetcher(), getSamplesPerFrame());

			if (last) {
				SpeechDone();
			}
		}
	}

	/**
	 * 
	 */
	protected synchronized void timedOut() {
		if (demoPlaying.getBoolean()) {
			timeout = getNumberTimeoutFrames();
			return;
		}
		
		reset();

		phraseListeners.fire(new IFire<ISpeechPhraseListener>() {
			@Override
			public void fire(
					ISpeechPhraseListener listener) {
				listener.phraseTerminated();
			}

		});

		// this apparently happens in normal cases
		Logging.writeLogLine(1, logSpeech, "Speech timed out");
	}

	private synchronized void SpeechDone() {
		Logging.writeLogLine(1, logSpeech, "Done with speech phrase");
		SpeechOff(); /* stop interrupting */

		lpc.stop();
		if ((gate & GT_WDAT) == 0)
			lpc.init();
		status &= ~(SS_TS | SS_SPEAKING);
		gate = (gate & ~GT_WDAT) | GT_WCMD;

		senderList.fire(new IFire<ISpeechDataSender>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see ejs.base.utils.ListenerList.IFire#fire(java.lang.Object)
			 */
			@Override
			public void fire(ISpeechDataSender listener) {
				listener.speechDone();
			}
		});

		phraseListeners.fire(new IFire<ISpeechPhraseListener>() {

			@Override
			public void fire(ISpeechPhraseListener listener) {
				listener.phraseStopped();
			}

		});
	}

	private synchronized void SpeechOff() {
		speechOn = false;
		speechTimer.cancelTask(speechTimerTask);
		//machine.getFastMachineTimer().cancelTask(speechTimerTask);
	}

	public void addSpeechListener(ISpeechDataSender sender) {
		senderList.add(sender);
	}

	public void removeSpeechListener(ISpeechDataSender sender) {
		senderList.remove(sender);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.base.properties.IPersistable#loadState(v9t9.base.settings.
	 * ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.base.properties.IPersistable#saveState(v9t9.base.settings.
	 * ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * v9t9.common.hardware.ISpeechChip#addPhraseListener(v9t9.common.speech
	 * .ISpeechPhraseListener)
	 */
	@Override
	public void addPhraseListener(ISpeechPhraseListener phraseListener) {
		phraseListeners.add(phraseListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * v9t9.common.hardware.ISpeechChip#removePhraseListener(v9t9.common.speech
	 * .ISpeechPhraseListener)
	 */
	@Override
	public void removePhraseListener(ISpeechPhraseListener phraseListener) {
		phraseListeners.remove(phraseListener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISpeechChip#addParametersListener(v9t9.common.speech.ISpeechParametersListener)
	 */
	@Override
	public void addParametersListener(ILPCParametersListener listener) {
		paramListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISpeechChip#removeParametersListener(v9t9.common.speech.ISpeechParametersListener)
	 */
	@Override
	public void removeParametersListener(ILPCParametersListener listener) {
		paramListeners.remove(listener);
	}

	/**
	 * @return
	 */
	public LPCSpeech getLpcSpeech() {
		return lpc;
	}

	
}
