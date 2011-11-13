/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

		/*
		final IProperty imageProperty = propertySource.getProperty("image");
		button.setEnabled(imageProperty.getString() != null);

		imgPropListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(final IProperty property) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						button.setEnabled(property.getString() != null);
					}
				});
			}
		};
		
		imageProperty.addListener(imgPropListener);
		
		button.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageProperty.removeListener(imgPropListener);
			}
		});
		*/
		
		for (IProperty prop : propertySource.getProperties()) {
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
	}
}
