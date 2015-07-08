/*
  CassetteReader.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import v9t9.common.cassette.CassetteFileUtils;
import v9t9.common.cassette.ICassetteChip;
import v9t9.common.cassette.ICassetteDeck;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.settings.BasicSettingsHandler;
import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;


/**

From 99/4A console ROM:

*
* THIS IS A SET OF ROUTINES DEFINED FOR AUDIO CASSETTE
* COMMUNICATION. THEY ARE ENTERED THROUGH A GRAPHICS
* LANGUAGE PROGRAM, WHICH GIVES INFORMATION LIKE THE NO.
* OF BLOCKS (=64 BYTES) TO BE WRITTEN, OR THE NUMBER OF
* FREE BLOCKS IN CASE OF READ MODE; THE VDP START ADR.
* AND THE BAUD RATE.
*   THE ROUTINES TAKE CARE OF THE NECESSARY ENCODING/
* DECODING AND THE ERROR CHECKING.
*
* THE BIPHASE FORMAT, USED IN THIS SET OF ROUTINES, HAS
* THE FOLLOWING REPRESENTATIONS FOR THE BINARY DIGITS:
*
*     ---------------             ---------         ----
*    |               |           |         |       |
* ---                 ---     ---           -------
*     <-----"0"----->            <---- "1"--------->
*
* REPRESENTATIONS OF THE BITS MAY BE CHANGED IN PHASE BY
* 180 DEGREES, DEPENDING UPON THE VALUE OF THE BIT STREAM
* AFTER THE PREVIOUS BIT.
*
*    PRINCIPLES OF OPERATION
*
*          WRITING
*   THE ACTUAL IMPLEMENTATION OF THE BIPHASE RECORDING SCHEME
* IS RELATIVELY SIMPLE. THE VALUE FOR THE DATA RATE, AS INDICATED
* BY THE GRAPHICS LANGUAGE PROGRAM, IS USED AS A TIMER VALUE FOR
* THE INTERNAL TMS9985 TIMER/COUNTER. IT IS USED AS A TIMER VALUE
* FOR HALF A BIT CELL.
*
*      ------------                    ------        ----
*     |            |                  |      |      |
*  ---              ---            ---        ------
*     |<----><---->                    <-----><----->
*         DRATE                            DRATE
*
*   EACH BIT CELL THUS CONSISTS OF TWO TIMER INTERVALS. THE TIMER
* INTERRUPT AT THE BEGINING OF EACH BIT CELL CAUSES THE OUTPUT LINE
* TO CHANGE VALUE. THE NEXT TIMER INTERRUPT, IN THE MIDDLE OF THE
* BIT CELL, ONLY CHANGES THE VALUE OF THE OUTPUT LINE IF THE BIT TO
* BE OUTPUT EQUALS A BINARY "1".
*
*          READING
*    ON READING BACK, THE BASIC TIMER INTERVAL TIME IS SET TO 1.5
* TIMES THE DRATE OF THE WRITE SECTION. THE TIMER IS SYYNCHRONIZED
* ON THE FLUX CHANGE AT THE BEGINING OF THE BIT CELL. AFTER THE TIMER
* HAS GIVEN AN INTERRUPT, THE CURRENT INPUT LINE VALUE IS COMPARED TO
* THE VALUE AT THE BEGINING OF THE BIT CELL. IF THIS VALUE HAS
* CHANGED, THE BIT VALUE IS ASSUMED TO BE "1" IF NOT, IT WILL BE
* A "0"
*      TO PROVIDE A TIME-OUT MECHANISM THE TIMER AUTOMATICALLY
* RESTARTS ITSELF WITH THE SAME RATE.  IF THE TIMER TIMES OUT BEFORE
* THE NEXT FLUX CHANGE, AN ILLEGAL BIT LENGTH IS ASSUMED, AND AN
* ERROR RETURN CODE IS PRODUCED.
*
*********************************************************************
*
*    CASSETTE WRITE ROUTINE
*
* WRITES N BLOCKS OF 64 BYTES TO THE AUDIO CASSETTE.
*
* THE OUTPUT FORMAT USED IS:
*   - ZERO LEADER CONSISTING OF LDCNT ZEROES
*   - SYNC BYTE (8 "1" BITS)
*   - NUMBER OF BLOCKS TO FOLLOW (8 BITS)
*   - CHECKSUM (8 BITS)
*   - 2*N BLOCKS, CONSISTING OF:
*      - 8 BYTES OF ZERO
*      - 1 BYTE OF ONES
*      - 64 BYTES OF INFORMATION
*      - CHECKSUM (8 BITS)
*   - EACH BLOCK IS REPEATED TWICE. THE LEADING ZEROES AND
*     ONES ARE USED FOR TIMING AND TO R
*   - TRAILER OF EIGHT "1" BITS
*
*********************************************************************
*
* CASSETTE READ ROUTINES
*
* DEVIATION OF UP TO -25 TO +50 PERCENT OF THE
*    NOMINAL BAUD RATE IS PERMITTED
*
*********************************************************************
*
*  BIT INPUT ROUTINE
*
* READ ONE BIT FROM THE INPUT STREAM. RETURN TO CALLER+2
* IF BIT READ IS "1"
*     THE VALUE OF THE BIT CELL IS COMPUTED BY DETERMINING
* THE INPUT LINE VALUE AT 3/4 OF THE BIT CELL LENGTH. IF THE
* INPUT LINE LEVEL HAS CHANGED DURING THAT PERIOD, THE BIT
* READ = "1"; IF NOT, THE BIT READ = "0"
*     THE NEXT FLUX CHANGE SHOULD COME WITHIN 3/4 OF A BIT
* CELL, IN ORDER TO ACCEPT THE BIT

 * @author ejs
 *
 */
