/*
  DemoRecorder.java

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
package v9t9.engine.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.demos.IDemoActor;
import v9t9.common.demos.IDemoOutputStream;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoHandler.IDemoListener;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
import v9t9.common.machine.IMachine;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;
/**
 * @author ejs
 *
 */
public class DemoRecorder implements IDemoRecorder {
	private final IDemoOutputStream os;
	private final ListenerList<IDemoListener> listeners;


	private final IMachine machine;
	
	private IDemoRecordingActor[] recordingActors;
	
	
	public DemoRecorder(IMachine machine, IDemoOutputStream os, ListenerList<IDemoListener> listeners) throws IOException {
		this.machine = machine;
		this.os = os;
		this.listeners = listeners;
		
		List<IDemoRecordingActor> actors = new ArrayList<IDemoRecordingActor>();
		for (IDemoRecordingActor actor : machine.getDemoManager().createRecordingActors()) {
			if (actor.shouldRecordFor(os.getDemoFormat())) {
				actors.add(actor);
			}
		}
		recordingActors = (IDemoRecordingActor[]) actors.toArray(new IDemoRecordingActor[actors.size()]);
		
		for (IDemoActor actor : recordingActors) {
			actor.setup(machine);
		}
		
		connect();
		
	}

	public synchronized void stop() throws IOException {
		disconnect();
		
		os.close();
	}

	public synchronized void fail(final Throwable t) {
		t.printStackTrace();
		
		listeners.fire(new IFire<IDemoListener>() {

			@Override
			public void fire(IDemoListener listener) {
				t.printStackTrace();
				listener.firedEvent(new NotifyEvent(System.currentTimeMillis(), null, Level.ERROR, 
						"Unexpected error writing demo: " + t.getMessage()));
			}
		});

		disconnect();
	}
	
	private synchronized void connect() throws IOException {
		
		for (IDemoRecordingActor actor : recordingActors) {
			actor.connectForRecording(this);
		}
	}

	private synchronized void disconnect() {
		for (IDemoRecordingActor actor : recordingActors) {
			actor.disconnectFromRecording(this);
		}
	}

	public synchronized void flushData() throws IOException {
		for (IDemoRecordingActor actor : recordingActors) {
			actor.flushRecording(this);
		}
		
		if (os.getOutputStream() != null)
			os.getOutputStream().flush();
	}

	@Override
	public IMachine getMachine() {
		return machine;
	}
	
	@Override
	public IDemoOutputStream getOutputStream() {
		return os;
	}
}
