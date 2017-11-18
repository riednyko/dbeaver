/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017 Alexander Fedorov (alexander.fedorov@jkiss.org)
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
package org.jkiss.dbeaver.runtime.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.jkiss.dbeaver.runtime.ide.ui.IdeUi;

public abstract class CreateLinkHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        IStructuredSelection structured = HandlerUtil.getCurrentStructuredSelection(event);
        if (structured.isEmpty()) {
            return null;
        }
        Object first = structured.getFirstElement();
        IResource resource = Adapters.adapt(first, IResource.class);
        IStatus validation = validateSelected(resource);
        if (!validation.isOK()) {
            StatusAdapter statusAdapter = new StatusAdapter(validation);
            statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "Create link");
            StatusManager.getManager().handle(statusAdapter, StatusManager.SHOW);
            return null;
        }

        List<Path> paths = selectTarget(event);
        if (paths == null || paths.isEmpty()) {
            return null;
        }
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor)
                    throws CoreException, InvocationTargetException, InterruptedException
            {
                createLink(resource, paths, monitor);
            }
        };
        IRunnableContext context = getRunnableContext(event);
        try {
            context.run(true, true, operation);
        } catch (InvocationTargetException e) {
            IStatus error = IdeUi.createError("Unable to create link", e.getTargetException());
            StatusAdapter statusAdapter = new StatusAdapter(error);
            statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "Create link");
            StatusManager.getManager().handle(statusAdapter, StatusManager.LOG | StatusManager.SHOW);
        } catch (InterruptedException e) {
            // skip
        }
        return null;
    }

    protected IStatus validateSelected(IResource resource)
    {
        if (resource instanceof IContainer) {
            return Status.OK_STATUS;
        }
        String message = NLS.bind("Unable to create link inside {0}", resource);
        return IdeUi.createError(message);
    }

    protected abstract List<Path> selectTarget(ExecutionEvent event);

    protected IRunnableContext getRunnableContext(ExecutionEvent event)
    {
        final IWorkbenchWindow activeWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        if (activeWindow != null) {
            return activeWindow;
        }
        return PlatformUI.getWorkbench().getProgressService();
    }

    protected abstract void createLink(IResource resource, List<Path> paths, IProgressMonitor monitor)
            throws CoreException;

}