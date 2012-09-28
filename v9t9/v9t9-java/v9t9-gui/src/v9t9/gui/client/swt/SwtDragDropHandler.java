/**
 * 
 */
package v9t9.gui.client.swt;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

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
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;

import ejs.base.properties.IPropertyListener;

import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.video.ICanvas;
import v9t9.common.video.VdpFormat;
import v9t9.gui.client.swt.imageimport.IImageImportHandler;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import v9t9.gui.client.swt.svg.SVGException;
import v9t9.gui.client.swt.svg.SVGSalamanderLoader;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.ImageFrame;
import v9t9.video.imageimport.ImageImport;

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
	private File lastURLFile;
	private String lastURL;
	private final IEventNotifier notifier;
	protected IPropertyListener importPropertyListener;
	private final IImageImportHandler importHandler;

	public SwtDragDropHandler(Control control, ISwtVideoRenderer renderer, 
			IEventNotifier notifier, IImageImportHandler importHandler) {
		this.importHandler = importHandler;
		if (renderer == null || control == null || notifier == null)
			throw new NullPointerException();
		
		this.control = control;
		this.renderer = renderer;
		this.notifier = notifier;
		
		//source = new DragSource(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		//source.addDragListener(this);
		target = new DropTarget(control, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.addDropListener(this);
		
		if (System.getProperty("os.name").equals("Linux")) {
			if (source != null)
				source.setTransfer(new Transfer[] { 
						FileTransfer.getInstance(), 
						ImageTransfer.getInstance(),
						URLTransfer.getInstance(), 
				});
			target.setTransfer(new Transfer[] {
					FileTransfer.getInstance(), 
					URLTransfer.getInstance(),
			});
		} else {
			if (source != null)
				source.setTransfer(new Transfer[] { 
						ImageTransfer.getInstance(), 
						FileTransfer.getInstance(), 
						URLTransfer.getInstance(), 
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
		if (source != null && !source.isDisposed()) {
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
		
		if (FileTransfer.getInstance().isSupportedType(event.dataType) ||
			URLTransfer.getInstance().isSupportedType(event.dataType)) { 
			// apparently the event comes twice...
			if (tempSourceFile == null) {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
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
			if (URLTransfer.getInstance().isSupportedType(event.dataType)) {
				try {
					event.data = tempSourceFile.toURI().toURL().toString();
				} catch (MalformedURLException e) {
					e.printStackTrace();
					event.doit = false;
				}
			} else {
				event.data = new String[] { tempSourceFile.getAbsolutePath() };
			}
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
		ICanvas vc = renderer.getCanvas();
		ImageDataCanvas idc = (ImageDataCanvas) vc;
		ImageData data = (ImageData) idc.getImageData().clone();
		
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
		int xoffs = idc.getXOffset();
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
		
		VdpFormat format = renderer.getCanvas().getFormat();
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
		if (dragSourceInProgress) {
			event.detail = DND.DROP_NONE;
			dragSourceInProgress = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void drop(DropTargetEvent event) {
		try {
			ImageFrame[] frames = null;
			
			if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
				String[] files = (String[]) event.data;
				if (files != null) {
					frames = loadImageFromFile(notifier, files[0]);
					importHandler.getHistory().add(files[0]);
				}
			}
			if (frames == null && ImageTransfer.getInstance().isSupportedType(event.currentDataType)) {
				frames = ImageUtils.convertToBufferedImages(new ImageData[] { (ImageData) event.data });
			}
			if (frames == null && URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
				
				String[] entries = ((String) event.data).split("\n");
				String trimmed = null;
				for (String entry : entries) {
					trimmed = entry.replaceAll("\u00A0", "").trim();
					importHandler.getHistory().add(trimmed);
					break;
				}
				
				if (!trimmed.equals(lastURL)) {
					if (lastURLFile != null)
						lastURLFile.delete();
					File temp = File.createTempFile("url", ".img");
					URL url = new URL(trimmed);
					lastURLFile = readImageFromURL(temp, url);
					if (lastURLFile != null)
						lastURL = trimmed;
					else {
						lastURLFile = null;
						temp.delete();
					}
				}
				if (lastURLFile != null) {
					frames = loadImageFromFile(notifier, lastURLFile.getAbsolutePath());
				}
			}
			
			if (frames != null) {

				importHandler.importImage(frames);
				
				renderer.setFocus();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private File readImageFromURL(File temp, URL url) {
		System.out.println("Loading " + url + " into " + temp);

		InputStream is = null;
		try {
			is = url.openStream();
			OutputStream os = null;
			try {
				os = new BufferedOutputStream(new FileOutputStream(temp));
				byte[] buf = new byte[32768];
				int len;
				while ((len = is.read(buf)) != -1) {
					os.write(buf, 0, len);
				}
			} catch (IOException e) {
				notifier.notifyEvent(null, Level.ERROR, 
						"Could not read '" +
						url + "' to '" + temp + "' (" + e.getMessage() + ")" );
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			notifier.notifyEvent(null, Level.ERROR, 
					"Could not load '" +
					url + "' (" + e.getMessage() + ")" );
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return temp;
	}

	public static ImageFrame[] loadImageFromFile(IEventNotifier notifier, String file) {
		ImageFrame[] info = null;
		URL url;
		try {
			url = new File(file).toURI().toURL();
		} catch (MalformedURLException e4) {
			return null;
		}
		try {
			ImageLoader imgLoader = new ImageLoader();
			ImageData[] datas = imgLoader.load(file);
			if (datas.length > 0) {
				info = ImageUtils.convertToBufferedImages(datas);
			}
		} catch (SWTException e) {
			BufferedImage img;
			try {
				img = ImageIO.read(url);
				if (img != null)
					info = new ImageFrame[] { new ImageFrame(img, true) };
			} catch (Throwable e1) {
				// IOException or IllegalArgumentException from broken file
			}
		}

		if (info == null) {
			SVGSalamanderLoader loader = new SVGSalamanderLoader(url);
			try {
				BufferedImage img = loader.getImageData(loader.getSize());
				if (false) {
					//BufferedImage img = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
					info = new ImageFrame[] { new ImageFrame(img, true) };
				} else {
					// hmm... something about the AWT-ness makes it impossible to clip properly
					ImageData data = ImageUtils.convertAwtImageData(img);
					info = ImageUtils.convertToBufferedImages(new ImageData[] { data });
				}
			} catch (SVGException e2) {
				notifier.notifyEvent(null, Level.ERROR, 
						"Could not load '" +
								file + "' (" + e2.getMessage() + ")" );
				return null;
			}
		}
		if (info == null)
			notifier.notifyEvent(null, Level.ERROR, 
					"Image format not recognized for '" +
					file + "'" );

		return info;
	}

}
