/**
 * 
 */
package org.ejs.coffee.core.sound;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * Interface to PulseAudio APIs
 * @author ejs
 * @noimplement
 * @noextend
 */
public interface PulseAudioLibrary extends Library {
	public static class pa_sample_spec extends Structure {
		public int /*pa_sample_format_t*/ format;
		public int rate;
		public byte channels;
	}

	public static class pa_simple extends Structure {
		public Pointer mainloop;
		public Pointer context;
		public Pointer stream;
		public int direction;
		public Pointer data;
		public int read_index, read_length;
		public int operation_success;
	}

	public static class pa_channel_map extends Structure {
	}

	public static class pa_buffer_attr extends Structure {
	}

	PulseAudioLibrary INSTANCE = (PulseAudioLibrary) Native.loadLibrary("pulse-simple",
			PulseAudioLibrary.class);
	public static final int PA_SAMPLE_U8 = 0;
	public static final int PA_SAMPLE_S16LE = 3;
	public static final int PA_SAMPLE_S16BE = 4;
	public static final int PA_STREAM_NODIRECTION = 0;
	public static final int PA_STREAM_PLAYBACK = 1;
	public static final int PA_STREAM_RECORD = 2;
	public static final int PA_STREAM_UPLOAD = 3;
	
	PulseAudioLibrary.pa_simple pa_simple_new(String server,
			String	name,
			int /*pa_stream_direction_t */ 	dir,
			String dev,
			String stream_name,
			PulseAudioLibrary.pa_sample_spec ss,
			PulseAudioLibrary.pa_channel_map map,
			PulseAudioLibrary.pa_buffer_attr attr,
			IntByReference  	error	 
		);
	
	int 	pa_simple_write (PulseAudioLibrary.pa_simple s, byte[] data, int bytes, IntByReference error);
	
	void pa_simple_free(PulseAudioLibrary.pa_simple simple);

	void pa_simple_drain(PulseAudioLibrary.pa_simple simple, IntByReference error);
	
	String pa_strerror(int error);
}