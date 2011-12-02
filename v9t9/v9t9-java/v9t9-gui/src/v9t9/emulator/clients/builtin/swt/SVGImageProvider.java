/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.TreeMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.ejs.coffee.core.utils.Pair;


/**
 * @author ejs
 *
 */
public class SVGImageProvider extends MultiImageSizeProvider {

	private final ISVGLoader svgLoader;
	private Thread loadIconThread;
	
	private Point desiredSize;
	private Image scaledImage;
	private boolean svgFailed;
	private IImageBar imageBar;
	
	/**
	 * @param iconMap
	 */
	public SVGImageProvider(TreeMap<Integer, Image> iconMap, ISVGLoader svgIcon) {
		super(iconMap);
		this.svgLoader = svgIcon;
	}

	public void setImageBar(IImageBar imageBar) {
		this.imageBar = imageBar;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.MultiImageSizeProvider#getImage(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public synchronized Pair<Double, Image> getImage(final int sx, final int sy) {
		boolean recreate = false;
		final Point size = new Point(sx, sy);
		if (scaledImage == null || !size.equals(desiredSize)) {
			if (loadIconThread == null || !svgLoader.isSlow()) {
				recreate = true;
			}
		}
		if (!svgFailed && recreate) {
			desiredSize = size;
			if (scaledImage != null)
				scaledImage.dispose();
			scaledImage = null;
			
			if (svgLoader.isSlow()) {
				loadIconThread = new Thread("Scaling icon") {

					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							return;
						}
						fetchImage(desiredSize);
						loadIconThread = null;
					}
					
				};
				
				loadIconThread.start();
			}
			else {
				fetchImage(size);
			}
		}
		if (scaledImage == null) {
			return super.getImage(sx, sy);
		}
		else {
			int min = iconMap.values().iterator().next().getBounds().width;
			double ratio = (double) scaledImage.getBounds().width / min;
			//System.out.println("Using svg image " + scaledImage.getBounds() + " at " +ratio);
			return new Pair<Double, Image>(ratio, scaledImage);
		}
		
	}

	/**
	 * @param size
	 */
	protected void fetchImage(final Point size) {
		final ImageData scaledImageData;
		if (!svgLoader.isValid()) {
			svgFailed = true;
			return;
		}
		
		try {
			//int min = iconMap.values().iterator().next().getBounds().width;
			Point scaledSize = new Point(size.x, size.y);
			Point svgSize = svgLoader.getSize();
			scaledSize.y = size.y * svgSize.y / svgSize.x;
			
			long start = System.currentTimeMillis();
			
			scaledImageData = svgLoader.getImageData(scaledSize);
			long end = System.currentTimeMillis();
			System.out.println("Loaded " + svgLoader.getURI() + " @ " + scaledSize + ": " + (end - start) + " ms");
			svgFailed = false;
			
			final Composite composite = imageBar.getComposite();
			
			if (composite != null && !composite.isDisposed() && scaledImageData != null) {
				Runnable runnable = new Runnable() {
					public void run() {
						if (!composite.isDisposed()) {
							scaledImage = new Image(composite.getDisplay(), scaledImageData);
							System.out.println("Got image " + scaledImage.getBounds());
							imageBar.redrawAll();
						}
					}
				};
				if (svgLoader.isSlow())
					composite.getDisplay().asyncExec(runnable);
				else
					composite.getDisplay().syncExec(runnable);
			}
		} catch (SVGException e) {
			e.printStackTrace();
			svgFailed = true;
		}
	}
}
