/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
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

package org.jkiss.dbeaver.ui.controls.folders;

import org.jkiss.code.Nullable;

/**
 * IFolderedPart
 */
public interface ITabbedFolderContainer
{

    @Nullable
    ITabbedFolder getActiveFolder();

    /**
     * Switch to specified folder ID. If folderId isnull then switches to default (fuirst) folder
     */
    void switchFolder(@Nullable String folderId);

    void addFolderListener(ITabbedFolderListener listener);

    void removeFolderListener(ITabbedFolderListener listener);

}