public class CassetteReader {

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {

		File audioFile = new File(args[0]);
		
		AudioFileFormat format = CassetteFileUtils.scanAudioFile(audioFile);
		
		final int BASE_CLOCK = 3000000;
		final int POLL_CLOCK = 1378 * 3 / 2;
		float secsPerPoll = (float) POLL_CLOCK / BASE_CLOCK;
		
		ISettingsHandler settings = new BasicSettingsHandler();
		IProperty debug = settings.get(ICassetteChip.settingCassetteDebug);
		
		CassetteReader reader = new CassetteReader(audioFile, format, debug, null);
		while (!reader.isDone()) {
			
			boolean val = reader.readBit(secsPerPoll);
			System.out.print(reader.getPosition() + ": ");
			System.out.println(val);
		}
	}

	
	private long position;
	private AudioInputStream is;
	private float mag;
	private int nch;
	private int sampSize;
	private boolean bigEndian;
	private boolean signed;
	private boolean endOfTape;
	private float min;
	private float max;
	private float dcOffset;
	private float prev;
	private int polarity;


	private float samplesFrac;
	private IProperty debug;
	private ICassetteDeck deck;

	/**
	 * @param debug 
	 * @param is 
	 * @throws FileNotFoundException 
	 * 
	 */
	public CassetteReader(File audioFile, AudioFileFormat format,
			IProperty debug, ICassetteDeck deck) throws FileNotFoundException {
		this.deck = deck;
		
		this.is = new AudioInputStream(
			new FileInputStream(audioFile),
			format.getFormat(),
			audioFile.length());

		this.debug = debug;
		
		// why doesn't Java provide a way to skip the header!?!?
		try {
			if (format.getType() == Type.WAVE) {
				is.skip(44);
			}
		} catch (IOException e) {
		}
		
		nch = is.getFormat().getChannels();
		sampSize = is.getFormat().getFrameSize() / nch;
		bigEndian = is.getFormat().isBigEndian();
		signed = is.getFormat().getEncoding() == Encoding.PCM_SIGNED;
		mag = 1.0f;
		min = 1f;
		max = -1f;
		
		if (deck != null)
			deck.setSampleRate((int) is.getFormat().getFrameRate());
	}

	/**
	 * @return
	 */
	private boolean isDone() {
		try {
			return is.available() == 0;
		} catch (IOException e) {
			return true;
		}
	}

	/**
	 * @return
	 */
	public long getPosition() {
		return position;
	}
	

