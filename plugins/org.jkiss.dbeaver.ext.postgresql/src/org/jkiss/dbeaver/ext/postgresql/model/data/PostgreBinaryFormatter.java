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
package org.jkiss.dbeaver.ext.postgresql.model.data;

import org.jkiss.dbeaver.model.impl.data.formatters.BinaryFormatterHex;

/**
 * PostgreBinaryFormatter
 */
public class PostgreBinaryFormatter extends BinaryFormatterHex {

    public static final PostgreBinaryFormatter INSTANCE = new PostgreBinaryFormatter();
    private static final String HEX_PREFIX = "decode('";
    private static final String HEX_POSTFIX = "','hex')";

    @Override
    public String getId()
    {
        return "pghex";
    }

    @Override
    public String getTitle()
    {
        return "PostgreSQL Hex";
    }

    @Override
    public String toString(byte[] bytes, int offset, int length)
    {
        return HEX_PREFIX + super.toString(bytes, offset, length) + HEX_POSTFIX;
    }

    @Override
    public byte[] toBytes(String string)
    {
        if (string.startsWith(HEX_PREFIX)) {
            string = string.substring(
                HEX_PREFIX.length(),
                string.length() - HEX_POSTFIX.length());
        }
        return super.toBytes(string);
    }

}
