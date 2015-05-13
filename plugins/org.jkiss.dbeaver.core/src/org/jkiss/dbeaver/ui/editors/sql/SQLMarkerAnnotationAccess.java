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
package org.jkiss.dbeaver.ui.editors.sql;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;

/**
 * SQLMarkerAnnotationAccess
 */
public class SQLMarkerAnnotationAccess implements IAnnotationAccess {

    @Override
    public Object getType(Annotation annotation) {
        return annotation.getType();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated assumed to always return <code>true</code>
     */
    @Override
    public boolean isMultiLine(Annotation annotation) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated assumed to always return <code>true</code>
     */
    @Override
    public boolean isTemporary(Annotation annotation) {
        return !annotation.isPersistent();
    }

}
