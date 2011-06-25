/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.ejs.coffee.core.utils.Pair;

/**
 * @author ejs
 *
 */
public class SVGImageProvider extends MultiImageSizeProvider {

	private final SVGLoader svgIcon;
	private Job loadIconJob;
	
	private Point desiredSize;
	private Image scaledImage;
	private final ImageBar buttonBar;
	private boolean svgFailed;
	
	/**
	 * @param iconMap
	 */
	public SVGImageProvider(TreeMap<Integer, Image> iconMap, ImageBar buttonBar, SVGLoader svgIcon) {
		super(iconMap);
		this.buttonBar = buttonBar;
		this.svgIcon = svgIcon;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.MultiImageSizeProvider#getImage(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public Pair<Double, Image> getImage(final Point size) {
		boolean recreate = false;
		if (scaledImage == null || !size.equals(desiredSize)) {
			/*if (loadIconJob != null) {
				loadIconJob.cancel();
				try {
					loadIconJob.join();
				} catch (InterruptedException e) {
				}
			}*/
			if (loadIconJob == null)
				recreate = true;
		}
		if (!svgFailed && recreate) {
			desiredSize = size;
			if (scaledImage != null)
				scaledImage.dispose();
			scaledImage = null;
			loadIconJob = new Job("Scaling icon") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final ImageData scaledImageData;
					try {
						//int min = iconMap.values().iterator().next().getBounds().width;
						Point scaledSize = new Point(size.x, size.y);
						Point svgSize = svgIcon.getSize();
						if (buttonBar.isHorizontal())
							scaledSize.y = size.y * svgSize.y / svgSize.x;
						else
							scaledSize.x = size.x * svgSize.x / svgSize.y;
						
						long start = System.currentTimeMillis();
						
						scaledImageData = svgIcon.getImageData(scaledSize);
						long end = System.currentTimeMillis();
						System.out.println("Loaded " + svgIcon.getFileName() + " @ " + scaledSize + ": " + (end - start) + " ms");
						svgFailed = false;
						
						if (!buttonBar.isDisposed()) {
							buttonBar.getDisplay().asyncExec(new Runnable() {
								public void run() {
									if (!buttonBar.isDisposed()) {
										scaledImage = new Image(buttonBar.getDisplay(), scaledImageData);
										System.out.println("Got image " + scaledImage.getBounds());
										buttonBar.redrawAll();
									}
								}
							});
						}
					} catch (CoreException e) {
						svgFailed = true;
					}
					loadIconJob = null;
					
					
					return Status.OK_STATUS;
				}
				
			};
			loadIconJob.schedule(100);
		}
		if (scaledImage == null) {
			return super.getImage(size);
		}
		else {
			int min = iconMap.values().iterator().next().getBounds().width;
			double ratio = (double) scaledImage.getBounds().width / min;
			//System.out.println("Using svg image " + scaledImage.getBounds() + " at " +ratio);
			return new Pair<Double, Image>(ratio, scaledImage);
		}
		
	}
}
