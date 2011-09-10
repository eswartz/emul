/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
import v9t9.engine.VdpHandler;

/**
 * Support dragging image out of window, or into window (and VDP buffer)
 * @author ejs
 *
 */
public class SwtDragDropHandler implements DragSourceListener, DropTargetListener {

	private DragSource source;
	private DropTarget target;
	private final ISwtVideoRenderer renderer;
	private final VdpHandler vdpHandler;
	private File tempSourceFile;

	/**
	 * @param videoControl
	 * @param renderer
	 * @param vdpHandler
	 */
	public SwtDragDropHandler(Control control, ISwtVideoRenderer renderer,
			VdpHandler vdpHandler) {
		this.renderer = renderer;
		this.vdpHandler = vdpHandler;
		
		source = new DragSource(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		source.addDragListener(this);
		source.setTransfer(new Transfer[] { FileTransfer.getInstance() /*, ImageTransfer.getInstance()*/ });
		
		target = new DropTarget(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.addDropListener(this);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance(), ImageTransfer.getInstance() });
		
		tempSourceFile = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		// always allowed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		ImageData visData;
		try {
			visData = readImage();
		} catch (Throwable t) {
			t.printStackTrace();
			event.doit = false;
			return;
		}
		
		if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
			// apparently the event comes twice...
			if (tempSourceFile == null) {
				try {
					tempSourceFile = File.createTempFile("v9t9", ".png");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				ImageLoader imgLoader = new ImageLoader();
				try {
					imgLoader.data = new ImageData[] { visData };
					imgLoader.save(tempSourceFile.getAbsolutePath(), SWT.IMAGE_PNG);
				} catch (SWTException e) {
					e.printStackTrace();
					return;
				}
			}
			
			//System.out.println("sending file: " + tempSourceFile);
			
			event.data = new String[] { tempSourceFile.getAbsolutePath() };
		}
		else if (ImageTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = visData;
			((ImageData) event.data).type = Transfer.registerType("image/png");
			
		}
			
	}

	/**
	 * @return
	 */
	private ImageData readImage() {
		VdpCanvas vc = renderer.getCanvas();
		ImageDataCanvas idc = (ImageDataCanvas) vc;
		ImageData data = idc.getImageData();
		
		// clip to visible size
		int visWidth = vc.getVisibleWidth();
		int visHeight = vc.getVisibleHeight();
		
		boolean dblWide = false;
		int ystep = 1;
		if (visWidth > 256 && visHeight <= 212) {
			dblWide = true;
			visHeight *= 2;
			ystep = 2;
		}
		
		ImageData visData = new ImageData(visWidth, visHeight, data.depth, data.palette);
		int[] pix = new int[visWidth];
		int xoffs = idc.getXOffset() + idc.getExtraSpace() / 2;
		int yoffs = idc.getYOffset();
		
		for (int y = 0; y < visHeight; y += ystep) {
			data.getPixels(xoffs, yoffs + y / ystep, visWidth, pix, 0);
			visData.setPixels(0, y, visWidth, pix, 0);
			if (dblWide) {
				visData.setPixels(0, y + 1, visWidth, pix, 0);
				
			}
		}
		return visData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
		if (tempSourceFile != null) {
			tempSourceFile.delete();
			tempSourceFile = null;
		}
			
		
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragEnter(DropTargetEvent event) {
		// set DND.DROP_NONE if not supported
		if (!(renderer.getCanvas() instanceof IBitmapPixelAccess)) {
			System.out.println("Not a bitmap");
			event.detail = DND.DROP_NONE;
			return;
		}
		
		Format format = renderer.getCanvas().getFormat();
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8) {
			System.out.println("Unsupported format: " + format);
			event.detail = DND.DROP_NONE;
			return;
		}
		
		event.detail = DND.DROP_COPY;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragOver(DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_SELECT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragOperationChanged(DropTargetEvent event) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragLeave(DropTargetEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void drop(DropTargetEvent event) {
		try {
			if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
				String[] files = (String[]) event.data;
				ImageLoader imgLoader = new ImageLoader();
				ImageData[] datas = imgLoader.load(files[0]);
				if (datas.length > 0) {
					ImageData data = datas[0];
					importImage(data);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * @param data
	 */
	private void importImage(ImageData data) {
		// scale aspect-sensitively
		ImageDataCanvas vc = (ImageDataCanvas) renderer.getCanvas();
		
		int targWidth = vc.getVisibleWidth();
		int targHeight = vc.getVisibleHeight();
		float aspect = targWidth * targHeight / 256.f  / 192.f;
		if (renderer.getCanvas().getFormat() == Format.COLOR16_4x4) {
			targWidth = 64;
			targHeight = 48;
			aspect = 1.0f;
		}
		int realWidth = data.width;
		int realHeight = data.height;
		if (realWidth < 0 || realHeight < 0) {
			return;
		}
		
		if (realWidth != targWidth && realHeight != targHeight) {
			if (realWidth * targHeight * aspect > realHeight * targWidth) {
				targHeight = (int) (targWidth * realHeight / realWidth / aspect);
			} else {
				targWidth = (int) (targHeight * realWidth * aspect / realHeight);
			}
		}
		
		ImageData scaled = data. scaledTo(targWidth, targHeight);
		
		BufferedImage img = new BufferedImage(targWidth, targHeight, BufferedImage.TYPE_INT_ARGB);
		int[] pix = new int[scaled.width * scaled.height];

		for (int y = 0; y < targHeight; y++) {
			scaled.getPixels(0, y, targWidth, pix, targWidth * y);
		}
		if (!scaled.palette.isDirect) {
			// apply palette... wtf
			for (int i = 0; i < pix.length; i++) {
				RGB rgb = scaled.palette.colors[pix[i]]; 
				pix[i] = 0xff000000 | (rgb.red << 16) | (rgb.green << 8) | (rgb.blue);
			}
		}
		else if (scaled.palette.blueShift != 0) {
			// assume it was BGR
			for (int i = 0; i < pix.length; i++) {
				int p = pix[i];
				int r = p & 0xff0000;
				int b = p & 0xff;
				pix[i] = (p & 0xff00) | (r >> 16) | (b << 16);
			}
		}
		
		img.setRGB(0, 0, targWidth, targHeight, pix, 0, pix.length / targHeight);
		vc.setImageData(img);
		
		synchronized (vdpHandler) {
			IBitmapPixelAccess access = (IBitmapPixelAccess) renderer.getCanvas();
			vdpHandler.getVdpModeRedrawHandler().importImageData(access);
		}
		
		//renderer.getAwtCanvas().repaint();
	}


}
