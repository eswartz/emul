/*
  ConvertDemos.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.demos;

import ejs.base.utils.CountingOutputStream;
import ejs.base.utils.ListenerList;
import gnu.getopt.Getopt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.BitSet;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoManager;
import v9t9.common.demos.IDemoOutputStream;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.speech.ILPCParameters;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.engine.demos.DemoPlayer;
import v9t9.engine.demos.actors.SpeechDemoConverter;
import v9t9.engine.demos.events.OldSpeechEvent;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import v9t9.engine.demos.format.old.OldDemoFormatOutputStream;
import v9t9.machine.f99b.machine.F99bMachineModel;
import v9t9.machine.ti99.machine.StandardMachineModel;

/**
 * @author ejs
 *
 */
public class ConvertDemos {


	public static void main(String[] args) {
		Getopt getopt = new Getopt("ConvertDemos", args, "h?d:srFO");
		getopt.setOpterr(true);

		if (args.length == 0) {
			help();
			System.exit(0);
		}
		

		ConvertDemos cvt = null;
		
		IMachineModel model = null;
		String newDirPath = ".";
		int opt;
		boolean shrink = false;
		boolean recurse = false;
		boolean oldFormat = false;
		
		while ((opt = getopt.getopt()) != -1) {
			if (opt == 'h' || opt == '?') {
				help();
				System.exit(0);
			}
			else if (opt == 'd') {
				newDirPath = getopt.getOptarg();
			}
			else if (opt == 's') {
				shrink = true;
			}
			else if (opt == 'r') {
				recurse = true;
			}
			else if (opt == 'F') {
				model = new F99bMachineModel();
			}
			else if (opt == 'O') {
				oldFormat = true;
			}
		}
		
		if (model == null) {
			model = new StandardMachineModel();
		}
		
		cvt = new ConvertDemos(model);
		cvt.setShrink(shrink);
		cvt.setRecurse(recurse);
		cvt.setOldFormat(oldFormat);
		
		File newDir = new File(newDirPath);
		newDir.mkdirs();
		
		
		for (int index = getopt.getOptind(); index < args.length; index++) {
			String file = args[index];
			
			File fromFile = new File(file);
			
			if (fromFile.isDirectory()) {
				cvt.convertDirectory(fromFile, newDir);
			}
			else {
				cvt.convertFile(fromFile, newDir);
			}			
		}
		
	}
	

	private static void help() {
		System.out.println("ConvertDemos [files or directories...] [-s] [-r] [-d newdir]");
		System.out.println();
		System.out.println("Convert demos from TI Emulator! / V9t9 v6.0 to the current format.");
		System.out.println();
		System.out.println("The -r argument will work along directories recursively.");
		System.out.println("The -s argument will shrink the demos to remove video memory changes that are not visible.");
	}


	private boolean recurse;
	private boolean oldFormat;

	private IDemoManager manager;
	private boolean shrink;
	private IMachine machine;
	
	/** one bit per byte per address whose contents are known to come from the demo */
	private BitSet knownVideoMemory;
	/** one bit per byte for memory whose contents are pending visibility */
	private BitSet unrecordedVideoMemory;
	
	/** one bit per 1<<memGranularity bytes for memory contributing to video tables */
	private BitSet visibleVideoMemory;
	private SpeechDemoConverter speechConverter;

	public ConvertDemos(IMachineModel machineModel) {
		ISettingsHandler settings = new BasicSettingsHandler();
		this.machine = machineModel.createMachine(settings);
		this.manager = machine.getDemoManager();
	}
	


