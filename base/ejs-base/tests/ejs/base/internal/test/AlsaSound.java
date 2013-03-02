/*
  AlsaSound.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.internal.test;

import ejs.base.sound.AlsaLibrary;

public class AlsaSound {
	public static void main(String[] args) throws InterruptedException {
    	AlsaLibrary.snd_pcm_t.Ref pcmref = new AlsaLibrary.snd_pcm_t.Ref();
    	
    	String device = "default";
    	int ret = AlsaLibrary.INSTANCE.snd_pcm_open(pcmref, device, AlsaLibrary.SND_PCM_STREAM_PLAYBACK, 0);
    	if (ret < 0) {
    		System.err.println(AlsaLibrary.INSTANCE.snd_strerror(ret));
    		System.exit(1);
    	}
    	
    	System.out.println(pcmref);
    	AlsaLibrary.snd_pcm_t pcm = pcmref.get();
    	
    	ret = AlsaLibrary.INSTANCE.snd_pcm_set_params(pcm, AlsaLibrary.SND_PCM_FORMAT_S16_LE, AlsaLibrary.SND_PCM_ACCESS_RW_INTERLEAVED, 
    			2, 48000, 1, 500000);
    	if (ret < 0) {
    		System.err.println(AlsaLibrary.INSTANCE.snd_strerror(ret));
    		System.exit(1);
    	}

    	System.out.println(pcm);
    	
    	byte[] wave = new byte[44100 * 2 * 5 * 2];
    	for (int i = 0; i < 44100 * 2 * 5; i += 4) {
    		short left = (short) (Math.sin(i * 440 * Math.PI / 2 / 44100.) * 32768); 
    		short right = (short) (Math.sin(i * 330 * Math.PI / 2 / 44100.) * 32768); 
    		wave[i] = (byte) (left & 0xff); 
    		wave[i + 1] = (byte) (left >> 8); 
    		wave[i + 2] = (byte) (right & 0xff);
    		wave[i + 3] = (byte) (right >> 8);
    	}
    	
    	for (int index = 0; index < 44100 * 2 * 5 * 2; index += 4410 * 2 * 2) {
    		int frames = AlsaLibrary.INSTANCE.snd_pcm_writei(pcm, wave, 4410 * 2 * 2);
    		if (frames < 0 ) {
    			frames = AlsaLibrary.INSTANCE.snd_pcm_recover(pcm, frames, 0);
    		}
    		if (frames < 0) {
    			System.err.println("snd_pcm_writei failed: " + AlsaLibrary.INSTANCE.snd_strerror(frames));
    			break;
    		}
    	}
    	AlsaLibrary.INSTANCE.snd_pcm_close(pcm);
    	
    }
}
