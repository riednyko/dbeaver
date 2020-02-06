/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.controls.resultset.colors;

import org.eclipse.osgi.util.NLS;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.virtual.DBVEntity;
import org.jkiss.dbeaver.ui.UITextUtils;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetViewer;
import org.jkiss.dbeaver.ui.controls.resultset.internal.ResultSetMessages;
import org.jkiss.utils.CommonUtils;

public class ResetRowColorAction extends ColorAction {
    private ResultSetViewer resultSetViewer;
    private final DBDAttributeBinding attribute;

    public ResetRowColorAction(ResultSetViewer resultSetViewer, DBDAttributeBinding attr, Object value) {
        super(resultSetViewer, NLS.bind(ResultSetMessages.actions_name_color_reset_by,
            attr.getName() + " = " + UITextUtils.getShortText(resultSetViewer.getSizingGC(), CommonUtils.toString(value), 100)));
        this.resultSetViewer = resultSetViewer;
        this.attribute = attr;
    }

    @Override
    public void run() {
        final DBVEntity vEntity = getColorsVirtualEntity();
        vEntity.removeColorOverride(attribute);
        updateColors(vEntity);
    }
}
