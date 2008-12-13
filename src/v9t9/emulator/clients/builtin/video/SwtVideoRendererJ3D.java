/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Node;
import javax.media.j3d.PointArray;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRendererJ3D extends SwtVideoRenderer {
	
	private Frame jframe;
	private Canvas3D canvas3d;
	private SimpleUniverse u;
	private PointArray screenPointsArray;
	private BranchGroup sceneRoot;

	public SwtVideoRendererJ3D() {
		super();
		
	}
	
	protected VdpCanvas createCanvas() {
		return new ImageDataCanvas24Bit();
	}

	// get a nice graphics config
    private static GraphicsConfiguration getGraphicsConfig() {
    	return SimpleUniverse.getPreferredConfiguration();
    }
    
	protected void initWidgets() {
		super.initWidgets();
		
		
	}
	
	@Override
	protected int getStyleBits() {
		return SWT.EMBEDDED + SWT.NO_BACKGROUND;
	}
	@Override
	protected void resizeWidgets() {
		if (!canvas.isVisible())
			return;
		
		if (canvas3d == null) {
			jframe = SWT_AWT.new_Frame(canvas);
			canvas3d = new Canvas3D(getGraphicsConfig()) {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					doUpdate();
				}
			};
			jframe.addWindowListener(new WindowAdapter() {
		            public void windowClosing(WindowEvent e) {
		                System.exit(0);
		            }
		        });

			jframe.add(canvas3d);
			
			
			
			// Create a basic universe setup and the root of our scene
			u = new SimpleUniverse(canvas3d);
			sceneRoot = new BranchGroup();
	
			sceneRoot.setCapability(BranchGroup.ALLOW_DETACH);
			screenPointsArray = new PointArray(49152, PointArray.COORDINATES | PointArray.COLOR_3);
			
			int idx = 0;
			for (idx = 0; idx < 49152; idx++) {
				screenPointsArray.setCoordinate(idx, 
						new float[] { (idx%256) / 256.0f, (idx / 256) / 192.0f, 0 });
				screenPointsArray.setColor(idx, new byte[] { (byte) (idx%32), (byte) idx, (byte) (idx+33) });
			}
			screenPointsArray.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
			
			Shape3D screenPoints = new Shape3D(screenPointsArray);
			
			sceneRoot.addChild(screenPoints);
			// Change the back clip distance; the default is small for
			// some lw3d worlds
			//View theView = u.getViewer().getView();
			//theView.setBackClipDistance(50000f);
			u.addBranchGraph(sceneRoot);
			
			
			 ViewingPlatform viewingPlatform = u.getViewingPlatform();
		        // This will move the ViewPlatform back a bit so the
		        // objects in the scene can be viewed.
		        viewingPlatform.setNominalViewingTransform();
		}		
		super.resizeWidgets();
		
	}

	@Override
	protected void doRepaint(GC gc, Rectangle updateRect) {
		if (canvas3d == null)
			return;
		
		doUpdate();
		//canvas3d.repaint();
	}
	
	protected void doUpdate() {
		final ImageData imageData = ((ImageDataCanvas) vdpCanvas).getImageData();
		if (imageData != null) {
			
			//Point canvasSize = canvas.getSize();
			
			synchronized (vdpCanvas) {
				//canvas3d.setSize(canvasSize.x, canvasSize.y);
				
				//sceneRoot.detach();
				
				int idx = 0;
				for (idx = 0; idx < 49152; idx++) {
					screenPointsArray.setColor(idx, new byte[] { imageData.data[idx*3],
							imageData.data[idx*3+1],
							imageData.data[idx*3+2] });
				}
				//sceneRoot.compile();
				//u.addBranchGraph(sceneRoot);
				canvas3d.repaint();
			}
		}
	}

}
