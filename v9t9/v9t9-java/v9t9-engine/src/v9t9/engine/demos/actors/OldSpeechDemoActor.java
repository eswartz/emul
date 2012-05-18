/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.machine.IMachine;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.common.speech.ISpeechPhraseListener;
import v9t9.engine.demos.events.OldSpeechEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.format.old.OldDemoFormat;
import v9t9.engine.speech.SpeechTMS5220;

/**
 * @author ejs
 *
 */
public class OldSpeechDemoActor extends BaseDemoActor {

	private SpeechTMS5220 speech;
	private ISpeechPhraseListener phraseListener;
	
	private SpeechDemoConverter converter; 
	private ILPCParametersListener convertParamsListener;

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return OldSpeechEvent.ID;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		this.speech = (SpeechTMS5220) machine.getSpeech();
		speech.reset();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#shouldRecordFor(byte[])
	 */
	@Override
	public boolean shouldRecordFor(byte[] header) {
		return OldDemoFormat.DEMO_MAGIC_HEADER_V910.equals(header);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void connectForRecording(final IDemoRecorder recorder) throws IOException {
		if (phraseListener == null) {
			this.phraseListener = new ISpeechPhraseListener() {
				
				@Override
				public void phraseTerminated() {
					try {
						recorder.getOutputStream().writeEvent(
								new OldSpeechEvent(OldSpeechEvent.SPEECH_TERMINATING));
					} catch (Throwable t) {
						recorder.fail(t);
					}
				}
				
				@Override
				public void phraseStopped() {
					try {
						recorder.getOutputStream().writeEvent(
								new OldSpeechEvent(OldSpeechEvent.SPEECH_STOPPING));
					} catch (Throwable t) {
						recorder.fail(t);
					}
				}
				
				@Override
				public void phraseStarted() {
					try {
						recorder.getOutputStream().writeEvent(
								new OldSpeechEvent(OldSpeechEvent.SPEECH_STARTING));
					} catch (Throwable t) {
						recorder.fail(t);
					}
				}
				
				@Override
				public void phraseByteAdded(byte byt) {
					try {
						recorder.getOutputStream().writeEvent(
								new OldSpeechEvent(byt));
					} catch (Throwable t) {
						recorder.fail(t);
					}
				}
			};
		}
		
		speech.addPhraseListener(phraseListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void flushRecording(IDemoRecorder recorder) throws IOException {
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void disconnectFromRecording(IDemoRecorder recorder) {
		speech.removePhraseListener(phraseListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#setupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void setupPlayback(final IDemoPlayer player) {
		super.setupPlayback(player);
		
		converter = new SpeechDemoConverter();
		convertParamsListener = new ILPCParametersListener() {
			
			@Override
			public void parametersAdded(ILPCParameters params) {
				try {
					player.executeEvent(new SpeechEvent(params));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		converter.addEquationListener(convertParamsListener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		OldSpeechEvent ev = (OldSpeechEvent) event;
		switch (ev.getCode()) {
		//
		//	legacy handling: convert to new format
		//
		case OldSpeechEvent.SPEECH_STARTING:
			converter.startPhrase();
			break;
		case OldSpeechEvent.SPEECH_STOPPING:
			converter.stopPhrase();
			break;
		case OldSpeechEvent.SPEECH_TERMINATING:
			converter.terminatePhrase();
			//speech.reset();
			break;
		case OldSpeechEvent.SPEECH_ADDING_BYTE:
			converter.pushByte(ev.getAddedByte());
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		super.cleanupPlayback(player);
		
		converter.removeEquationListener(convertParamsListener);
		
		//speech.reset();
	}

}