	/**
	 * @param recurse
	 */
	private void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}


	/**
	 * @param shrink the shrink to set
	 */
	public void setShrink(boolean shrink) {
		this.shrink = shrink;
	}


	/**
	 * @param oldFormat
	 */
	private void setOldFormat(boolean oldFormat) {
		this.oldFormat = oldFormat;
	}

	/**
	 * @param fromDir
	 * @param newDir
	 */
	private void convertDirectory(File fromDir, File newDir) {
		File[] demos = fromDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dem");
			}
		});
		for (File ent : demos) {
			convertFile(ent, newDir);
		}

		if (recurse) {
			File[] dirs = fromDir.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".") && pathname.isDirectory();
				}
			});
			for (File dir : dirs) {
				convertDirectory(dir, newDir);
			}
		}
	}


	/**
	 * @param ent
	 * @param newDir
	 */
	private void convertFile(File fromFile, File newDir) {
		File toFile = new File(newDir, fromFile.getName());
		
		if (fromFile.equals(toFile)) {
			System.out.print("Cannot convert " + fromFile + " in place: please use different '-d dir' argument");
			return;
		}
		
		System.out.print("Converting " + fromFile.getName() + " to " + toFile + "... ");
		
		URI from = fromFile.toURI();
		URI to = toFile.toURI();
		try {
			convert(from, to);
			System.out.println("done!");
		} catch (NotifyException e) {
			System.out.flush();
			System.err.flush();
			System.err.print("\n\t");
			System.err.println(e.getEvent());
		} catch (IOException e) {
			System.out.flush();
			System.err.flush();
			System.err.print("\n\t");
			System.err.println(e.getMessage());
		}
		
	}

	private void convert(URI from, URI to) throws IOException, NotifyException {
		IDemoInputStream is = manager.createDemoReader(from);
		if (is == null) {
			System.out.flush();
			System.err.flush();
			System.err.println("unrecognized file");
			System.err.flush();
			return;
		}
		
		IDemoOutputStream os = null;
		try {
			if (oldFormat) {
				os = new OldDemoFormatOutputStream(
						new CountingOutputStream(new BufferedOutputStream(
								manager.getDemoLocator().createOutputStream(to))));
			} else {
				os = manager.createDemoWriter(to);
			}
			
			try {
				setupSpeechConverter(os);
				
				if (shrink)
					processEventsAndShrink(from, is, os);
				else
					processEvents(is, os);
				
				System.out.print("\n\tinput size: " + formatSize(is.getPosition()) 
						+ "; output size: " + formatSize(os.getPosition()) + ")"); 
				System.out.print("\n\tinput time: " + formatTime(is.getElapsedTime()) 
						+ "; output time: " + formatTime(os.getElapsedTime()) + ")");
				System.out.print("\n\t");
			} finally {
				is.close();
			}
		} finally {
			if (os != null)
				os.close();
		}
	}


	/**
	 * 
	 */
	private void setupSpeechConverter(final IDemoOutputStream os) {
		speechConverter = new SpeechDemoConverter();
		
		ILPCParametersListener convertParamsListener = new ILPCParametersListener() {
			
			@Override
			public void parametersAdded(ILPCParameters params) {
				try {
					os.writeEvent(new SpeechEvent(params));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		speechConverter.addEquationListener(convertParamsListener);		
	}


	private String formatSize(long sz) {
		return sz + " bytes";
	}


	private String formatTime(long elapsedTime) {
		return elapsedTime / 1000 + "." + elapsedTime % 1000 + " sec";
	}


	/**
	 * Timer rates may differ between formats.  Scale appropriately.
	 * @param os
	 * @param is
	 * @param timer current ticks in input stream format.
	 * @param event
	 * @return
	 * @throws NotifyException 
	 */
	private long updateAndEmitTimerTick(IDemoOutputStream os,
			IDemoInputStream is, long timer, TimerTick event) throws IOException {
		
		long newTimer = is.getElapsedTime();
		
		while (os.getElapsedTime() < newTimer) {
			os.writeEvent(new TimerTick(os.getElapsedTime()));
		}
		return newTimer;
	}


	/**
	 * Convert old-style speech events (where bytes are fed serially into the FIFO,
	 * but without any conception of timing) into the current format (passing
	 * equations, known to be complete).
	 * @param event
	 */
	private void convertSpeechEvent(IDemoOutputStream os, OldSpeechEvent event) throws IOException {
		switch (event.getCode()) {
		case OldSpeechEvent.SPEECH_STARTING:
			speechConverter.startPhrase();
			break;
		case OldSpeechEvent.SPEECH_STOPPING:
			speechConverter.stopPhrase();
			break;
		case OldSpeechEvent.SPEECH_TERMINATING:
			speechConverter.terminatePhrase();
			break;
		case OldSpeechEvent.SPEECH_ADDING_BYTE:
			speechConverter.pushByte(event.getAddedByte());
			break;
		}
	}

	private void processEvents(IDemoInputStream is, IDemoOutputStream os) throws IOException {
		IDemoEvent event;
		
		long timer = 0;
		
		while ((event = is.readNext()) != null)  {
			if (event instanceof TimerTick) {
				timer = updateAndEmitTimerTick(os, is, timer, (TimerTick) event);
			}
			else if (event instanceof OldSpeechEvent) {
				convertSpeechEvent(os, (OldSpeechEvent) event);
			}
			else {
				os.writeEvent(event);
			}
		}
		
		updateAndEmitTimerTick(os, is, timer, null);
	}


	private void processEventsAndShrink(URI uri, IDemoInputStream is, IDemoOutputStream os) throws IOException {
		IDemoEvent event;

		long timer = 0;
		
		BitSet tempVideoWriteSet = new BitSet();
		
		// Demos should all start with a VDP memory dump
		// then VDP register dump, so assume nothing
		
		knownVideoMemory = new BitSet();
		unrecordedVideoMemory = new BitSet();
		visibleVideoMemory = new BitSet();
		
		int visMemGranularity = 7;
		
		IDemoPlayer player = new DemoPlayer(machine,
				uri, is, new ListenerList<IDemoHandler.IDemoListener>());
		
		while ((event = is.readNext()) != null)  {
			
			if (event instanceof TimerTick) {
				
				flushVideoEvents(os, tempVideoWriteSet);

				timer = updateAndEmitTimerTick(os, is, timer, (TimerTick) event);
			}
			else if (event instanceof OldSpeechEvent) {
				convertSpeechEvent(os, (OldSpeechEvent) event);
				continue;
			}

			else if (event instanceof VideoWriteRegisterEvent) {
				// When the video mode changes, new video memory
				// may be exposed.  Execute the event to see.  
				player.executeEvent(event);
				
				BitSet newVisibleVideoMemory = machine.getVdp().getVisibleMemory(visMemGranularity);
				BitSet diff = (BitSet) newVisibleVideoMemory.clone();
				diff.andNot(visibleVideoMemory);
				if (!diff.isEmpty()) {
					// New video memory is exposed.  See if we have pending
					// video memory writes.
					BitSet regionToBytes = new BitSet();
					for (int i = diff.nextSetBit(0); i >= 0; i = diff.nextSetBit(i+1)) {
						int addr = i << visMemGranularity;
						int size = 1 << visMemGranularity;
						regionToBytes.set(addr, addr + size);
					}
					emitVideoMemoryEvents(os, regionToBytes);
				}
				visibleVideoMemory = newVisibleVideoMemory;
			}
			
			else if (event instanceof VideoWriteDataEvent) {
				// Stifle the event if it doesn't affect visible memory.
				VideoWriteDataEvent vwEvent = (VideoWriteDataEvent) event;
				player.executeEvent(event);
				
				for (int i = 0; i < vwEvent.getLength(); i++) {
					int visAddr = vwEvent.getAddress() + i;
					unrecordedVideoMemory.set(visAddr);
					if (visibleVideoMemory.get(visAddr >>> visMemGranularity)) {
						tempVideoWriteSet.set(visAddr);
					}
				}
				
				// hold off sending events until we encounter a timer tick
//				flushVideoEvents(os, tempVideoWriteSet);
				
				// don't emit the full event
				continue;
			}
			
			os.writeEvent(event);
		}		
		
		flushVideoEvents(os, tempVideoWriteSet);
		updateAndEmitTimerTick(os, is, timer, null);
	}

	/**
	 * @param tempVideoWriteSet
	 * @param os 
	 * @throws NotifyException 
	 */
	private void flushVideoEvents(IDemoOutputStream os, BitSet tempVideoWriteSet) throws IOException {
		// emit the events that do affect visible memory
		if (!tempVideoWriteSet.isEmpty()) {
			emitVideoMemoryEvents(os, tempVideoWriteSet);
			tempVideoWriteSet.clear();
		}
	}


	/**
	 * Emit events recording the contents of video memory for knownVisibleMemoryChanges
	 * and unset them in unrecordedVideoMemory.  
	 * @param os
	 * @param knownVisibleMemoryChanges
	 * @throws NotifyException 
	 */
	private void emitVideoMemoryEvents(IDemoOutputStream os,
			BitSet knownVisibleMemoryChanges) throws IOException {

		if (unrecordedVideoMemory.isEmpty())
			return;
		
		int startAddr = -1;
		int endAddr = -1;
		
		for (int i = knownVisibleMemoryChanges.nextSetBit(0);
				i >= 0;
				i = knownVisibleMemoryChanges.nextSetBit(i + 1)) {
			
			if (unrecordedVideoMemory.get(i)) {
				unrecordedVideoMemory.set(i, false);
				int addr = i;
				if (addr != endAddr) {
					if (endAddr >= 0) {
						emitVideoMemoryEvent(os, startAddr, endAddr);
					}
					startAddr = addr;
				}
				endAddr = addr + 1;
			}
		}
		
		if (startAddr >= 0) {
			emitVideoMemoryEvent(os, startAddr, endAddr);
		}
	}


	/**
	 * @param os
	 * @param startAddr
	 * @param endAddr
	 * @throws NotifyException 
	 */
	private void emitVideoMemoryEvent(IDemoOutputStream os, int startAddr,
			int endAddr) throws IOException {
		int length = endAddr - startAddr;
		ByteMemoryAccess access = machine.getVdp().getByteReadMemoryAccess(startAddr);
		VideoWriteDataEvent ev =  new VideoWriteDataEvent(startAddr, 
				access.memory, access.offset, length);
		os.writeEvent(ev);
		
		for (int i = startAddr; i < endAddr; i++) {
			knownVideoMemory.set(i);
		}
	}

}
