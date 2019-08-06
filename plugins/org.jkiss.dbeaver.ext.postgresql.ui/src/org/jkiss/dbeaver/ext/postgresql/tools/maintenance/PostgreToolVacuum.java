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
package org.jkiss.dbeaver.ext.postgresql.tools.maintenance;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.PostgreMessages;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDatabase;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreObject;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTableBase;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.tools.IUserInterfaceTool;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Table vacuum
 */
public class PostgreToolVacuum implements IUserInterfaceTool
{
    @Override
    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException
    {
        List<PostgreTableBase> tables = CommonUtils.filterCollection(objects, PostgreTableBase.class);
        if (!tables.isEmpty()) {
            SQLDialog dialog = new SQLDialog(activePart.getSite(), tables);
            dialog.open();
        } else {
            List<PostgreDatabase> databases = CommonUtils.filterCollection(objects, PostgreDatabase.class);
            if (!databases.isEmpty()) {
                SQLDialog dialog = new SQLDialog(activePart.getSite(), databases.get(0));
                dialog.open();
            }
        }
    }

    static class SQLDialog extends TableToolDialog {

        private Button fullCheck;
        private Button freezeCheck;
        private Button analyzeCheck;
        private Button dpsCheck;

        public SQLDialog(IWorkbenchPartSite partSite, Collection<PostgreTableBase> selectedTables)
        {
            super(partSite, PostgreMessages.tool_vacuum_title_table, selectedTables);
        }

        public SQLDialog(IWorkbenchPartSite partSite, PostgreDatabase database)
        {
            super(partSite, PostgreMessages.tool_vacuum_title_database, database);
        }

        @Override
        protected void generateObjectCommand(List<String> lines, PostgreObject object) {
            String sql = "VACUUM ";
            List<String> options = new ArrayList<>();
            if (fullCheck.getSelection()) options.add("FULL");
            if (freezeCheck.getSelection()) options.add("FREEZE");
            options.add("VERBOSE");
            if (analyzeCheck.getSelection()) options.add("ANALYZE");
            if (dpsCheck != null && dpsCheck.getSelection()) options.add("DISABLE_PAGE_SKIPPING");
            if (((PostgreDataSource)getExecutionContext().getDataSource()).isServerVersionAtLeast(9, 6)) {
                sql += "(" + String.join(",", options) + ")";
            } else {
                sql += String.join(" ", options);
            }
            //sql += ")";
            if (object instanceof PostgreTableBase) {
                sql += " " + ((PostgreTableBase)object).getFullyQualifiedName(DBPEvaluationContext.DDL);
            }
            lines.add(sql);
        }

        @Override
        protected void createControls(Composite parent) {
            Group optionsGroup = UIUtils.createControlGroup(parent, PostgreMessages.tool_vacuum_group_option, 1, 0, 0);
            optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fullCheck = UIUtils.createCheckbox(optionsGroup, "Full", PostgreMessages.tool_vacuum_full_check_tooltip, false, 0);
            fullCheck.addSelectionListener(SQL_CHANGE_LISTENER);
            freezeCheck = UIUtils.createCheckbox(optionsGroup, "Freeze", PostgreMessages.tool_vacuum_freeze_check_tooltip, false, 0);
            freezeCheck.addSelectionListener(SQL_CHANGE_LISTENER);
            analyzeCheck = UIUtils.createCheckbox(optionsGroup, "Analyze", PostgreMessages.tool_vacuum_analyze_check_tooltip, false, 0);
            analyzeCheck.addSelectionListener(SQL_CHANGE_LISTENER);
            if (((PostgreDataSource)getExecutionContext().getDataSource()).isServerVersionAtLeast(9, 6)) {
                dpsCheck = UIUtils.createCheckbox(optionsGroup, "Disable page skipping", PostgreMessages.tool_vacuum_dps_check_tooltip, false, 0);
                dpsCheck.addSelectionListener(SQL_CHANGE_LISTENER);
            }

            createObjectsSelector(parent);
        }
    }

}
