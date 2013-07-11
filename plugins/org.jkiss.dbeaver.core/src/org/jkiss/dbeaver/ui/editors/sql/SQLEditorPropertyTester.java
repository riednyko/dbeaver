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
package org.jkiss.dbeaver.ui.editors.sql;

import org.eclipse.core.expressions.PropertyTester;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlanner;
import org.jkiss.dbeaver.ui.ActionUtils;

/**
 * DatabaseEditorPropertyTester
 */
public class SQLEditorPropertyTester extends PropertyTester
{
    //static final Log log = LogFactory.getLog(SQLEditorPropertyTester.class);

    public static final String NAMESPACE = "org.jkiss.dbeaver.ui.editors.sql";
    public static final String PROP_CAN_EXECUTE = "canExecute";
    public static final String PROP_CAN_EXPLAIN = "canExplain";
    public static final String PROP_CAN_NAVIGATE = "canNavigate";
    public static final String PROP_CAN_EXPORT = "canExport";

    public SQLEditorPropertyTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof SQLEditor)) {
            return false;
        }
        SQLEditor editor = (SQLEditor)receiver;
        boolean isConnected = editor.getDataSourceContainer() != null && editor.getDataSourceContainer().isConnected();
        if (property.equals(PROP_CAN_EXECUTE)) {
            if (isConnected) {
                if ("statement".equals(expectedValue)) {
                    return editor.hasActiveQuery();
                } else {
                    return true;
                }
            }
            return false;
        } else if (property.equals(PROP_CAN_EXPLAIN)) {
            return isConnected && editor.hasActiveQuery() && DBUtils.getAdapter(DBCQueryPlanner.class, editor.getDataSource()) != null;
        } else if (property.equals(PROP_CAN_NAVIGATE)) {
            return isConnected;
        } else if (property.equals(PROP_CAN_EXPORT)) {
            return isConnected && editor.hasActiveQuery();
        }
        return false;
    }

    public static void firePropertyChange(String propName)
    {
        ActionUtils.evaluatePropertyState(NAMESPACE + "." + propName);
    }

}