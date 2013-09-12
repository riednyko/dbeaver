/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.model.plan;

import org.jkiss.dbeaver.model.DBPNamedObject;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

/**
 * DB2 EXPLAIN_ARGUMENT table
 *
 * @author Denis Forveille
 */
public class DB2PlanOperatorArgument implements DBPNamedObject {

    private DB2PlanOperator db2Operator;

    //private Integer operatorId;
    private String argumentType;
    private String argumentValue;
    private String longArgumentValue;

    // ------------
    // Constructors
    // ------------

    public DB2PlanOperatorArgument(JDBCResultSet dbResult, DB2PlanOperator db2Operator)
    {
        this.db2Operator = db2Operator;

        //this.operatorId = JDBCUtils.safeGetInteger(dbResult, "OPERATOR_ID");
        this.argumentType = JDBCUtils.safeGetStringTrimmed(dbResult, "ARGUMENT_TYPE");
        this.argumentValue = JDBCUtils.safeGetStringTrimmed(dbResult, "ARGUMENT_VALUE");
        // TODO DF: bad. this is a Clob!
        this.longArgumentValue = JDBCUtils.safeGetString(dbResult, "LONG_ARGUMENT_VALUE");
    }

    @Override
    public String toString()
    {
        return argumentValue;
    }

    // ----------------
    // Standard Getters
    // ----------------

    public String getArgumentValue()
    {
        return argumentValue;
    }

    public String getArgumentType()
    {
        return argumentType;
    }

    public String getLongArgumentValue()
    {
        return longArgumentValue;
    }

    public DB2PlanOperator getDb2Operator()
    {
        return db2Operator;
    }

    @Override
    public String getName()
    {
        if (argumentType.equals("EARLYOUT")) {
            return "Early Out Flag";
        }
        if (argumentType.equals("OUTERJN")) {
            return "Outer Join Type";
        }
        if (argumentType.equals("JN INPUT")) {
            return "Join Input Leg";
        }
        if (argumentType.equals("PREFETCH")) {
            return "Prefetch";
        }
        if (argumentType.equals("SCANDIR")) {
            return "Scan Direction";
        }
        if (argumentType.equals("SPEED")) {
            return "Speed";
        }
        return argumentType;
    }
}
