/**
 * 
 */
package v9t9.engine.speech.encode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ISpeechDataSender;
import v9t9.engine.speech.LPCParameters;
import v9t9.engine.speech.LPCSpeech;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class LPCSpeechEncoder {

	private ILPCEngine lpc;
	private LPCEncoderParams params;
	private int frame;

	/**
	 * 
	 */
	public LPCSpeechEncoder(LPCEncoderParams params, ILPCEngine lpc) {
		this.params = params;
		this.lpc = lpc;
		frame = 0;
	}
	
	public LPCAnalysisFrame encode(float[] content) {
		int frameSize = content.length; //params.getFrameSize();
		
		int len = Math.min(content.length, frameSize);
		LPCAnalysisFrame results = lpc.analyze(content, 0, len);
		
		boolean voiced = results.invPitch != 0;
		System.out.print("frame: " + frame + "; ");
		if (voiced) {
			System.out.print("pitch: " + (params.getHertz() / results.invPitch) + "; ");
		} else {
			System.out.print("pitch: unvoiced; ");
		}
		System.out.print("power: " + results.power + "; ");
		
		for (int c = 0; c < results.coefs.length; c++)
			System.out.print(c + ": " + results.coefs[c] + "; ");
		
		System.out.println();
		
		frame++;
		
		return results;
	}
	
	public static void main(String[] args) throws IOException, LineUnavailableException {
		String fileName = args[0];
		File theFile = new File(fileName);

		AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
		AudioInputStream is = new AudioInputStream(
				new FileInputStream(fileName),
				format,
				theFile.length());
		

		int playbackHz = 44100;
		
		int framesPerSecond = 20;
		
		LPCEncoderParams params = new LPCEncoderParams(
				(int) format.getFrameRate(), framesPerSecond, 10);
		
//		ILPCEngine engine = new RtLPCEngine(params);
		ILPCEngine engine = new OpenLPCEngine(params);
		
		byte[] buffer = new byte[params.getFrameSize() * format.getSampleSizeInBits() / 8];
		float[] content = new float[params.getFrameSize()];
		
		int len;
		
		
		List<LPCAnalysisFrame> anaFrames = new ArrayList<LPCAnalysisFrame>();
		
		// HACK: ignore header
		is.read(buffer, 0, 0x28);
		
		LPCSpeechEncoder encoder = new LPCSpeechEncoder(params, engine);
				
		while ((len = is.read(buffer)) > 0) {
			for (int i = 0; i < len; i += 2) {
				int sample = buffer[i] & 0xff | (buffer[i + 1] & 0xff) << 8; 
				content[i / 2] = (short) sample / 32768.0f;
			}
			
			encoder.lowpass(content);
			
			LPCAnalysisFrame anaFrame = encoder.encode(content);
			anaFrames.add(anaFrame);
		}
		
		is.close();

		
		if (true) {
			ISettingsHandler settings = new BasicSettingsHandler();
			LPCSpeech speech = new LPCSpeech(settings);
			
			ListenerList<ISpeechDataSender> senderList = new ListenerList<ISpeechDataSender>();
			speech.setSenderList(senderList);
			
			settings.get(ISpeechChip.settingTalkSpeed).setDouble(1);
			
			speech.init();
			
			
			ISpeechDataSender sender = new SpeechDataSender(playbackHz, 20);
			senderList.add(sender);
			
			LPCConverter converter = new LPCConverter((int) format.getFrameRate(), playbackHz);
			for (LPCAnalysisFrame anaFrame : anaFrames) {
				ILPCParameters parms = converter.apply(anaFrame);
				System.out.println(parms);
				speech.frame((LPCParameters) parms, playbackHz / framesPerSecond);
				
			}
		} else {
			ISpeechDataSender sender = new SpeechDataSender(playbackHz, framesPerSecond);
			float[] out = new float[playbackHz / framesPerSecond];
			
			for (LPCAnalysisFrame anaFrame : anaFrames) {
				
				engine.synthesize(out, 0, out.length, playbackHz, anaFrame);
				
				for (int o = 0; o < out.length; o++) {
					sender.sendSample((short) (out[o] * 32767), o, out.length);
				}
				
			}
		}
	}

	/**
	 * @param content
	 * @param f
	 */
	public void lowpass(float[] content) {
		float last = 0f;
				
		float q = 0.5f;
		for (int idx = 0; idx < content.length; idx ++) {
			float v = content[idx];

//			v = (1f * v - 0.5f * last);
//			last = v;
//			
//			content[idx] = v;
			
			content[idx] = (v * (1 - q) - last * q);
			last = content[idx];
		}
	}
	
}
