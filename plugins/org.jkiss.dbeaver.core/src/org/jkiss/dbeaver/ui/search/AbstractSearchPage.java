/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ui.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.ui.views.navigator.database.DatabaseNavigatorTree;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AbstractSearchPage extends DialogPage implements IObjectSearchPage {

    static final protected Log log = LogFactory.getLog(AbstractSearchPage.class);

    protected IObjectSearchContainer container;

    protected AbstractSearchPage(String title) {
        super(title);
    }

    @Override
    public void setSearchContainer(IObjectSearchContainer container)
    {
        this.container = container;
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible) {
            updateEnablement();
        }
    }

    protected abstract void updateEnablement();

    protected static List<DBNNode> loadTreeState(IPreferenceStore store, String propName)
    {
        final List<DBNNode> result = new ArrayList<DBNNode>();
        final String sources = store.getString(propName);
        if (!CommonUtils.isEmpty(sources)) {
            try {
                DBeaverUI.runInProgressService(new DBRRunnableWithProgress() {
                    @Override
                    public void run(DBRProgressMonitor monitor)
                    {
                        StringTokenizer st = new StringTokenizer(sources, "|"); //$NON-NLS-1$
                        while (st.hasMoreTokens()) {
                            String nodePath = st.nextToken();
                            try {
                                DBNNode node = DBNModel.getInstance().getNodeByPath(monitor, nodePath);
                                if (node != null) {
                                    result.add(node);
                                }
                            } catch (DBException e) {
                                log.error(e);
                            }
                        }
                    }
                });
            } catch (InvocationTargetException e) {
                log.error(e.getTargetException());
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return result;
    }

    protected static void saveTreeState(IPreferenceStore store, String propName, DatabaseNavigatorTree tree)
    {
        // Object sources
        StringBuilder sourcesString = new StringBuilder();
        IStructuredSelection ss = (IStructuredSelection) tree.getViewer().getSelection();
        for (Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
            DBNNode node = (DBNNode) iter.next();
            if (sourcesString.length() > 0) {
                sourcesString.append("|"); //$NON-NLS-1$
            }
            sourcesString.append(node.getNodeItemPath());
        }
        store.setValue(propName, sourcesString.toString());
    }

}
