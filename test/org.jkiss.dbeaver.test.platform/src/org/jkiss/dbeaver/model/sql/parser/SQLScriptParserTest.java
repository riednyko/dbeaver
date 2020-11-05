/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
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
package org.jkiss.dbeaver.model.sql.parser;

import org.eclipse.jface.text.Document;
import org.jkiss.dbeaver.ext.oracle.model.OracleSQLDialect;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDialect;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.sql.SQLScriptElement;
import org.jkiss.dbeaver.model.sql.SQLSyntaxManager;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SQLScriptParserTest {
    private final SQLDialect POSTGRE_DIALECT = new PostgreDialect();
    private final SQLDialect ORACLE_DIALECT = new OracleSQLDialect();

    @Mock
    private DBPDataSource dataSource;
    @Mock
    private DBPDataSourceContainer dataSourceContainer;
    @Mock
    private DBCExecutionContext executionContext;

    @Before
    public void init() {
        DBPConnectionConfiguration connectionConfiguration = new DBPConnectionConfiguration();
        DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
        Mockito.when(dataSource.getContainer()).thenReturn(dataSourceContainer);
        Mockito.when(dataSourceContainer.getConnectionConfiguration()).thenReturn(connectionConfiguration);
        Mockito.when(dataSourceContainer.getActualConnectionConfiguration()).thenReturn(connectionConfiguration);
        Mockito.when(dataSourceContainer.getPreferenceStore()).thenReturn(preferenceStore);
        Mockito.when(executionContext.getDataSource()).thenReturn(dataSource);
    }

    @Test
    public void parsePostgresDoubleDollar() {
        assertParse(POSTGRE_DIALECT,
            "do $a$\n\nbegin\n\traise notice 'hello';\nend\n\n$a$\n\ndummy",
            new String[]{
                "do $a$\n\nbegin\n\traise notice 'hello';\nend\n\n$a$",
                "dummy"
            });

        assertParse(POSTGRE_DIALECT,
            "$a$\n\n$b$\n\n$b$\n\n$a$\n\n$a$$a$\n\ndummy",
            new String[]{
                "$a$\n\n$b$\n\n$b$\n\n$a$",
                "$a$$a$",
                "dummy"
            });

        assertParse(POSTGRE_DIALECT,
            "do $$\ndeclare\nbegin\nnull;\nend $$",
            new String[]{
                "do $$\ndeclare\nbegin\nnull;\nend $$"
            });
    }

    @Test
    public void parseOracleDeclareBlock() {
        assertParse(ORACLE_DIALECT,
            "BEGIN\n" +
            "    BEGIN\n" +
            "    END;\n" +
            "END;" +
            "BEGIN\n" +
            "    NULL;\n" +
            "END;",
            new String[]{
                "BEGIN\n" +
                "    BEGIN\n" +
                "    END;\n" +
                "END;",
                "BEGIN\n" +
                "    NULL;\n" +
                "END;",
            });

        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "BEGIN\n" +
            "    NULL;\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "BEGIN\n" +
                "    NULL;\n" +
                "END;"
            });

        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "    text VARCHAR(10);\n" +
            "\n" +
            "    PROCEDURE greet(text IN VARCHAR2)\n" +
            "    IS\n" +
            "    BEGIN\n" +
            "        dbms_output.put_line(text);\n" +
            "    END;\n" +
            "BEGIN\n" +
            "    text := 'hello';\n" +
            "    greet(text);\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "    text VARCHAR(10);\n" +
                "\n" +
                "    PROCEDURE greet(text IN VARCHAR2)\n" +
                "    IS\n" +
                "    BEGIN\n" +
                "        dbms_output.put_line(text);\n" +
                "    END;\n" +
                "BEGIN\n" +
                "    text := 'hello';\n" +
                "    greet(text);\n" +
                "END;"
            });

        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "    text VARCHAR(10);\n" +
            "\n" +
            "    PROCEDURE greet(text IN VARCHAR2)\n" +
            "    IS\n" +
            "    BEGIN\n" +
            "        dbms_output.put_line(text);\n" +
            "    END;\n" +
            "BEGIN\n" +
            "    DECLARE\n" +
            "    BEGIN\n" +
            "        text := 'hello';\n" +
            "        greet(text);\n" +
            "    END;\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "    text VARCHAR(10);\n" +
                "\n" +
                "    PROCEDURE greet(text IN VARCHAR2)\n" +
                "    IS\n" +
                "    BEGIN\n" +
                "        dbms_output.put_line(text);\n" +
                "    END;\n" +
                "BEGIN\n" +
                "    DECLARE\n" +
                "    BEGIN\n" +
                "        text := 'hello';\n" +
                "        greet(text);\n" +
                "    END;\n" +
                "END;"
            });

        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "    TYPE EmpRecTyp IS RECORD (\n" +
            "        emp_id     NUMBER(6),\n" +
            "        emp_sal    NUMBER(8,2)\n" +
            "    );\n" +
            "    PROCEDURE raise_salary (emp_info EmpRecTyp) IS\n" +
            "    BEGIN\n" +
            "        UPDATE employees SET salary = salary + salary * 0.10\n" +
            "        WHERE employee_id = emp_info.emp_id;\n" +
            "    END raise_salary;\n" +
            "BEGIN\n" +
            "    NULL;\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "    TYPE EmpRecTyp IS RECORD (\n" +
                "        emp_id     NUMBER(6),\n" +
                "        emp_sal    NUMBER(8,2)\n" +
                "    );\n" +
                "    PROCEDURE raise_salary (emp_info EmpRecTyp) IS\n" +
                "    BEGIN\n" +
                "        UPDATE employees SET salary = salary + salary * 0.10\n" +
                "        WHERE employee_id = emp_info.emp_id;\n" +
                "    END raise_salary;\n" +
                "BEGIN\n" +
                "    NULL;\n" +
                "END;"
            });

        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "  TYPE rec1_t IS RECORD (field1 VARCHAR2(16), field2 NUMBER, field3 DATE);\n" +
            "  TYPE rec2_t IS RECORD (id INTEGER NOT NULL := -1, \n" +
            "  name VARCHAR2(64) NOT NULL := '[anonymous]');\n" +
            "  rec1 rec1_t;\n" +
            "  rec2 rec2_t;\n" +
            "  rec3 employees%ROWTYPE;\n" +
            "  TYPE rec4_t IS RECORD (first_name employees.first_name%TYPE, \n" +
            "                         last_name employees.last_name%TYPE, \n" +
            "                         rating NUMBER);\n" +
            "  rec4 rec4_t;\n" +
            "BEGIN\n" +
            "  rec1.field1 := 'Yesterday';\n" +
            "  rec1.field2 := 65;\n" +
            "  rec1.field3 := TRUNC(SYSDATE-1);\n" +
            "  DBMS_OUTPUT.PUT_LINE(rec2.name);\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "  TYPE rec1_t IS RECORD (field1 VARCHAR2(16), field2 NUMBER, field3 DATE);\n" +
                "  TYPE rec2_t IS RECORD (id INTEGER NOT NULL := -1, \n" +
                "  name VARCHAR2(64) NOT NULL := '[anonymous]');\n" +
                "  rec1 rec1_t;\n" +
                "  rec2 rec2_t;\n" +
                "  rec3 employees%ROWTYPE;\n" +
                "  TYPE rec4_t IS RECORD (first_name employees.first_name%TYPE, \n" +
                "                         last_name employees.last_name%TYPE, \n" +
                "                         rating NUMBER);\n" +
                "  rec4 rec4_t;\n" +
                "BEGIN\n" +
                "  rec1.field1 := 'Yesterday';\n" +
                "  rec1.field2 := 65;\n" +
                "  rec1.field3 := TRUNC(SYSDATE-1);\n" +
                "  DBMS_OUTPUT.PUT_LINE(rec2.name);\n" +
                "END;"
            });
        
        assertParse(ORACLE_DIALECT,
            "DECLARE\n" +
            "    test_v NUMBER:=0;\n" +
            "    FUNCTION test_f(value_in_v IN number)\n" +
            "    RETURN\n" +
            "        varchar2\n" +
            "    IS\n" +
            "        value_char_out VARCHAR2(10);\n" +
            "    BEGIN\n" +
            "        SELECT to_char(value_in_v) INTO value_char_out FROM dual;\n" +
            "        RETURN value_char_out;\n" +
            "    END; \n" +
            "BEGIN\n" +
            "    dbms_output.put_line('Start');\n" +
            "    dbms_output.put_line(test_v||chr(9)||test_f(test_v));\n" +
            "    dbms_output.put_line('End');\n" +
            "END;",
            new String[]{
                "DECLARE\n" +
                "    test_v NUMBER:=0;\n" +
                "    FUNCTION test_f(value_in_v IN number)\n" +
                "    RETURN\n" +
                "        varchar2\n" +
                "    IS\n" +
                "        value_char_out VARCHAR2(10);\n" +
                "    BEGIN\n" +
                "        SELECT to_char(value_in_v) INTO value_char_out FROM dual;\n" +
                "        RETURN value_char_out;\n" +
                "    END; \n" +
                "BEGIN\n" +
                "    dbms_output.put_line('Start');\n" +
                "    dbms_output.put_line(test_v||chr(9)||test_f(test_v));\n" +
                "    dbms_output.put_line('End');\n" +
                "END;"
            });
    }

    private void assertParse(SQLDialect dialect, String query, String[] expected) {
        setDialect(dialect);
        SQLParserContext context = createParserContext(dialect, query);
        List<SQLScriptElement> elements = SQLScriptParser.extractScriptQueries(context, 0, context.getDocument().getLength(), false, false, false);
        Assert.assertEquals(expected.length, elements.size());
        for (int index = 0; index < expected.length; index++) {
            Assert.assertEquals(expected[index], elements.get(index).getText());
        }
    }

    private SQLParserContext createParserContext(SQLDialect dialect, String query) {
        SQLSyntaxManager syntaxManager = new SQLSyntaxManager();
        syntaxManager.init(dialect, dataSourceContainer.getPreferenceStore());
        SQLRuleManager ruleManager = new SQLRuleManager(syntaxManager);
        ruleManager.loadRules(dataSource, false);
        Document document = new Document(query);
        return new SQLParserContext(() -> executionContext, syntaxManager, ruleManager, document);
    }

    private void setDialect(SQLDialect dialect) {
        Mockito.when(dataSource.getSQLDialect()).thenReturn(dialect);
    }
}