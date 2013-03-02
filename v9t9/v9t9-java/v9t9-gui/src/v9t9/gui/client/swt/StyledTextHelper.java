/*
  StyledTextHelper.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
