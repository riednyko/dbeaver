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
package org.jkiss.dbeaver.ui.gis.panel;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.gis.DBGeometry;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetSelection;
import org.jkiss.dbeaver.ui.data.IDataController;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.editors.BaseValueEditor;
import org.jkiss.dbeaver.ui.gis.GeometryDataUtils;
import org.jkiss.dbeaver.ui.gis.IGeometryViewer;

import java.util.List;

public class GISBrowserViewer extends BaseValueEditor<Browser> implements IGeometryViewer {

    private static final Log log = Log.getLog(GISBrowserViewer.class);

    private GISLeafletViewer leafletViewer;

    public GISBrowserViewer(IValueController controller) {
        super(controller);
    }
    
    @Override
    protected Browser createControl(Composite editPlaceholder)
    {
        leafletViewer = new GISLeafletViewer(editPlaceholder, valueController);
        return leafletViewer.getBrowser();
    }

    @Override
    public void primeEditorValue(@Nullable Object value) throws DBException
    {
        IDataController dataController = valueController.getDataController();
        if (dataController instanceof IResultSetController) {
            //IResultSetSelection selection = ((IResultSetController) dataController).getSelection();

        }
        DBSTypedObject valueType = valueController.getValueType();
        if (value instanceof DBGeometry) {
            if (dataController instanceof IResultSetController && valueType instanceof DBSAttributeBase) {
                IResultSetController resultSetController = (IResultSetController) dataController;
                List<GeometryDataUtils.GeomAttrs> geomAttrs = GeometryDataUtils.extractGeometryAttributes(resultSetController);
                for (GeometryDataUtils.GeomAttrs ga : geomAttrs) {
                    if (ga.geomAttr.matches((DBSAttributeBase) valueType, false)) {
                        GeometryDataUtils.setGeometryProperties(resultSetController, ga, (DBGeometry) value);
                        break;
                    }
                }
            }
            leafletViewer.setGeometryData(new DBGeometry[] {(DBGeometry) value});
        } else {
            // No data
            leafletViewer.setGeometryData(new DBGeometry[0]);
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Object extractEditorValue() throws DBCException {
        return leafletViewer.getCurrentValue();
    }

}