/**
 * 
 */
package v9t9.audio.sound;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

/**
 * This voice is the base of tone or noise generation against a frequency
 * period. The period is inversely related to the frequency.
 * 
 * In the original SN76489A(N) chip, the chip operates at 3.579545 MHz and
 * increments counters for each voice at that rate (divided by 32). When a
 * counter passes the tone/noise period register, the output for the voice is
 * toggled or altered, creating a square wave or noise.
 * 
 * Thus, a small period produces a high-pitched tone and a large period produces
 * a low-pitched tone.
 * 
 * Here, we are not driving such a high-speed clock.  Also, we want to support
 * sound effects, which rely on knowing the phase of the current frequency
 * (the "clock", which steps from 0 to the period).  
 * 
 * Thus, we work at {@value #soundClock} Hz instead.  In order to maintain the
 * same precision at high frequencies, we use an accumulator that decrements
 * by 3.57 MHz / soundClock using fractional integers.  
 * 
 * @author ejs
 * 
 */
public abstract class ClockedSoundVoice extends SoundVoice
{
	private static final Logger logger = Logger.getLogger(ClockedSoundVoice.class);
	
	/** The clock rate against which all generation is done */
	protected int 		refClock;
	
	/** the driving clock output frequency */
	protected int		soundClock;		

	/** incoming clock period */
	protected int		period;			
	
	/** 
	 * Current accumulator, fractional 16:8.
	 * 
	 * Once it reaches period << 8, the voice updates.
	 */
	protected int		accum;			
	/** amount to add to the accum per clock, fractional 16:8 */
	protected int		incr;

	public ClockedSoundVoice(String name) {
		super(name);
	}
	
	public void setReferenceClock(int refClock) {
		this.refClock = (refClock - 7) / 16;
		setupVoice();
	}
	
	/**
	 * @return the refClock
	 */
	public int getRefClock() {
		return refClock;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public void reset() {
		accum = 0;
	}
	
	public int getSoundClock() {
		return soundClock;
	}

	public void setSoundClock(int clock) {
		this.soundClock = clock;
		setupVoice();
	}
	
	@Override
	public String toString() {
		return super.toString() + "; period="+period +" (hertz=~" + approxHertz() + ")";
	}

	public int getPeriod() {
		return period;
	}
	
	public void setPeriod(int period) {
		this.period = period;
		setupVoice();
	}
	
	public void setupVoice() {
		if (soundClock > 0 && refClock  > 0 && period > 0) {
			incr = (int) (65536L * refClock / soundClock);
			if (incr >= period << 16) {
				// sound will alias, just silence
				incr = 0;
			}
		}
		else
			incr = 0;
		
		dump();
	}
	
	protected void dump() {
		if (logger.isDebugEnabled()) {
			if (getVolume() == 0)
				logger.debug(MessageFormat.format(
						"voice_cache_values[{0}]: period={1} hertz={2}   OFF",
						getName(), period, approxHertz()));
			else
				logger.debug(MessageFormat.format(
					"voice_cache_values[{0}]: period=>{1}, hertz={2}, volume={3}",
					getName(),
				   period,
				   approxHertz(),
				   getVolume(),
				   getName()));
		}
	}
	
	/** For testing! */
	public void setFrequency(int hz) {
		setPeriod(hz > 0 ? refClock / hz : 0);
	}
	/**
	 * @return
	 */
	private int approxHertz() {
		return period > 0 ? refClock / period : soundClock;
	}

	protected final boolean updateAccumulator(int amount) {
		accum -= amount;
		boolean flag = false;
		if (period > 0) {
			while (accum < 0) {
				accum += period << 16;
				flag = !flag;
			}
		}
		return flag;
	}
	
	protected boolean updateAccumulator() {
		return updateAccumulator(incr);
	}
	
	protected void updateEffect() {
	}


	
}