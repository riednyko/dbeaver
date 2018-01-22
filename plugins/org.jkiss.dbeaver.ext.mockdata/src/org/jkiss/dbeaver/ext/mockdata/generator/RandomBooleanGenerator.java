/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2010-2017 Eugene Fradkin (eugene.fradkin@gmail.com)
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
package org.jkiss.dbeaver.ext.mockdata.generator;

import org.jkiss.dbeaver.ext.mockdata.model.MockValueGenerator;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSDataManipulator;

import java.util.Map;
import java.util.Random;

public class RandomBooleanGenerator implements MockValueGenerator {

    private Random random = new Random();

    @Override
    public void init(DBSDataManipulator container, Map<String, Object> properties) throws DBCException {

    }

    @Override
    public void nextRow() {

    }

    @Override
    public Object generateValue(DBSAttributeBase attribute) throws DBCException {
        return new Boolean(random.nextBoolean());
    }

    @Override
    public void dispose() {

    }
}
