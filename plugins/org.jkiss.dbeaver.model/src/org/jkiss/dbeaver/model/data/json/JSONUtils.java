/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.model.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.utils.CommonUtils;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JSON utils
 */
public class JSONUtils {

    private static final Log log = Log.getLog(JSONUtils.class);

    private static SimpleDateFormat dateFormat;

    static {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        dateFormat = new SimpleDateFormat(DBConstants.DEFAULT_ISO_TIMESTAMP_FORMAT);
        dateFormat.setTimeZone(tz);
    }

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static Date parseDate(String str) {
        if (CommonUtils.isEmpty(str)) {
            return null;
        }
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            log.error("Error parsing date");
            return new Date(0l);
        }
    }

    public static String formatISODate(Date date) {
        return "ISODate(\"" + formatDate(date) + "\")";  //$NON-NLS-1$//$NON-NLS-2$
    }

    public static String escapeJsonString(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '"':
                case '\\':
                case '/':
                    result.append("\\").append(c);
                    break;
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }

    @NotNull
    public static JsonWriter field(@NotNull JsonWriter json, @NotNull String name, @Nullable String value) throws IOException {
        json.name(name);
        if (value == null) json.nullValue(); else json.value(value);
        return json;
    }

    @NotNull
    public static JsonWriter fieldNE(@NotNull JsonWriter json, @NotNull String name, @Nullable String value) throws IOException {
        if (CommonUtils.isEmpty(value)) {
            return json;
        }
        json.name(name);
        json.value(value);
        return json;
    }

    @NotNull
    public static JsonWriter field(@NotNull JsonWriter json, @NotNull String name, long value) throws IOException {
        json.name(name);
        json.value(value);
        return json;
    }

    @NotNull
    public static JsonWriter field(@NotNull JsonWriter json, @NotNull String name, double value) throws IOException {
        json.name(name);
        json.value(value);
        return json;
    }

    @NotNull
    public static JsonWriter field(@NotNull JsonWriter json, @NotNull String name, boolean value) throws IOException {
        json.name(name);
        json.value(value);
        return json;
    }

    public static void serializeStringList(@NotNull JsonWriter json, @NotNull String tagName, @Nullable Collection<String> list) throws IOException {
        if (!CommonUtils.isEmpty(list)) {
            json.name(tagName);
            json.beginArray();
            for (String include : CommonUtils.safeCollection(list)) {
                json.value(include);
            }
            json.endArray();
        }
    }

    public static void serializeObjectList(@NotNull JsonWriter json, @NotNull String tagName, @Nullable Collection<Object> list) throws IOException {
        if (!CommonUtils.isEmpty(list)) {
            json.name(tagName);
            json.beginArray();
            for (Object value : CommonUtils.safeCollection(list)) {
                if (value == null) {
                    json.nullValue();
                } else if (value instanceof Number) {
                    json.value((Number) value);
                } else if (value instanceof Boolean) {
                    json.value((Boolean) value);
                } else {
                    json.value(value.toString());
                }
            }
            json.endArray();
        }
    }

    public static void serializeProperties(@NotNull JsonWriter json, @NotNull String tagName, @Nullable Map<String, String> properties) throws IOException {
        if (!CommonUtils.isEmpty(properties)) {
            json.name(tagName);
            json.beginObject();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                field(json, entry.getKey(), entry.getValue());
            }
            json.endObject();
        }
    }

    @NotNull
    public static Map<String, Object> parseMap(@NotNull Gson gson, @NotNull Reader reader) {
        return gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
    }

    @NotNull
    public static Map<String, Object> getObject(@NotNull Map<String, Object> map, @NotNull String name) {
        Map<String, Object> object = (Map<String, Object>) map.get(name);
        if (object == null) {
            return Collections.emptyMap();
        } else {
            return object;
        }
    }

    @NotNull
    public static Iterable<Map.Entry<String, Object>> getObjectElements(@NotNull Map<String, Object> map, @NotNull String name) {
        Map<String, Object> object = (Map<String, Object>) map.get(name);
        if (object == null) {
            return Collections.emptyList();
        } else {
            return object.entrySet();
        }
    }

    public static <T> T getObjectProperty(Object object, String name) {
        if (object instanceof Map) {
            return (T) ((Map) object).get(name);
        }
        log.error("Object " + object + " is not map");
        return null;
    }
}
