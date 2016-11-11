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
package org.jkiss.dbeaver.model.impl;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLDataSource;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.*;

/**
 * Various objects cache
 */
public abstract class AbstractObjectCache<OWNER extends DBSObject, OBJECT extends DBSObject>
    implements DBSObjectCache<OWNER, OBJECT>
{
    private static final Log log = Log.getLog(AbstractObjectCache.class);

    private List<OBJECT> objectList;
    private Map<String, OBJECT> objectMap;
    protected volatile boolean fullCache = false;
    protected volatile boolean caseSensitive = true;
    protected Comparator<OBJECT> listOrderComparator;

    protected AbstractObjectCache() {
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public Comparator<OBJECT> getListOrderComparator()
    {
        return listOrderComparator;
    }

    public void setListOrderComparator(Comparator<OBJECT> listOrderComparator)
    {
        this.listOrderComparator = listOrderComparator;
    }

    @NotNull
    @Override
    public List<OBJECT> getCachedObjects()
    {
        synchronized (this) {
            return objectList == null ? Collections.<OBJECT>emptyList() : objectList;
        }
    }

    public <SUB_TYPE> Collection<SUB_TYPE> getTypedObjects(DBRProgressMonitor monitor, OWNER owner, Class<SUB_TYPE> type)
        throws DBException
    {
        List<SUB_TYPE> result = new ArrayList<>();
        for (OBJECT object : getAllObjects(monitor, owner)) {
            if (type.isInstance(object)) {
                result.add(type.cast(object));
            }
        }
        return result;
    }

    @Nullable
    @Override
    public OBJECT getCachedObject(@NotNull String name)
    {
        synchronized (this) {
            return objectList == null || name == null ? null : getObjectMap().get(caseSensitive ? name : name.toUpperCase());
        }
    }

    @Override
    public void cacheObject(@NotNull OBJECT object)
    {
        synchronized (this) {
            if (this.objectList == null) {
                this.objectList = new ArrayList<>();
            }
            detectCaseSensitivity(object);
            this.objectList.add(object);
            if (this.objectMap != null) {
                String name = getObjectName(object);
                checkDuplicateName(name, object);
                this.objectMap.put(name, object);
            }
        }
    }

    @Override
    public void removeObject(@NotNull OBJECT object, boolean resetFullCache)
    {
        synchronized (this) {
            if (this.objectList != null) {
                detectCaseSensitivity(object);
                this.objectList.remove(object);
                if (this.objectMap != null) {
                    this.objectMap.remove(getObjectName(object));
                }
            }
            if (resetFullCache) {
                fullCache = false;
            }
        }
    }

    @Nullable
    public <SUB_TYPE> SUB_TYPE getObject(DBRProgressMonitor monitor, OWNER owner, String name, Class<SUB_TYPE> type)
        throws DBException
    {
        final OBJECT object = getObject(monitor, owner, name);
        return type.isInstance(object) ? type.cast(object) : null;
    }

    public boolean isFullyCached()
    {
        return this.fullCache;
    }

    @Override
    public void clearCache()
    {
        synchronized (this) {
            this.objectList = null;
            this.objectMap = null;
            this.fullCache = false;
        }
    }

    public void setCache(List<OBJECT> objects)
    {
        synchronized (this) {
            this.objectList = objects;
            this.objectMap = null;
            this.fullCache = true;
        }
    }

    /**
     * Merges new cache with existing.
     * If objects with the same name were already cached - leave them in cache
     * (because they might be referenced somewhere).
     */
    protected void mergeCache(List<OBJECT> objects)
    {
        synchronized (this) {
            if (this.objectList != null) {
                // Merge lists
                objects = new ArrayList<>(objects);
                for (int i = 0; i < objects.size(); i++) {
                    OBJECT newObject = objects.get(i);
                    String newObjectName = getObjectName(newObject);
                    for (int k = 0; k < objectList.size(); k++) {
                        OBJECT oldObject = objectList.get(k);
                        String oldObjectName = getObjectName(oldObject);
                        if (newObjectName.equals(oldObjectName)) {
                            objects.set(i, oldObject);
                            break;
                        }
                    }
                }
            }
        }
        setCache(objects);
    }

    private synchronized Map<String, OBJECT> getObjectMap()
    {
        if (this.objectMap == null) {
            this.objectMap = new HashMap<>();
            for (OBJECT object : objectList) {
                String name = getObjectName(object);
                checkDuplicateName(name, object);
                this.objectMap.put(name, object);
            }
        }
        return this.objectMap;
    }

    private void checkDuplicateName(String name, OBJECT object) {
        if (this.objectMap.containsKey(name)) {
            log.debug("Duplicate object name '" + name + "' in cache " + this.getClass().getSimpleName() + ". Last value: " + DBUtils.getObjectFullName(object, DBPEvaluationContext.DDL));
        }
    }

    protected void detectCaseSensitivity(DBSObject object) {
        if (this.caseSensitive) {
            DBPDataSource dataSource = object.getDataSource();
            if (dataSource instanceof SQLDataSource &&
                ((SQLDataSource) dataSource).getSQLDialect().storesUnquotedCase() == DBPIdentifierCase.MIXED)
            {
                this.caseSensitive = false;
            }
        }
    }

    protected void invalidateObjects(DBRProgressMonitor monitor, OWNER owner, Iterator<OBJECT> objectIter)
    {

    }

    public void clearChildrenOf(DBSObject parent) {
        synchronized (this) {
            if (objectList == null) {
                return;
            }
            for (OBJECT object : objectList) {
                if (object.getParentObject() == parent) {
                    removeObject(object, true);
                }
            }
        }
    }

    @NotNull
    protected String getObjectName(@NotNull OBJECT object) {
        String name;
        if (object instanceof DBPUniqueObject) {
            name = ((DBPUniqueObject) object).getUniqueName();
        } else {
            name = object.getName();
        }
        if (!caseSensitive) {
            return name.toUpperCase();
        }
        return name;
    }

    protected class CacheIterator implements Iterator<OBJECT> {
        private Iterator<OBJECT> listIterator = objectList.iterator();
        private OBJECT curObject;
        public CacheIterator()
        {
        }

        @Override
        public boolean hasNext()
        {
            return listIterator.hasNext();
        }

        @Override
        public OBJECT next()
        {
            return (curObject = listIterator.next());
        }

        @Override
        public void remove()
        {
            listIterator.remove();
            if (objectMap != null) {
                objectMap.remove(getObjectName(curObject));
            }
        }
    }
}
