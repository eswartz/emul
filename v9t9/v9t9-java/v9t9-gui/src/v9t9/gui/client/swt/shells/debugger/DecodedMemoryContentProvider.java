/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import v9t9.gui.common.IMemoryDecoder;

/**
 * This decodes memory according to the attached memory decoders for
 * each row of memory.   
 * @author ejs
 *
 */
public class DecodedMemoryContentProvider implements ILazyContentProvider {

	MemoryRange range;
	private TableViewer tableViewer;
	private final IMemoryDecoder decoderProvider;
	
	public DecodedMemoryContentProvider(IMemoryDecoder decoder) {
		this.decoderProvider = decoder;
	}
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		if (range != null)
			range.removeMemoryListener();
		
		range = (MemoryRange) newInput;
		if (range != null) {
			range.attachMemoryListener();

			decoderProvider.initialize(range);
			
			// clear
			tableViewer.setItemCount(0);
			tableViewer.refresh(true); 
			
			// reset
			tableViewer.setItemCount(decoderProvider.getItemCount());
			tableViewer.refresh(true);
			
		} else {
			decoderProvider.initialize(null);
			tableViewer.setItemCount(0);
			tableViewer.refresh(true);
		}
	}

	public void updateElement(int index) {
		if (range == null)
			return;
		
		/*
		//System.out.println(index);
		int addr = index * chunkSize;
		DecodedRow row = (DecodedRow) tableViewer.getElementAt(index);
		if (row == null) {
			Pair<Object, Integer> info = decoderProvider.decode(addr,chunkSize);
			row = new DecodedRow(addr, range, info.first, info.second);
		}
		*/
		DecodedRow row = (DecodedRow) tableViewer.getElementAt(index);
		if (row == null) {
			IDecodedContent content = decoderProvider.decodeItem(index);
			row = new DecodedRow(content, range);
		}
		
		tableViewer.replace(row, index);
	}
	
	
}