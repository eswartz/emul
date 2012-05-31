/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.format.DemoFormat;
import v9t9.engine.speech.LPCParameters;
import v9t9.engine.speech.SpeechTMS5220;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class SpeechDemoActor extends BaseDemoActor implements IDemoReversePlaybackActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return SpeechEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new SpeechDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new SpeechDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new SpeechDemoActor();
		}
		
	}
	
	private SpeechTMS5220 speech;
	private ILPCParametersListener paramsListener;
	private List<ILPCParameters> currentPhraseParamsList;
	
	private IProperty demoRate;
	private IProperty talkRate;
	private double origTalkRate;
	private IPropertyListener demoRateListener;
	
	private List<SpeechEvent> reversedEventsList;
	private SpeechEvent lastSpeechEvent;

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return SpeechEvent.ID;
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
		return DemoFormat.DEMO_MAGIC_HEADER_V9t9.equals(header);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void connectForRecording(final IDemoRecorder recorder) throws IOException {
		currentPhraseParamsList = new ArrayList<ILPCParameters>();
		
		paramsListener = new ILPCParametersListener() {
			
			@Override
			public void parametersAdded(ILPCParameters params) {
				// speech takes a really long time, so flush
				// everything now
//				try {
//					recorder.flushData();
//				} catch (IOException e) {
//					recorder.fail(e);
//				}
				
				synchronized (SpeechDemoActor.this) {
					LPCParameters copy = new LPCParameters();
					copy.copyFrom((LPCParameters) params);
					currentPhraseParamsList.add(copy);
				}
			}
		};
		
		speech.addParametersListener(paramsListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void flushRecording(IDemoRecorder recorder) throws IOException {
		for (ILPCParameters params : currentPhraseParamsList) {
			recorder.getOutputStream().writeEvent(
					new SpeechEvent(params));
		}
		currentPhraseParamsList.clear();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void disconnectFromRecording(IDemoRecorder recorder) {
		speech.removeParametersListener(paramsListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#setupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void setupPlayback(final IDemoPlayer player) {
		super.setupPlayback(player);
		
		// have the talk speed match the demo speed
		talkRate = Settings.get(player.getMachine(), ISpeechChip.settingTalkSpeed);
		demoRate = Settings.get(player.getMachine(), IDemoHandler.settingDemoPlaybackRate);

		origTalkRate = talkRate.getDouble();
		
		if (demoRateListener == null) {
			demoRateListener = new IPropertyListener() {
				
				@Override
				public void propertyChanged(IProperty property) {
					talkRate.setDouble(property.getDouble());
				}
			};
		}
		demoRate.addListenerAndFire(demoRateListener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SpeechEvent ev = (SpeechEvent) event;
		LPCParameters params = (LPCParameters) ev.getParams();
		speech.getLpcSpeech().frame(params, speech.getSamplesPerFrame());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		super.cleanupPlayback(player);
		
		demoRate.removeListener(demoRateListener);
		talkRate.setDouble(origTalkRate);
	}

	

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#setupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupReversePlayback(IDemoPlayer player) {
		reversedEventsList = new LinkedList<SpeechEvent>();
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#queueEventForReversing(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void queueEventForReversing(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SpeechEvent ev = (SpeechEvent) event;
		if (ev.getParams().isRepeat()) {
			// repeats the *next* event, but we may have lost the last
			// params in the events list, so duplicate the last we know of
			if (lastSpeechEvent != null)
				ev = lastSpeechEvent;
		}
		reversedEventsList.add(0, ev);
		lastSpeechEvent = ev;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#emitReversedEvents(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public IDemoEvent[] emitReversedEvents(IDemoPlayer player)
			throws IOException {
		IDemoEvent[] evs = (IDemoEvent[]) reversedEventsList.toArray(new IDemoEvent[reversedEventsList.size()]);
		reversedEventsList.clear();
		return evs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void cleanupReversePlayback(IDemoPlayer player) {
		reversedEventsList = null;
	}
}
