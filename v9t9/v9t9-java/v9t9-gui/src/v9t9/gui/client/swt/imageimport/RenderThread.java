/*
  RenderThread.java

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
			vdpImporter.importImageToCanvas(datas[index]);
			if (datas.length == 1)
				break;
			
			try {
				Thread.sleep(Math.max(25, datas[index].delayMs));
			} catch (InterruptedException e) {
			}
			
			index = (index + 1) % datas.length;
		}
	}

}
