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

package org.jkiss.dbeaver.model.data;

import org.eclipse.jface.action.IContributionManager;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPPropertyManager;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

/**
 * DBD Value Handler.
 * Extract, edit and bind database values.
 */
public interface DBDValueHandler 
{
    // Default value, means no features are supported
    int FEATURE_NONE = 0;
    // VIEWER is ability to render value editor in separate panel
    int FEATURE_VIEWER = 1;
    // EDITOR is ability to show value editor in separate dialog or standalone editor
    int FEATURE_EDITOR = 2;
    // INLINE_EDITOR is ability to show editor in grid cell
    int FEATURE_INLINE_EDITOR = 4;
    // SHOW_ICON means grid should render type icon before cell value
    int FEATURE_SHOW_ICON = 8;
    // FEATURE_COMPOSITE composite values (which doesn't have their own "value" but have nested valuable elements)
    int FEATURE_COMPOSITE = 16;

    /**
     * Handler features. Bit set.
     * See constants FEATURE_*
     * @return features bits
     */
    int getFeatures();

    /**
     * Gets value object's type.
     * May return base interface of object's type -
     * it is not required to return exact implementation class
     * (moreover it may be unknown before certain value is extracted)
     * @return value object type
     */
    Class getValueObjectType();

    /**
     * Extracts object from result set
     *
     * @param session session
     * @param resultSet result set
     * @param type attribute type
     * @param index attribute index (zero based)
     * @return value or null
     * @throws org.jkiss.dbeaver.model.exec.DBCException on error
     */
    @Nullable
    Object fetchValueObject(@NotNull DBCSession session, @NotNull DBCResultSet resultSet, @NotNull DBSTypedObject type, int index)
        throws DBCException;

    /**
     * Binds specified parameter to statement
     *
     * @param session execution context
     * @param statement statement
     * @param type attribute type
     * @param index parameter index (zero based)
     * @param value parameter value (can be null). Value is get from fetchValueObject function or from
     * object set by editor (editValue function).  @throws org.jkiss.dbeaver.model.exec.DBCException on error
     */
    void bindValueObject(@NotNull DBCSession session, @NotNull DBCStatement statement, @NotNull DBSTypedObject type, int index, @Nullable Object value)
        throws DBCException;

    /**
     * Creates new value from object.
     * Must analyse passed object and convert it (if possible) to appropriate handler's type.
     * For null objects returns null of DBDValue marked as null
     *
     *
     * @param session execution context
     * @param type attribute type
     * @param object source object
     * @param copy
     * @return initial object value
     * @throws org.jkiss.dbeaver.model.exec.DBCException on error
     */
    @Nullable
    Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, @Nullable Object object, boolean copy)
        throws DBCException;

    /**
     * Release any internal resources associated with this value.
     * This method is called after value binding and statement execution/close.
     * @param value value
     */
    void releaseValueObject(@Nullable Object value);

    /**
     * Converts value to human readable format
     *
     * @param column column
     * @param value value
     * @param format string format
     * @return formatted string
     */
    @NotNull
    String getValueDisplayString(@NotNull DBSTypedObject column, @Nullable Object value, @NotNull DBDDisplayFormat format);

    /**
     * Fills context menu for certain value
     *
     * @param manager context menu manager
     * @param controller value controller
     * @throws DBCException on error
     */
    void contributeActions(@NotNull IContributionManager manager, @NotNull DBDValueController controller)
        throws DBCException;

    /**
     * Fills value's custom properties
     * @param propertySource property source
     * @param controller value controller
     */
    void contributeProperties(@NotNull DBPPropertyManager propertySource, @NotNull DBDValueController controller);

    /**
     * Creates value editor.
     * Value editor could be:
     * <li>inline editor (control created withing inline placeholder)</li>
     * <li>dialog (modal or modeless)</li>
     * <li>workbench editor</li>
     * Modeless dialogs and editors must implement DBDValueEditor and
     * must register themselves within value controller. On close they must unregister themselves within
     * value controller.
     * @param controller value controller  @return true if editor was successfully opened.
     * makes since only for inline editors, otherwise return value is ignored.
     * @return true on success
     * @throws org.jkiss.dbeaver.DBException on error
     */
    @Nullable
    DBDValueEditor createEditor(@NotNull DBDValueController controller)
        throws DBException;

    DBCLogicalOperator[] getSupportedOperators(@NotNull DBDAttributeBinding attribute);

}
