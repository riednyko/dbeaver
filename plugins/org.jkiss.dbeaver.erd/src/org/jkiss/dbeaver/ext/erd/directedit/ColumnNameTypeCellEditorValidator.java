/*
 * Copyright (C) 2010-2015 Serge Rieder
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
/*
 * Created on Jul 19, 2004
 */
package org.jkiss.dbeaver.ext.erd.directedit;

import org.eclipse.jface.viewers.ICellEditorValidator;


/**
 * ICellValidator to validate direct edit values in the column label
 * Collaborates with an instance of ValidationMessageHandler
 * @author Serge Rieder
 */
public class ColumnNameTypeCellEditorValidator implements ICellEditorValidator
{

	private ValidationMessageHandler handler;
	
	/**
	 * @param validationMessageHandler the validation message handler to pass error information to
	 */
	public ColumnNameTypeCellEditorValidator(ValidationMessageHandler validationMessageHandler)
	{
		this.handler = validationMessageHandler;
	}

	/**
	 * @return the error message if an error has occurred, otherwise null
	 */
	@Override
    public String isValid(Object value)
	{
		String string = (String)value;
		String name = null;
		String type = null;
		int colonIndex = string.indexOf(':');
		if (colonIndex >= 0)
		{
			name = string.substring(0, colonIndex);
			if (string.length() > colonIndex+1)
			{
				type = string.substring(colonIndex+1);
			}
		}
		if (name != null && type!= null)
		{
			
			if (name.indexOf(" ")!= -1)
			{
				String text = "Column name should not include the space character";
				return setMessageText(text);
			} else {
                return unsetMessageText();
            }
		}
		else
		{
			String text = "Invalid format for text entry. Needs [name]:[type] format";
			return setMessageText(text);
		}
	}


	private String unsetMessageText()
	{
		handler.reset();
		return null;
	}

	


	private String setMessageText(String text)
	{
		handler.setMessageText(text);
		return text;
	}



}
