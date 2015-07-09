/*
  CassetteVoice.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import v9t9.common.cassette.CassetteConsts;
import v9t9.common.cassette.CassetteFileUtils;
import v9t9.common.cassette.ICassetteChip;
import v9t9.common.cassette.ICassetteDeck;
import v9t9.common.cpu.ICpu;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.sound.ISoundGenerator;
import v9t9.engine.machine.BaseRegisterBank;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundView;
import ejs.base.sound.SoundChunk;
import ejs.base.sound.SoundFileListener;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class CassetteDeck extends BaseRegisterBank implements ICassetteDeck {

	private int outputState;
	private int motor;
	private IMachine machine;

	private IProperty cassetteEnabled;
	
	private File audioFile;
	
	protected IProperty dumpFullInstructions;

	private IProperty cassetteDebug;

	protected CassetteReader cassetteReader;
	protected long prevCassetteCycles;
	protected int avgCassetteCycles;
	protected long lastScroll;
	private long prevPos;
	private int prevTicks;

	private IProperty cassetteCompressSilence;
	private ISoundGenerator generator;
	private ISoundOutput output;
	
	private SoundFileListener soundListener;
	private boolean canPlay;

	public CassetteDeck(String id, String name, boolean canPlay,
			ListenerList<IRegisterWriteListener> listeners, IMachine machine_) {
		super(id, name, listeners);
		this.canPlay = canPlay;
		this.machine = machine_;
		
		cassetteEnabled = machine.getSettings().get(ICassetteChip.settingCassetteEnabled);
		
		dumpFullInstructions = machine.getSettings().get(ICpu.settingDumpFullInstructions);
		
		cassetteDebug = machine.getSettings().get(ICassetteChip.settingCassetteDebug);
		cassetteCompressSilence = machine.getSettings().get(ICassetteChip.settingCassetteCompressSilence);

		cassetteEnabled.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				if (!property.getBoolean()) {
					stopCassette();
				}

			}
		});
		cassetteCompressSilence.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				updateRecordBlank();
			}
		});

	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#canPlay()
	 */
	@Override
	public boolean canPlay() {
		return canPlay;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#canRecord()
	 */
	@Override
	public boolean canRecord() {
		return true;
	}
	
	protected void log(String msg) {
		if (cassetteDebug.getBoolean()) {
			PrintWriter pw = Logging.getLog(dumpFullInstructions);
			if (pw != null)
				pw.println("[Cassette] " + msg);
			System.out.println("[Cassette] " + msg);
		}
	}
	
	public void stopCassette() {
		synchronized (CassetteDeck.this) {
			if (cassetteReader != null)
				cassetteReader.close();
			cassetteReader = null;
			
			if (soundListener != null) {
				soundListener.stopped();
				output.removeEmitter(soundListener);
				soundListener = null;
			}
		}
	}
	
	public void playCassette() {
		synchronized (CassetteDeck.this) {
			if (cassetteReader != null)
				cassetteReader.close();
		}
	
		if (audioFile != null) {
			try {
				AudioFileFormat format = CassetteFileUtils.scanAudioFile(audioFile);
				
				synchronized (CassetteDeck.this) {
					cassetteReader = new CassetteReader(
							audioFile, format, 
							cassetteDebug, this);
					
					prevPos = 0;
					prevTicks = getTickCount();
				}
			} catch (IOException e) {
				machine.getEventNotifier().notifyEvent(this, Level.ERROR, 
						"Failed to open audio file " + audioFile+ " for cassette: "+e.getMessage());
			} catch (UnsupportedAudioFileException e) {
				machine.getEventNotifier().notifyEvent(this, Level.ERROR, 
						"Could not recognize audio format in " + audioFile+ " for cassette: "+e.getMessage());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#isPlaying()
	 */
	@Override
	public boolean isPlaying() {
		synchronized (CassetteDeck.this) {
			return cassetteReader != null;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#isRecording()
	 */
	@Override
	public boolean isRecording() {
		return soundListener != null;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#recordCassette()
	 */
	@Override
	public void recordCassette() {
		if (soundListener != null) {
			stopCassette();
		}
		
		if (generator == null || output == null)
			return;
		
		synchronized (this) {
			soundListener = new SoundFileListener();
			AudioFormat audioFormat = generator.getAudioFormat();
			soundListener.started(audioFormat);
			
			updateRecordBlank();
			
			soundListener.setPauseProperty(machine.getSettings().get(IMachine.settingPauseMachine));
			
			soundListener.setFileName(audioFile.getPath());
			
			output.addEmitter(soundListener);
			
			if (cassetteCompressSilence.getBoolean()) {
				// write an initial 1sec of silence
				ISoundView silence = new SoundChunk(new float[(int) audioFormat.getFrameRate()], audioFormat);
				soundListener.played(silence);
			}
		}
	}
	
	
	@Override
	public void writeBit(boolean state) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.outputState = state ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT, this.outputState);
		}
	}
	
	@Override
	public boolean readBit() {
		boolean state = false;
		synchronized (CassetteDeck.this) {
			if (cassetteReader == null) {
				//playCassette();
				return false;
			}
			ICpu cpu = machine.getCpu();
			if (cpu != null) {
				float time;
				long now = System.currentTimeMillis();
				
				long curCycles = cpu.getTotalCurrentCycleCount();
				long cycles = curCycles - prevCassetteCycles;
				prevCassetteCycles = curCycles;
				
				time = (float) cycles / cpu.getBaseCyclesPerSec();
				
				long pos = cassetteReader.getPosition();
				
				state = cassetteReader.readBit(time);
				
				//if (CassetteReader.DEBUG) System.out.print(state ? 'x' : '-');
				if (cassetteDebug.getBoolean()) {
					long newPos = cassetteReader.getPosition();
					int ticks = getTickCount();
					
					if (prevTicks != ticks) {
						short pc = cpu.getState().getPC();
						IMemoryEntry entry = cpu.getConsole().getEntryAt(pc);
						String symbol = "";
						Pair<String, Short> info = entry.lookupSymbolNear(pc, 0x20);
						if (info != null) 
							symbol = info.first;
						else
							symbol = HexUtils.toHex4(pc);
						
						log(prevPos + "-" + newPos + " (" + symbol + "): " + state);
						prevPos = newPos;
						prevTicks = ticks;
					}
				}
				
				if (cassetteDebug.getBoolean()) {
					if (lastScroll + 1000 <= now) {
						lastScroll = now;
						System.out.println("[" + pos +"] vdp=" + 
								HexUtils.toHex4(((VdpTMS9918A) machine.getVdp()).getVdpMmio().getAddr())+" ");
					}
				}
				//fireRegisterChanged(baseReg + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT, state ? 1 : 0);
			}
		}
		return state;
	}
	
	/**
	 * @return
	 */
	private int getTickCount() {
		if (machine == null)
			return 0;
		ICpu cpu = machine.getCpu();
		if (cpu == null)
			return 0;
		return (int) (cpu.getTotalCurrentCycleCount() * machine.getCru().getClockRate() / cpu.getBaseCyclesPerSec());
	}

	@Override
	public void setMotor(boolean motor) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.motor = motor ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + CassetteConsts.REG_OFFS_CASSETTE_MOTOR, this.motor);
			
			updateRecordBlank();
		}
	}
	
	private void updateRecordBlank() {
		synchronized (this) {
			if (soundListener != null)
				soundListener.setIncludeSilence(motor >= 0 && !cassetteCompressSilence.getBoolean());
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		writeBit(settings.getBoolean("State"));
		setMotor(settings.getBoolean("Motor"));
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("State", outputState >= 0);
		settings.put("Motor", motor >= 0);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT,
				getId() + ":C",
				getName());
		register(baseReg + CassetteConsts.REG_OFFS_CASSETTE_MOTOR,
				getId() + ":M",
				getName());
		
		// INPUT and INPUT_RATE are hidden
		return CassetteConsts.REG_COUNT_CASSETTE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT) {
			return outputState;
		}
		else if (reg == baseReg + CassetteConsts.REG_OFFS_CASSETTE_MOTOR) {
			return motor;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT) {
			outputState = newValue;
			fireRegisterChanged(reg, this.outputState);
		}
		else if (reg == baseReg + CassetteConsts.REG_OFFS_CASSETTE_MOTOR) {
			motor = newValue;
			fireRegisterChanged(reg, this.motor);
		}
	}

	/**
	 * @param samp
	 */
	public void addFloatSample(float samp) {
		fireRegisterChanged(baseReg + CassetteConsts.REG_OFFS_CASSETTE_INPUT_SAMPLE, 
				(int) (samp * Integer.MAX_VALUE));
	}

	/**
	 * @param frameRate
	 */
	public void setSampleRate(int frameRate) {
		fireRegisterChanged(baseReg + CassetteConsts.REG_OFFS_CASSETTE_INPUT_RATE, 
				frameRate);		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#getInputFile()
	 */
	@Override
	public File getFile() {
		return audioFile;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#setInputFile(java.io.File)
	 */
	@Override
	public void setFile(File file) {
		this.audioFile = file;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#getOutput()
	 */
	@Override
	public ISoundOutput getOutput() {
		return output;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#getGenerator()
	 */
	@Override
	public ISoundGenerator getGenerator() {
		return generator;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#setOutput(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void setOutput(ISoundOutput output) {
		this.output = output;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cassette.ICassetteDeck#setGenerator(v9t9.common.sound.ISoundGenerator)
	 */
	@Override
	public void setGenerator(ISoundGenerator generator) {
		this.generator = generator;
	}

}
