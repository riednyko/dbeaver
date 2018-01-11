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
package org.jkiss.dbeaver.debug.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.jkiss.dbeaver.debug.DBGController;
import org.jkiss.dbeaver.debug.DBGException;
import org.jkiss.dbeaver.debug.DBGStackFrame;

/**
 * Delegates mostly everything to its debug target
 *
 */
public abstract class DatabaseThread extends DatabaseDebugElement implements IThread {
    
    private final Object sessionKey;

    public DatabaseThread(IDatabaseDebugTarget target, Object sessionKey) {
        super(target);
        this.sessionKey = sessionKey;
    }

    @Override
    public boolean canResume() {
        return getDebugTarget().canResume();
    }

    @Override
    public boolean canSuspend() {
        return getDebugTarget().canSuspend();
    }

    @Override
    public boolean isSuspended() {
        return getDebugTarget().isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public void suspend() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canStepInto() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canStepOver() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canStepReturn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStepping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void stepInto() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stepOver() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stepReturn() throws DebugException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canTerminate() {
        return getDebugTarget().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void terminate() throws DebugException {
        getDebugTarget().terminate();
    }

    @Override
    public IStackFrame[] getStackFrames() throws DebugException {
        List<IStackFrame> frames = new ArrayList<IStackFrame>();
        DBGController controller = getController();
        try {
            List<? extends DBGStackFrame> stack = controller.getStack(sessionKey);
            stack.size();
        } catch (DBGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (IStackFrame[]) frames.toArray(new IStackFrame[frames.size()]);
    }

    @Override
    public boolean hasStackFrames() throws DebugException {
        return true;
    }

    @Override
    public int getPriority() throws DebugException {
        // no idea for now
        return 0;
    }

    @Override
    public IStackFrame getTopStackFrame() throws DebugException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        // TODO Auto-generated method stub
        return null;
    }

    public void resumedByTarget() {
        // TODO Auto-generated method stub

    }

    public void setStepping(boolean b) {
        // TODO Auto-generated method stub

    }

}
