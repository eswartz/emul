/**
 * 
 */
package v9t9.engine.speech;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import ejs.base.utils.BitInputStream;
import ejs.base.utils.BitOutputStream;

import v9t9.common.speech.ILPCParameters;

/**
 * @author  ejs
 */
public class LPCParameters implements ILPCParameters {
	/**
	 * 
	 */
	public boolean repeat;
	/**
	 * 
	 */
	public int pitchParam;
	public int pitch;
	/**
	 * 
	 */
	public int energyParam;
	public int energy;
	/**
	 * Parameter (index) 
	 */
	public final int[] kParam = new int[10];
	/**
	 * Parameter value (from ROM table) 
	 */
	public final int[] kVal = new int[10];

	/**
	 * 
	 */
	public void init() {
		repeat = false;
		pitch = energy = 0;
		pitchParam = energyParam = 0;
		Arrays.fill(kParam, 0);
		Arrays.fill(kVal, 0);
		
	}

	public void copyFrom(LPCParameters params) {
		repeat = params.repeat;
		energy = params.energy;
		pitch = params.pitch;
		energyParam = params.energyParam;
		pitchParam = params.pitchParam;
		System.arraycopy(params.kVal, 0, kVal, 0, kVal.length);
		System.arraycopy(params.kParam, 0, kParam, 0, kParam.length);
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		/*  Read energy  */
		builder.append("E: " +  energyParam + " [" + energy + "] ");
		if (energyParam == 15) {
			builder.append("[last]");
		} else if (energyParam == 0) {	/* silent frame */
			builder.append("[silence]");
		} else {
			/*  Repeat bit  */
			builder.append("R: " + repeat + " ");

			/*  Pitch code  */
			builder.append("P: " + pitchParam);

			if (pitchParam == 0) {		/* unvoiced */
				builder.append(" [unvoiced] ");

			} else {				/* voiced */
				builder.append(" [" + pitch +"] ");
			}

			/*  Get K parameters  */

			if (repeat) {
				builder.append("[repeated] ");
			}

			builder.append("K0: " + kParam[0] + " [" + kVal[0] + "] ");

			builder.append("K1: " + kParam[1] + " [" + kVal[1] + "] ");

			builder.append("K2: " + kParam[2] +" [" + kVal[2] + "] ");

			builder.append("K3: " + kParam[3] + " [" + kVal[3] + "] ");


			if (pitchParam != 0) {	/* unvoiced? */
				builder.append("K4: " + kParam[4] + " [" + kVal[4] + "] ");

				builder.append("K5: " + kParam[5] + " [" + kVal[5] + "] ");

				builder.append("K6: " + kParam[6] + " [" + kVal[6] + "] ");

				builder.append("K7: " + kParam[7] + " [" + kVal[7] + "] ");

				builder.append("K8: " + kParam[8] + " [" + kVal[8] + "] ");

				builder.append("K9: " + kParam[9] + " [" + kVal[9] + "] ");
			}
		}
		
		return builder.toString();
	}

	@Override
	public void fromBytes(BitInputStream bs) throws IOException {
		init();
		energyParam = bs.readBits(4);
		if (energyParam != 0 && energyParam != 15) {
			repeat = bs.readBits(1) == 1;
			pitchParam = bs.readBits(6);
			if (!repeat) {
				kParam[0] = bs.readBits(5);
				kParam[1] = bs.readBits(5);
				kParam[2] = bs.readBits(4);
				kParam[3] = bs.readBits(4);
				if (pitchParam != 0) {
					kParam[4] = bs.readBits(4);
					kParam[5] = bs.readBits(4);
					kParam[6] = bs.readBits(4);
					kParam[7] = bs.readBits(3);
					kParam[8] = bs.readBits(3);
					kParam[9] = bs.readBits(3);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.speech.ILPCParameters#fromBytes(byte[])
	 */
	@Override
	public void fromBytes(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		BitInputStream bs = new BitInputStream(bis);
		fromBytes(bs);
		bs.close();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.speech.ILPCParameters#toBytes()
	 */
	@Override
	public void toBytes(BitOutputStream bs) throws IOException  {
		bs.writeBits(energyParam, 4);
		if (energyParam != 0 && energyParam != 15) {
			bs.writeBits(repeat ? 1 : 0, 1);
			bs.writeBits(pitchParam, 6);
			if (!repeat) {
				bs.writeBits(kParam[0], 5);
				bs.writeBits(kParam[1], 5);
				bs.writeBits(kParam[2], 4);
				bs.writeBits(kParam[3], 4);
				if (pitchParam != 0) {
					bs.writeBits(kParam[4], 4);
					bs.writeBits(kParam[5], 4);
					bs.writeBits(kParam[6], 4);
					bs.writeBits(kParam[7], 3);
					bs.writeBits(kParam[8], 3);
					bs.writeBits(kParam[9], 3);
				}
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ILPCParameters#toBytes()
	 */
	@Override
	public byte[] toBytes() throws IOException  {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BitOutputStream bs = new BitOutputStream(bos);
		toBytes(bs);
		bs.close();
		return bos.toByteArray();
	}


	/**
	 * @return
	 */
	public boolean isLast() {
		return energyParam == 15;
	}

	/**
	 * @return
	 */
	public boolean isSilent() {
		return energyParam == 0;
	}

	/**
	 * @return
	 */
	public boolean isUnvoiced() {
		return pitchParam == 0;
	}

	
}