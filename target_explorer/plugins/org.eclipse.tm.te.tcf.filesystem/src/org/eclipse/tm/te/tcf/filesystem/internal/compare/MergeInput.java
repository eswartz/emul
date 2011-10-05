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
package org.eclipse.tm.te.tcf.filesystem.internal.compare;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.swt.graphics.Image;

/**
 * An abstract compare input whose purpose is to support change notification
 * through a {@link CompareInputChangeNotifier}.
 */
public class MergeInput implements ICompareInput {

	// The left element.
	private ITypedElement left;
	// The right element.
	private ITypedElement right;
	// The compare input change listener list.
	private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	/**
	 * Create a <code>MergeInput</code>.
	 * @param left the left element.
	 * @param right the right element.
	 */
	public MergeInput(ITypedElement left, ITypedElement right) {
		this.left = left;
		this.right = right;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	@Override
	public void addCompareInputChangeListener(ICompareInputChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	@Override
	public void removeCompareInputChangeListener(ICompareInputChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Fire a compare input change event. This method must be called from the UI
	 * thread.
	 */
	void fireInputChanged() {
		if (!listeners.isEmpty()) {
			Object[] _listeners = listeners.getListeners();
			for (int i = 0; i < _listeners.length; i++) {
				final ICompareInputChangeListener listener = (ICompareInputChangeListener) _listeners[i];
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.compareInputChanged(MergeInput.this);
					}

					@Override
					public void handleException(Throwable exception) {
						// Logged by the safe runner
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#copy(boolean)
	 */
	@Override
	public void copy(boolean leftToRight) {
		Assert.isTrue(false, "Copy is not support by this type of compare input"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getAncestor()
	 */
	@Override
	public ITypedElement getAncestor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getImage()
	 */
	@Override
	public Image getImage() {
		ITypedElement element = getMainElement();
		return element == null ? null : element.getImage();
	}

	/**
	 * Return the main non-null element that identifies this input. By default,
	 * the fLeft is returned if non-null. If the fLeft is null, the fRight is
	 * returned. If both the fLeft and fRight are null the ancestor is returned.
	 *
	 * @return the main non-null element that identifies this input
	 */
	private ITypedElement getMainElement() {
		if (left != null)
			return left;
		if (right != null)
			return right;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getKind()
	 */
	@Override
	public int getKind() {
		return Differencer.CHANGE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getLeft()
	 */
	@Override
	public ITypedElement getLeft() {
		return left;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getName()
	 */
	@Override
	public String getName() {
		ITypedElement element = getMainElement();
		return element == null ? null : element.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getRight()
	 */
	@Override
	public ITypedElement getRight() {
		return right;
	}
}
