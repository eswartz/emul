/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.ImageImport;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;

/**
 * Support dragging image out of window, or into window (and VDP buffer)
 * @author ejs
 *
 */
public class SwtDragDropHandler implements DragSourceListener, DropTargetListener {

	private DragSource source;
	private DropTarget target;
	private final ISwtVideoRenderer renderer;
	private File tempSourceFile;
	private boolean dragSourceInProgress;
	private DisposeListener disposeListener;
	private final Control control;

	public SwtDragDropHandler(Control control, ISwtVideoRenderer renderer) {
		this.control = control;
		this.renderer = renderer;
		
		source = new DragSource(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		source.addDragListener(this);
		target = new DropTarget(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.addDropListener(this);
		
		if (System.getProperty("os.name").equals("Linux")) {
			source.setTransfer(new Transfer[] { FileTransfer.getInstance(), 
					//MyImageTransfer.getInstance() 
			});
			target.setTransfer(new Transfer[] { 
					FileTransfer.getInstance(), 
					//MyImageTransfer.getInstance() 
			});
		} else {
			source.setTransfer(new Transfer[] { 
					ImageTransfer.getInstance(), 
					FileTransfer.getInstance(), 
			});
			target.setTransfer(new Transfer[] { 
					ImageTransfer.getInstance(),
					FileTransfer.getInstance(), 
			});
		}
		
		tempSourceFile = null;
		
		disposeListener = new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
		control.addDisposeListener(disposeListener);
	}

	/**
	 * 
	 */
	public void dispose() {
		if (tempSourceFile != null) {
			tempSourceFile.delete();
			tempSourceFile = null;
		}
		if (!source.isDisposed()) {
			source.removeDragListener(this);
			source.dispose();
		}
		if (!target.isDisposed()) {
			target.removeDropListener(this);
			target.dispose();
		}
		if (disposeListener != null && !control.isDisposed()) {
			control.removeDisposeListener(disposeListener);
			disposeListener = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		// always allowed
		if (!dragSourceInProgress) {
			if (tempSourceFile != null) {
				tempSourceFile.delete();
				tempSourceFile = null;
			}
		}
		dragSourceInProgress = true;
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
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMDD-HHmmss");
				String name = MessageFormat.format("v9t9-{0}.png",
						fmt.format(new Date())); 
				tempSourceFile = new File(System.getProperty("java.io.tmpdir"), name);
				
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
			((ImageData) event.data).type = Transfer.registerType("image/bmp");
			
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
		dragSourceInProgress = false;
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragEnter(DropTargetEvent event) {
		// set DND.DROP_NONE if not supported
		
		Format format = renderer.getCanvas().getFormat();
		if (!ImageImport.isModeSupported(format)) {
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
				if (files == null)
					return;
				ImageLoader imgLoader = new ImageLoader();
				String file = files[0];
				try {
					ImageData[] datas = imgLoader.load(file);
					if (datas.length > 0) {
						ImageData data = datas[0];
						importImage(data);
					}
				} catch (SWTException e) {
					java.awt.Image img = ImageIO.read(new File(file));
					if (img != null)
						importImage(img, false);
					else
						MessageDialog.openError(null, "Failed To Import", 
								"Image format not recognized for '" +
								file + "' (tried both SWT and AWT)" );
				}
			}
			else if (ImageTransfer.getInstance().isSupportedType(event.currentDataType)) {
				importImage((ImageData) event.data);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * @param data
	 */
	private void importImage(ImageData data) {

		// convert to AWT image -- don't scale with SWT, which is lame
		BufferedImage img = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
		int[] pix = new int[data.width * data.height];

		for (int y = 0; y < data.height; y++) {
			int offs = data.width * y;
			data.getPixels(0, y, data.width, pix, offs);
		}
		if (!data.palette.isDirect) {
			// apply palette..
			for (int i = 0; i < pix.length; i++) {
				RGB rgb = data.palette.colors[pix[i]]; 
				pix[i] = (rgb.red << 16) | (rgb.green << 8) | (rgb.blue);
			}
		}
		else if (data.palette.blueShift != 0) {
			// assume it was BGR
			for (int i = 0; i < pix.length; i++) {
				int p = pix[i];
				int r = p & 0xff0000;
				int b = p & 0xff;
				pix[i] = (p & 0xff00) | (r >> 16) | (b << 16);
			}
		}
		
		// apply alpha
		for (int y = 0; y < data.height; y++) {
			int offs = data.width * y;
			
			if (data.alphaData != null) {
				for (int x = 0; x < data.width; x++)
					pix[offs + x] |= ((data.alphaData[offs + x] & 0xff) << 24);
			} else {
				int alpha = data.alpha != -1 ? data.alpha << 24 : 0xff000000;
				for (int x = 0; x < data.width; x++)
					pix[offs + x] |= alpha;
			}
		}
		
		img.setRGB(0, 0, data.width, data.height, pix, 0, pix.length / data.height);

		importImage(img, !data.palette.isDirect);
		
	}

	/**
	 * @param img
	 * @param isLowColor 
	 */
	protected void importImage(java.awt.Image img, boolean isLowColor) {
		ImageImport importer = new ImageImport(
				(ImageDataCanvas) renderer.getCanvas(), renderer.getVdpHandler());
		importer.importImage(img, isLowColor);
	}


}
