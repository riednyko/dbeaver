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

package  org.jkiss.dbeaver.ui.controls.lightgrid;

import org.jkiss.utils.CommonUtils;

/**
 * GridPos
 */
public class GridPos
{
    public int col;
    public int row;
    public GridPos next;

    public GridPos(int col, int row)
    {
        this.col = col;
        this.row = row;
    }

    public GridPos(int col, int row, GridPos next) {
        this.col = col;
        this.row = row;
        this.next = next;
    }

    public GridPos(GridPos copy)
    {
        this.col = copy.col;
        this.row = copy.row;
        this.next = copy.next == null ? null : new GridPos(copy.next);
    }

   public boolean isValid()
    {
        return col >= 0 && row >= 0;
    }

    public boolean equals(Object object)
    {
        return object instanceof GridPos && equalsTo((GridPos) object);
    }

    public boolean equals(int col, int row)
    {
        return this.col == col && this.row == row;
    }

    public boolean equalsTo(GridPos pos)
    {
        return this.col == pos.col && this.row == pos.row && CommonUtils.equalObjects(this.next, pos.next);
    }

    public String toString()
    {
        return col + ":" + row + (next == null ? "" : "->" + next);
    }

    public int hashCode()
    {
        return col ^ row ^ (next == null ? 0 : next.hashCode());
    }
}
