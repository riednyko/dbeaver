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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreCharset
 */
public class PostgreCharset extends PostgreInformation {

    private String name;
    private String repertoire;
    private String formOfUse;
    private PostgreCollation defaultCollation;

    public PostgreCharset(PostgreDataSource dataSource, ResultSet dbResult)
        throws SQLException
    {
        super(dataSource);
        this.loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
        throws SQLException
    {
        this.name = JDBCUtils.safeGetString(dbResult, "character_set_name");
        this.repertoire = JDBCUtils.safeGetString(dbResult, "character_repertoire");
        this.formOfUse = JDBCUtils.safeGetString(dbResult, "form_of_use");
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return name;
    }

    @Property(viewable = true, order = 3)
    public String getRepertoire() {
        return repertoire;
    }

    @Property(viewable = true, order = 4)
    public String getFormOfUse() {
        return formOfUse;
    }

    @Property(viewable = true, order = 5)
    public PostgreCollation getDefaultCollation() {
        return defaultCollation;
    }
}
