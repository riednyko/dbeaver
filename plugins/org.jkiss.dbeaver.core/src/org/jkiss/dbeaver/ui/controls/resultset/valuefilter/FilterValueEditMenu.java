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
package org.jkiss.dbeaver.ui.controls.resultset.valuefilter;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.data.DBDLabelValuePair;
import org.jkiss.dbeaver.model.exec.DBCLogicalOperator;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetRow;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetViewer;

public class FilterValueEditMenu extends Composite{

	private Shell shell;
    private Object value;
    private Display display;
	private GenericFilterValueEdit filter;
    
	public FilterValueEditMenu(Composite parent, int style, @Nullable Control trigger,@NotNull ResultSetViewer viewer, @NotNull DBDAttributeBinding attr, @NotNull ResultSetRow[] rows) {
		super(parent, style);
		filter = new GenericFilterValueEdit(viewer, attr, rows, DBCLogicalOperator.IN);
		this.display = Display.getDefault();
		init(trigger);
	}

private void init(Control trigger) {
		
		shell = new Shell(display, SWT.BORDER);
		shell.setLayout(new GridLayout());
		
//		shell.addTraverseListener(new TraverseListener() {
//			
//			@Override
//			public void keyTraversed(TraverseEvent e) {
//				if (e.detail == SWT.TRAVERSE_ESCAPE)
//		    		 shell.setVisible(false);
//		    		 shell.close(); 	
//			}
//		});
		shell.addListener(SWT.Deactivate, e-> {
	       	 if (shell != null && !shell.isDisposed()) {
	    		 shell.setVisible(false);	    
	    		 shell.dispose();    		 
	    	 }
		   	});
		
	   	createMultiValueSelector(shell);
	   	shell.pack();
		
		
		if(trigger != null) {
			trigger.addListener(SWT.MouseDown, e -> {
				setVisible(true);
		    });
		}
	    
	}


	public void setLocation(Point location) {
		shell.setLocation(location);
	}
	
	public int open() {		
		shell.open();		
		while (!shell.isDisposed()) {
		    if (!display.readAndDispatch()) {
		        display.sleep();
		    }
		}
		return isAnythingSelected() ?  IDialogConstants.OK_ID : IDialogConstants.CANCEL_ID;
	}
	

	
	
	private boolean isAnythingSelected() {
		return value != null;
	}


	private void createMultiValueSelector(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        gd.heightHint = 300;
        tableComposite.setLayoutData(gd);
		
        filter.setupTable(tableComposite, SWT.BORDER | SWT.SINGLE | SWT.NO_SCROLL | SWT.V_SCROLL, false, false, SWT.NONE);
        filter.table.getTable().setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        TableViewerColumn resultsetColumn = new TableViewerColumn(filter.table, SWT.NONE);
        resultsetColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return filter.attr.getValueHandler().getValueDisplayString(filter.attr, ((DBDLabelValuePair)element).getValue(), DBDDisplayFormat.UI);
            }});
        
        resultsetColumn.getColumn().setResizable(false);
        TableColumnLayout tableLayout = new TableColumnLayout();
        tableComposite.setLayout(tableLayout);
        
        // Resize the column to fit the contents
 		resultsetColumn.getColumn().pack(); 	   
 	    int resultsetWidth = resultsetColumn.getColumn().getWidth(); 	    
 	    // Set  column to fill 100%, but with its packed width as minimum
 	    tableLayout.setColumnData(resultsetColumn.getColumn(), new ColumnWeightData(100, resultsetWidth));	
        
  
 	    filter.table.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				value = ((DBDLabelValuePair) event.getStructuredSelection().getFirstElement());
				shell.setVisible(false);
	    		shell.close();
				
			}
		});
 	    
 	    
           
        if ( filter.attr.getDataKind() == DBPDataKind.STRING) {
        	filter.addFilterTextbox(parent);
        }
        filter.filterPattern = null;
        filter.loadValues();
    }
	
	public Object getValue() {	          
        return ((DBDLabelValuePair) value).getValue();
    }
}
