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

package org.jkiss.dbeaver.ext.erd;

import org.eclipse.draw2d.PrintFigureOperation;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

/**
 * ERD constants
 */
public class ERDConstants {

    public static final String ERD_CONTROL_ID = "org.jkiss.dbeaver.erd.ERDEditor";

    public static final String PREF_PRINT_PAGE_MODE = "erd.print.page-mode";
    public static final String PREF_PRINT_MARGIN_TOP = "erd.print.margin-top";
    public static final String PREF_PRINT_MARGIN_BOTTOM = "erd.print.margin-bottom";
    public static final String PREF_PRINT_MARGIN_LEFT = "erd.print.margin-left";
    public static final String PREF_PRINT_MARGIN_RIGHT = "erd.print.margin-right";

    public static final int PRINT_MODE_DEFAULT = PrintFigureOperation.TILE;
    public static final int PRINT_MARGIN_DEFAULT = 0;

    public static final String PREF_GRID_ENABLED = "erd.grid.enabled";
    public static final String PREF_GRID_SNAP_ENABLED = "erd.grid.snap";
    public static final String PREF_GRID_WIDTH = "erd.grid.width";
    public static final String PREF_GRID_HEIGHT = "erd.grid.height";
    public static final String PREF_ATTR_VISIBILITY = "erd.attr.visibility";

    public static DBSEntityConstraintType CONSTRAINT_LOGICAL_FK = new DBSEntityConstraintType("erdkey", "Logical Key", null, true, false);
}
