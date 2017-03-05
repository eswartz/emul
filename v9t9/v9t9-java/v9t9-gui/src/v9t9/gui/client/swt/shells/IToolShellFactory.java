/*
  IToolShellFactory.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Implementors of this factory provide "tool shells", which are
 * dialogs that pop up over the emulator, usually in response
 * to a press on the emulator button bar, and may remain until
 * the user toggles them or the user clicks elsewhere. 
 * @author ejs
 *
 */
public interface IToolShellFactory {
	/** Center the shell inside the emulator or outside it? */
	public enum Centering {
		INSIDE,
		OUTSIDE,
		CENTER,
		BELOW
	}
	
	/** The behavior of the shell */
	public static class Behavior {
		/** Control the location where the tool shell is centered. */
		public Centering centering;
		/** When non-<code>null</code>, keep the tool shell centered over this control */
		public Control centerOverControl;
		/** If true, a user click on the emulator window outside
		 * the tool shell will dismiss it.
		 */
		public boolean dismissOnClickOutside;
		/** SWT shell style
		 */
		public int style = SWT.TOOL | SWT.RESIZE | SWT.CLOSE | SWT.TITLE;
		/** Name of the preference storing the boundary of the window when
		 * it is dismissed.
		 */
		public String boundsPref;
		public Rectangle defaultBounds;
		/** Tell whether the shell is initially visible, or whether the tool shell will show it */
		public boolean initiallyVisible = true;
	}
	
	/** Provides the tool shell behavior (queried once when a shell is created) */
	Behavior getBehavior();
	
	/** Implements the tool shell content */
	Control createContents(Shell shell);
}