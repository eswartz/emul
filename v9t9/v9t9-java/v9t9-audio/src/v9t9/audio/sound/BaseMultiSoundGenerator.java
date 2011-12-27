/**
 * 
 */
package v9t9.audio.sound;

import java.util.Map;
import java.util.TreeMap;

import ejs.base.sound.ISoundVoice;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.sound.IMultiSoundChip;
import v9t9.common.sound.ISoundGenerator;

/**
 * @author ejs
 *
 */
public abstract class BaseMultiSoundGenerator implements ISoundGenerator {

	private TreeMap<Integer, ISoundGenerator> regIdToGenerator = new TreeMap<Integer, ISoundGenerator>();
	private ISoundGenerator[] generators;
	private ISoundVoice[] voices;

	/**
	 * 
	 */
	public BaseMultiSoundGenerator(IMachine machine) {
		IMultiSoundChip sound = (IMultiSoundChip) machine.getSound();
		
		// Prolly in the card, the console chip is ignored, because it
		// borks the intended use of stereo on the other chips.
		// So we ignore it.
		this.generators = new ISoundGenerator[sound.getChipCount()];
		int regBase = 0;
		int vcount = 0;
		for (int i = 0; i < sound.getChipCount(); i++) {
			ISoundChip soundChip = sound.getChip(i);
			//System.out.println(soundChip.getGroupName() + ": " + soundChip.getFirstRegister());
			generators[i] = createSoundGenerator(machine, soundChip, regBase);
			soundChip.addWriteListener(this);
			regIdToGenerator.put(soundChip.getFirstRegister(), generators[i]);
			regBase += soundChip.getRegisterCount();
			vcount += generators[i].getSoundVoices().length;
		}
		
		voices = new ISoundVoice[vcount];
		int vidx = 0;
		for (int i = 0; i < sound.getChipCount(); i++) {
			ISoundVoice[] svs = generators[i].getSoundVoices();
			System.arraycopy(svs, 0, voices, vidx, svs.length);
			vidx += svs.length;
		}
	}

	abstract protected ISoundGenerator createSoundGenerator(IMachine machine,
			ISoundChip sound, int regBase);

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
	 */
	@Override
	public synchronized void registerChanged(int reg, int value) {
		Map.Entry<Integer, ISoundGenerator> generatorMap = regIdToGenerator.floorEntry(reg);
		if (generatorMap == null)
			throw new IllegalArgumentException();
		ISoundGenerator generator = generatorMap.getValue();
		generator.registerChanged(reg, value);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getSoundVoices()
	 */
	@Override
	public ISoundVoice[] getSoundVoices() {
		return voices;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#tick()
	 */
	@Override
	public void tick() {
		for (ISoundGenerator generator : generators)
			generator.tick();
	}

}
