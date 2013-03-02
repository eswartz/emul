/*
  SpeechDataSender.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.speech.encode;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import v9t9.common.speech.ISpeechDataSender;

/**
 * @author ejs
 *
 */
public class SpeechDataSender implements ISpeechDataSender {
	/**
	 * 
	 */
	private  OutputStream fos;
	private byte[] speechWaveForm;
	private int speechIdx;

	private SourceDataLine speechLine;
	/**
	 * @param fos
	 * @throws LineUnavailableException 
	 */
	public SpeechDataSender(int hertz, int framesPerSecond) throws LineUnavailableException {
		speechWaveForm = new byte[hertz / framesPerSecond * 2];
		speechIdx = 0;
		
		AudioFormat speechFormat = new AudioFormat(hertz, 16, 1, true, false);
		Line.Info spInfo = new DataLine.Info(SourceDataLine.class,
				speechFormat);
		if (!AudioSystem.isLineSupported(spInfo)) {
			System.err.println("Line not supported: " + speechFormat);
			System.exit(1);
		}
		
		
		int speechFramesPerTick = (int) (speechFormat.getFrameRate() / 100);
		speechLine = (SourceDataLine) AudioSystem.getLine(spInfo);
		speechLine.open(speechFormat, speechFramesPerTick * 10);
		speechLine.start();
		
		
	}
	
	/**
	 * @param fos the fos to set
	 */
	public void setOutputStream(OutputStream fos) {
		this.fos = fos;
	}

	public void sendSample(short val, int pos, int length) {
		
		//val ^= 0x8000;
		if (speechIdx >= speechWaveForm.length) {
			speechLine.write(speechWaveForm, 0, speechWaveForm.length);
			speechIdx = 0;
		}
		speechWaveForm[speechIdx++] = (byte) (val & 0xff);
		speechWaveForm[speechIdx++] = (byte) (val >> 8);
		
		if (fos != null) {
			try {
				fos.write(val & 0xff);
				fos.write(val >> 8);
			} catch (IOException e) {
			}
		}
//				if (pos == 0)
//					System.out.println();
//				System.out.print(val + " ");
	}

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechDataSender#speechDone()
	 */
	@Override
	public void speechDone() {
		System.out.println("\n// done");
		

		speechLine.write(speechWaveForm, 0, speechIdx);
		speechLine.flush();
		
	}
}