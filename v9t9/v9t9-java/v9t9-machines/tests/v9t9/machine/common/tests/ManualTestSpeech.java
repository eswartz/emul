/*
  ManualTestSpeech.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import java.io.FileOutputStream;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.speech.TMS5220Consts;
import v9t9.engine.speech.LPCSpeech;
import v9t9.engine.speech.SpeechTMS5220;
import v9t9.engine.speech.encode.SpeechDataSender;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;

/**
 * @author ejs
 *
 */
public class ManualTestSpeech {

	/**
	 * 
	 */
	private static final short[] THAT_IS_RIGHT = new short[] {
	//	118,
		166, 209,
		198,37,104,82,151,
		206,91,138,224,232,
		116,186,18,85,130,
		204,247,169,124,180,
		116,239,185,183,184,
		197,45,20,32,131,
		7,7,90,29,179,
		6,90,206,91,77,
		136,166,108,126,167,
		181,81,155,177,233,
		230,0,4,170,236,
		1,11,0,170,100,
		53,247,66,175,185,
		104,185,26,150,25,
		208,101,228,106,86,
		121,192,234,147,57,
		95,83,228,141,111,
		118,139,83,151,106,
		102,156,181,251,216,
		167,58,135,185,84,
		49,209,106,4,0,
		6,200,54,194,0,
		59,176,192,3,0,
		0
		
	};

	public static void main(String[] args) throws Exception {
		ManualTestSpeech ts = new ManualTestSpeech();
		ts.run();
		
	}

	private void run() throws Exception {
		
		ISettingsHandler settings = new BasicSettingsHandler();
		
		DataFiles.addSearchPath(settings, "/usr/local/src/v9t9-data/roms");
		DataFiles.addSearchPath(settings, "l:/src/v9t9-data/roms");
		IMachine machine = new StandardTI994AMachineModel().createMachine(settings);
		SpeechTMS5220 tms5220 = (SpeechTMS5220) machine.getSpeech();
		
		settings.get(ISpeechChip.settingLogSpeech).setInt(1);
		settings.get(ISpeechChip.settingTalkSpeed).setDouble(1.0);
		
		LPCSpeech speech = tms5220.getLpcSpeech();
		speech.init();
		
		machine.start();
		machine.setPaused(true);
		
		final FileOutputStream fos = new FileOutputStream("/tmp/speech.raw");
		
		SpeechDataSender sender = new SpeechDataSender(8000, 20);
		sender.setOutputStream(fos);
		
		tms5220.addSpeechListener(sender);
		
		// reset
		tms5220.command((byte) 0x70);

		// "test" from TEII
		sayDirect(tms5220, new short[] {
				0, 0x10, 0x80, 0x1d, 0xc5, 0x3, 0x70, 0xac, 0x87,
				0x1, 0x52, 0x28, 0x2e, 0x69, 0xcc, 0xee, 0x1a,
				0x35, 0x79, 0xa5, 0x31, 0xbb, 0x6b, 0xd4, 0xe4,
				0x9d, 0xae, 0x34, 0x66, 0x4b, 0x89, 0x1c, 0x3d,
				0x52, 0x57, 0x35, 0x2c, 0xbb, 0xb4, 0x7, 0x8, 0xf0,
				0x53, 0x84, 0x4, 0x34, 0x60, 0x80, 0x9f, 0x32, 0x3d,
				0x10, 0x80, 0x9f, 0xdc, 0x24, 0, 0, 0xe, 0x58, 0xc2,
				0x3, 0x4, 0x58, 0xc2, 0xfc, 0x1
		});
		
		
		if (true) {
			// should exit quickly (or not...)
			//sayPhrase(tms5220, 0xfff0);
			
			sayPhrase(tms5220, 0x351a);	// HELLO
			sayPhrase(tms5220, 0x71f4);	// UHOH
			
			sayDirect(tms5220, THAT_IS_RIGHT);
			
			sayPhrase(tms5220, 0x4642);	// MORE
	
			sayDirect(tms5220, THAT_IS_RIGHT);
			sayDirect(tms5220, THAT_IS_RIGHT);
		}
		
		///
		
		// bad usage, no waiting -- should delay anyway!
//		sayPhrase(tms5220, 0x1D82);	// CHECK
//		sayPhrase(tms5220, 0x2612);	// DRAW
//		sayPhrase(tms5220, 0x1c48);	// BYE
//		sayPhrase(tms5220, 0x3148);	// GOODBYE
		
		if (true) {
		sayPhrase(tms5220, 0x1a42, false);	// BE
		sayPhrase(tms5220, 0x4642, false);	// MORE
		sayPhrase(tms5220, 0x51b3, false);	// POSITIVE
		sayPhrase(tms5220, 0x1714, false);	// ABOUT
		sayPhrase(tms5220, 0x69b6, false);	// THE1
		sayPhrase(tms5220, 0x208b, false);	// CONNECTED
		sayPhrase(tms5220, 0x2034, false);	// COMPUTER
		sayPhrase(tms5220, 0x24ea, false);	// DOING
		sayPhrase(tms5220, 0x2599, false);	// DOUBLE
		sayPhrase(tms5220, 0x6e69, false);	// TIME
		sayPhrase(tms5220, 0x1769, false);	// AFTER
		sayPhrase(tms5220, 0x70ce, false);	// TWELVE
		sayPhrase(tms5220, 0x4e66, false);	// P
		sayPhrase(tms5220, 0x4233, false);	// M
		
		} else {
//			sayPhrase(tms5220, 0x70ce, false);	// TWELVE
//			sayPhrase(tms5220, 0x4e66, false);	// P
		}
		Thread.sleep(2000);
		
		fos.close();
		//System.exit(0);
	}

