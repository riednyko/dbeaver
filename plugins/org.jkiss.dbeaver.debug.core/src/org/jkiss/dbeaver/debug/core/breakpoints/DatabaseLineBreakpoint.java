/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2018 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
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

package org.jkiss.dbeaver.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.jkiss.dbeaver.debug.core.DebugCore;
import org.jkiss.dbeaver.model.struct.DBSObject;

public class DatabaseLineBreakpoint extends DatabaseBreakpoint implements IDatabaseLineBreakpoint {

    public DatabaseLineBreakpoint() {
    }

    public DatabaseLineBreakpoint(DBSObject databaseObject, IResource resource, final int lineNumber,
            final int charStart, final int charEnd, final boolean add) throws DebugException {
        this(databaseObject, resource, lineNumber, charStart, charEnd, add, new HashMap<String, Object>(),
                DebugCore.BREAKPOINT_ID_DATABASE_LINE);
    }

    protected DatabaseLineBreakpoint(DBSObject databaseObject, final IResource resource, final int lineNumber,
            final int charStart, final int charEnd, final boolean add, final Map<String, Object> attributes,
            final String markerType) throws DebugException {
        IWorkspaceRunnable wr = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {

                // create the marker
                setMarker(resource.createMarker(markerType));

                // add attributes
                addDatabaseBreakpointAttributes(attributes, databaseObject);
                addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
                ensureMarker().setAttributes(attributes);

                // add to breakpoint manager if requested
                register(add);
            }

        };
        run(getMarkerRule(resource), wr);
    }

    @Override
    public int getLineNumber() throws CoreException {
        IMarker m = getMarker();
        if (m != null) {
            return m.getAttribute(IMarker.LINE_NUMBER, -1);
        }
        return -1;
    }

    @Override
    public int getCharStart() throws CoreException {
        IMarker m = getMarker();
        if (m != null) {
            return m.getAttribute(IMarker.CHAR_START, -1);
        }
        return -1;
    }

    @Override
    public int getCharEnd() throws CoreException {
        IMarker m = getMarker();
        if (m != null) {
            return m.getAttribute(IMarker.CHAR_END, -1);
        }
        return -1;
    }

    public void addLineBreakpointAttributes(Map<String, Object> attributes, String modelIdentifier, boolean enabled,
            int lineNumber, int charStart, int charEnd) {
        attributes.put(IBreakpoint.ID, modelIdentifier);
        attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
        attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
        attributes.put(IMarker.CHAR_START, new Integer(charStart));
        attributes.put(IMarker.CHAR_END, new Integer(charEnd));
    }
}
