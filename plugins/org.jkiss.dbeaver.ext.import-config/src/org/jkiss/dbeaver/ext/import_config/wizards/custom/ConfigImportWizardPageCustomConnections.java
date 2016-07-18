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

package org.jkiss.dbeaver.ext.import_config.wizards.custom;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.ArrayUtils;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.import_config.Activator;
import org.jkiss.dbeaver.ext.import_config.wizards.ConfigImportWizardPage;
import org.jkiss.dbeaver.ext.import_config.wizards.ImportConnectionInfo;
import org.jkiss.dbeaver.ext.import_config.wizards.ImportData;
import org.jkiss.dbeaver.ext.import_config.wizards.ImportDriverInfo;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.utils.RuntimeUtils;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.xml.XMLException;
import org.jkiss.utils.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConfigImportWizardPageCustomConnections extends ConfigImportWizardPage {

    protected ConfigImportWizardPageCustomConnections()
    {
        super("Connections");
        setTitle("Connections");
        setDescription("Import connections");
    }

    @Override
    protected void loadConnections(ImportData importData) throws DBException
    {
        setErrorMessage(null);

        ConfigImportWizardCustom wizard = (ConfigImportWizardCustom) getWizard();
        final DBPDriver driver = wizard.getDriver();

        ImportDriverInfo driverInfo = new ImportDriverInfo(driver.getId(), driver.getName(), driver.getSampleURL(), driver.getDriverClassName());
        importData.addDriver(driverInfo);

        File inputFile = wizard.getInputFile();
        try (InputStream is = new FileInputStream(inputFile)) {
            try (Reader reader = new InputStreamReader(is, wizard.getInputFileEncoding())) {
                if (wizard.getImportType() == ConfigImportWizardCustom.ImportType.CSV) {
                    importCSV(importData, driverInfo, reader);
                } else {
                    importXML(importData, driverInfo, reader);
                }
            }
        } catch (Exception e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void importCSV(ImportData importData, ImportDriverInfo driver, Reader reader) throws IOException {
        final CSVReader csvReader = new CSVReader(reader);
        final String[] header = csvReader.readNext();
        if (ArrayUtils.isEmpty(header)) {
            setErrorMessage("No connection found");
            return;
        }
        for (;;) {
            final String[] line = csvReader.readNext();
            if (ArrayUtils.isEmpty(line)) {
                break;
            }
            Map<String, String> conProps = new HashMap<>();
            for (int i = 0; i < line.length; i++) {
                if (i >= header.length) {
                    break;
                }
                conProps.put(header[i], line[i]);
            }
            importData.addConnection(makeConnectionFromProps(driver, conProps));
        }
    }

    private void importXML(ImportData importData, ImportDriverInfo driver, Reader reader) {

    }

    private ImportConnectionInfo makeConnectionFromProps(ImportDriverInfo driver, Map<String, String> conProps) {
        return new ImportConnectionInfo(
            driver,
            conProps.get("id"),
            conProps.get("name"),
            conProps.get("url"),
            conProps.get("host"),
            conProps.get("port"),
            conProps.get("database"),
            conProps.get("user"),
            conProps.get("password")
        );
    }

}
