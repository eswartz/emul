/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.image.BufferedImage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.jface.EditGroup;
import org.ejs.coffee.core.jface.PropertySourceEditor;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.IPropertySource;

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

	/**
	 * @param shell
	 * @param imageDragDropHandler 
	 * @param imageImportHandler 
	 * @param none
	 * @param importer
	 */
	public ImageImportOptionsDialog(final Shell shell, int style, 
			final IImageImportHandler imageImportHandler, final IPropertyListener listener) {
		super(shell, style);
		
		shell.setText("Image Importer");

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		
		propertySource = imageImportHandler.getImageImportOptions().createPropertySource();
		
		PropertySourceEditor editor = new PropertySourceEditor(propertySource, "Options");
		final EditGroup editGroup = editor.createEditor(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editGroup);
		
		editGroup.setToolTipText("Drag an image onto or out of this dialog");

		final IProperty imageProperty = propertySource.getProperty("image");

		final ImageLabel imageLabel = new ImageLabel(editGroup.getContainer(), SWT.BORDER);
		/*final ImageClipDecorator clipDecorator = */ new ImageClipDecorator(
				imageLabel, imageImportHandler.getImageImportOptions(), listener);
		
		//imageLabel.setClip((Rectangle) propertySource.getProperty("clip").getValue());
		
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
		
		
		this.pack();
		
		imageProperty.firePropertyChange();
		
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
	}
}
