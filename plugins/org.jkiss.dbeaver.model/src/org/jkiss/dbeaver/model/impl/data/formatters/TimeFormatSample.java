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
package org.jkiss.dbeaver.model.impl.data.formatters;

import org.jkiss.dbeaver.model.data.DBDDataFormatterSample;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TimeFormatSample implements DBDDataFormatterSample {

    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    @Override
    public Map<Object, Object> getDefaultProperties(Locale locale)
    {
//        SimpleDateFormat tmp = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
//        String pattern = tmp.toPattern();
        return Collections.singletonMap((Object)DateTimeDataFormatter.PROP_PATTERN, (Object)DEFAULT_TIME_PATTERN);
    }

    @Override
    public Object getSampleValue()
    {
        return new Date();
    }

}
