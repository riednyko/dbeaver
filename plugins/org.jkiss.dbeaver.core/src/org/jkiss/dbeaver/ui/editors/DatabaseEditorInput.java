/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.IDataSourceContainerProvider;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.DBECommandContextImpl;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.properties.PropertySourceEditable;

import java.util.*;

/**
 * DatabaseEditorInput
 */
public abstract class DatabaseEditorInput<NODE extends DBNDatabaseNode> implements IPersistableElement, IDatabaseEditorInput, IDataSourceContainerProvider
{
    private final NODE node;
    private final DBCExecutionContext executionContext;
    private final DBECommandContext commandContext;
    private String defaultPageId;
    private String defaultFolderId;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private PropertySourceEditable propertySource;

    protected DatabaseEditorInput(NODE node)
    {
        this(node, null);
    }

    protected DatabaseEditorInput(NODE node, DBECommandContext commandContext)
    {
        this.node = node;
        this.executionContext = node.getDataSource().getDefaultContext(false);
        this.commandContext = commandContext != null ?
            commandContext :
            new DBECommandContextImpl(
                this.executionContext,
                false);
    }

    @Override
    public boolean exists()
    {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return ImageDescriptor.createFromImage(node.getNodeIconDefault());
    }

    @Override
    public String getName()
    {
        if (DBeaverCore.getGlobalPreferenceStore().getBoolean(DBeaverPreferences.NAVIGATOR_EDITOR_FULL_NAME)) {
            return node.getNodeFullName();
        } else {
            return node.getName();
        }
    }

    @Override
    public IPersistableElement getPersistable()
    {
        return getExecutionContext() == null ? null : this;
    }

    @Override
    public String getToolTipText()
    {
        return node.getNodeDescription();
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        if (IWorkbenchAdapter.class.equals(adapter)) {
            return new WorkbenchAdapter() {
                @Override
                public ImageDescriptor getImageDescriptor(Object object) {
                    return ImageDescriptor.createFromImage(node.getNodeIconDefault());
                }
                @Override
                public String getLabel(Object o) {
                    return node.getName();
                }
                @Override
                public Object getParent(Object o) {
                    return node.getParentNode();
                }
            };
        }

        return null;
    }

    @Override
    public DBSDataSourceContainer getDataSourceContainer()
    {
        return executionContext.getDataSource().getContainer();
    }

    @Override
    public DBCExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Override
    public NODE getTreeNode()
    {
        return node;
    }

    @Override
    public DBSObject getDatabaseObject()
    {
        return node.getObject();
    }

    @Override
    public String getDefaultPageId()
    {
        return defaultPageId;
    }

    @Override
    public String getDefaultFolderId()
    {
        return defaultFolderId;
    }

    @Override
    public DBECommandContext getCommandContext()
    {
        return commandContext;
    }

    public void setDefaultPageId(String defaultPageId)
    {
        this.defaultPageId = defaultPageId;
    }

    public void setDefaultFolderId(String defaultFolderId)
    {
        this.defaultFolderId = defaultFolderId;
    }

    @Override
    public Collection<String> getAttributeNames() {
        return new ArrayList<String>(attributes.keySet());
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object setAttribute(String name, Object value) {
        if (value == null) {
            return attributes.remove(name);
        } else {
            return attributes.put(name, value);
        }
    }

    @Override
    public IPropertySource2 getPropertySource()
    {
        if (propertySource == null) {
            propertySource = new PropertySourceEditable(
                getCommandContext(),
                getTreeNode(),
                getDatabaseObject());
            propertySource.collectProperties();
        }
        return propertySource;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this ||
            (obj instanceof DatabaseEditorInput && ((DatabaseEditorInput<?>)obj).node.equals(node));
    }

    @Override
    public String getFactoryId()
    {
        return DatabaseEditorInputFactory.ID_FACTORY;
    }

    @Override
    public void saveState(IMemento memento)
    {
        DatabaseEditorInputFactory.saveState(memento, this);
    }
}
