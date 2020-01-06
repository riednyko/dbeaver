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
package org.jkiss.dbeaver.tasks.ui.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

import java.util.Map;

public class TaskHandlerGroupBy extends AbstractHandler implements IElementUpdater {
	
    public enum GroupBy {
        project,
        category,
        type
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        GroupBy groupBy = CommonUtils.valueOf(GroupBy.class, event.getParameter("group"), GroupBy.project);
        DatabaseTasksView view = (DatabaseTasksView) HandlerUtil.getActivePart(event);
        switch (groupBy) {
            case project:
                view.setGroupByProject(!view.isGroupByProject());
                break;
            case category:
                view.setGroupByCategory(!view.isGroupByCategory());
                break;
            case type:
                view.setGroupByType(!view.isGroupByType());
                break;
        }
        view.regroupTasks();

        return null;
    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        DatabaseTasksView taskView = (DatabaseTasksView) UIUtils.findView(UIUtils.getActiveWorkbenchWindow(), DatabaseTasksView.VIEW_ID);
        if (taskView != null) {
            GroupBy groupBy = CommonUtils.valueOf(GroupBy.class, (String)parameters.get("group"), GroupBy.project);

            switch (groupBy) {
                case project:
                    element.setChecked(taskView.isGroupByProject());
                    //element.setIcon(DBeaverIcons.getImageDescriptor(DBIcon.PROJECT));
                    break;
                case category:
                    element.setChecked(taskView.isGroupByCategory());
                    //element.setIcon(DBeaverIcons.getImageDescriptor(DBIcon.TREE_DATABASE_CATEGORY));
                    break;
                case type:
                    element.setChecked(taskView.isGroupByType());
                    //element.setIcon(DBeaverIcons.getImageDescriptor(DBIcon.TREE_TASK));
                    break;
            }
            element.setText("Group by " + groupBy.name());
            element.setTooltip("Group tasks by " + groupBy.name());
        }
    }


}