/**
 * 
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import ejs.base.settings.ISettingProperty;

import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageBar;

/**
 * @author ejs
 *
 */
public class SpeechDialog extends Composite {
	public static final String SPEECH_DIALOG_TOOL_ID = "speech.dialog";

	public SpeechDialog(final Shell shell, final SwtWindow window, final IMachine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Speech Options");

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		Label label;
		
		// rate
		final ISettingProperty talkRateProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingTalkSpeed);
		
		label = new Label(this, SWT.WRAP);
		label.setText("Talk Rate");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner rateSpinner = new Spinner(this, SWT.NONE);
		rateSpinner.setToolTipText("Adjust how fast speech is generated (1 = normal, 0.5 = half speed, etc.)");
		rateSpinner.setMinimum(10);
		rateSpinner.setMaximum(500);
		rateSpinner.setDigits(2);
		rateSpinner.setSelection((int) (talkRateProperty.getDouble() * 100));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(rateSpinner);
		
		rateSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				talkRateProperty.setDouble(rateSpinner.getSelection() / 100.);
			}
		});
		
		// pitch
		final ISettingProperty pitchAdjustProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchAdjust);
		
		label = new Label(this, SWT.WRAP);
		label.setText("Pitch Adjustment");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchSpinner = new Spinner(this, SWT.NONE);
		pitchSpinner.setToolTipText("Adjust the pitch of speech (1 = normal, 0.5 = octave lower, etc.)");
		pitchSpinner.setMinimum(25);
		pitchSpinner.setMaximum(400);
		pitchSpinner.setDigits(2);
		pitchSpinner.setSelection((int) (pitchAdjustProperty.getDouble() * 100));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(pitchSpinner);
		
		pitchSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pitchAdjustProperty.setDouble(pitchSpinner.getSelection() / 100.);
			}
		});
		
		
		// pitch range
		final ISettingProperty pitchRangeAdjustProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchRangeAdjust);
		
		label = new Label(this, SWT.WRAP);
		label.setText("Pitch Range Adjustment");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchRangeSpinner = new Spinner(this, SWT.NONE);
		pitchRangeSpinner.setToolTipText("Adjust the range of pitch variation of speech (1 = normal, 0.5 = half as much, 0 = robotic, etc.)");
		pitchRangeSpinner.setMinimum(0);
		pitchRangeSpinner.setMaximum(800);
		pitchRangeSpinner.setDigits(2);
		pitchRangeSpinner.setSelection((int) (pitchRangeAdjustProperty.getDouble() * 100));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(pitchRangeSpinner);
		
		pitchRangeSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pitchRangeAdjustProperty.setDouble(pitchRangeSpinner.getSelection() / 100.);
			}
		});
		

		
		// pitch midrange 
		final ISettingProperty pitchMidRangeAdjustRateProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchMidRangeAdjustRate);
		
		label = new Label(this, SWT.WRAP);
		label.setText("Pitch Midrange Adjust Rate");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchMidRangeSpinner = new Spinner(this, SWT.NONE);
		pitchMidRangeSpinner.setToolTipText("Adjust the rate at which pitch midrange is changed (when the pitch range selection is not 1) -- -1 uses fixed midrange, 0 adjusts immediately, etc.");
		pitchMidRangeSpinner.setMinimum(-1);
		pitchMidRangeSpinner.setMaximum(100);
		pitchMidRangeSpinner.setSelection(pitchMidRangeAdjustRateProperty.getInt());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(pitchMidRangeSpinner);
		
		pitchMidRangeSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pitchMidRangeAdjustRateProperty.setInt(pitchMidRangeSpinner.getSelection());
			}
		});

		
		// whisper
		final ISettingProperty forceUnvoicedProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingForceUnvoiced);
		
		label = new Label(this, SWT.WRAP);
		label.setText("Always Whisper");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Button whisperButton = new Button(this, SWT.TOGGLE);
		whisperButton.setToolTipText("If set, all speech is whispered");
		whisperButton.setText(forceUnvoicedProperty.getBoolean() ? "on" : "off");
		whisperButton.setSelection(forceUnvoicedProperty.getBoolean());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(whisperButton);
		
		whisperButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				forceUnvoicedProperty.setBoolean(whisperButton.getSelection());
				whisperButton.setText(forceUnvoicedProperty.getBoolean() ? "on" : "off");
			}
		});

		
		// reset
		Button resetButton = new Button(this, SWT.PUSH);
		resetButton.setText("Reset to defaults");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(resetButton);
		
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				talkRateProperty.resetToDefault();
				rateSpinner.setSelection((int) (talkRateProperty.getDouble() * 100));
				
				pitchAdjustProperty.resetToDefault();
				pitchSpinner.setSelection((int) (pitchAdjustProperty.getDouble() * 100));
				
				pitchRangeAdjustProperty.resetToDefault();
				pitchRangeSpinner.setSelection((int) (pitchRangeAdjustProperty.getDouble() * 100));
				
				pitchMidRangeAdjustRateProperty.resetToDefault();
				pitchMidRangeSpinner.setSelection((int) (pitchMidRangeAdjustRateProperty.getDouble() * 100));
				
				forceUnvoicedProperty.resetToDefault();
				whisperButton.setSelection(forceUnvoicedProperty.getBoolean());
				whisperButton.setText(forceUnvoicedProperty.getBoolean() ? "on" : "off");
				
			}
		});
		

//		GridDataFactory.fillDefaults().grab(true, true).applyTo(editGroup);
	}

	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageBar buttonBar, 
			final IMachine machine,
			final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "SpeechDialogBounds";
				behavior.centering = Centering.OUTSIDE;
				behavior.centerOverControl = buttonBar.getShell();
				behavior.dismissOnClickOutside = true;
			}
			public Control createContents(Shell shell) {
				SpeechDialog dialog = new SpeechDialog(shell, window, machine);
				return dialog;
			}
			@Override
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}



}
