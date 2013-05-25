/**
 * 
 */
package v9t9.gui.client.swt.handlers;

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;

import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.demos.DemoContentSource;
import v9t9.common.demos.IDemo;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class DemoContentHandler implements IEmulatorContentHandler {

	private IMachine machine;
	private IDemo demo;

	/**
	 * @param machine
	 */
	public DemoContentHandler(DemoContentSource source) {
		this.machine = source.getMachine();
		this.demo = source.getContent();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getImage(v9t9.common.client.IEmulatorContentSource)
	 */
	@Override
	public ImageDescriptor getImage() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getLabel(v9t9.common.client.IEmulatorContentSource)
	 */
	@Override
	public String getLabel() {
		return "Play demo: " + demo.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getDescription(v9t9.common.client.IEmulatorContentSource)
	 */
	@Override
	public String getDescription() {
		return "Pauses the emulator to play back the given demo.";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#handleContent(v9t9.common.client.IEmulatorContentSource)
	 */
	@Override
	public void handleContent() throws NotifyException {
		try {
			machine.getDemoHandler().startPlayback(demo.getURI());
		} catch (IOException e) {
			throw new NotifyException(null, "Failed to play demo", e);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#requireConfirmation()
	 */
	@Override
	public boolean requireConfirmation() {
		return true;
	}

}
