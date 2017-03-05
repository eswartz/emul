/*
  IPropertyEditor.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.properties;

import org.eclipse.swt.widgets.Composite;


/**
 * @author ejs
 *
 */
public interface IPropertyEditor {
	IPropertyEditorControl createEditor(Composite parent);
}
