/**
 * 
 */
package v9t9.engine.sound;

import java.text.MessageFormat;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;


public abstract class ClockedSoundVoice extends SoundVoice
{
	protected static final int		CLOCKSTEP = 55930;		// clock stepper, balancing desired clock of 55930 by actual generated frequency
	protected int		soundClock;			// the driving clock

	protected byte	operation[] = { 0, 0, 0 };	// operation bytes
	
	protected int		period16;		// from operation, scaled by 55930
	protected int		hertz;			// calculated from OPERATION_FREQUENCY_xxx
	
	protected int		clock;			// clock, stepping from 0 to period16 by clockstep
	protected int		accum;			// current accumulator, tracking the clock
	protected int		incr;			// amount to add to the accum per clock

	public ClockedSoundVoice(String name) {
		super(name);
		this.soundClock = 55930;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public void reset() {
		clock = 0;
		accum = 0;
	}
	
	public void setSoundClock(int clock) {
		this.soundClock = clock;
	}
	
	@Override
	public String toString() {
		return super.toString() + "; hertz="+hertz;
	}
	protected int getOperationNoiseType() {
		return ( operation[SoundTMS9919.OPERATION_CONTROL] & 0x4 );
	}

	protected int getOperationNoisePeriod()  {
		return ( operation[SoundTMS9919.OPERATION_CONTROL] & 0x3 );
	}
	
	protected byte getOperationAttenuation() {
		return (byte) ( operation[SoundTMS9919.OPERATION_ATTENUATION] & 0xf );
	}
	
	protected int getOperationPeriod() {
		int period = ( (operation[SoundTMS9919.OPERATION_FREQUENCY_LO] & 0xf) |
		( (operation[SoundTMS9919.OPERATION_FREQUENCY_HI] & 0x3f) << 4 ) );
		return period;
	}
	
	protected void dump() {
		if (false) {
			if (getVolume() == 0)
				System.out.println(MessageFormat.format(
						"voice_cache_values[{0}]: hz={1}   OFF",
						getName(), hertz));
			else
				System.out.println(MessageFormat.format(
					"voice_cache_values[{5}]: lo=>{0}, hi=>{1}, period=>{2}, hertz={3}, volume={4}",
				   HexUtils.toHex4(operation[SoundTMS9919.OPERATION_FREQUENCY_LO]), 
				   HexUtils.toHex4(operation[SoundTMS9919.OPERATION_FREQUENCY_HI]),
				   HexUtils.toHex4((period16 / 65536)),
				   hertz,
				   getVolume(),
				   getName()));
		}
	}
	
	protected void updateAccumulator() {
		accum += incr;
		if (period16 > 0)
			clock = (clock + CLOCKSTEP) % period16;
		else
			clock = 0;
	}
	
	protected void updateEffect() {
	}
	
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		operation[0] = (byte) settings.getInt("Op1");
		operation[1] = (byte) settings.getInt("Op2");
		operation[2] = (byte) settings.getInt("Op3");
		accum = settings.getInt("Accumulator");
		clock = settings.getInt("Clock");
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Op1", operation[0]);
		settings.put("Op2", operation[1]);
		settings.put("Op3", operation[2]);
		settings.put("Accumulator", accum);
		settings.put("Clock", clock);
	}

	public int getClock() {
		return clock;
	}
}