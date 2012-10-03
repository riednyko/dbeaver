/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.model.impl.data;

import org.jkiss.dbeaver.model.data.DBDDataFormatterSample;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TimeFormatSample implements DBDDataFormatterSample {

    @Override
    public Map<Object, Object> getDefaultProperties(Locale locale)
    {
        SimpleDateFormat tmp = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.LONG, locale);
        String pattern = tmp.toPattern();
        return Collections.singletonMap((Object)DateTimeDataFormatter.PROP_PATTERN, (Object)pattern);
    }

    @Override
    public Object getSampleValue()
    {
        return new Date();
    }

}
