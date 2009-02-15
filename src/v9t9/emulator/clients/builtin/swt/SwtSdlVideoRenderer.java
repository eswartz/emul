/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import sdljava.SDLException;
import v9t9.emulator.clients.builtin.sdl.SdlVideoRenderer;

/**
 * @author Ed
 *
 */
public class SwtSdlVideoRenderer extends SdlVideoRenderer implements ISwtVideoRenderer {

	private Canvas sdlContainer;

	public SwtSdlVideoRenderer() throws SDLException {
		super();
	}

	public Control createControl(Composite parent, int flags) {
		//shell = parent.getShell();
		sdlContainer = new Canvas(parent, SWT.EMBEDDED | SWT.NO_MERGE_PAINTS | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		
		// no layout -- let canvas size drive it
		//frame.setLayout(new FlowLayout());
		sdlContainer.setLayout(new Layout() {

			@Override
			protected Point computeSize(Composite composite, int hint,
					int hint2, boolean flushCache) {
				if (surface != null)
					return new Point(surface.getWidth(), surface.getHeight());
				else
					return new Point(256, 192);
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Point mySize = composite.getSize();
				desiredWidth = mySize.x;
				desiredHeight = mySize.y;
				try {
					getRenderingSurface();
				} catch (SDLException e) {
					e.printStackTrace();
				}
			}
			
		});
		sdlContainer.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());

		// TODO: actually reparent the SDL window into this canvas...
		// or wire up a way for us to have a native bridge to do it
		
		return sdlContainer;
	}

}
