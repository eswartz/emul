/**
 * 
 */
package v9t9.audio.sound;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.hardware.ICassetteChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.RegisterInfo;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public class CassetteSoundGenerator implements ISoundGenerator {
	protected final Map<Integer, SoundVoice> regIdToVoices = 
			new HashMap<Integer, SoundVoice>();
		protected final Map<Integer, IRegisterAccess.IRegisterWriteListener> regIdToListener = 
			new HashMap<Integer, IRegisterAccess.IRegisterWriteListener>();

	private CassetteSoundVoice cassette1Voice;
	private ICassetteChip cassetteChip;

	/**
	 * 
	 */
	public CassetteSoundGenerator(IMachine machine) {
		this.cassetteChip = machine.getCassette();
		cassetteChip.addWriteListener(this);
		cassette1Voice = new CassetteSoundVoice("cs1");
		setupCassetteVoice(TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT, cassette1Voice);
	}
	
	/**
	 * @param regBase
	 */
	protected int setupCassetteVoice(int regBase, final CassetteSoundVoice voice) {
		RegisterInfo info;
		info = cassetteChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("C:C");
		
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT, voice);
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1, voice);
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2, voice);
		
		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_OUTPUT,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setState(value);
			}
		});
		
		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setMotor1(value, value >= 0);
			}
		});
		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setMotor2(value, value >= 0);
			}
		});
		
		return TMS9919Consts.REG_COUNT_CASSETTE;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
	 */
	@Override
	public void registerChanged(int reg, int value) {

		SoundVoice v = regIdToVoices.get(reg);
		if (v == null)
			return;
		IRegisterAccess.IRegisterWriteListener listener = regIdToListener.get(reg);
		if (listener == null)
			throw new IllegalStateException();
		
		listener.registerChanged(reg, value);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getSoundVoices()
	 */
	@Override
	public ISoundVoice[] getSoundVoices() {
		return new ISoundVoice[] { cassette1Voice };
	}

}
