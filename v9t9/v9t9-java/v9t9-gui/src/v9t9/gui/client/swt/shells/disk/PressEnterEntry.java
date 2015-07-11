/**
 * 
 */
package v9t9.gui.client.swt.shells.disk;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.ejs.gui.common.FontUtils;

import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class PressEnterEntry extends BaseSettingEntry {

	/**
	 * @param dialog_
	 * @param parent
	 * @param setting_
	 * @param style
	 */
	public PressEnterEntry(IDeviceSelectorDialog dialog_, Composite parent,
			IProperty setting_) {
		super(dialog_, parent, setting_, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.BaseSettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		int emSize = FontUtils.measureText(getDisplay(), getFont(), "M").x;
		
		Button enterButton = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().hint(emSize * 10, emSize * 4).grab(true, true).align(SWT.CENTER, SWT.BOTTOM).applyTo(enterButton);
		
		enterButton.setText("Press ENTER");
		enterButton.setToolTipText("Send an ENTER keypress to the 99/4A");
		
		enterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				machine.getKeyboardHandler().pasteText("\r");
			}
		});
	}

}
