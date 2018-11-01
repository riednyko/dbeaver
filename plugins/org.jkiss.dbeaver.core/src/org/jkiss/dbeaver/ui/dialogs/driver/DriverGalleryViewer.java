/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.dialogs.driver;

import org.eclipse.jface.viewers.*;
import org.eclipse.nebula.jface.galleryviewer.GalleryTreeViewer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPNamedObject;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.registry.DataSourceProviderDescriptor;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.editors.EditorUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DriverGalleryViewer
 *
 * @author Serge Rider
 */
public class DriverGalleryViewer extends GalleryTreeViewer {

    //private final Gallery gallery;
    private final List<DBPDriver> allDrivers = new ArrayList<>();;

    public DriverGalleryViewer(Composite parent, Object site, List<DataSourceProviderDescriptor> providers, boolean expandRecent) {
        super(new Gallery(parent, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER));
        gallery.setBackground(EditorUtils.getDefaultTextBackground());
        gallery.setForeground(EditorUtils.getDefaultTextForeground());
        gallery.setLayoutData(new GridData(GridData.FILL_BOTH));

        gallery.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (site instanceof ISelectionChangedListener) {
                    ((ISelectionChangedListener) site).selectionChanged(new SelectionChangedEvent(DriverGalleryViewer.this, getSelection()));
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (site instanceof IDoubleClickListener) {
                    ((IDoubleClickListener) site).doubleClick(new DoubleClickEvent(DriverGalleryViewer.this, getSelection()));
                }
            }
        });

        // Renderers
        DefaultGalleryGroupRenderer groupRenderer = new DefaultGalleryGroupRenderer();
        groupRenderer.setMaxImageHeight(16);
        groupRenderer.setMaxImageWidth(16);
        groupRenderer.setItemHeight(60);
        groupRenderer.setItemWidth(200);
        gallery.setGroupRenderer(groupRenderer);
        //gallery.setGroupRenderer(new NoGroupRenderer());

        DriverGalleryItemRenderer ir = new DriverGalleryItemRenderer(parent);
        gallery.setItemRenderer(ir);

        for (DataSourceProviderDescriptor dpd : providers) {
            allDrivers.addAll(dpd.getEnabledDrivers());
        }
        allDrivers.sort(Comparator.comparing(DBPNamedObject::getName));

        GalleryItem groupRecent = new GalleryItem(gallery, SWT.NONE);
        groupRecent.setText("Recent drivers"); //$NON-NLS-1$
        groupRecent.setImage(DBeaverIcons.getImage(DBIcon.TREE_SCHEMA));
        groupRecent.setData("recent");
        groupRecent.setExpanded(true);

        GalleryItem groupAll = new GalleryItem(gallery, SWT.NONE);
        groupAll.setText("All drivers"); //$NON-NLS-1$
        groupAll.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
        groupAll.setData("all");
        groupAll.setExpanded(true);

        for (DBPDriver driver : allDrivers) {

            GalleryItem item = new GalleryItem(groupAll, SWT.NONE);
            item.setImage(DBeaverIcons.getImage(driver.getIcon()));
            item.setText(driver.getName()); //$NON-NLS-1$
            item.setText(0, driver.getName()); //$NON-NLS-1$
            List<DBPDataSourceContainer> usedBy = driver.getUsedBy();
            if (!usedBy.isEmpty()) {
                item.setText(1, "Connections: " + usedBy.size());
            }
            item.setText(2, driver.getCategory());
            item.setData(driver);
        }
    }

    public Control getControl() {
        return getGallery();
    }

    @Override
    public Object getInput() {
        return allDrivers;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void addTraverseListener(TraverseListener traverseListener) {
        if (traverseListener != null) {
            gallery.addTraverseListener(traverseListener);
        }
    }

/*
    @Override
    public ISelection getSelection() {
        GalleryItem[] itemSelection = gallery.getSelection();
        Object[] selectedDrivers = new Object[itemSelection.length];
        for (int i = 0; i < itemSelection.length; i++) {
            selectedDrivers[i] = itemSelection[i].getData();
        }
        return new StructuredSelection(selectedDrivers);
    }
*/

}
