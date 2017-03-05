/*
  SwtImageImportSupport.java

  (c) 2011-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.imageimport;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.IEventNotifier;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtDragDropHandler;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.ImageImportOptionsDialog;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportData;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class SwtImageImportSupport extends ImageImportHandler {
	protected IVideoRenderer videoRenderer;
	protected IEventNotifier eventNotifier;
	private Control imageDndControl;
	private IPropertyListener importPropertyListener;
	private IMachine machine;
	private Thread importJob;
	private boolean importAgain;
	private ImageImportOptionsDialog dialog;
	public SwtImageImportSupport(IMachine machine, IEventNotifier eventNotifier, IVideoRenderer videoRenderer) {
		this.machine = machine;
		if (eventNotifier == null || videoRenderer == null)
			throw new NullPointerException();
		
		this.eventNotifier = eventNotifier;
		this.videoRenderer = videoRenderer;
	}
	/**
	 * @param window 
	 * @param shell2
	 * @return
	 */
	public ImageImportOptionsDialog getImageImportDialog(Shell shell, SwtWindow window) {
		if (dialog == null || dialog.isDisposed()) {
			dialog = new ImageImportOptionsDialog(shell, SWT.NONE, window, 
					this, importPropertyListener);
		}
		return dialog;
	}

	public void setImageImportDnDControl(final Control control) {
		this.imageDndControl = control;
		if (getVideoRenderer() != null) {
			importPropertyListener = new IPropertyListener() {
				@Override
				public void propertyChanged(IProperty property) {
					if (property == null || !property.isHidden()) {
						scheduleImportJob();
					}
				}
			};

			// TODO: this is general DnD assignment -- not just images like the owner
			/*imageDragDropHandler =*/ new SwtDragDropHandler(imageDndControl, 
					(ISwtVideoRenderer) getVideoRenderer(), 
					machine,
					this);
		}
	}
	
	protected synchronized void scheduleImportJob() {
		if (importJob == null) {
			importJob = new Thread() {
				public void run() {
					// in case, e.g., mode changed
					ImageImport importer = createImageImport();
					stopRendering();
					try {
						markBusy(true);
						ImageImportData[] datas = importImage(importer);
						displayImages(datas);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						markBusy(false);
					}
					synchronized (SwtImageImportSupport.this) {
						importJob = null;
						if (!interrupted() && importAgain) {
							importAgain = false;
							scheduleImportJob();
						}
					}
				}
			};
			importJob.start();
		} else {
			importAgain = true;
		}
	}
	
	/**
	 * @param busy
	 */
	protected void markBusy(final boolean busy) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Cursor cursor = busy ? Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT) : null;
				if (dialog != null && !dialog.isDisposed())
					dialog.setCursor(cursor);
				if (getVideoRenderer() instanceof ISwtVideoRenderer)
					((ISwtVideoRenderer) getVideoRenderer()).getControl().
						setCursor(cursor);
			}
		});
	}
	public void addImageImportDnDControl(Control control) {
		if (getVideoRenderer() == null)
			throw new IllegalStateException();
		
		// TODO: this is general DnD assignment -- not just images like the owner
		new SwtDragDropHandler(control, 
				(ISwtVideoRenderer) getVideoRenderer(), 
				machine,
				this);
	}
	

	/**
	 * @return
	 */
	protected IEventNotifier getEventNotifier() {
		return eventNotifier;
	}

	/**
	 * @return
	 */
	protected IVideoRenderer getVideoRenderer() {
		return videoRenderer;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.imageimport.ImageImportHandler#getCanvasRenderer()
	 */
	@Override
	protected IVdpCanvasRenderer getCanvasRenderer() {
		return videoRenderer.getCanvasHandler();
	}

	@Override
	protected IVdpCanvas getCanvas() {
		ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
		return renderer.getCanvas();
	}
	@Override
	protected IVdpChip getVdpHandler() {
		ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
		final IVdpChip vdp = renderer.getVdpHandler();
		return vdp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.imageimport.IImageImportHandler#dispose()
	 */
	@Override
	public synchronized void dispose() {
		importAgain = false;
		if (importJob != null) {
			importJob.interrupt();
			try {
				importJob.join(500);
			} catch (InterruptedException e) {
			}
			importJob = null;
		}
		super.dispose();
	}
}

