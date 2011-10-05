/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.swt.graphics.Image;

/**
 * A subclass of DecoratingLabelProvider provides an FS Tree Viewer
 * with a label provider which combines a nested label provider and an optional
 * decorator. The decorator decorates the label text, image
 * provided by the nested label provider.
 *
 */
public class FSTreeDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider {

	//The label provider for the execution context viewer.
	private FSTreeLabelProvider fProvider;
	//The label decorator decorating the above label provider.
	private ILabelDecorator fDecorator;

	/**
	 * Create a FSTreeDecoratingLabelProvider with an FSTreeLabelProvider and a decorator.
	 *
	 * @param provider The label provider to be decorated.
	 * @param decorator The label decorator.
	 */
	public FSTreeDecoratingLabelProvider(FSTreeLabelProvider provider, ILabelDecorator decorator) {
		super(provider, decorator);
		fProvider = provider;
		fDecorator = decorator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		Image image = fProvider.getColumnImage(element, columnIndex);
		if (columnIndex == 0 && fDecorator != null) {
			if (fDecorator instanceof LabelDecorator) {
				LabelDecorator ld2 = (LabelDecorator) fDecorator;
				Image decorated = ld2.decorateImage(image, element, getDecorationContext());
				if (decorated != null) {
					return decorated;
				}
			} else {
				Image decorated = fDecorator.decorateImage(image, element);
				if (decorated != null) {
					return decorated;
				}
			}
		}
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		String text = fProvider.getColumnText(element, columnIndex);
		if (columnIndex == 0 && fDecorator != null) {
			if (fDecorator instanceof LabelDecorator) {
				LabelDecorator ld2 = (LabelDecorator) fDecorator;
				String decorated = ld2.decorateText(text, element, getDecorationContext());
				if (decorated != null) {
					return decorated;
				}
			} else {
				String decorated = fDecorator.decorateText(text, element);
				if (decorated != null) {
					return decorated;
				}
			}
		}
		return text;
	}

}