	/**
	 * @return
	 */
	public float readSample() {
		if (endOfTape) {
			return 0f;
		}
		try {
			float total = 0.f;
			byte[] buf = new byte[is.getFormat().getFrameSize()];
			int len = is.read(buf);
			if (len != buf.length) {
				if (!endOfTape) {
					mag = 0f;
					endOfTape = true;
				}
				return 0f;
			}
			position++;
			for (int ch = 0; ch < nch; ch++) {
				int bufIdx = ch * sampSize;
				int samp = 0;
				if (sampSize == 1) {
					samp = buf[bufIdx] & 0xff;
					if (!signed)
						samp -= 0x80;
					else
						samp = (byte) samp;
					total += samp / 128f;
				}
				else if (sampSize == 2) {
					if (bigEndian)
						samp = ((buf[bufIdx] & 0xff) << 8) | (buf[bufIdx+1] & 0xff);
					else
						samp = ((buf[bufIdx+1] & 0xff) << 8) | (buf[bufIdx] & 0xff);
					if (!signed)
						samp -= 32768;
					else
						samp = (short) samp;
					total += samp / 32768f;
				}
				else if (sampSize == 4) {
					long lsamp;
					if (bigEndian)
						lsamp = ((buf[bufIdx+3] & 0xff) << 24) | ((buf[bufIdx+2] & 0xff) << 16) |
							((buf[bufIdx+1] & 0xff) << 8) | (buf[bufIdx] & 0xff);
					else
						lsamp = ((buf[bufIdx] & 0xff) << 24) | ((buf[bufIdx+1] & 0xff) << 16) |
							((buf[bufIdx+2] & 0xff) << 8) | (buf[bufIdx+3] & 0xff);
					if (!signed)
						lsamp -= 0x80000000L;
					else
						lsamp = (int) lsamp;

					total += lsamp / (float)0x80000000L;
				}
			}
			
			float samp = total / nch;
			
			float absSamp = Math.abs(samp);
			if (absSamp >= mag) {
				mag = (mag * 7 + absSamp) / 8f;
			} else {
				mag = (mag * 255f) / 256f;
			}
			
			if (absSamp < 0.001f)
				return 0f;
			
			return samp;
			
		} catch (IOException e) {
			return 0;
		}
	}
	
	public boolean readBit(float secs) {
		if (isEndOfTape()) {
			polarity = 0;
			return false;
		}
		
		float samplesf = (is.getFormat().getFrameRate() * secs);
		int samples = (int) samplesf;
		samplesFrac += (samplesf - samples);
		if (samplesFrac >= 1.0f) {
			samplesFrac -= 1.0f;
			samples++;
		}
		if (samples > 0) {
			polarity = scanPolarities(samples);
		}
		return polarity > 0;
	}
	/**
	 * Read the current polarity
	 * @param secs amount of time, in seconds, to poll
	 * @return
	 */
	protected int scanPolarities(int samples) {
		if (samples > 48) {
			samples = 48;
		}
		if (debug.getBoolean()) System.out.print(" @"+ samples+":");
		
		int newPolarity = polarity;
		
		while (samples-- > 0) {
			float samp = readSample();
			
			if (deck != null) {
				deck.addFloatSample(samp);
			}
			
			if (samp < min) {
				min = samp;
			} else if (samp > max) {
				max = samp;
			} 

			if (max > 0 && min < 0) {
				dcOffset = (dcOffset + (max + min) / 2) / 2;
			} else {
				dcOffset = 0f;
			}
			
			samp -= dcOffset;
			
			if (samp < 0) {
				if (prev < 0) {
					newPolarity = -1;
				}
			} else if (samp > 0) {
				if (prev > 0) {
					newPolarity = 1;
				}
			}
			
			max *= 0.99f;
			min *= 0.99f;
			
			prev = samp;
			
		}
		
		return newPolarity;
	}

	public boolean isEndOfTape() {
		return endOfTape;
	}
	/**
	 * 
	 */
	public void close() {
		try { is.close(); } catch (IOException e) { }
		endOfTape = true;
		if (deck != null)
			deck.setSampleRate(0);
	}


}
