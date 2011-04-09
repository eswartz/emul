/**
 * Apr 3, 2011
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.ImageData;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.engine.VdpHandler;

/**
 * @author ejs
 * 
 */
public class AwtCanvas extends Canvas implements DragGestureListener,
		DropTargetListener {
	private final VdpHandler vdp;

	/**
	 * @param config
	 */
	public AwtCanvas(AwtVideoRenderer renderer, VdpHandler vdp) {
		super();
		this.renderer = renderer;
		this.vdp = vdp;

		DropTarget dt = new DropTarget(this, this);
		dt.setFlavorMap(SystemFlavorMap.getDefaultFlavorMap());

		setDropTarget(dt);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY_OR_MOVE, this);

		/*
		 * {
		 * 
		 * videoDragSource = new DragSource(videoControl, DND.DROP_COPY);
		 * videoDragSource.setTransfer(new Transfer[] {
		 * ImageTransfer.getInstance() }); videoDragSource.addDragListener(new
		 * DragSourceListener() {
		 * 
		 * @Override public void dragStart(DragSourceEvent event) {
		 * 
		 * }
		 * 
		 * @Override public void dragSetData(DragSourceEvent event) { if
		 * (ImageTransfer.getInstance().isSupportedType(event.dataType)) {
		 * event.data = renderer.getCanvas().getImageData(); } }
		 * 
		 * @Override public void dragFinished(DragSourceEvent event) {
		 * 
		 * } }); }
		 */

	}

	private static final long serialVersionUID = 8795221581767897631L;

	private AwtVideoRenderer renderer;

	@Override
	public void paint(Graphics g) {
		Rectangle clipRect = g.getClipBounds();
		// System.out.println("Clippy rect: " + clipRect);
		renderer.doRedraw(g, clipRect.x, clipRect.y, clipRect.width,
				clipRect.height);

	}

	@Override
	public void update(Graphics g) {
		// do not clear background
		paint(g);
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
				VdpCanvas vc = renderer.getCanvas();
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
				&& !dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
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
		if (!(renderer.getCanvas() instanceof ImageDataCanvas))
			return;
		
		Transferable transferable = dtde.getTransferable();
		Image image = null;

		DataFlavor[] flavors = dtde.getCurrentDataFlavors();
		/*
		 * for (DataFlavor flavor : flavors) { System.out.println(flavor); }
		 */

		try {
			if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				dtde.acceptDrop(DND.DROP_COPY);
				String filename = null;
				for (DataFlavor flavor : flavors) {
					try {
						Object data = transferable.getTransferData(flavor);
						if (data instanceof String) {
							String uriStr = data.toString().trim();
							try {
								URI uri = new URI(uriStr);
								filename = uri.getPath();
							} catch (URISyntaxException e) {
								filename = uriStr;
							}
							break;
						}
					} catch (IOException e) {
						continue;
					}
				}
				if (filename == null) {
					System.err.println("Failed to convert string!");
					return;
				}
				image = ImageIO.read(new File(filename));
				if (image == null) {
					System.err.println("Failed to load image from " + filename);
					return;
				}
			} else if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				dtde.acceptDrop(DND.DROP_COPY);
				image = (Image) transferable
						.getTransferData(DataFlavor.imageFlavor);
			}
			if (image == null) {
				dtde.rejectDrop();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.out.println(image);
		
		// scale aspect-sensitively
		ImageDataCanvas vc = (ImageDataCanvas) renderer.getCanvas();
		
		int targWidth = vc.getVisibleWidth();
		int targHeight = vc.getVisibleHeight();
		int realWidth = image.getWidth(null);
		int realHeight = image.getHeight(null);
		if (realWidth < 0 || realHeight < 0) {
			return;
		}
		
		if (realWidth * targHeight > realHeight * targWidth) {
			targHeight = targWidth * realHeight / realWidth;
		} else {
			targWidth = targHeight * realWidth / realHeight;
		}
		
		BufferedImage scaled = getScaledInstance(image, targWidth, targHeight, 
				//RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, 
				RenderingHints.VALUE_INTERPOLATION_BILINEAR, 
				false);
		System.out.println(scaled.getWidth(null) + " x " +scaled.getHeight(null));
		
		vc.setImageData(scaled);
		
		synchronized (vdp) {
			vdp.getVdpModeRedrawHandler().importImageData();
		}
		
		//renderer.getAwtCanvas().repaint();
	}

	/**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public BufferedImage getScaledInstance(Image img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth(null);
            h = img.getHeight(null);
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
