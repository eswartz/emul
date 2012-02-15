/**
 * 
 */
package v9t9.gui.client.awt;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.ImageData;

import v9t9.common.video.ICanvas;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.AwtImageUtils;
import v9t9.video.imageimport.ImageImport;

/**
 * Handle images copied in or out of the screen.
 * @author ejs
 *
 */
public class AwtDragDropHandler implements DragGestureListener, DropTargetListener {

	private final AwtVideoRenderer renderer;
	private AwtImageImportSupport imageImportSupport;
	
	/**
	 * @param renderer
	 */
	public AwtDragDropHandler(Component component, AwtVideoRenderer renderer) {
		this.renderer = renderer;
		
		DropTarget dt = new DropTarget(component, this);
		dt.setFlavorMap(SystemFlavorMap.getDefaultFlavorMap());

		component.setDropTarget(dt);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				component, DnDConstants.ACTION_COPY_OR_MOVE, this);

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.
	 * DragGestureEvent)
	 */
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		if (!dge.getTriggerEvent().isControlDown())
			return;

		Transferable transferable = new Transferable() {

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return false;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}

			@Override
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				if (flavor != DataFlavor.imageFlavor)
					throw new UnsupportedFlavorException(flavor);
				ICanvas vc = renderer.getCanvas();
				ImageData data = ((ImageDataCanvas) vc).getImageData();

				ComponentColorModel colorModel = new ComponentColorModel(
						ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
						false, false, ComponentColorModel.OPAQUE,
						DataBuffer.TYPE_BYTE);
				DataBuffer buffer = new DataBufferByte(data.data,
						data.data.length);
				WritableRaster raster = WritableRaster.createWritableRaster(
						new ComponentSampleModel(DataBuffer.TYPE_BYTE,
								data.width, data.height, 3, data.width * 3,
								new int[] { 0, 1, 2 }),

						buffer, /* bounds.width, bounds.height, 24, */
						new Point(0, 0));
				BufferedImage image = new BufferedImage(colorModel, raster,
						false, null);
				return image;
			}
		};
		dge.startDrag(null, transferable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
	 * )
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if (!dtde.isDataFlavorSupported(DataFlavor.imageFlavor)
				&& !dtde.isDataFlavorSupported(DataFlavor.stringFlavor)
				&& !dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.rejectDrag();
			return;
		}
		if (!ImageImport.isModeSupported(renderer.getCanvas().getFormat())) {
			dtde.rejectDrag();
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.
	 * DropTargetDragEvent)
	 */
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	@Override
	public void drop(DropTargetDropEvent dtde) {
		Transferable transferable = dtde.getTransferable();
		BufferedImage image = null;

		DataFlavor[] flavors = dtde.getCurrentDataFlavors();
		/*
		 * for (DataFlavor flavor : flavors) { System.out.println(flavor); }
		 */

		try {
			if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)
					|| dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				URL url = null;
				dtde.acceptDrop(DND.DROP_COPY);
				for (DataFlavor flavor : flavors) {
					try {
						Object data = transferable.getTransferData(flavor);
						if (data instanceof List<?>) {
							url = ((File)((List<?>)data).get(0)).toURI().toURL();
							imageImportSupport.getHistory().add(url.toString());
							break;
						}
						else if (data instanceof String) {
							String uriStr = data.toString().split("\n")[0].trim();
							url = new URL(uriStr);
							imageImportSupport.getHistory().add(uriStr);
							break;
						}
					} catch (IOException e) {
						continue;
					}
				}
				if (url == null) {
					System.err.println("Failed to convert string!");
					return;
				}
				image = ImageIO.read(url.openStream());
				if (image == null) {
					System.err.println("Failed to load image from " + url);
					return;
				}
			} else if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				dtde.acceptDrop(DND.DROP_COPY);
				Image origImage = (Image) transferable
						.getTransferData(DataFlavor.imageFlavor);
				if (!(origImage instanceof BufferedImage)) {
					image = AwtImageUtils.getScaledInstance(origImage,
							origImage.getWidth(null),
							origImage.getHeight(null),
							RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
							false);
				} else {
					image = (BufferedImage) origImage;
				}
			}
			if (image == null) {
				dtde.rejectDrop();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		//System.out.println(image);
		
		if (imageImportSupport == null) {
			imageImportSupport = new AwtImageImportSupport(
					(ImageDataCanvas) renderer.getCanvas(), renderer.getVdpHandler(), renderer);
			imageImportSupport.resetOptions();
		}
		
		imageImportSupport.importImage(image, true);
		
		renderer.setFocus();
	}
}
