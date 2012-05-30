/**
 * 
 */
package v9t9.engine.demos.actors;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.memory.SimpleMemoryWriteTracker;
import v9t9.engine.demos.events.VideoWriteDataEvent;

/**
 * @author ejs
 * @deprecated does not work as expected
 */
public class VdpV9938DataDemoActor extends VdpDataDemoActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return VideoWriteDataEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new VdpV9938DataDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new VdpV9938DataDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return null;
		}
		
	}
	
	@Override
	public String getEventIdentifier() {
		return VideoWriteDataEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.VdpDataDemoActor#createMemoryTracker()
	 */
	@Override
	protected SimpleMemoryWriteTracker createMemoryTracker() {
		return new SimpleMemoryWriteTracker(vdp.getVideoMemory(), 8) {
			/* (non-Javadoc)
			 * @see v9t9.common.memory.SimpleMemoryWriteTracker#recordChange(int, java.lang.Number)
			 */
			@Override
			protected void recordChange(int addr, Number value) {
				if (((IVdpV9938) vdp).isAccelActive())
					return;
				super.recordChange(addr, value);
			}
		};
	}
	
}
