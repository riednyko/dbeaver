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
package org.jkiss.dbeaver.model.impl.data.transformers;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.data.ProxyValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transforms numeric attribute value into epoch time
 */
public class EpochTimeAttributeTransformer implements DBDAttributeTransformer {

    static final Log log = Log.getLog(EpochTimeAttributeTransformer.class);
    private static final String PROP_UNIT = "unit";

    enum EpochUnit {
        seconds,
        milliseconds,
        nanoseconds
    }

    @Override
    public void transformAttribute(@NotNull DBCSession session, @NotNull DBDAttributeBinding attribute, @NotNull List<Object[]> rows, @NotNull Map<String, String> options) throws DBException {
        // TODO: Change attribute type (to DATETIME)
        EpochUnit unit = EpochUnit.milliseconds;
        if (options.containsKey(PROP_UNIT)) {
            try {
                unit = EpochUnit.valueOf(options.get(PROP_UNIT));
            } catch (IllegalArgumentException e) {
                log.error("Bad unit option", e);
            }
        }
        attribute.setValueHandler(new EpochValueHandler(attribute.getValueHandler(), unit));
    }

    private class EpochValueHandler extends ProxyValueHandler {
        private final EpochUnit unit;
        public EpochValueHandler(DBDValueHandler target, EpochUnit unit) {
            super(target);
            this.unit = unit;
        }

        @NotNull
        @Override
        public String getValueDisplayString(@NotNull DBSTypedObject column, @Nullable Object value, @NotNull DBDDisplayFormat format) {
            if (value instanceof Number) {
                long dateValue = ((Number) value).longValue();
                switch (unit) {
                    case seconds: dateValue *= 1000; break;
                    case nanoseconds: dateValue /= 1000; break;
                }
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(
                    new Date(dateValue));
            }
            return DBUtils.getDefaultValueDisplayString(value, format);
        }
    }
}
