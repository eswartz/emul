/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;

import v9t9.emulator.clients.builtin.video.image.ImageImportOptions;

/**
 * Decorator that supports adding an overlay clip rectangle to an ImageLabel and
 * reporting changes to a property.
 * 
 * @author ejs
 * 
 */
public class ImageClipDecorator implements PaintListener {

	private boolean dragging;
	private Rectangle physClip;
	private Point origDrag;
	private final ImageLabel imageLabel;
	private final ImageImportOptions importOptions;
	private final IPropertyListener clipListener;
	private IProperty clipProperty;
	private IPropertyListener clipPropertyListener;

	/**
	 * @param parent
	 * @param style
	 */
	public ImageClipDecorator(ImageLabel imageLabel_,
			final ImageImportOptions importOptions, IPropertyListener clipListener) {
		this.imageLabel = imageLabel_;
		this.importOptions = importOptions;
		this.clipListener = clipListener;
		dragging = false;

		imageLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				if (dragging && e.button == 1) {
					Rectangle clip = updateClip(e.x, e.y);
					publishClip(clip);

					dragging = false;
					origDrag = null;
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (!dragging && e.button == 1) {
					physClip = null;
					updateClip(e.x, e.y);
					imageLabel.redraw();
					dragging = true;
				} else if (!dragging) {
					setClip(null);
					publishClip(null);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {

			}
		});

		imageLabel.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (dragging && (e.stateMask & SWT.BUTTON1) != 0) {
					updateClip(e.x, e.y);
				}
			}
		});

		imageLabel.addPaintListener(this);
		
		clipProperty = importOptions.createPropertySource().getProperty("clip");
		clipPropertyListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				java.awt.Rectangle curClip = importOptions.getClip();
				if (curClip == null)
					setClip(null);
				else
					setClip(new Rectangle(curClip.x, curClip.y, curClip.width, curClip.height));
				imageLabel.redraw();
			}
		};
		clipProperty.addListener(clipPropertyListener);
		
		imageLabel.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				clipProperty.removeListener(clipPropertyListener);
			}
		});
	}

	protected Rectangle updateClip(int mx, int my) {
		if (physClip == null) {
			origDrag = new Point(mx, my);
			physClip = new Rectangle(mx, my, 0, 0);
		} else {
			int x, y, sx, sy;
			if (origDrag.x < mx) {
				x = origDrag.x;
				sx = mx - x;
			} else {
				x = mx;
				sx = origDrag.x - x;
			}
			if (origDrag.y < my) {
				y = origDrag.y;
				sy = my - y;
			} else {
				y = my;
				sy = origDrag.y - y;
			}
			physClip = new Rectangle(x, y, sx, sy);
		}

		return setClip(physClip);
	}

	protected void publishClip(Rectangle clip) {
		if (clip == null) {
			//System.out.println("Publishing clip: " + clip);
			importOptions.setClip(null);
		} else {
			Rectangle clipScaled = getSourceClip(clip);
			//System.out.println("Publishing clip: " + clipScaled);
			importOptions.setClip(new java.awt.Rectangle(clipScaled.x,
					clipScaled.y, clipScaled.width, clipScaled.height));
		}

		clipListener.propertyChanged(null);

	}

	/**
	 * Set the logical clip area in the widget
	 * 
	 * @return
	 */
	public Rectangle setClip(Rectangle clip) {
		imageLabel.setClip(clip);
		imageLabel.redraw();
		return clip;
	}

	/**
	 * Get the logical clip area in the current image from the given
	 * widget-relative clip
	 */
	public Rectangle getSourceClip(Rectangle clip) {
		Image image = imageLabel.getImage();
		
		Rectangle imageBounds = image.getBounds();
		if (clip == null || image == null || imageBounds.isEmpty())
			return null;

		Rectangle imageClientArea = imageLabel.getBounds();
		
		Point imageSize = ImageUtils.scaleSizeToSize(
				new Point(imageBounds.width, imageBounds.height),
				new Point(imageClientArea.width, imageClientArea.height));
						
		Rectangle scaled = ImageUtils.scaleRectToSize(clip, 
				new Point(imageBounds.width, imageBounds.height),
				imageSize
		);
		if (scaled.x < 0)
			scaled.x = 0;
		if (scaled.y < 0)
			scaled.y = 0;
		if (scaled.width + scaled.x > imageBounds.width)
			scaled.width = imageBounds.width - scaled.x;
		if (scaled.height + scaled.y > imageBounds.height)
			scaled.height = imageBounds.height - scaled.y;
		
		return scaled;
	}

	@Override
	public void paintControl(PaintEvent e) {
		if (imageLabel.getImage() != null) {
			Rectangle rbounds = imageLabel.getClientArea();

			Rectangle clip = imageLabel.getClip();
			if (clip != null) {
				Region r = new Region();
				r.add(rbounds);
				r.subtract(clip);

				e.gc.setForeground(imageLabel.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
				e.gc.drawRectangle(clip);
				e.gc.setAlpha(128);
				e.gc.setBackground(imageLabel.getDisplay().getSystemColor(
						SWT.COLOR_BLACK));
				e.gc.setClipping(r);
				e.gc.fillRectangle(rbounds);
			}
		}
	}

}
