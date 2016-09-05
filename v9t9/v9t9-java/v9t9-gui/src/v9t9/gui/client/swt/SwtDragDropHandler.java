/*
  SwtDragDropHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
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

import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.video.ICanvas;
import v9t9.gui.client.swt.imageimport.IImageImportHandler;
import v9t9.video.ImageDataCanvas;
import v9t9.video.common.ImageUtils;
import v9t9.video.imageimport.ImageFrame;
import v9t9.video.svg.SVGException;
import v9t9.video.svg.SVGSalamanderLoader;
import ejs.base.properties.IPropertyListener;

/**
 * Support dragging image out of window, or into window (and VDP buffer)
 * @author ejs
 *
 */
public class SwtDragDropHandler implements DragSourceListener, DropTargetListener {

	private static final Logger log = Logger.getLogger(SwtDragDropHandler.class);
	
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
	private final IImageImportHandler imageImportHandler;

	private IEmulatorContentSourceProvider[] emulatorContentProviders;

	private IMachine machine;

	public SwtDragDropHandler(Control control, ISwtVideoRenderer renderer,
			IMachine machine, IImageImportHandler imageImportHandler) {
		this.machine = machine;
		this.imageImportHandler = imageImportHandler;
		this.notifier = machine.getEventNotifier();
		this.emulatorContentProviders = machine.getEmulatorContentProviders(); 
		if (renderer == null || control == null || notifier == null)
			throw new NullPointerException();
		
		this.control = control;
		this.renderer = renderer;
		
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
		
//		VdpFormat format = renderer.getCanvas().getFormat();
//		if (!ImageImport.isModeSupported(format)) {
//			System.out.println("Unsupported format: " + format);
//			event.detail = DND.DROP_NONE;
//			return;
//		}
		
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
		// gross:  we only know the content now 
		
		boolean didImage = false;
		try {
			didImage = tryLoadImage(event);
		} catch (NotifyException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException t) {
			notifier.notifyEvent(null, Level.WARNING, t.getMessage());
			return;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		if (!didImage) {
			try {
				tryLoadFile(event);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private File getDraggedFile(DropTargetEvent event, String pattern) throws IOException {
		File file = null;

		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String[] files = (String[]) event.data;
			if (files != null) {
				String path = files[0];
				log.debug("dropped file: " + path);
				if (!path.matches(pattern)) {
					log.debug("ignoring because doesn't match pattern");
					return null;
				}
				file = new File(path);
				return file;
			}
		}
		if (URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String[] entries = ((String) event.data).split("\n");
			String trimmed = null;
			for (String entry : entries) {
				trimmed = entry.replaceAll("\u00A0", "").trim();
				break;
			}
			
			if (!trimmed.equals(lastURL)) {
				if (lastURLFile != null)
					lastURLFile.delete();
				
				log.debug("dropped URL: " + trimmed);
				if (!trimmed.matches(pattern) && !trimmed.endsWith("/")) {
					log.debug("ignoring because doesn't match pattern");
					return null;
				}
				
				File temp = null;
				int dotIdx = trimmed.lastIndexOf('/');
				if (dotIdx >= 0)
					temp = new File(System.getProperty("java.io.tmpdir"), trimmed.substring(dotIdx+1));
				else
					temp = File.createTempFile("url", ".img");
				
				log.debug("copying file to " + temp);
				URL url = new URL(trimmed);
				lastURLFile = readFileFromURL(temp, url);
				if (lastURLFile != null) {
					lastURL = trimmed;
					return lastURLFile;
				}
				else {
					lastURLFile = null;
					temp.delete();
					return null;
				}
			}
			return lastURLFile;
		}
		return null;
	}
	/**
	 * @param event
	 * @throws IOException 
	 */
	private void tryLoadFile(DropTargetEvent event) throws IOException {

		File file = getDraggedFile(event, ".*");
		if (file == null)
			return;
		
		List<IEmulatorContentSource> sources = new ArrayList<IEmulatorContentSource>();
		for (IEmulatorContentSourceProvider provider : emulatorContentProviders) {
			try {
				IEmulatorContentSource[] sourceArr = provider.analyze(file.toURI());
				sources.addAll(Arrays.asList(sourceArr));
			} catch (Throwable t) {
				log.error("error scanning " + file + " with " + provider, t);
			}
		}
		
		if (sources.isEmpty()) {
			MessageDialog.openWarning(control.getShell(), "Unknown File", 
					"V9t9 does not recognize this file:\n\n" + file);			
			return;
		}
		
		// see if we can skip the dialog
		boolean anyFound = false;
		for (IEmulatorContentSource source : sources) {
			IEmulatorContentHandler[] handlers = machine.getClient().getEmulatorContentHandlers(source);
			if (handlers.length > 0) {
				anyFound = true;
				continue;
			}
			
			if (handlers.length == 1 && sources.size() == 1) {
				IEmulatorContentHandler handler = handlers[0];
				if (!handler.requireConfirmation()) {
					try {
						handler.handleContent();
					} catch (NotifyException e) {
						notifier.notifyEvent(e.getEvent());
					}
					return;
				}
			}
		}
		
		if (!anyFound) {
			MessageDialog.openWarning(control.getShell(), "Unhandled File", 
					"V9t9 does not know what to do with " + sources.get(0).getLabel());		
			return;
		}
		
		// okay, show a dialog
		
		EmulatorSourceHandlerDialog dialog = new EmulatorSourceHandlerDialog(
				control.getShell(), machine.getClient(),
				sources.toArray(new IEmulatorContentSource[sources.size()]));
		
		int ret = dialog.open();
		if (ret == Window.OK) {
			IEmulatorContentHandler handler = dialog.getHandler();
			if (handler != null) {
				try {
					handler.handleContent();
				} catch (NotifyException e) {
					notifier.notifyEvent(e.getEvent());
				}
			}
		}
	}

	/**
	 * @param event
	 * @throws NotifyException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	protected boolean tryLoadImage(DropTargetEvent event) throws NotifyException,
			IOException, MalformedURLException {
		ImageFrame[] frames = null;

		if (ImageTransfer.getInstance().isSupportedType(event.currentDataType)) {
			frames = ImageUtils.convertToBufferedImages(new ImageData[] { (ImageData) event.data });
		} else {
			File file = getDraggedFile(event, "(?i).*\\.(gif|png|bmp|jpeg|jpg|svg|svgt|img)");
			if (file == null)
				return false;
			
			String theFile = file.getAbsolutePath();
			frames = ImageUtils.loadImageFromFile(theFile);
			imageImportHandler.getHistory().add(theFile);

		}
		if (frames != null) {

			imageImportHandler.importImage(frames);
			
			renderer.setFocus();
			
			return true;
		}
		
		return false;
	}

	private File readFileFromURL(File temp, URL url) {
		log.info("Loading " + url + " into " + temp);

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

}
