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
import ejs.base.sound.ArraySoundView;
import ejs.base.sound.SoundFileListener;
import ejs.base.sound.SoundFormat;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class LPCSpeechEncoder {

	private ILPCEngine lpc;
	private int frame;
	private ILPCFilter filter;

	/**
	 * 
	 */
	public LPCSpeechEncoder(ILPCFilter filter, ILPCEngine lpc) {
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
		
		boolean voiced = results.pitch != 0;
		System.out.print("frame: " + frame + "; ");
		if (voiced) {
			System.out.print("pitch: " + results.pitch + "; ");
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

		// nominal speech reproduction rate
		int playbackHz = 8000;
		
		// nominal framerate (25 ms)
		int framesPerSecond = 40;
		
		// go through eight cycles starting with a raw input .wav,
		// encoding it to LPC, playing that back, then using
		// that output as input.
		int count = 0;
		for (int i = 0; i < 8; i++) {
			// analyze and construct LPC speech 
			ArrayList<LPCAnalysisFrame> anaFrames = new ArrayList<LPCAnalysisFrame>();
			ILPCEngine engine = analyze(theFile, framesPerSecond, playbackHz, anaFrames);
			
			// play it back and record to file 
			File outFile = new File("/tmp/speech_out" + (count != 0 ? count : "") + ".wav");
			count++;
			playbackSpeech(outFile, playbackHz, framesPerSecond, anaFrames, engine);
			
			// read from that output on the next cycle
			theFile = outFile;
		}
	}

	/**
	 * @param theFile
	 * @param playbackHz 
	 * @return
	 * @throws WavFileException 
	 * @throws IOException 
	 */
	private static ILPCEngine analyze(File theFile, int framesPerSecond,
			int playbackHz, List<LPCAnalysisFrame> anaFrames) throws IOException, WavFileException {

//		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
//		AudioInputStream is = new AudioInputStream(
//				new FileInputStream(fileName),
//				format,
//				theFile.length());

		WavFile wf = WavFile.openWavFile(theFile);
		AudioFormat format = new AudioFormat(
				wf.getSampleRate(), wf.getValidBits(), wf.getNumChannels(), 
				true, false);
				
		LPCEncoderParams params = new LPCEncoderParams(
				(int) format.getFrameRate() , playbackHz, framesPerSecond, 10);
		
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
		
		LPCSpeechEncoder encoder = new LPCSpeechEncoder(filter, engine);
				
		int nc = format.getChannels();
		while ((len = wf.readFrames(buffer, frames)) > 0) {
			for (int i = 0; i < len; i += nc) {
				content[i] = (float) buffer[0][i];
			}
			
			LPCAnalysisFrame anaFrame = encoder.encode(content);
			anaFrames.add(anaFrame);
		}
		
		wf.close();

		return engine;
	}


	/**
	 * @param playbackHz
	 * @param framesPerSecond
	 * @param anaFrames
	 * @param engine
	 * @throws LineUnavailableException
	 */
	protected static void playbackSpeech(File outputFile, int playbackHz, int framesPerSecond,
			ArrayList<LPCAnalysisFrame> anaFrames, ILPCEngine engine)
			throws LineUnavailableException {
		SpeechDataSender sender = new SpeechDataSender(playbackHz, 20);
		
		//final FileOutputStream fos = new FileOutputStream("/tmp/speech_out.raw");
		final SoundFileListener output = new SoundFileListener();
		final SoundFormat outputFormat = new SoundFormat(playbackHz, 1, 
				SoundFormat.Type.SIGNED_16_LE);
		output.started(outputFormat);
		
		output.setIncludeSilence(true);
		output.setFileName(outputFile.getPath());

//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		sender.setOutputStream(bos);
		//sender.setOutputStream(fos);
		
		ISpeechDataSender bufferSender = new ISpeechDataSender() {
			float[] ent = new float[1];
			ArraySoundView singleSampleView = new ArraySoundView(0, ent, 0, 1, outputFormat);
			
			@Override
			public void speechDone() {
				
			}
			
			@Override
			public void sendSample(short val, int pos, int length) {
				ent[0] = val / 32768.f;
				output.played(singleSampleView);
			}
		};
		
		if (true) {
			ISettingsHandler settings = new BasicSettingsHandler();
			LPCSpeech speech = new LPCSpeech(settings);
			
			ListenerList<ISpeechDataSender> senderList = new ListenerList<ISpeechDataSender>();
			senderList.add(sender);
			senderList.add(bufferSender);
			speech.setSenderList(senderList);
			
			settings.get(ISpeechChip.settingTalkSpeed).setDouble(1);
			
			speech.init();
			
			LPCConverter converter = new LPCConverter();
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
	
		output.stopped();
	}

}
