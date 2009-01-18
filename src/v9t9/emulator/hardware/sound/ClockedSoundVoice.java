/**
 * 
 */
package v9t9.emulator.hardware.sound;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.utils.Utils;

public abstract class ClockedSoundVoice extends SoundVoice
{
	protected byte	operation[] = { 0, 0, 0 };	// operation bytes
	
	protected int		period, hertz;	// calculated from OPERATION_FREQUENCY_xxx
	
	protected int		div;			// divisor to add to the delta per clock
	protected int		delta;			// current accumulator, tracking the clock

	public ClockedSoundVoice(String name) {
		super(name);
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
		byte mask = 0x3f; //(byte) (isEnhanced ? 0x7f : 0x3f);
		return ( (operation[SoundTMS9919.OPERATION_FREQUENCY_LO] & 0xf) |
		( (operation[SoundTMS9919.OPERATION_FREQUENCY_HI] & mask) << 4 ) );
	}
	
	protected void dump() {
		if (true) {
			if (getVolume() == 0)
				System.out.println(MessageFormat.format(
						"voice_cache_values[{0}]: hz={1}   OFF",
						getName(), hertz));
			else
				System.out.println(MessageFormat.format(
					"voice_cache_values[{5}]: lo=>{0}, hi=>{1}, period=>{2}, hertz={3}, volume={4}",
				   Utils.toHex4(operation[SoundTMS9919.OPERATION_FREQUENCY_LO]), 
				   Utils.toHex4(operation[SoundTMS9919.OPERATION_FREQUENCY_HI]),
				   Utils.toHex4(period),
				   hertz,
				   getVolume(),
				   getName()));
		}
	}
	
	protected void updateDivisor() {
		div += delta;
	}
	
	protected boolean updateMagnitude() {
		return false;
	}
	
	@Override
	public void loadState(IDialogSettings settings) {
		super.loadState(settings);
		operation[0] = (byte) Utils.readSavedInt(settings, "Op1");
		operation[1] = (byte) Utils.readSavedInt(settings, "Op2");
		operation[2] = (byte) Utils.readSavedInt(settings, "Op3");
		div = (byte) Utils.readSavedInt(settings, "Accumulator");
	}
	
	@Override
	public void saveState(IDialogSettings settings) {
		super.saveState(settings);
		settings.put("Op1", operation[0]);
		settings.put("Op2", operation[1]);
		settings.put("Op3", operation[2]);
		settings.put("Accumulator", div);
	}
}