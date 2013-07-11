/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.dnd;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.jkiss.dbeaver.model.navigator.DBNNode;

import java.util.Collection;

/**
 * Used to move DBNNode around in a database navigator.
 */
public final class TreeNodeTransfer extends LocalObjectTransfer<Collection<DBNNode>> {

	private static final TreeNodeTransfer INSTANCE = new TreeNodeTransfer();
	private static final String TYPE_NAME = "DBNNode Transfer"//$NON-NLS-1$
			+ System.currentTimeMillis() + ":" + INSTANCE.hashCode();//$NON-NLS-1$
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton instance.
	 *
	 * @return The singleton instance
	 */
	public static TreeNodeTransfer getInstance() {
		return INSTANCE;
	}

	private TreeNodeTransfer() {
	}

	/**
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	@Override
    protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/**
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	@Override
    protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

    public static Collection<DBNNode> getFromClipboard()
    {
        Clipboard clipboard = new Clipboard(Display.getDefault());
        try {
            return (Collection<DBNNode>) clipboard.getContents(TreeNodeTransfer.getInstance());
        } finally {
            clipboard.dispose();
        }
    }

}
