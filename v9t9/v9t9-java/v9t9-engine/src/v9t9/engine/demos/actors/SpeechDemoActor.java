/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.common.speech.TMS5220Consts;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.speech.LPCParameters;
import v9t9.engine.speech.SpeechTMS5220;

/**
 * @author ejs
 *
 */
public class SpeechDemoActor extends BaseDemoActor {

	private SpeechTMS5220 speech;
	private ILPCParametersListener paramsListener;
	private List<ILPCParameters> currentPhraseParamsList;
	
	private IProperty demoRate;
	private IProperty talkRate;
	private double origTalkRate;
	private IPropertyListener demoRateListener;

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
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public synchronized void connectForRecording(final IDemoRecorder recorder) throws IOException {
		currentPhraseParamsList = new ArrayList<ILPCParameters>();
		
		this.paramsListener = new ILPCParametersListener() {
			
			@Override
			public void parametersAdded(ILPCParameters params) {
				// speech takes a really long time, so flush
				// everything now
				try {
					recorder.flushData();
				} catch (IOException e) {
					recorder.fail(e);
				}
				
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
	public void setupPlayback(IDemoPlayer player) {
		super.setupPlayback(player);
		
		speech.reset();
		
		// have the talk speed match the demo speed
		talkRate = Settings.get(player.getMachine(), ISpeechChip.settingTalkSpeed);
		demoRate = Settings.get(player.getMachine(), IDemoHandler.settingDemoPlaybackRate);

		origTalkRate = talkRate.getDouble();
		
		demoRateListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				talkRate.setDouble(property.getDouble());
			}
		};
		demoRate.addListenerAndFire(demoRateListener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SpeechEvent ev = (SpeechEvent) event;
		switch (ev.getCode()) {
		case SpeechEvent.SPEECH_ADDING_EQUATION:
			LPCParameters params = (LPCParameters) ev.getData();
			speech.getLpcSpeech().frame(params, speech.getSamplesPerFrame());
			break;
		
		//
		//	legacy handling -- known to be broken!
		//
		case SpeechEvent.SPEECH_STARTING:
			// wait for previous phrase to end
			while ((speech.read() & TMS5220Consts.SS_TS + TMS5220Consts.SS_BE) == TMS5220Consts.SS_TS) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return;
				}
			}
			
			speech.command((byte) 0x60);
			break;
		case SpeechEvent.SPEECH_STOPPING:
			break;
		case SpeechEvent.SPEECH_TERMINATING:
			speech.reset();
			break;
		case SpeechEvent.SPEECH_ADDING_BYTE:
			speech.write((Byte) ev.getData());
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		super.cleanupPlayback(player);
		
		demoRate.removeListener(demoRateListener);
		talkRate.setDouble(origTalkRate);
		
		speech.reset();
	}

}
