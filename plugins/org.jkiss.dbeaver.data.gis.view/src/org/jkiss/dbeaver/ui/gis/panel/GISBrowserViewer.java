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

import com.vividsolutions.jts.geom.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.gis.GisAttribute;
import org.jkiss.dbeaver.model.gis.GisConstants;
import org.jkiss.dbeaver.model.gis.GisTransformRequest;
import org.jkiss.dbeaver.model.gis.GisTransformUtils;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.editors.BaseValueEditor;
import org.jkiss.dbeaver.ui.gis.IGeometryViewer;
import org.jkiss.dbeaver.ui.gis.internal.GISViewerActivator;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.IOUtils;

import java.io.*;

public class GISBrowserViewer extends BaseValueEditor<Browser> implements IGeometryViewer {

    private static final Log log = Log.getLog(GISBrowserViewer.class);

    private Object lastValue;
    private File scriptFile;

    public GISBrowserViewer(IValueController controller) {
        super(controller);
    }
    
    @Override
    protected Browser createControl(Composite editPlaceholder)
    {
        Browser browser = new Browser(editPlaceholder, SWT.NONE);
        browser.addDisposeListener(e -> {
            cleanupFiles();
        });
        return browser;
    }

    @Override
    public void primeEditorValue(@Nullable Object value) throws DBException
    {
        if (CommonUtils.equalObjects(lastValue, value)) {
            return;
        }
        if (control != null) {
            try {
                if (DBUtils.isNullValue(value)) {
                    control.setUrl("about:blank");
                } else {
                    File file = generateViewScript(new Object[] { value } );
                    control.setUrl(file.toURI().toURL().toString());
                }
            } catch (IOException e) {
                throw new DBException("Error generating viewer script", e);
            }
        }
        lastValue = value;
    }

    private File generateViewScript(Object[] values) throws IOException {
        if (scriptFile == null) {
            File tempDir = DBWorkbench.getPlatform().getTempFolder(new VoidProgressMonitor(), "gis-viewer-files");
            checkIncludesExistence(tempDir);

            scriptFile = File.createTempFile("view", "gis.html", tempDir);
        }
        int baseSRID = 0;
        String[] geomValues = new String[values.length];
        String[] geomTipValues = new String[values.length];
        boolean showMap = false;
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof Geometry) {
                int srid = ((Geometry) value).getSRID();
                if (srid == 0) {
                    srid = GisConstants.DEFAULT_SRID;
                } else {
                    if (baseSRID == 0) {
                        baseSRID = srid;
                    }
                }
                if (srid == GisConstants.DEFAULT_SRID) {
                    showMap = true;
                } else {
                    try {
                        GisTransformRequest request = new GisTransformRequest((Geometry) value, srid, GisConstants.DEFAULT_SRID);
                        GisTransformUtils.transformGisData(request);
                        value = request.getTargetValue();
                        showMap = request.isShowOnMap();
                    } catch (DBException e) {
                        log.debug("Error transforming CRS", e);
                        showMap = false;
                    }
                }
            }
            geomValues[i] = "'" + value + "'";
            geomTipValues[i] = "";
        }
        if (baseSRID == 0) {
            if (valueController.getValueType() instanceof GisAttribute) {
                try {
                    baseSRID = ((GisAttribute) valueController.getValueType()).getAttributeGeometrySRID(new VoidProgressMonitor());
                } catch (DBCException e) {
                    log.error(e);
                }
            }
        }
        if (baseSRID == 0) {
            baseSRID = GisConstants.DEFAULT_SRID;
        }
        int defaultSRID = baseSRID;
        String geomValuesString = String.join(",", geomValues);
        String geomTipValuesString = String.join(",", geomTipValues);
        boolean isShowMap = showMap;

        InputStream fis = GISViewerActivator.getDefault().getResourceStream(GISBrowserViewerConstants.VIEW_TEMPLATE_PATH);
        if (fis == null) {
            throw new IOException("View template file not found (" + GISBrowserViewerConstants.VIEW_TEMPLATE_PATH + ")");
        }
        try (InputStreamReader isr = new InputStreamReader(fis)) {
            String viewTemplate = IOUtils.readToString(isr);
            viewTemplate = GeneralUtils.replaceVariables(viewTemplate, name -> {
                if (name.equals("geomValues")) {
                    return geomValuesString;
                } else if (name.equals("geomTipValues")) {
                    return String.valueOf(geomTipValuesString);
                } else if (name.equals("geomSRID")) {
                    return String.valueOf(defaultSRID);
                } else if (name.equals("showMap")) {
                    return String.valueOf(isShowMap);
                }
                return null;
            });
            try (FileOutputStream fos = new FileOutputStream(scriptFile)) {
                fos.write(viewTemplate.getBytes(GeneralUtils.UTF8_CHARSET));
            }
        } finally {
            ContentUtils.close(fis);
        }

        return scriptFile;
    }

    private void checkIncludesExistence(File scriptDir) throws IOException {
        File incFolder = new File(scriptDir, "inc");
        if (!incFolder.exists()) {
            if (!incFolder.mkdirs()) {
                throw new IOException("Can't create inc folder '" + incFolder.getAbsolutePath() + "'");
            }
            for (String fileName : GISBrowserViewerConstants.INC_FILES) {
                InputStream fis = GISViewerActivator.getDefault().getResourceStream(GISBrowserViewerConstants.WEB_INC_PATH + fileName);
                if (fis != null) {
                    try (FileOutputStream fos = new FileOutputStream(new File(incFolder, fileName))) {
                        try {
                            IOUtils.copyStream(fis, fos);
                        } catch (Exception e) {
                            log.warn("Error copying inc file " + fileName, e);
                        } finally {
                            ContentUtils.close(fis);
                        }
                    }
                }
            }
        }
    }

    private void cleanupFiles() {
        if (scriptFile != null) {
            if (!scriptFile.delete()) {
                log.debug("Can't delete temp script file '" + scriptFile.getAbsolutePath() + "'");
            }
        }
    }

    @Override
    public Object extractEditorValue() throws DBCException {
        return lastValue;
    }

    @Override
    public void createControl() {
        super.createControl();
    }

}