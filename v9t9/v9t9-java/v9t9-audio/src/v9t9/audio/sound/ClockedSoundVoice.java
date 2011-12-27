/**
 * 
 */
package v9t9.audio.sound;

import java.text.MessageFormat;

import ejs.base.utils.HexUtils;

public abstract class ClockedSoundVoice extends SoundVoice
{
	protected static final int		CLOCKSTEP = 55930;		// clock stepper, balancing desired clock of 55930 by actual generated frequency
	protected int		soundClock;			// the driving clock

	private byte		atten;		
	protected int 		period;
	
	protected long		period16;		// from operation, scaled by 55930
	protected int		hertz;			// calculated from OPERATION_FREQUENCY_xxx
	
	protected int		clock;			// clock, stepping from 0 to period16 by clockstep
	protected int		accum;			// current accumulator, tracking the clock
	protected long		incr;			// amount to add to the accum per clock

	public ClockedSoundVoice(String name) {
		super(name);
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
	
	public void setOperationAttenuation(int atten) {
		this.atten = (byte) atten;
	}
	
	protected byte getOperationAttenuation() {
		return (byte) atten;
	}

	public void setOperationPeriod(int period) {
		this.period = (period & 0x3ff);
	}
	
	protected int getOperationPeriod() {
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
					"voice_cache_values[{5}]: freq=>{0}, period=>{1}, hertz={2}, volume={3}",
				   Long.toHexString(period16), 
				   HexUtils.toHex4(hertz),
				   hertz,
				   getVolume(),
				   getName()));
		}
	}
	
	protected void updateAccumulator() {
		accum += incr;
		if (period16 > 0)
			clock = (int) ((clock + CLOCKSTEP) % period16);
		else
			clock = 0;
	}
	
	protected void updateEffect() {
	}
	
	public int getClock() {
		return clock;
	}
}