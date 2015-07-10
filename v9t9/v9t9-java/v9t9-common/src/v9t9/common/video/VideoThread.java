/*
  VideoThread.java

  (c) 2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.Logging;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.cpu.ICpu;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class VideoThread extends Thread implements IPropertyListener {

	private IVideoRenderer videoRenderer;
	private Object sync;
	private IProperty dumpVdpAccess;
	private IProperty dumpFullInstructions;
	private IProperty pauseMachine;

	public VideoThread(IVideoRenderer videoRenderer) {
		setName("Video Thread");
		this.videoRenderer = videoRenderer;
		sync = new Object();
		
		ISettingsHandler settings = videoRenderer.getVdpHandler().getMachine().getSettings();
		dumpVdpAccess = settings.get(IVdpChip.settingDumpVdpAccess);
		dumpFullInstructions = settings.get(ICpu.settingDumpFullInstructions);
		pauseMachine = settings.get(IMachine.settingPauseMachine);
		
	}
	
	/**
	 * Invoke Object#notifyAll() on this object to trigger
	 * a redraw.
	 * @return
	 */
	public Object getSync() {
		return sync;
	}
	
	@Override
	public void run() {
		pauseMachine.addListener(this);
		synchronized (sync) {
			while (!isInterrupted()) {
				try {
					// attempt update every so often if the CPU is 
					// asleep (to make up for some unexpected expose events
					// caused by tool windows opening/closing)
					sync.wait(1000);
					
					// got notify
					synchronized (videoRenderer.getVdpHandler()) {
						synchronized (videoRenderer) {
							synchronized (videoRenderer.getCanvasHandler()) {
								synchronized (videoRenderer.getCanvas()) {
									updateVideo();
								}
							}
						}
					}
				} catch (InterruptedException e) {
					break;
				}
			}
			
		}
		pauseMachine.removeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.properties.IPropertyListener#propertyChanged(ejs.base.properties.IProperty)
	 */
	@Override
	public void propertyChanged(IProperty property) {
		synchronized (sync) {
			if (property.getBoolean())
				sync.notifyAll();
		}
	}
	
	private void log(String msg) {
		if (dumpVdpAccess.getBoolean()) {
			PrintWriter pw = Logging.getLog(dumpFullInstructions);
			if (pw != null)
				pw.println("[VideoThread] " + msg);
		}
	}

	public void updateVideo() {
		if (videoRenderer.isIdle() && videoRenderer.isVisible()) {
			log("updating VDP canvas");
			videoRenderer.getCanvasHandler().update();
			
			// always queue in case of real-time video effects
			videoRenderer.queueRedraw();
		}
	}
}
