/*
  BaseSwtJavaClient.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

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
import v9t9.gui.client.swt.wizards.SetupWizard;
import v9t9.gui.sound.JavaSoundHandler;
import ejs.base.timer.FastTimer;

/**
 * @author ejs
 *
 */
public abstract class BaseSwtJavaClient implements IClient {

	static {
		// set early for GNOME Shell
		Display.setAppName("V9t9");
	}
	
	protected IVdpChip video;
	protected IMachine machine;
	protected ISwtVideoRenderer videoRenderer;
	protected Display display;
	private long avgUpdateTime;
	protected long expectedUpdateTime;
	private int displaySkips;
	
	protected final int QUANTUM = 1000 / 60;
    protected final int soundTick = 100;
    protected final int clientTick = 100;
    protected final int videoUpdateTick = 30;

	protected IEventNotifier eventNotifier;
	protected final ISettingsHandler settingsHandler;
	protected ISoundHandler soundHandler;
	protected FastTimer videoTimer;
	protected FastTimer keyTimer;
	private FastTimer soundTimer;
	private SwtWindow window;

	protected IKeyboardHandler keyboardHandler;

	/**
	 * @param machine 
	 * 
	 */
	public BaseSwtJavaClient(final IMachine machine) {
		this.settingsHandler = Settings.getSettings(machine);
		this.display = Display.getDefault();
    	this.video = machine.getVdp();
    	this.machine = machine;
    	machine.setClient(this);
    	
    	this.videoTimer = new FastTimer("Video Timer");
    	this.keyTimer = new FastTimer("Keyboard/Joystick Timer");
    	this.soundTimer = new FastTimer("Sound Timer");
    	
    	setupRenderer();
    	        
        soundHandler = new JavaSoundHandler(machine);
        
        window = new SwtWindow(display, machine, 
        		(ISwtVideoRenderer) videoRenderer, settingsHandler,
        		soundHandler);
        eventNotifier = window.getEventNotifier();

        keyboardHandler.setEventNotifier(eventNotifier);
        
        //window.setSwtVideoRenderer((ISwtVideoRenderer) videoRenderer);

        expectedUpdateTime = QUANTUM;
        
        //video.setCanvas(videoRenderer.getCanvas());

        Shell shell = window.getShell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
		        if (machine.isAlive()) {
		        	machine.stop();
		        }
			}
		});
		
		keyboardHandler.init(videoRenderer);
        
