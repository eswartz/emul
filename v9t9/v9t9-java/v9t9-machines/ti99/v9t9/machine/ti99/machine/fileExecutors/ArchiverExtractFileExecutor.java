/*
  EditAssmRunProgramFileExecutor.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine.fileExecutors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IFileExecutor;
import v9t9.common.keyboard.IPasteListener;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.engine.files.directory.EmuDiskSettings;
import v9t9.machine.EmulatorMachinesData;
import ejs.base.properties.IProperty;
import ejs.base.utils.FileUtils;

/**
 * @author ejs
 *
 */
public class ArchiverExtractFileExecutor implements IFileExecutor {

	/**
	 * 
	 */
	private static final int TEMP_DRIVE_NUM = 5;
	public String userFileDir;

	/**
	 * @author ejs
	 *
	 */
	private final class ArchiverLoadedPasteListener implements IPasteListener {
		private final IMachine machine;
		private final IProperty dskProp;

		private ArchiverLoadedPasteListener(
				IMachine machine,
				IProperty dskProp) {
			this.machine = machine;
			this.dskProp = dskProp;
		}

		@Override
		public void pasteCanceled() {
			// okay
			machine.getEventNotifier().notifyEvent(null, Level.INFO, "Paste was canceled");
			machine.getKeyboardHandler().removePasteListener(this);
		}

		@Override
		public void pasteCompleted() {
			// point to downloaded file now
			dskProp.setString(userFileDir);
//			emuDskProp.setBoolean(wasEnabled);
			
			machine.getKeyboardHandler().removePasteListener(this);
			
			// now extract files
			String contents = "2"  + IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO  /* extract files */
					+ TEMP_DRIVE_NUM + IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO  /* select drive */
					+ fileName + "\n" + IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO;
			machine.getKeyboardHandler().pasteText(
					contents);
			
			machine.getEventNotifier().notifyEvent(null, Level.INFO, 
					"V9t9 entered the source drive and filename; you need to enter the target drive and filename.");
		}
	}

	private IModule module;
	private String devicePath;
	private String fileName;

	public ArchiverExtractFileExecutor(IModule module, String fileName, String userFileDir) {
		this.module = module;
		this.userFileDir = userFileDir;
		this.devicePath = "DSK" + TEMP_DRIVE_NUM + "." + fileName;
		this.fileName = fileName;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Extract archive file " + devicePath;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + ", run Archiver 3 (bundled in V9t9),\n"+
				"and extract the archive file " + devicePath +".\n" +
				"\n"+
				"You will need to complete the process by selecting the target\n"+
				"drive and responding to the prompts.\n"+
				"\n"+
				"NOTE: this option turns on 'file in a directory' disk support and\n"+
				"changes DSK5 for you.\n";
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(final IMachine machine) throws NotifyException {
		final IProperty emuDskProp = machine.getSettings().get(EmuDiskSettings.emuDiskDsrEnabled);
		emuDskProp.setBoolean(true);
		
		final IProperty dskProp = EmuDiskSettings.getEmuDiskSetting(machine, TEMP_DRIVE_NUM);
		URL arc3Loc = EmulatorMachinesData.getDataURL("ti99/disks/archiver3/arc33_1");
		
		File arc3TmpDir = new File(System.getProperty("java.io.tmpdir"), "archiver3");
		arc3TmpDir.mkdir();
		if (!arc3TmpDir.exists()) {
			throw new NotifyException(null, 
					"Could not create a temporary directory: " +arc3TmpDir +"\nPlease ensure the temporary folder is available and try again.");
		}
		
		File arc3TmpFile = new File(arc3TmpDir, "arc33_1");
		
		try {
			InputStream is = arc3Loc.openStream();
			byte[] content = FileUtils.readInputStreamContentsAndClose(is);
			FileUtils.writeOutputStreamContentsAndClose(new FileOutputStream(arc3TmpFile), content, content.length);
		} catch (IOException e) {
			throw new NotifyException(null, 
					"Could not extract Archiver 3:\n", e);
		}
		
		dskProp.setString(arc3TmpDir.getAbsolutePath());
		
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(" 2"+	// space for title, 2 for editor/assembler
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO + 
				"5DSK"+TEMP_DRIVE_NUM+".ARC33_1\n" + IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO +
				" " + // past opening screen
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO); 
		
		IPasteListener listener = new ArchiverLoadedPasteListener(machine, dskProp);
		machine.getKeyboardHandler().addPasteListener(listener);
		
	}

}
