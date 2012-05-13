/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.awt.image.BufferedImage;
import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.properties.EditGroup;
import org.ejs.gui.properties.FieldPropertyEditorProvider;
import org.ejs.gui.properties.PropertySourceEditor;

import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtDragDropHandler;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageBar;
import v9t9.gui.client.swt.imageimport.IImageImportHandler;
import v9t9.gui.client.swt.imageimport.ImageClipDecorator;
import v9t9.gui.client.swt.imageimport.ImageLabel;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import v9t9.gui.client.swt.imageimport.SwtImageImportSupport;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.properties.IPropertySource;
import ejs.base.utils.Pair;

/**
 * Control the options used for importing images
 * @author ejs
 *
 */
public class ImageImportOptionsDialog extends Composite {
	
	private final class ImagePropertyListener implements
			IPropertyListener {
		private final IProperty imageProperty;
		private final ImageLabel imageLabel;
		private Image img;
		private BufferedImage bufImg;

		private ImagePropertyListener(IProperty imageProperty,
				ImageLabel imageLabel) {
			this.imageProperty = imageProperty;
			this.imageLabel = imageLabel;
		}

		@Override
		public void propertyChanged(IProperty property) {
			updateImage();
		}

		public void updateImage() {
			if (img != null) {
				img.dispose();
				img = null;
			}
			bufImg = (BufferedImage) imageProperty.getValue();
			
			if (bufImg != null && !imageLabel.isDisposed()) {
				if (img == null) { 
					img = ImageUtils.convertAwtImage(getDisplay(), bufImg);
				}
				imageLabel.setImage(img);
			}
		}
		
		public void dispose() {
			if (img != null)
				img.dispose();
			if (img != null)
				img.dispose();
			img = null;
			img = null;
		}
	}

	private IPropertySource propertySource;
	public static final String IMAGE_IMPORTER_ID = "swt.image.importer";

	/**
	 * @param shell
	 * @param window 
	 * @param imageDragDropHandler 
	 * @param imageImportHandler 
	 * @param none
	 * @param importer
	 */
	public ImageImportOptionsDialog(final Shell shell, int style, 
			final SwtWindow window, final IImageImportHandler imageImportHandler, final IPropertyListener listener) {
		super(shell, style);
		
		shell.setText("Image Importer");

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		
		propertySource = imageImportHandler.getImageImportOptions().createPropertySource();
		
		PropertySourceEditor editor = new PropertySourceEditor(
				new FieldPropertyEditorProvider(),
				propertySource, "Options");
		final EditGroup editGroup = editor.createEditor(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editGroup);
		
		editGroup.setToolTipText("Drag an image onto or out of this dialog");

		final IProperty imageProperty = propertySource.getProperty("image");

		final ImageLabel imageLabel = new ImageLabel(editGroup.getContainer(), SWT.BORDER);
		/*final ImageClipDecorator clipDecorator = */ new ImageClipDecorator(
				imageLabel, 
				imageImportHandler.getImageImportOptions().createPropertySource().getProperty("clip"),
				listener);
		
		final ImagePropertyListener imagePropertyListener = 
			new ImagePropertyListener(imageProperty, imageLabel);
		imageProperty.addListener(imagePropertyListener);
		
		GridDataFactory.fillDefaults().grab(true, true).
			minSize(64, 64).applyTo(imageLabel);
		
		for (IProperty prop : propertySource.getProperties()) {
			if (listener != imageProperty) 
				prop.addListener(listener);
		}
		
		final Button reset = new Button(editGroup.getContainer(), SWT.PUSH);
		reset.setText("Reset Options");
		reset.setToolTipText("Select best options for current video settings");
		reset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageImportHandler.resetOptions();
				editGroup.reset();
				listener.propertyChanged(null);
			}
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(reset);
		
		

		final Button button = new Button(this, SWT.PUSH);
		button.setText("Import Again");
		button.setToolTipText("Import the last dragged image (for example, if the screen mode or contents changed)");
		button.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				listener.propertyChanged(null);
			}
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(button);
		
		imageLabel.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				createImageImportMenu(window, imageImportHandler, e);
			}
		});
		
		this.pack();
		
		//imageProperty.firePropertyChange();
		imagePropertyListener.propertyChanged(imageProperty);
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				imagePropertyListener.dispose();
				imageProperty.removeListener(imagePropertyListener);
				
				for (IProperty prop : propertySource.getProperties()) {
					if (listener != imageProperty) 
						prop.removeListener(listener);
				}
			}
		});
		
		if (imageImportHandler.getImageImportOptions().getImage() == null) {
			showFileOpenDialog(window, imageImportHandler, imageImportHandler.getHistory());
		}
	}

	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageBar buttonBar, final SwtImageImportSupport imageSupport,
			final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "ImageImporterBounds";
				behavior.centering = Centering.OUTSIDE;
				behavior.centerOverControl = buttonBar.getShell();
				behavior.dismissOnClickOutside = true;
			}
			public Control createContents(Shell shell) {
				ImageImportOptionsDialog dialog = imageSupport.createImageImportDialog(shell, window);
				imageSupport.addImageImportDnDControl(dialog);
				return dialog;
			}
			@Override
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}


	/**
	 * @param window
	 * @param imageSupport
	 * @param e
	 */
	public static void createImageImportMenu(final SwtWindow window,
			final IImageImportHandler imageSupport, MenuDetectEvent e) {
		final Collection<String> fileHistory = imageSupport.getHistory();
		
		Control control = (Control) e.widget;
		Menu menu = new Menu(control);
		MenuItem vitem = new MenuItem(menu, SWT.NONE);
		vitem.setText("Load file...");
		
		vitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFileOpenDialog(window, imageSupport, fileHistory);
			}
		});
		
		// not persistent
		if (!fileHistory.isEmpty()) {
			new MenuItem(menu, SWT.SEPARATOR);
			
			int index = 0;
			for (final String file : fileHistory) {
				MenuItem hitem = new MenuItem(menu, SWT.NONE);
				hitem.setText((index < 10 ? "&" + index + " " : "")
					+ file);
				index++;
				
				hitem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Pair<BufferedImage, Boolean> info = SwtDragDropHandler.loadImageFromFile(window.getEventNotifier(), file);
						
						if (info != null) {
							imageSupport.importImage(info.first, !info.second);
							((ISwtVideoRenderer) window.getVideoRenderer()).setFocus();
						}
					}
				});
			}
		}
		
		window.showMenu(menu, null, e.x, e.y);
	}

	/**
	 * @param window
	 * @param imageSupport
	 * @param fileHistory
	 */
	protected static void showFileOpenDialog(final SwtWindow window,
			final IImageImportHandler imageSupport,
			final Collection<String> fileHistory) {
		String file = window.openFileSelectionDialog("Open Image", null, null, false, 
				new String[] { ".jpg", ".jpeg", ".gif", ".png", ".bmp", ".tga" });
		if (file != null) {
			Pair<BufferedImage, Boolean> info = SwtDragDropHandler.loadImageFromFile(
					window.getEventNotifier(), file);
			
			if (info != null) {
				imageSupport.importImage(info.first, !info.second);
				((ISwtVideoRenderer) window.getVideoRenderer()).setFocus();
				
				fileHistory.add(file);
			}
		}
	}

}
