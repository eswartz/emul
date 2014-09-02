/*
  LPCSpeechEncoder.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import uk.co.labbookpages.WavFile;
import uk.co.labbookpages.WavFileException;
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
	private ILPCFilter filter;

	/**
	 * 
	 */
	public LPCSpeechEncoder(LPCEncoderParams params, ILPCFilter filter, ILPCEngine lpc) {
		this.params = params;
		this.filter = filter;
		this.lpc = lpc;
		frame = 0;
	}
	
	public LPCAnalysisFrame encode(float[] content) {
		int frameSize = content.length; //params.getFrameSize();
		
		int len = Math.min(content.length, frameSize);
		
		if (filter != null)
			filter.filter(content, 0, len, content, lpc.getY());
		
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
	
	public static void main(String[] args) throws IOException, LineUnavailableException, WavFileException {
		String fileName = args[0];
		File theFile = new File(fileName);

//		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
//		AudioInputStream is = new AudioInputStream(
//				new FileInputStream(fileName),
//				format,
//				theFile.length());

		WavFile wf = WavFile.openWavFile(theFile);
		AudioFormat format = new AudioFormat(
				wf.getSampleRate(), wf.getValidBits(), wf.getNumChannels(), 
				true, false);
				
		// nominal speech reproduction rate
		int playbackHz = 8000;
		
		// nominal framerate (25 ms)
		int framesPerSecond = 40;
		
		LPCEncoderParams params = new LPCEncoderParams(
				(int) format.getFrameRate() , framesPerSecond, 10);
		
		ILPCFilter filter = null;
//		ILPCFilter filter = new OpenLPCFilter(params);
//		ILPCFilter filter = new SimpleLPCFilter(params);
		filter = new LowPassLPCFilter(params, new SimpleLPCFilter(params));
//		ILPCFilter filter = new LowPassLPCFilter(params, new SimpleLPCFilter(params));
		//ILPCFilter filter = new LowPassLPCFilter(params, null); //, new OpenLPCFilter(params));
//		ILPCFilter filter = new LowPassLPCFilter(params, new OpenLPCFilter(params));
//		ILPCEngine engine = new RtLPCEngine(params);
		ILPCEngine engine = new OpenLPCEngine(params);
		
		int frames = (int) format.getFrameRate() / framesPerSecond;
		double[][] buffer = new double[format.getChannels()][frames];
		float[] content = new float[frames];
		
		int len;
		
		
		List<LPCAnalysisFrame> anaFrames = new ArrayList<LPCAnalysisFrame>();
		
		LPCSpeechEncoder encoder = new LPCSpeechEncoder(params, filter, engine);
				
		int nc = format.getChannels();
		while ((len = wf.readFrames(buffer, frames)) > 0) {
			for (int i = 0; i < len; i += nc) {
				content[i] = (float) buffer[0][i];
			}
			
			LPCAnalysisFrame anaFrame = encoder.encode(content);
			anaFrames.add(anaFrame);
		}
		
		wf.close();

		SpeechDataSender sender = new SpeechDataSender(playbackHz, 20);
		
		final FileOutputStream fos = new FileOutputStream("/tmp/speech_out.raw");
		
		sender.setOutputStream(fos);
		
		if (true) {
			ISettingsHandler settings = new BasicSettingsHandler();
			LPCSpeech speech = new LPCSpeech(settings);
			
			ListenerList<ISpeechDataSender> senderList = new ListenerList<ISpeechDataSender>();
			speech.setSenderList(senderList);
			
			settings.get(ISpeechChip.settingTalkSpeed).setDouble(1);
			
			speech.init();
			
			senderList.add(sender);
			
			LPCConverter converter = new LPCConverter((int) format.getFrameRate(), playbackHz);
			for (LPCAnalysisFrame anaFrame : anaFrames) {
				ILPCParameters parms = converter.apply(anaFrame);
				System.out.println(parms);
				speech.frame((LPCParameters) parms, playbackHz / framesPerSecond);
				
			}
		} else {
			float[] out = new float[playbackHz / framesPerSecond];
			
			for (LPCAnalysisFrame anaFrame : anaFrames) {
				
				engine.synthesize(out, 0, out.length, playbackHz, anaFrame);
				
				for (int o = 0; o < out.length; o++) {
					sender.sendSample((short) (Math.max(-1, Math.min(1, out[o])) * 32767), o, out.length);
				}
				
			}
		}
		fos.close();
	}

}