	/**
	 * @param tms5220
	 * @param s
	 * @throws InterruptedException 
	 */
	private void sayDirect(SpeechTMS5220 tms5220, short[] s) throws InterruptedException {
	
		// wait for previous phrase to end
		while ((tms5220.read() & TMS5220Consts.SS_TS) != 0) {
			Thread.sleep(10);
		}

		// speak external
		tms5220.command((byte) 0x60);
		
		int toCopy = 16;
		for (int idx = 0; idx < s.length; ) {
			for (int cnt = 0; cnt < toCopy && idx < s.length; cnt++) {
				tms5220.write((byte) s[idx++]);
			}
			toCopy = 8;
			
			if (idx >= s.length)
				break;
			
			while ((tms5220.read() & TMS5220Consts.SS_BL + TMS5220Consts.SS_TS) == 
					TMS5220Consts.SS_TS) 
			{
				Thread.sleep(1);
				
			}
		}
		
	}
	private void sayPhrase(SpeechTMS5220 tms5220, int addr) throws InterruptedException {
		sayPhrase(tms5220, addr, true);
	}
	private void sayPhrase(SpeechTMS5220 tms5220, int addr, boolean wait) throws InterruptedException {
		if (wait) {
			// wait for previous phrase to end
			int stat;
			while (true) {
				stat = tms5220.read() & TMS5220Consts.SS_BL + TMS5220Consts.SS_TS;
				if ((stat & TMS5220Consts.SS_TS) == 0 || stat == 0 /* (stat & TMS5220Consts.SS_BL) != 0*/)
					break;
	//			if ((stat & TMS5220Consts.SS_BL) != 0)
	//				break;
				Thread.sleep(10);
			}
		} else {
			Thread.sleep((long) (Math.random() * 30 + 10));
		}
		
		// read to reset addr pointers
		tms5220.command((byte) 0x10);
		// set phrase addr
		//tms5220.setAddr(addr);
		
		for (int i = 0; i < 5; i++) {
			tms5220.command((byte) (0x40 | ((addr >> (i * 4)) & 0xf) ));
			
		}
		
		// speak
		tms5220.command((byte) 0x50);
		
		if (wait) {
			while ((tms5220.read() & TMS5220Consts.SS_TS) != 0) {
				Thread.sleep(100);
			}
		}
	}
}
