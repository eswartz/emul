/*
  SpeechDialog.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageCanvas;
import ejs.base.settings.ISettingProperty;

/**
 * @author ejs
 *
 */
public class SpeechDialog extends Composite {
	public static final String SPEECH_DIALOG_TOOL_ID = "speech.dialog";
	private Group pitchGroup;
	private ISettingProperty talkRateProperty;
	private ISettingProperty forceUnvoicedProperty;
	private ISettingProperty pitchAdjustProperty;
	private ISettingProperty pitchRangeAdjustProperty;
	private ISettingProperty pitchMidRangeAdjustRateProperty;
	private Button whisperButton;

	public SpeechDialog(final Shell shell, final SwtWindow window, final IMachine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Speech Options");

		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6).applyTo(this);

		Label label;
		
		// rate
		talkRateProperty = (ISettingProperty) Settings.get(
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
		
		

		
		// whisper
		forceUnvoicedProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingForceUnvoiced);
		
		label = new Label(this, SWT.WRAP);
		
		label.setText("Always Whisper");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		whisperButton = new Button(this, SWT.TOGGLE);
		whisperButton.setToolTipText("If set, all speech is whispered");
		whisperButton.setText(forceUnvoicedProperty.getBoolean() ? "on" : "off");
		whisperButton.setSelection(forceUnvoicedProperty.getBoolean());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(whisperButton);

		whisperButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePitchUI(whisperButton.getSelection());
			}
		});

		
		pitchGroup = new Group(this, SWT.SHADOW_ETCHED_IN);
		pitchGroup.setText("Pitch Control");
		
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6).applyTo(pitchGroup);
		GridDataFactory.fillDefaults().grab(false, false).indent(2, 2).span(2, 1).applyTo(pitchGroup);
		
		// pitch
		pitchAdjustProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchAdjust);
		
		label = new Label(pitchGroup, SWT.WRAP);
		label.setText("Frequency Adjustment");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchSpinner = new Spinner(pitchGroup, SWT.NONE);
		pitchSpinner.setToolTipText("Adjust the pitch of speech (1 = normal, 0.5 = octave lower, etc.)");
		pitchSpinner.setMinimum(10);
		pitchSpinner.setMaximum(1000);
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
		pitchRangeAdjustProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchRangeAdjust);
		
		label = new Label(pitchGroup, SWT.WRAP);
		label.setText("Range Adjustment");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchRangeSpinner = new Spinner(pitchGroup, SWT.NONE);
		pitchRangeSpinner.setToolTipText("Adjust the range of pitch variation of speech (1 = normal, 0.5 = half as much, 0 = robotic, etc.)");
		pitchRangeSpinner.setMinimum(0);
		pitchRangeSpinner.setMaximum(1000);
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
		pitchMidRangeAdjustRateProperty = (ISettingProperty) Settings.get(
				machine, ISpeechChip.settingPitchMidRangeAdjustRate);
		
		label = new Label(pitchGroup, SWT.WRAP);
		label.setText("Midrange Adjust Rate");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		
		final Spinner pitchMidRangeSpinner = new Spinner(pitchGroup, SWT.NONE);
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

		
		// reset
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		Button resetButton = new Button(this, SWT.PUSH);
		resetButton.setText("Reset to defaults");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(resetButton);
		
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
				pitchMidRangeSpinner.setSelection(pitchMidRangeAdjustRateProperty.getInt());
				
				forceUnvoicedProperty.resetToDefault();
				updatePitchUI(forceUnvoicedProperty.getBoolean());
			}
		});
		

//		GridDataFactory.fillDefaults().grab(true, true).applyTo(editGroup);
	}

	/**
	 * @param selection
	 */
	protected void updatePitchUI(boolean selection) {
		forceUnvoicedProperty.setBoolean(selection);
		whisperButton.setText(forceUnvoicedProperty.getBoolean() ? "on" : "off");
		SwtDialogUtils.setEnabled(pitchGroup, ! forceUnvoicedProperty.getBoolean());
	}

	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageCanvas buttonBar, 
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
