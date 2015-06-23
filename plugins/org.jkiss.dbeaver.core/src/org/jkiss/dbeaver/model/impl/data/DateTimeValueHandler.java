/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.model.impl.data;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.data.editors.DateTimeEditorHelper;
import org.jkiss.dbeaver.model.impl.data.editors.DateTimeInlineEditor;
import org.jkiss.dbeaver.model.impl.data.editors.DateTimeStandaloneEditor;
import org.jkiss.dbeaver.model.impl.data.formatters.DefaultDataFormatter;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.properties.PropertySourceAbstract;

import java.text.ParseException;
import java.util.Date;

/**
 * Date/time value handler
 */
public abstract class DateTimeValueHandler extends BaseValueHandler implements DateTimeEditorHelper {

    protected static final Log log = Log.getLog(DateTimeValueHandler.class);

    private DBDDataFormatterProfile formatterProfile;
    private DBDDataFormatter formatter;

    public DateTimeValueHandler(DBDDataFormatterProfile formatterProfile)
    {
        this.formatterProfile = formatterProfile;
    }

    @Override
    public DBDValueEditor createEditor(@NotNull DBDValueController controller)
        throws DBException
    {
        switch (controller.getEditType()) {
            case INLINE:
            case PANEL:
                return new DateTimeInlineEditor(controller, this);
            case EDITOR:
                return new DateTimeStandaloneEditor(controller, this);
            default:
                return null;
        }
    }

    @Override
    public int getFeatures()
    {
        return FEATURE_VIEWER | FEATURE_EDITOR | FEATURE_INLINE_EDITOR;
    }

    @Override
    public Class getValueObjectType()
    {
        return Date.class;
    }

    @Override
    public Date getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, Object object, boolean copy) throws DBCException
    {
        if (object == null) {
            return null;
        } else if (object instanceof Date) {
            return (Date) (copy ? ((Date)object).clone() : object);
        } else if (object instanceof String) {
            String strValue = (String)object;
            try {
                return (Date) getFormatter(type).parseValue(strValue, null);
            } catch (ParseException e) {
                // Try to parse with standard date/time formats

                //DateFormat.get
                try {
                    // Try to parse as java date
                    @SuppressWarnings("deprecation")
                    Date result = new Date(strValue);
                    return result;
                } catch (Exception e1) {
                    log.debug("Can't parse string value [" + strValue + "] to date/time value", e);
                    return null;
                }
            }
        } else {
            log.warn("Unrecognized type '" + object.getClass().getName() + "' - can't convert to date/time value");
            return null;
        }
    }

    @Override
    public void contributeActions(@NotNull IContributionManager manager, @NotNull final DBDValueController controller)
        throws DBCException
    {
        manager.add(new Action(CoreMessages.model_jdbc_set_to_current_time, DBeaverIcons.getImageDescriptor(DBIcon.TYPE_DATETIME)) {
            @Override
            public void run() {
                controller.updateValue(new Date());
            }
        });
    }

    @Override
    public void contributeProperties(@NotNull PropertySourceAbstract propertySource, @NotNull DBDValueController controller)
    {
        super.contributeProperties(propertySource, controller);
        propertySource.addProperty(
            "Date/Time",
            "format", //$NON-NLS-1$
            "Pattern",
            getFormatter(controller.getValueType()).getPattern());
    }

    @NotNull
    @Override
    public String getValueDisplayString(@NotNull DBSTypedObject column, Object value, @NotNull DBDDisplayFormat format)
    {
        if (value != null) {
            try {
                return getFormatter(column).formatValue(value);
            } catch (Exception e) {
                return String.valueOf(value);
            }
        }
        return super.getValueDisplayString(column, null, format);
    }

    @Nullable
    protected static java.sql.Time getTimeValue(Object value)
    {
        if (value instanceof java.sql.Time) {
            return (java.sql.Time) value;
        } else if (value instanceof Date) {
            return new java.sql.Time(((Date) value).getTime());
        } else if (value != null) {
            return java.sql.Time.valueOf(value.toString());
        } else {
            return null;
        }
    }

    @Nullable
    protected static java.sql.Date getDateValue(Object value)
    {
        if (value instanceof java.sql.Date) {
            return (java.sql.Date) value;
        } else if (value instanceof Date) {
            return new java.sql.Date(((Date) value).getTime());
        } else if (value != null) {
            return java.sql.Date.valueOf(value.toString());
        } else {
            return null;
        }
    }

    @Nullable
    protected static java.sql.Timestamp getTimestampValue(Object value)
    {
        if (value instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) value;
        } else if (value instanceof Date) {
            return new java.sql.Timestamp(((Date) value).getTime());
        } else if (value != null) {
            return java.sql.Timestamp.valueOf(value.toString());
        } else {
            return null;
        }
    }

    protected static String getTwoDigitValue(int value)
    {
        if (value < 10) {
            return "0" + value;
        } else {
            return String.valueOf(value);
        }
    }

    public DBDDataFormatter getFormatter(String typeId)
    {
        try {
            return formatterProfile.createFormatter(typeId);
        } catch (Exception e) {
            log.error("Can't create formatter for datetime value handler", e); //$NON-NLS-1$
            return DefaultDataFormatter.INSTANCE;
        }
    }

    @Override
    @NotNull
    public DBDDataFormatter getFormatter(DBSTypedObject column)
    {
        if (formatter == null) {
            switch (column.getTypeID()) {
                case java.sql.Types.TIME:
                    formatter = getFormatter(DBDDataFormatter.TYPE_NAME_TIME);
                    break;
                case java.sql.Types.DATE:
                    formatter = getFormatter(DBDDataFormatter.TYPE_NAME_DATE);
                    break;
                default:
                    formatter = getFormatter(DBDDataFormatter.TYPE_NAME_TIMESTAMP);
                    break;
            }
        }
        return formatter;
    }

    private boolean isTimestamp(DBDValueController valueController) {
        return valueController.getValueType().getTypeID() == java.sql.Types.TIMESTAMP;
    }

}