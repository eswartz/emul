/**
 * 
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
				Thread.sleep(datas[index].delayMs);
			} catch (InterruptedException e) {
			}
			
			index = (index + 1) % datas.length;
		}
	}

}