//        TimerTask clientTask = new TimerTask() {
//        	
//        	@Override
//        	public void run() {
//        		//System.out.print('.');
//				keyboardHandler.scan(machine.getKeyboardState());
//        	}
//        };
//        keyTimer.scheduleTask(clientTask, clientTick);
        
        TimerTask videoUpdateTask = new TimerTask() {

            @Override
			public void run() {
            	updateVideo();
            }
        };
        videoTimer.scheduleTask(videoUpdateTask, videoUpdateTick);
        
        // update sound as often as registers change
        final TimerTask soundGenerateTask = new TimerTask() {
			@Override
			public void run() {
				int pos;
				int total;
				synchronized (machine.getCpu()) {
					pos = machine.getCpu().getCurrentCycleCount();
					total = machine.getCpu().getCurrentTargetCycleCount();
				}
				soundHandler.generateSound(pos, total);

			}
		};
        machine.getSound().addWriteListener(new IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				soundTimer.invoke(soundGenerateTask);
			}
		});
        
        // update speech as soon as possible
        final TimerTask speechGenerateTask = new TimerTask() {
			@Override
			public void run() {
				soundTimer.invoke(soundGenerateTask);
			}
		};
		final TimerTask speechDoneTask = new TimerTask() {
			@Override
			public void run() {
				int pos;
				int total;
				synchronized (machine.getCpu()) {
					pos = machine.getCpu().getCurrentCycleCount();
					total = machine.getCpu().getCurrentTargetCycleCount();
				}
				soundHandler.flushAudio(pos, total);
			}
		};
        machine.getSpeech().addSpeechListener(new ISpeechDataSender() {
			
			@Override
			public void sendSample(short val, int pos, int length) {
				soundTimer.invoke(speechGenerateTask);
			}
			
			@Override
			public void speechDone() {
				soundTimer.invoke(speechDoneTask);
			}
		});
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

	abstract protected void setupRenderer();

	/**
	 * @param display2
	 * @return
	 */
	protected ISwtVideoRenderer createSwtVideoRenderer(Display display2) {
	   	ISwtVideoRenderer videoRenderer = null;
		if (false && videoRenderer == null && SWT.getPlatform().equals("gtk")) {
	    	// try OpenGL first ?
	    	try {
	    		Class<?> klass = Class.forName(
	    				SwtVideoRenderer.class.getName() + "OGL");
	    		videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load OpenGL/GTK-specific support: " +e.getMessage());
	    	}
	    }
	
	    if (false && videoRenderer == null) {
	    	// try J3D first ?
	    	try {
	    		Class<?> klass = Class.forName(
	    				SwtVideoRenderer.class.getName() + "J3D");
	    		videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load J3D support: " +e.getMessage());
	    	}
	    }
	
	    /*
	    if (videoRenderer == null && SWT.getPlatform().equals("gtk")) {
	    	try {
	        	Class<?> klass = Class.forName(
	        			SwtVideoRenderer.class.getName() + "GTK");
	        	videoRenderer = (ISwtVideoRenderer) klass.getConstructor().newInstance();
	    	} catch (Exception e) {
	    		System.err.println("Cannot load GTK-specific support: " +e.getMessage());
	    	}
	    }*/
	    if (videoRenderer == null)
	    	videoRenderer = new SwtVideoRenderer(machine);
	    return videoRenderer;
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
        if (settingsHandler.get(settingNewConfiguration).getBoolean()) {
//        	ROMSetupDialog dialog = ROMSetupDialog.createDialog(window.getShell(), machine, window);
//        	dialog.open();
        	SetupWizard wizard = new SetupWizard(machine, window, SetupWizard.Page.INTRO);
        	WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        	dialog.open();
        }
	}
	
	public void close() {
		soundTimer.cancel();
		if (soundHandler != null)
			soundHandler.dispose();
		
		keyTimer.cancel();
		videoTimer.cancel();
		try {
			machine.stop();
		} catch (TerminatedException e) {
			// expected
		}
		
		display.asyncExec(new Runnable() {
			public void run() {
				if (videoRenderer != null)
					videoRenderer.dispose();
			}
		});
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void updateVideo() {
		//long start = System.currentTimeMillis();
		if (videoRenderer.isIdle() && videoRenderer.isVisible()) {
			/*
			try {
				if (!video.update())
					return;
			} catch (Throwable t) {
				t.printStackTrace();
			}
			*/
			videoRenderer.getCanvasHandler().update();
			videoRenderer.redraw();
			// compensate for slow frames
	    	long elapsed = videoRenderer.getLastUpdateTime() * 4;
	    	//System.out.println(elapsed + " / " + expectedUpdateTime);
			if (elapsed * 2 >= expectedUpdateTime) {
				//System.out.println("slow :" + elapsed);
				//displaySkips += (elapsed + 1000 / 60 - 1) / (1000 / 60);
				expectedUpdateTime <<= 1;
				if (expectedUpdateTime > 1000)
					expectedUpdateTime = 1000;
				displaySkips = 0;
				//nextVideoUpdate = next + avgUpdateTime;
			} else if (elapsed <= expectedUpdateTime / 2 && elapsed >= QUANTUM) {
				displaySkips++;
				if (displaySkips > 30) {
					//System.out.println("fast :" + (elapsed));
	    			expectedUpdateTime >>= 1;
	    			if (expectedUpdateTime < QUANTUM)
	    				expectedUpdateTime = QUANTUM;
	    			displaySkips = 0;
				}
				//nextVideoUpdate = start + 1000 / 60;
			} else {
				displaySkips = 0;
			}
			//nextVideoUpdate = System.currentTimeMillis() + expectedUpdateTime * 4;
			avgUpdateTime = (avgUpdateTime + elapsed * 9) / 10; 
		} else {
			//displaySkips--;
		}
	}

	public void handleEvents() {
		display.syncExec(new Runnable() {
			public void run() {
				try {
					if (display.isDisposed())
						return;
					while (display.readAndDispatch()) ;
					//display.sleep();
				} catch (SWTException e) {
					e.printStackTrace();
				} catch (SWTError e) {
					e.printStackTrace();
				}
			}
		});
	}

	public boolean isAlive() {
		return !display.isDisposed();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#asyncExecInUI(java.lang.Runnable)
	 */
	@Override
	public void asyncExecInUI(final Runnable runnable) {
		machine.asyncExec(new Runnable() {
			public void run() {
				display.syncExec(runnable);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IClient#getVideoRenderer()
	 */
	@Override
	public IVideoRenderer getVideoRenderer() {
		return videoRenderer;
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
		if (source instanceof DemoContentSource)
			return new IEmulatorContentHandler[] { new DemoContentHandler((DemoContentSource) source) };
		if (source instanceof ModuleContentSource)
			return new IEmulatorContentHandler[] { new ModuleContentHandler((ModuleContentSource) source) };
		if (source instanceof EmulatedDiskContentSource) {
			EmulatedDiskContentSource diskSource = (EmulatedDiskContentSource) source;
			IFileExecutionHandler execHandler = machine.getEmulatedFileHandler().getFileExecutionHandler();
			
			diskSource.getCatalog().deviceName = diskSource.getDevice();
			 
			IFileExecutor[] execs = execHandler.analyze(machine, diskSource.getDrive(), diskSource.getCatalog());
			
			IFileExecutor[] allExecs = new IFileExecutor[execs.length + 1];
			allExecs[0] = new DoNothingFileExecutor(diskSource.getContent(), "Load disk");
			System.arraycopy(execs, 0, allExecs, 1, execs.length);
			execs = allExecs;
			
			IEmulatorContentHandler[] handlers = new IEmulatorContentHandler[execs.length];
			for (int i = 0; i < execs.length; i++) {
				handlers[i] = new FileExecutorContentHandler(window.getShell(), machine, diskSource, execs[i]);
			}
			return handlers;
		}
		return IEmulatorContentHandler.NONE;
	}

}