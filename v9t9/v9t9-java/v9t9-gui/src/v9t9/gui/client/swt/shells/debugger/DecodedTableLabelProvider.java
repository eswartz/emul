/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class DecodedTableLabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

	private final ILabelProvider contentLabelProvider;
	private final int chunkSize;

	/**
	 * @param contentLabelProvider 
	 * 
	 */
	public DecodedTableLabelProvider(ILabelProvider contentLabelProvider, int chunkSize) {
		this.contentLabelProvider = contentLabelProvider;
		this.chunkSize = chunkSize;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		DecodedRow row = (DecodedRow) element;
		IDecodedContent content = row.getContent();
		if (columnIndex == 0)
			 return HexUtils.toHex4(content.getAddr());
		if (columnIndex == 1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < content.getSize(); i++) {
				if (i > 0 && i % chunkSize == 0)
					sb.append(' ');
				sb.append(HexUtils.toHex2(
						row.getRange().readByte(
								 content.getAddr() + i)));
			}
			return sb.toString();
		}
		return contentLabelProvider.getText(content);
	}

}
