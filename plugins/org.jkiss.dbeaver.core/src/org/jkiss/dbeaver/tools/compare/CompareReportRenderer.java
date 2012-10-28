/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.tools.compare;

import org.jkiss.dbeaver.model.navigator.DBNDatabaseFolder;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.xml.XMLBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CompareReportRenderer {

    private CompareReport report;
    private XMLBuilder xml;
    private CompareObjectsSettings settings;

    public void renderReport(DBRProgressMonitor monitor, CompareReport report, CompareObjectsSettings settings, OutputStream outputStream) throws IOException
    {
        this.report = report;
        this.settings = settings;
        this.xml = new XMLBuilder(outputStream, "utf-8", true);
        this.xml.setButify(true);

        if (settings.isShowOnlyDifferences()) {
            // Mark differences on tree nodes
            List<CompareReportLine> reportLines = report.getReportLines();
            int reportLinesSize = reportLines.size();
            for (int i = 0; i < reportLinesSize; i++) {
                if (reportLines.get(i).hasDifference) {
                    int depth = reportLines.get(i).depth;
                    for (int k = i - 1; k >= 0; k--) {
                        if (reportLines.get(k).depth < depth) {
                            reportLines.get(k).hasDifference = true;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        xml.startElement("html");
        xml.startElement("head");
        xml.endElement();
        xml.startElement("body");

        renderHeader();

        xml.startElement("table");
        xml.addAttribute("width", "100%");
        //xml.addAttribute("border", "1");
        xml.addAttribute("cellspacing", 0);
        xml.addAttribute("cellpadding", 0);
        renderBody(monitor);

        xml.endElement();
        xml.endElement();
        xml.endElement();

        this.xml.flush();
    }

    private void renderHeader() throws IOException
    {
        int maxLevel = 0;
        for (CompareReportLine line : report.getReportLines()) {
            if (line.depth > maxLevel) {
                maxLevel = line.depth;
            }
        }
        maxLevel++;
        xml.startElement("style");
        StringBuilder styles = new StringBuilder();
        styles.append(".missing {color:red;} .differs {color:red;} ");
        styles.append(".object td {border-top:solid 1px; border-right:solid 1px; border-color: black; white-space:nowrap;} ");
        styles.append(".property td {border-right:solid 1px; border-color: black; white-space:nowrap;} ");
        for (int i = 1; i <= maxLevel; i++) {
            styles.append(".level").append(i).append(" td { padding-left:").append(20 * i).append("px; } ");
        }
        xml.addText(styles.toString());
        xml.endElement();
    }

    private void renderBody(DBRProgressMonitor monitor) throws IOException
    {
        // Table head
        xml.startElement("tr");
        xml.startElement("th");
        xml.addText("Structure");
        xml.endElement();
        for (DBNDatabaseNode node : report.getNodes()) {
            xml.startElement("th");
            xml.addText(node.getNodeFullName());
            xml.endElement();
        }
        xml.endElement();

        // Table body
        boolean showOnlyDifferences = settings.isShowOnlyDifferences();
        int objectCount = report.getNodes().size();
        List<CompareReportLine> reportLines = report.getReportLines();
        int reportLinesSize = reportLines.size();
        for (int i = 0; i < reportLinesSize; i++) {
            monitor.worked(1);
            CompareReportLine line = reportLines.get(i);
            if (showOnlyDifferences && !line.hasDifference) {
                continue;
            }
            boolean onlyStructure = line.structure instanceof DBNDatabaseFolder && !line.hasDifference;
            // Skip empty folders
            if (onlyStructure && (i >= reportLinesSize - 1 || reportLines.get(i + 1).depth <= line.depth)) {
                continue;
            }

            xml.startElement("tr");
            xml.addAttribute("class", "object level" + line.depth);
            xml.startElement("td");
            if (onlyStructure) {
                xml.addAttribute("colspan", line.nodes.length + 1);
            }
            xml.addText(line.structure.getNodeType());
            xml.endElement();
            if (!onlyStructure) {

                for (int k = 0; k < objectCount; k++) {
                    xml.startElement("td");
                    if (line.nodes[k] == null) {
                        xml.addAttribute("class", "missing");
                        xml.addText("N/A");
                    } else {
                        xml.addText(line.nodes[k].getName());
                    }
                    xml.endElement();
                }
            }

            xml.endElement();

            if (line.properties != null) {
                for (CompareReportProperty reportProperty : line.properties) {
                    boolean differs = false;
                    Object firstValue = null;
                    boolean hasValue = false;
                    for (int k = 0; k < reportProperty.values.length; k++) {
                        if (line.nodes[k] == null) {
                            // Ignore properties of missing objects
                            continue;
                        }
                        Object value = reportProperty.values[k];
                        if (value != null) {
                            hasValue = true;
                            if (firstValue == null) {
                                firstValue = value;
                            }
                        }
                        if (!CompareUtils.equalPropertyValues(value, firstValue)) {
                            differs = true;
                            break;
                        }
                    }
                    if (!hasValue) {
                        // Skip[ properties when nobody have it's value
                        continue;
                    }
                    if (showOnlyDifferences && !differs) {
                        continue;
                    }
                    xml.startElement("tr");
                    xml.addAttribute("class", "property level" + (line.depth + 1) + (differs ? " differs" : ""));
                    xml.startElement("td");
                    xml.addText(reportProperty.property.getDisplayName());
                    xml.endElement();

                    for (int k = 0; k < objectCount; k++) {
                        xml.startElement("td");
                        String stringValue = "";
                        if (reportProperty.values[k] != null) {
                            stringValue = reportProperty.values[k].toString();
                        }
                        if (CommonUtils.isEmpty(stringValue)) {
                            xml.addText("&nbsp;", false);
                        } else {
                            xml.addText(stringValue);
                        }

                        xml.endElement();
                    }

                    xml.endElement();
                }
            }
        }

        // Table footer
        xml.startElement("tr");
        xml.addAttribute("class", "object");
        xml.startElement("td");
        xml.addAttribute("colspan", report.getNodes().size() + 1);
        xml.addText("" + reportLines.size() + " objects compared");
        xml.endElement();
        xml.endElement();

    }
}