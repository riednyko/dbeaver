package org.jkiss.dbeaver.postgresql.pldbg;

public interface Breakpoint {
	
	DebugObject<?> getObj();	
	BreakpointProperties getProperties();
	void drop() throws DebugException;

}
