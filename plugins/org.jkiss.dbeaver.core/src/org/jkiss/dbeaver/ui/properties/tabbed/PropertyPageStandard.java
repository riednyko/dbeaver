/*
 * Copyright (C) 2010-2013 Serge Rieder
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
package org.jkiss.dbeaver.ui.properties.tabbed;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.*;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.properties.ILazyPropertyLoadListener;
import org.jkiss.utils.CommonUtils;

import java.util.Iterator;

public class PropertyPageStandard extends PropertySheetPage implements ILazyPropertyLoadListener, IPropertySourceProvider {

    private static class PropertySourceCache {
        Object object;
        IPropertySource propertySource;
        boolean cached;

        public PropertySourceCache(Object object)
        {
            if (object instanceof IPropertySource) {
                // Sometimes IPropertySource wrapper (e.g. PropertySourceEditable) may be used instead of real object
                this.object = ((IPropertySource)object).getEditableValue();
            } else {
                this.object = object;
            }
        }
    }
    private PropertySourceCache[] curSelection = null;

    public PropertyPageStandard()
    {
        setSorter(
            new PropertySheetSorter() {
                @Override
                public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB)
                {
                    // No damn sorting
                    return 0;
                }
            }
        );
        setPropertySourceProvider(this);
        // Register lazy load listener
        PropertiesContributor.getInstance().addLazyListener(this);
    }

    @Override
    public void dispose()
    {
        // Unregister lazy load listener
        PropertiesContributor.getInstance().removeLazyListener(this);
        super.dispose();
    }

    @Override
    public void handlePropertyLoad(Object object, IPropertyDescriptor property, Object propertyValue, boolean completed)
    {
        // Make page refresh if our main object was updated
        if (!CommonUtils.isEmpty(curSelection) && !getControl().isDisposed()) {
            for (PropertySourceCache cache : curSelection) {
                if ((cache.propertySource != null && cache.propertySource.getEditableValue() == object) ||
                    cache.object == object)
                {
                    refresh();
                    return;
                }
            }
        }
        //System.out.println("HEY: " + object + " | " + propertyId + " | " + propertyValue + " : " + completed);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        // Create objects cache
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            curSelection = new PropertySourceCache[ss.size()];
            if (ss.size() == 1) {
                curSelection[0] = new PropertySourceCache(ss.getFirstElement());
            } else {
                int index = 0;
                for (Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
                    curSelection[index++] = new PropertySourceCache(iter.next());
                }
            }
        }
        getControl().setRedraw(false);
        try {
            super.selectionChanged(part, selection);
        } finally {
            getControl().setRedraw(true);
        }
    }

    @Override
    public IPropertySource getPropertySource(Object object)
    {
        if (object == null || object.getClass().isPrimitive() || object instanceof CharSequence || object instanceof Number || object instanceof Boolean) {
            // Just for better performance
            return null;
        }
        // Seek in cached property sources
        // Without cache we'll fall in infinite recursion when refreshing lazy props
        // (get prop source from adapter, load props, load lazy props -> refresh -> get prop source from adapter, etc).
        if (!CommonUtils.isEmpty(curSelection)) {
            for (PropertySourceCache cache : curSelection) {
                if (cache.object == object) {
                    if (!cache.cached) {
                        cache.propertySource = RuntimeUtils.getObjectAdapter(object, IPropertySource.class);
                        cache.cached = true;
                    }
                    return cache.propertySource;
                }
            }
        }
        return RuntimeUtils.getObjectAdapter(object, IPropertySource.class);
    }

}