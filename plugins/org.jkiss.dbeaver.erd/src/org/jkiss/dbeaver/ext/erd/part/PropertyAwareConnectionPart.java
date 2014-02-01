/*
 * Copyright (C) 2010-2014 Serge Rieder
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
/*
 * Created on Jul 15, 2004
 */
package org.jkiss.dbeaver.ext.erd.part;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.jkiss.dbeaver.ext.erd.model.ERDObject;
import org.jkiss.dbeaver.model.DBPNamedObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An ConnectionEditPart base class which is property aware, that is, can handle property change notification events
 * All our ConnectionEditPart are subclasses of this
 * @author Serge Rieder
 */
public abstract class PropertyAwareConnectionPart extends AbstractConnectionEditPart implements PropertyChangeListener, DBPNamedObject
{
    @Override
    public String getName()
    {
        return ((ERDObject)getModel()).getName();
    }

    protected boolean isEditEnabled()
    {
        return getRoot().getContents() instanceof DiagramPart && ((DiagramPart) getRoot().getContents()).getDiagram().isLayoutManualAllowed();
    }

	/**
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	@Override
    public void activate()
	{
		super.activate();
		ERDObject<?> erdObject = (ERDObject<?>) getModel();
		erdObject.addPropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.gef.EditPart#deactivate()
	 */
	@Override
    public void deactivate()
	{
		super.deactivate();
		ERDObject<?> erdObject = (ERDObject<?>) getModel();
		erdObject.removePropertyChangeListener(this);
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(PropertyChangeEvent evt)
	{

		String property = evt.getPropertyName();

		if (ERDObject.CHILD.equals(property))
			refreshChildren();
		else if (ERDObject.INPUT.equals(property))
			refreshTargetConnections();
		else if (ERDObject.OUTPUT.equals(property))
			refreshSourceConnections();

		/*
		 * if (FlowElement.CHILDREN.equals(prop)) refreshChildren(); else if
		 * (FlowElement.INPUTS.equals(prop)) refreshTargetConnections(); else if
		 * (FlowElement.OUTPUTS.equals(prop)) refreshSourceConnections(); else
		 * if (Activity.NAME.equals(prop)) refreshVisuals(); // Causes Graph to
		 * re-layout
		 */
		((GraphicalEditPart) (getViewer().getContents())).getFigure().revalidate();
	}

}