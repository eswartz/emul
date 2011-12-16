/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;

/**
 * Full qualified id implementation.
 */
public class FullQualifiedId implements IFullQualifiedId {
	// The list of id's
	private final List<IdData> ids = new ArrayList<IdData>();

	// Inner class describing the data of an id.
	private class IdData {
		public String type = null;
		public String id = null;
		public String secondaryId = null;

		/**
		 * Constructor.
		 *
		 * @param type The id type.
		 * @param id The id. Must not be <code>null</code>.
		 * @param secondaryId The secondary id.
		 */
		public IdData(String type, String id, String secondaryId) {
			Assert.isNotNull(id);

			this.type = type;
			this.id = id;
			this.secondaryId = secondaryId;
		}

		/**
		 * Creates a string representation of the id and the given children.
		 */
		public String toString(String children) {
			String typeStr = type != null && type.trim().length() > 0 ? type.trim() : "ID"; //$NON-NLS-1$

			StringBuilder toString = new StringBuilder();
			toString.append('<');
			toString.append(typeStr);
			toString.append(" id=\""); //$NON-NLS-1$
			toString.append(id.trim());
			toString.append('"');
			if (secondaryId != null && secondaryId.trim().length() > 0) {
				toString.append(" secondaryId=\""); //$NON-NLS-1$
				toString.append(secondaryId.trim());
				toString.append('"');
			}
			if (children != null && children.trim().length() > 0) {
				toString.append('>');
				toString.append(children);
				toString.append("</"); //$NON-NLS-1$
				toString.append(type);
				toString.append('>');
			}
			else {
				toString.append("/>"); //$NON-NLS-1$
			}
			return toString.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return toString(null);
		}
	}

	/**
	 * Constructor.
	 */
	public FullQualifiedId(String type, String id, String secondaryId) {
		ids.add(new IdData(type, id, secondaryId));
	}

	/*
	 * Private constructor for child and parent creation.
	 * To create the parent, id has to be null!
	 */
	private FullQualifiedId(IdData[] parentData, String type, String id, String secondaryId) {
		for (IdData parent : parentData) {
			ids.add(new IdData(parent.type, parent.id, parent.secondaryId));
		}
		if (id != null) {
			ids.add(new IdData(type, id, secondaryId));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId#createChildId(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IFullQualifiedId createChildId(String type, String id, String secondaryId) {
		return new FullQualifiedId(ids.toArray(new IdData[ids.size()]), type, id, secondaryId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId#getType()
	 */
	@Override
	public String getType() {
		return getIdData().type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId#getId()
	 */
	@Override
	public String getId() {
		return getIdData().id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId#getSecondaryId()
	 */
	@Override
	public String getSecondaryId() {
		return getIdData().secondaryId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId#getParentId()
	 */
	@Override
	public IFullQualifiedId getParentId() {
		if (ids.size() > 1) {
			return new FullQualifiedId(ids.subList(0, ids.size() - 1)
			                .toArray(new IdData[ids.size() - 1]), null, null, null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Assert.isTrue(!ids.isEmpty());

		String toString = getIdData().toString();
		if (ids.size() > 1) {
			for (int i = ids.size() - 1; i > 0; i--) {
				toString = ids.get(i - 1).toString(toString);
			}
		}
		return toString;
	}

	private IdData getIdData() {
		Assert.isTrue(!ids.isEmpty());
		return ids.get(ids.size() - 1);
	}
}
