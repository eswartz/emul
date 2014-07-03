/*
  BaseSwtJavaClient.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IClient;
import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.ISoundHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.demos.DemoContentSource;
import v9t9.common.dsr.IPrinterImageEngine;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.events.BaseEventNotifier;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.EmulatedDiskContentSource;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.machine.TerminatedException;
import v9t9.common.modules.ModuleContentSource;
import v9t9.common.settings.Settings;
import v9t9.common.speech.ISpeechDataSender;
import v9t9.gui.client.swt.fileimport.DoNothingFileExecutor;
import v9t9.gui.client.swt.handlers.DemoContentHandler;
import v9t9.gui.client.swt.handlers.FileExecutorContentHandler;
import v9t9.gui.client.swt.handlers.ModuleContentHandler;
import v9t9.gui.client.swt.shells.PrinterImageShell;
import v9t9.gui.client.swt.wizards.SetupWizard;
import v9t9.gui.sound.JavaSoundHandler;
import ejs.base.timer.FastTimer;

/**
 * @author ejs
 *
 */
public class ConsoleOnlyClient implements IClient {

	public static final String ID = "ConsoleOnly";
	
	protected IVdpChip video;
	protected IMachine machine;

	protected IEventNotifier eventNotifier;
	protected final ISettingsHandler settingsHandler;
	protected ISoundHandler soundHandler;

//	protected IKeyboardHandler keyboardHandler;
	
	public ConsoleOnlyClient(final IMachine machine) {
		this.settingsHandler = Settings.getSettings(machine);
    	this.video = machine.getVdp();
    	this.machine = machine;
    	machine.setClient(this);
    	
        soundHandler = new JavaSoundHandler(machine);
        
        eventNotifier = new BaseEventNotifier();

	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#tick()
	 */
	@Override
	public void tick() {
		// flush sound each tick
		int pos;
		int total;
		synchronized (machine.getCpu()) {
			pos = machine.getCpu().getCurrentCycleCount();
			total = machine.getCpu().getCurrentTargetCycleCount();
		}

		//System.out.println(pos + " / " + total);
		soundHandler.flushAudio(pos, total);
	}

	@Override
	public IEventNotifier getEventNotifier() {
		return eventNotifier;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#start()
	 */
	@Override
	public void start() {
	}
	
	public void close() {
		if (soundHandler != null)
			soundHandler.dispose();
		
		try {
			machine.stop();
		} catch (TerminatedException e) {
			// expected
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}


	public void handleEvents() {
	}

	public boolean isAlive() {
		return true;
	}
	
	@Override
	public void asyncExecInUI(final Runnable runnable) {
		runnable.run();
	}
	
	@Override
	public IVideoRenderer getVideoRenderer() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#getSoundHandler()
	 */
	@Override
	public ISoundHandler getSoundHandler() {
		return soundHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#getEmulatorContentHandlers(v9t9.common.client.IEmulatorContentSource)
	 */
	@Override
	public IEmulatorContentHandler[] getEmulatorContentHandlers(
			IEmulatorContentSource source) {
		return IEmulatorContentHandler.NONE;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}

}