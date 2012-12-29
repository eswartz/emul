/**
 * 
 */
package v9t9.audio.sound;

import java.util.Arrays;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.IFlushableSoundVoice;

/**
 * Reproduce the sound generated by the cassette recording.
 * 
 * From &lt;http://nouspikel.com/ti99/tms9901.htm#Ti99&gt;
 * 
 * <pre>
 * Pin INT13* / P9 is used to output sound to the cassette, this will be digital sound of course, modulated via an electronic circuit in the console.
 * </pre>
 * And from &lt;http://nouspikel.group.shef.ac.uk//ti99/cassette.htm#Cassette%20tape%20format&gt;:
 * 
 * <pre>
 * Texas Instruments adopted a frequency modulation encoding system to store data on tape. 
 * This is only a convention, and you may come up with another, if you feel like it. Similarly, TI defined the format the data 
 * should have whithin a tape file. Again, this is only a convention.
 * </pre>
 * 
 * (From the above, the modulation is not a convention!)
 * @author ejs
 *
 */
public class CassetteSoundVoice extends ClockedSoundVoice implements IFlushableSoundVoice {
	private static final short[] cassetteChirp = new short[256];
	final static int cassetteChirpMag = 0x1800;
	static {
		for (int i = 0; i < cassetteChirp.length; i++) {
			double sin = Math.sin(i * Math.PI * 2 / cassetteChirp.length);
			double sin2 = Math.sin(i * Math.PI / cassetteChirp.length);
			double v = sin * (sin2*sin2*sin2*sin2); 
			cassetteChirp[i] = (short) (v *  cassetteChirpMag); 
		}
	}
	private boolean wasSet;
	private boolean state;
	private boolean origState;
	private int[] deltas = new int[0];
	private int deltaIdx = 0;
	private int baseCycles;
	private boolean motor2;
	private boolean motor1;
	private float prevV;
	private float sign = 1f;
	private int leftover;
	private float dcOffset;
	
	public CassetteSoundVoice(String name) {
		super("Cassette");
	}
	
	@Override
	public void setupVoice() {
		setVolume((byte) (state ? MAX_VOLUME : 0));
		wasSet = true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#setSoundClock(int)
	 */
	public void setSoundClock(int soundClock) {
		this.soundClock = soundClock;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public void reset() {
		wasSet = false;
		origState = false;
		leftover = 0;
		deltaIdx = 0;
		baseCycles = 0;
		prevV = 0f;
		sign = 1f;
		dcOffset = 0f;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.SoundVoice#isActive()
	 */
	@Override
	public boolean isActive() {
		return super.isActive();
	}
	public synchronized void setState(int curr) {
		boolean newState = curr >= 0;
		curr = absp1(curr);
		
		// always note a change; the speed is what counts
		if (motor1 || motor2) 
		{
			int offs = curr >= baseCycles ? curr - baseCycles : curr;
			
			state = newState;
			baseCycles = curr;
			appendPos(state ? offs : -offs-1);
		}
	}

	/**
	 * @param pos
	 * @throws AssertionError
	 */
	protected void appendPos(int pos) throws AssertionError {
		if (deltaIdx > 0 && deltas[deltaIdx - 1] == pos)
			return;
		
		if (deltaIdx >= deltas.length) {
			int newlen = deltas.length * 2;
			if (newlen < 16)
				newlen = 16;
			deltas = Arrays.copyOf(deltas, newlen);
		}
		
		deltas[deltaIdx++] = pos;
	}

	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		
		//appendPos(state ? totalCount : -totalCount-1);
		return wasSet;
	}

	/* (non-Javadoc)
	 * @see v9t9.base.sound.ITimeAdjustSoundVoice#flushAudio(float[], int, int)
	 */
	@Override
	public synchronized boolean flushAudio(float[] soundGeneratorWorkBuffer, int from,
			int to, int totalCycles) {
		boolean generated = false;
		if (from >= to || deltaIdx <= 0) {
			dcOffset /= 1.1;
		} else {
			if (Double.isNaN(dcOffset))
				dcOffset = 0;
			
			generated = true;
			int ratio = 128 + balance;
			float sampleL = ((256 - ratio) * 1f) / 256.f;
			float sampleR = (ratio * 1f) / 256.f;
			
			int totalSamps = to - from;
			
			int total = leftover;
			for (int i = 0; i < deltaIdx; i++)
				total += absp1(deltas[i]);

			if (total == 0)
				total = 1;

			int firstFrom = from;
			
			int idx = 0;
			int consumed = absp1(deltas[idx]) + leftover;
			int next = from + (int) ((long) consumed * totalSamps / total);
			idx++;
			
			int origFrom = from;
			//leftover = 0;
			
			//System.out.println("**" + (next-origFrom));
			boolean on = origState;
			sign = on ? 1f : -1f;
			int diff = next - origFrom;
			
			while (from < to) {
				float v;
				// avoid weird spikes
				if (diff > 0) {
					int fullPos = (from - origFrom)  * cassetteChirp.length;
					int aPos = fullPos / diff;
					v = cassetteChirp[aPos] / (float) cassetteChirpMag;
				} else {
					v = prevV;
				}
				prevV = v;
				v *= sign;
				
				// this seems to be how the "perfect" wave is messed up in analog-land
				dcOffset += v * 8 / diff;
				v += dcOffset;
				
				soundGeneratorWorkBuffer[from++] += sampleL * v;
				soundGeneratorWorkBuffer[from++] += sampleR * v;
				
				
				if (from >= next) {
					if (idx < deltaIdx) {
						boolean nextOn = (deltas[idx] >= 0);
						consumed += absp1(deltas[idx++]);
						next = firstFrom + (int) ((long) consumed * totalSamps / total);
						on = nextOn;
						sign = -sign;
					} else {
						break;
					}
					origFrom = from;
					diff = next - origFrom;
					
				}
			}
		}
		
		deltaIdx = 0;
		origState = state;
		leftover = totalCycles - baseCycles;
		if (leftover < 0)
			leftover = 0;
		baseCycles = totalCycles;
		
		return generated;
	}
	
	/**
	 * @param i
	 * @return
	 */
	private int absp1(int i) {
		return i < 0 ? -(i+1) : i;
	}

	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		setVolume((byte) (settings.getBoolean("State") ? MAX_VOLUME : 0));
		motor1 = settings.getBoolean("Motor1");
		motor2 = settings.getBoolean("Motor2");
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("State", Boolean.toString(getVolume() != 0));
		settings.put("Motor1", Boolean.toString(motor1));
		settings.put("Motor2", Boolean.toString(motor2));
	}

	/**
	 * @param b
	 */
	public void setMotor1(int curr, boolean b) {
		motor1 = b;
		baseCycles = curr;
	}

	/**
	 * @param b
	 */
	public void setMotor2(int curr, boolean b) {
		motor2 = b;
		baseCycles = curr;
	}
	
}