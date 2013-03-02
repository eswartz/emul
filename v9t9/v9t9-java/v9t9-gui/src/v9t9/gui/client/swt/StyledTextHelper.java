/*
  StyledTextHelper.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt;

import java.util.Stack;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.TextStyle;

/**
 * Handle generating hierarchical text styles in the {@link StyledText}
 * widget, which can only handle linear styles.
 * @author ejs
 *
 */
public class StyledTextHelper {

	private Stack<StyleRange> styleStack = new Stack<StyleRange>();
	private final StyledText text;
	
	public StyledTextHelper(StyledText text) {
		this.text = text;
	}

	public StyleRange pushStyle(TextStyle style, int fontStyle) {
		StyleRange range = new StyleRange(style);
		range.fontStyle = fontStyle;
		range.start = text.getCharCount();
		
		StyleRange curRange = styleStack.isEmpty() ? null : styleStack.peek();
		if (curRange != null) {
			curRange.length = text.getCharCount() - curRange.start;
			text.setStyleRange(curRange);
		}
		styleStack.push(range);
		return range;
		
	}
	public StyleRange popStyle() {
		StyleRange range = styleStack.pop();
		range.length = text.getCharCount() - range.start;
		text.setStyleRange(range);

		StyleRange curRange = styleStack.isEmpty() ? null : styleStack.peek();
		if (curRange != null) {
			// start over
			StyleRange newRange = new StyleRange(curRange);
			newRange.start = text.getCharCount();
			newRange.length = 0;
			newRange.fontStyle = curRange.fontStyle;
			
			styleStack.pop();
			styleStack.push(newRange);
		}
		return range;
	}
}
