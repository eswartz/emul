/**
 * 
 */
package v9t9.gui.sound;

import java.util.Arrays;

/**
 * Utility class for constructing DFT from the sound signal
 * @author Ed
 *
 */
public class DFTAnalyzer {
	private int sampleSize;
	private double[] xx;
	private int xidx;
	private double rex[], imx[];
	private double mag[], phase[], uwphase[];
	private double avgmag[];
	                          
	public DFTAnalyzer(int sampleBits) {
		this.sampleSize = 1 << sampleBits;
		this.xx = new double[sampleSize];
		this.rex = new double[sampleSize / 2 + 1];
		this.imx = new double[sampleSize / 2 + 1];
		this.mag = new double[sampleSize / 2 + 1];
		this.phase = new double[sampleSize / 2 + 1];
		this.avgmag = new double[sampleSize / 2 + 1];
		this.uwphase = new double[sampleSize / 2 + 1];
		this.xidx = 0;
	}
	
	public void send(byte[] signal16le) {
		int sidx = 0;
		while (sidx < signal16le.length) {
			short sample = (short) ((signal16le[sidx++] & 0xff) | (signal16le[sidx++] << 8));
			xx[xidx++] = sample / 32768.0;
			if (xidx >= xx.length - 1) {
				calc();
				xidx = 0;
			}
		}
	}

	/**
	 * Calculate the DFT
	 */
	private void calc() {
		// clear accumulators
		Arrays.fill(rex, (double) 0.0);
		Arrays.fill(imx, (double) 0.0);
		
		// compute DFT, applying a Hamming window
		for (int k = 0; k <= sampleSize / 2; k++) {
			double w = 0.54 - 0.46 * Math.cos(2 * Math.PI * k / (sampleSize / 2));
			double phase = 2 * Math.PI * k / xx.length;
			for (int i = 0; i < xx.length; i++) {
				double ang = phase * i;
				rex[k] += xx[i] * w * Math.cos(ang);
				imx[k] -= xx[i] * w * Math.sin(ang);
			}
		}
		
		// convert to polar notation
		System.out.println();
		
		double maxMag = 0;
		for (int k = 0; k <= sampleSize / 2 ; k++) {
			mag[k] = Math.sqrt(rex[k]*rex[k] + imx[k]*imx[k]);
			// running average of magnitude
			avgmag[k] = (avgmag[k] + mag[k]) / 2;
			
			if (avgmag[k] > maxMag)
				maxMag = avgmag[k];
			
			//if (rex[k] == 0.0)
			//	phase[k] = imx[k] < 0 ? Math.PI / 2 : -Math.PI / 2;
			//else
				phase[k] = Math.atan2(imx[k], rex[k]);
			
			// unwrap the phase
			if (k > 0) {
				int c = (int) Math.round((uwphase[k-1] - phase[k]) / (2 * Math.PI)); 
				uwphase[k] = phase[k] + c * 2 * Math.PI;
			}
			
		}
		
		/*
		// ignore 0, due to 1/f noise
		for (int k = 1; k <= sampleSize / 2 ; k++) {
			if (avgmag[k] >= maxMag / 2) {
				int m;
				m = (int)(avgmag[k] * avgmag.length / (avgmag.length / 2));
				if (m != 0) {
					int h = (int) (k * (55930. / 2) / avgmag.length);
					System.out.println(h +  " Hz: " + m);
				}
			}
		}
		*/
		/*
		for (int k = 1; k <= sampleSize / 2 ; k++) {
			int h = (int) (k * (55930. / 2) / avgmag.length);
			System.out.println(h +  " Hz: " + uwphase[k]);
		}
		*/
	}
}
