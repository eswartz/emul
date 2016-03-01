/*
  AlsaLibrary.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Interface to ALSA PCM routines
 * 
 * @author ejs
 * @noimplement
 * @noextend
 */
public interface AlsaLibrary extends Library {
	public static class snd_pcm_t extends PointerType {
		public static class Ref extends PointerByReference {
			public snd_pcm_t get() {
				snd_pcm_t pcm = new snd_pcm_t();
				pcm.setPointer(getPointer().getPointer(0));
				return pcm;
			}
		}
	}
	
	public static class snd_pcm_sw_params_t extends PointerType {
		public static class Ref extends PointerByReference {
			public snd_pcm_sw_params_t get() {
				snd_pcm_sw_params_t params = new snd_pcm_sw_params_t();
				params.setPointer(getPointer().getPointer(0));
				return params;
			}
		}
	}

	public static class snd_pcm_hw_params_t extends PointerType {
		public static class Ref extends PointerByReference {
			public snd_pcm_hw_params_t get() {
				snd_pcm_hw_params_t params = new snd_pcm_hw_params_t();
				params.setPointer(getPointer().getPointer(0));
				return params;
			}
		}
	}
	AlsaLibrary INSTANCE = (AlsaLibrary) Native.synchronizedLibrary(
			(AlsaLibrary) Native.loadLibrary("asound", AlsaLibrary.class));
	
	public static final int SND_PCM_NONBLOCK = 1;
	
	public static final int SND_PCM_STREAM_PLAYBACK = 0;
	public static final int SND_PCM_STREAM_CAPTURE = 1;
	/** mmap access with simple interleaved channels */
	public static final int SND_PCM_ACCESS_MMAP_INTERLEAVED = 0;
	/** mmap access with simple non interleaved channels */
	public static final int SND_PCM_ACCESS_MMAP_NONINTERLEAVED = 1;
	/** mmap access with complex placement */
	public static final int SND_PCM_ACCESS_MMAP_COMPLEX = 2;
	/** snd_pcm_readi/snd_pcm_writei access */
	public static final int SND_PCM_ACCESS_RW_INTERLEAVED = 3;
	/** snd_pcm_readn/snd_pcm_writen access */
	public static final int SND_PCM_ACCESS_RW_NONINTERLEAVED = 4;
	public static final int SND_PCM_FORMAT_UNKNOWN = -1;
	public static final int SND_PCM_FORMAT_S8 = 0;
	public static final int SND_PCM_FORMAT_U8 = 1;
	public static final int SND_PCM_FORMAT_S16_LE = 2;
	public static final int SND_PCM_FORMAT_S16_BE = 3;
	public static final int SND_PCM_FORMAT_U16_LE = 4;
	public static final int SND_PCM_FORMAT_U16_BE = 5;
	public static final int SND_PCM_FORMAT_S24_LE = 6;
	public static final int SND_PCM_FORMAT_S24_BE = 7;
	public static final int SND_PCM_FORMAT_U24_LE = 8;
	public static final int SND_PCM_FORMAT_U24_BE = 9;
	public static final int SND_PCM_FORMAT_U32_LE = 10;
	public static final int SND_PCM_FORMAT_U32_BE = 11;
	public static final int SND_PCM_FORMAT_FLOAT_LE = 12;
	public static final int SND_PCM_FORMAT_FLOAT_BE = 13;
	public static final int SND_PCM_FORMAT_FLOAT64_LE = 14;
	public static final int SND_PCM_FORMAT_FLOAT64_BE = 15;

	int snd_pcm_open(snd_pcm_t.Ref pcm, String name,
			int /* snd_pcm_stream_t */stream, int mode);

	int snd_pcm_recover(snd_pcm_t pcm, int err, int silent);

	int snd_pcm_set_params(snd_pcm_t pcm,
			int /* snd_pcm_format_t */format, int /* snd_pcm_access_t */access,
			int channels, int rate, int soft_resample, int latency);

	int snd_pcm_get_params(snd_pcm_t pcm,
			IntByReference /* snd_pcm_uframes_t * */buffer_size,
			IntByReference /* snd_pcm_uframes_t * */period_size);

	int snd_pcm_writei(snd_pcm_t pcm, byte[] buffer, int size);

	String snd_strerror(int errnum);

	int snd_pcm_close(snd_pcm_t pcm);

	String snd_pcm_name(snd_pcm_t pcm);

	int snd_pcm_drop(snd_pcm_t pcm);
	int snd_pcm_start(snd_pcm_t pcm);
	int snd_pcm_prepare(snd_pcm_t pcm);

	int snd_pcm_drain(snd_pcm_t pcm);

	int snd_pcm_pause(snd_pcm_t pcm, int enable);

	int snd_pcm_hw_params_malloc(snd_pcm_hw_params_t.Ref ptr);
	void snd_pcm_hw_params_free(snd_pcm_hw_params_t params);
	int snd_pcm_hw_params(snd_pcm_t pcm, snd_pcm_hw_params_t params);
	int snd_pcm_hw_params_any(snd_pcm_t pcm, snd_pcm_hw_params_t params);
	
	int snd_pcm_hw_params_get_period_size(snd_pcm_hw_params_t params, IntByReference size, IntByReference dir);
	

	int snd_pcm_sw_params_malloc(snd_pcm_sw_params_t.Ref ptr);
	void snd_pcm_sw_params_free(snd_pcm_sw_params_t params);
	int snd_pcm_sw_params(snd_pcm_t pcm, snd_pcm_sw_params_t params);
	int snd_pcm_sw_params_current(snd_pcm_t pcm, snd_pcm_sw_params_t params);

	int snd_pcm_sw_params_set_start_threshold(snd_pcm_t handle,
			snd_pcm_sw_params_t params, int frames);

	int snd_pcm_sw_params_set_stop_threshold(snd_pcm_t handle,
			snd_pcm_sw_params_t params, int value);
	int snd_pcm_sw_params_get_boundary(snd_pcm_sw_params_t params,
			IntByReference boundary);
	int snd_pcm_sw_params_set_silence_threshold(snd_pcm_t handle,
			snd_pcm_sw_params_t params, int value);
	int snd_pcm_sw_params_set_avail_min(snd_pcm_t handle,
			snd_pcm_sw_params_t params, int value);

}