/*
  OldSpeechDemoActor.java

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
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.machine.IMachine;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.common.speech.ISpeechPhraseListener;
import v9t9.engine.demos.events.OldSpeechEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.format.old.OldDemoFormat;
import v9t9.engine.speech.SpeechTMS5220;

/**
 * Converting actor for old-style speech events.  The OldSpeechEvents cannot be played
 * back directly, due to timing issues.  Instead, we convert them to
 * LPC parameters and emit them as new-style SpeechEvents.
 * @author ejs
 *
 */
public class OldSpeechDemoActor extends BaseDemoActor implements IDemoReversePlaybackActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return OldSpeechEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new OldSpeechDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new OldSpeechDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new OldSpeechDemoActor();
		}
		
	}
	
	private SpeechTMS5220 speech;
	private ISpeechPhraseListener phraseListener;
	
	private SpeechDemoConverter converter; 
	private ILPCParametersListener convertParamsListener;
	
	private SpeechDemoConverter reverseConverter; 
	private ILPCParametersListener convertReversedParamsListener;
	private LinkedList<SpeechEvent> reversedEventsList;
	private int reversedEventsFlushIndex;

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


	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#setupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupReversePlayback(IDemoPlayer player) {
		reversedEventsList = new LinkedList<SpeechEvent>();
		
		reverseConverter = new SpeechDemoConverter();
		convertReversedParamsListener = new ILPCParametersListener() {
			
			@Override
			public void parametersAdded(ILPCParameters params) {
				reversedEventsList.add(new SpeechEvent(params));
				reversedEventsFlushIndex = reversedEventsList.size();
			}
		};
		reverseConverter.addEquationListener(convertReversedParamsListener);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#queueEventForReversing(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void queueEventForReversing(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		OldSpeechEvent ev = (OldSpeechEvent) event;
		switch (ev.getCode()) {
		//
		//	legacy handling: convert to new format
		//
		case OldSpeechEvent.SPEECH_STARTING:
			reverseConverter.startPhrase();
			break;
		case OldSpeechEvent.SPEECH_STOPPING:
			reverseConverter.stopPhrase();
			break;
		case OldSpeechEvent.SPEECH_TERMINATING:
			reverseConverter.terminatePhrase();
			break;
		case OldSpeechEvent.SPEECH_ADDING_BYTE:
			reverseConverter.pushByte(ev.getAddedByte());
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#emitReversedEvents(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public IDemoEvent[] emitReversedEvents(IDemoPlayer player)
			throws IOException {
		if (reversedEventsFlushIndex > 0) {
			reverseConverter.stopPhrase();
			List<SpeechEvent> subList = reversedEventsList.
					subList(0, reversedEventsFlushIndex);
			IDemoEvent[] evs = (IDemoEvent[]) subList.
					toArray(new IDemoEvent[reversedEventsFlushIndex]);
			subList.clear();
			reversedEventsFlushIndex = 0;
			return evs;
		}
		return new IDemoEvent[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void cleanupReversePlayback(IDemoPlayer player) {
		reverseConverter.removeEquationListener(convertReversedParamsListener);		
		reversedEventsList = null;
	}
}
