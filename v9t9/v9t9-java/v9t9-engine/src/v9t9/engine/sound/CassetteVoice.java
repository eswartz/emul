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
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.settings.SettingSchema;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.TMS9919Consts;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class CassetteVoice extends BaseVoice implements ICassetteVoice {

	public static SettingSchema settingCassetteReading = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"CassetteReading", false);
	
	public static SettingSchema settingCassetteInput = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CassetteInput", "");
	
	private int outputState;
	private int motor1;
	private int motor2;
	private IMachine machine;

	private IProperty cassetteInput;

	private IProperty cassetteReading;

	protected CassetteReader cassetteReader;
	protected int prevCassetteCycles;
	protected int avgCassetteCycles;
	protected long lastScroll;

	private float clockSecs = 1.0f / 1500f;

	public CassetteVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners, IMachine machine_) {
		super(id, name, listeners);
		this.machine = machine_;
		
		cassetteInput = machine.getSettings().get(settingCassetteInput);
		cassetteReading = machine.getSettings().get(settingCassetteReading);
		
		cassetteInput.addListenerAndFire(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				rewindTape();

			}
		});
		
		cassetteReading.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				if (property.getBoolean()) {
					rewindTape();
				}
			}
		});
	}

	/**
	 * 
	 */
	protected void rewindTape() {
		synchronized (CassetteVoice.this) {
			if (cassetteReader != null)
				cassetteReader.close();
		}
		String path = cassetteInput.getString();
		if (path != null && !path.isEmpty()) {

			File audioFile = new File(path);
			try {
				AudioFileFormat format = AudioSystem.getAudioFileFormat(audioFile);
				
				AudioInputStream is = new AudioInputStream(
						new FileInputStream(audioFile),
						format.getFormat(),
						audioFile.length());
				
				synchronized (CassetteVoice.this) {
					cassetteReader = new CassetteReader(is);
				}
			} catch (IOException e) {
				machine.getEventNotifier().notifyEvent(audioFile, Level.ERROR, 
						"Failed to open audio file " + audioFile + " for cassette: "+e.getMessage());
						
			} catch (UnsupportedAudioFileException e) {
				machine.getEventNotifier().notifyEvent(audioFile, Level.ERROR, 
						"Could not recognize audio format in " + audioFile + " for cassette: "+e.getMessage());
			}
		}		
	}

	public void setState(boolean state) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.outputState = state ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT, this.outputState);
		}
	}
	
	public void setClock(float secs) {
		if (clockSecs != secs) {
			System.out.println("\nnew cassette clock = " + secs);
			this.clockSecs = secs;
		}
		
	}
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ICassetteVoice#getState()
	 */
	@Override
	public boolean getState() {
		boolean state = false;
		synchronized (CassetteVoice.this) {
			if (cassetteReader == null) {
				if (cassetteReading.getBoolean())
					cassetteReading.firePropertyChange();
				else
					cassetteReading.setBoolean(true);
				return false;
			}
			ICpu cpu = machine.getCpu();
			if (cpu != null) {
				float time;
				int curCycles = cpu.getCurrentCycleCount();
				int cycles = curCycles;
				if (cycles > prevCassetteCycles) {
					cycles -= prevCassetteCycles;
					if (cycles < avgCassetteCycles || avgCassetteCycles < cpu.getBaseCyclesPerSec() / 3000)
						avgCassetteCycles = (avgCassetteCycles + cycles) / 2;
				} else {
					System.out.println();
					if (cycles < avgCassetteCycles / 2 || cycles > avgCassetteCycles * 2)
						cycles = avgCassetteCycles;
				}
				time = (float) cycles / cpu.getBaseCyclesPerSec();
				prevCassetteCycles = curCycles;
				state = cassetteReader.readBit(time) != 0;
				System.out.print(state ? '.' : ' ');
				
				long now = System.currentTimeMillis();
				if (lastScroll + 1000 <= now) {
					lastScroll = now;
					System.out.println();
				}
				//fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_INPUT, this.inputState);
			}
		}
		return state;
	}
	
	public void setMotor1(boolean motor) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.motor1 = motor ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1, this.motor1);
			
			synchronized (this) {
				cassetteReader = null;
			}
		}
	}
	public void setMotor2(boolean motor) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.motor2 = motor ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2, this.motor2);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		setState(settings.getBoolean("State"));
		setMotor1(settings.getBoolean("Motor1"));
		setMotor2(settings.getBoolean("Motor2"));
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("Output", outputState >= 0);
		settings.put("Motor1", motor1 >= 0);
		settings.put("Motor2", motor2 >= 0);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT,
				getId() + ":C",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1,
				getId() + ":1",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2,
				getId() + ":2",
				getName());
		return TMS9919Consts.REG_COUNT_CASSETTE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT) {
			return outputState;
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1) {
			return motor1;
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2) {
			return motor2;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT) {
			outputState = newValue;
			fireRegisterChanged(reg, this.outputState);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1) {
			motor1 = newValue;
			fireRegisterChanged(reg, this.motor1);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2) {
			motor2 = newValue;
			fireRegisterChanged(reg, this.motor2);
		}
	}
}
