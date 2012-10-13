/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Timer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.asm.IDecompilePhase;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.shells.IToolShellFactory;
import v9t9.gui.common.IMemoryDecoder;
import v9t9.gui.common.IMemoryDecoderProvider;
import ejs.base.settings.ISettingSection;

/**
 * @author Ed
 *
 */
public class DebuggerWindow extends Composite implements IMemoryWriteListener {

	private static final int NUM_MEMORY_VIEWERS = 3;
	private SashForm horizSash;
	/*private*/ final IMachine machine;
	/*private*/ CpuViewer cpuViewer;
	/*private*/ MemoryViewer[] memoryViewers;
	private SashForm vertSash;
	private RegisterViews regViewer;
	public static final String DEBUGGER_TOOL_ID = "debugger";

	public DebuggerWindow(Shell parent, int style, IMachine machine_, Timer timer) {
		super(parent, style);
		this.machine = machine_;
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		parent.setText("V9t9 Debugger");
		
		
		horizSash = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(horizSash);
		
		SashForm innerSash = new SashForm(horizSash, SWT.VERTICAL);
		
		cpuViewer = new CpuViewer(innerSash, SWT.BORDER, machine, timer);
		regViewer = new RegisterViews(innerSash, SWT.BORDER, machine);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(cpuViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(regViewer);

		cpuViewer.addTracker(regViewer);
		
		vertSash = new SashForm(horizSash, SWT.VERTICAL);
		
		final ISettingSection history = machine.getSettings().getInstanceSettings()
			.getHistorySettings().findOrAddSection(DEBUGGER_TOOL_ID);
		
		IMemoryDecoderProvider memoryDecoderProvider = createMemoryDecoderProvider();
		
		memoryViewers = new MemoryViewer[NUM_MEMORY_VIEWERS];
		for (int v = 0; v < memoryViewers.length; v++) {
			memoryViewers[v] = new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), 
					memoryDecoderProvider, timer);
			cpuViewer.addTracker(memoryViewers[v]);

		}
		
		for (IMemoryDomain domain : machine.getMemory().getDomains()) {
			domain.addWriteListener(this);
		}
		

		String[] sweights = history.getArray("SashDivisions");
		if (sweights != null && sweights.length == memoryViewers.length) {
			int[] weights = new int[sweights.length];
			boolean valid = true;
			for (int v = 0; v < sweights.length; v++) {
				try {
					weights[v] = Integer.parseInt(sweights[v]);
				} catch (NumberFormatException e) {
					valid = false;
					break;
				}
			}
			if (valid) {
				vertSash.setWeights(weights);
			}
		}
		
		
		for (int v = 0; v < memoryViewers.length; v++) {
			memoryViewers[v].loadState(history.getSection("MemoryViewer." + v));
		}
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {

				for (IMemoryDomain domain : machine.getMemory().getDomains()) {
					domain.removeWriteListener(DebuggerWindow.this);
				}
				for (int v = 0; v < memoryViewers.length; v++) {
					memoryViewers[v].saveState(history.findOrAddSection("MemoryViewer." + v));
				}
				
				int[] weights = vertSash.getWeights();
				String[] sweights = new String[weights.length];
				for (int v = 0; v < sweights.length; v++) {
					sweights[v] = "" + weights[v];
				}
				history.put("SashDivisions", sweights);
			}
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryWriteListener#changed(v9t9.common.memory.IMemoryEntry, int, java.lang.Number)
	 */
	@Override
	public void changed(IMemoryEntry entry, int addr, Number value) {
		if (machine.isPaused()) {
			for (final MemoryViewer viewer : memoryViewers) {
				if (viewer.contains(entry, addr)) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							viewer.refreshViewer();
						}
					});
				}
			}
		}
	}

	/**
	 * TODO: make generic
	 * @return
	 */
	private IMemoryDecoderProvider createMemoryDecoderProvider() {
		return new IMemoryDecoderProvider() {
			DisassemblerDecoder cpuDecoder = null;
			@Override
			public IMemoryDecoder getMemoryDecoder(IMemoryEntry entry) {
				if (entry.getDomain().getIdentifier().equals(IMemoryDomain.NAME_CPU)) {
					if (true||cpuDecoder == null) {
						IDecompilePhase decompiler = machine.getCpu().createDecompiler();
						if (decompiler != null) {
							cpuDecoder = new DisassemblerDecoder(entry.getDomain(), 
									machine.getCpu().getInstructionFactory(),
									machine.getCpu().createDecompiler());
						}
					}
					return cpuDecoder;
				}
				return null;
			}
		};
	}


	/**
	 * @param machine2
	 * @param buttonBar
	 * @param timer 
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final ImageCanvas buttonBar, final Timer timer) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "DebuggerWindowBounds";
				behavior.dismissOnClickOutside = false;
				behavior.centerOverControl = buttonBar;
			}
			public Control createContents(Shell shell) {
				return new DebuggerWindow(shell, SWT.NONE, machine, timer);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
}
