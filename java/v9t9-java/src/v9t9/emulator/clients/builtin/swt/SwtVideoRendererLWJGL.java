/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;

import v9t9.emulator.clients.builtin.video.VideoRenderer;


/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRendererLWJGL implements VideoRenderer {

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#redraw()
	 */
	@Override
	public void redraw() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#sync()
	 */
	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#getLastUpdateTime()
	 */
	@Override
	public long getLastUpdateTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#isIdle()
	 */
	@Override
	public boolean isIdle() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#setZoom(int)
	 */
	@Override
	public void setZoom(int zoom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#getZoom()
	 */
	@Override
	public int getZoom() {
		// TODO Auto-generated method stub
		return 0;
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#saveScreenShot(java.io.File)
	 */
	@Override
	public void saveScreenShot(File file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VideoRenderer#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}
