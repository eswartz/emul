/*
  RenderThread.java

  (c) 2012-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.imageimport;

import v9t9.video.imageimport.ImageImportData;

/**
 * @author ejs
 *
 */
public class RenderThread extends Thread {

	private VdpImageImporter vdpImporter;
	private ImageImportData[] datas;
	private volatile boolean cancelled;
	private int index;

	/**
	 * @param vdpImporter
	 * @param datas
	 */
	public RenderThread(VdpImageImporter vdpImporter, ImageImportData[] datas) {
		this.vdpImporter = vdpImporter;
		this.datas = datas;
		this.index = 0;
		setName("VDP Imported Image Renderer");
		setDaemon(true);
	}
	
	/**
	 * @param cancelled the cancelled to set
	 */
	public void cancel() {
		this.cancelled = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (datas.length == 0)
			return;
		while (!cancelled) {
			long now = System.currentTimeMillis();
			
			vdpImporter.importImageToCanvas(datas[index]);
			if (datas.length == 1)
				break;
			
			try {
				long ms = datas[index].delayMs - (System.currentTimeMillis() - now);
				if (ms > 0)
					Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
			
			index = (index + 1) % datas.length;
		}
	}

}
