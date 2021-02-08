/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.data.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ui.data.IValueController;

/**
* PagedPanelEditor
*/
public class PagedPanelEditor extends BaseValueEditor<Composite> {
    public PagedPanelEditor(IValueController controller) {
        super(controller);
    }

    @Override
    public Object extractEditorValue()
    {
        return null;
    }

    @Override
    public void primeEditorValue(@Nullable Object value) throws DBException
    {
    }

    @Override
    protected Composite createControl(Composite editPlaceholder)
    {
        return new Composite(editPlaceholder, SWT.NONE);
    }
}
