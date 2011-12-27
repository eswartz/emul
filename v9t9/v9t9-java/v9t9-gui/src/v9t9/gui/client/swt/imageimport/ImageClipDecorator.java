/**
 * 
 */
package v9t9.gui.client.swt.imageimport;

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

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * Decorator that supports adding an overlay clip rectangle to an ImageLabel and
 * reporting changes to a property.
 * 
 * @author ejs
 * 
 */
public class ImageClipDecorator implements PaintListener {

	/**
	 * Update the user-specified rectangle.
	 *
	 */
	public interface IBoundsUpdater {
		Rectangle update(Rectangle physClip);

	}

	private boolean dragging;
	private Rectangle physClip;
	private Point origDrag;
	private final ImageLabel imageLabel;
	private final IPropertyListener clipListener;
	private IPropertyListener clipPropertyListener;
	private final IProperty clipProperty;
	private final IBoundsUpdater boundsUpdater;

	/**
	 * @param parent
	 * @param style
	 */
	public ImageClipDecorator(ImageLabel imageLabel_, IProperty clipProperty_,
			IPropertyListener clipListener, IBoundsUpdater boundsUpdater) {
		this.imageLabel = imageLabel_;
		this.clipProperty = clipProperty_;
		this.clipListener = clipListener;
		this.boundsUpdater = boundsUpdater;
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
		
		//clipProperty = importOptions.createPropertySource().getProperty("clip");
		clipPropertyListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				java.awt.Rectangle curClip = (java.awt.Rectangle) property.getValue();
				if (curClip == null)
					setClip(null);
				else
					setClip(getDestClip(new Rectangle(curClip.x, curClip.y, curClip.width, curClip.height)));
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

	/**
	 * @param imageLabel2
	 * @param clipProperty2
	 * @param iPropertyListener
	 * @param iBoundsUpdater
	 */
	public ImageClipDecorator(ImageLabel imageLabel_, IProperty clipProperty_,
			IPropertyListener iPropertyListener) {
		this(imageLabel_, clipProperty_, iPropertyListener, null);
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
			
			if (boundsUpdater != null)
				physClip = boundsUpdater.update(physClip);
		}

		return setClip(physClip);
	}

	protected void publishClip(Rectangle clip) {
		if (clip == null) {
			//System.out.println("Publishing clip: " + clip);
			//importOptions.setClip(null);
			clipProperty.setString(null);
		} else {
			Rectangle clipScaled = getSourceClip(clip);
			//System.out.println("Publishing clip: " + clipScaled);
			clipProperty.setValue(new java.awt.Rectangle(clipScaled.x,
					clipScaled.y, clipScaled.width, clipScaled.height));
		}

		// is hidden property
		if (clipListener != null)
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
		
		if (image == null)
			return null;
		
		Rectangle imageBounds = image.getBounds();
		if (clip == null || imageBounds.isEmpty())
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


	/**
	 * Get the physical clip area in the widget from the given image-relative clip
	 */
	public Rectangle getDestClip(Rectangle clip) {
		Image image = imageLabel.getImage();
		
		if (image == null)
			return null;
		
		Rectangle imageBounds = image.getBounds();
		if (clip == null || imageBounds.isEmpty())
			return null;

		Rectangle imageClientArea = imageLabel.getBounds();
		
		Rectangle scaled = ImageUtils.scaleRectToSize(clip, 
				new Point(imageClientArea.width, imageClientArea.height),
				new Point(imageBounds.width, imageBounds.height)
		);
		if (scaled.x < 0)
			scaled.x = 0;
		if (scaled.y < 0)
			scaled.y = 0;
		if (scaled.width + scaled.x > imageClientArea.width)
			scaled.width = imageClientArea.width - scaled.x;
		if (scaled.height + scaled.y > imageClientArea.height)
			scaled.height = imageClientArea.height - scaled.y;
		
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
