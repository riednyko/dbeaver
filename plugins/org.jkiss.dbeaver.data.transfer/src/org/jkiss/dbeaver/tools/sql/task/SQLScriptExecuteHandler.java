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
package org.jkiss.dbeaver.tools.sql.task;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPContextProvider;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.sql.SQLScriptElement;
import org.jkiss.dbeaver.model.sql.SQLSyntaxManager;
import org.jkiss.dbeaver.model.sql.parser.SQLParserContext;
import org.jkiss.dbeaver.model.sql.parser.SQLRuleManager;
import org.jkiss.dbeaver.model.sql.parser.SQLScriptParser;
import org.jkiss.dbeaver.model.task.DBTTask;
import org.jkiss.dbeaver.model.task.DBTTaskExecutionListener;
import org.jkiss.dbeaver.model.task.DBTTaskHandler;
import org.jkiss.dbeaver.tools.sql.SQLScriptExecuteSettings;
import org.jkiss.utils.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

/**
 * SQLScriptExecuteHandler
 */
public class SQLScriptExecuteHandler implements DBTTaskHandler {

    @Override
    public void executeTask(
        @NotNull DBRRunnableContext runnableContext,
        @NotNull DBTTask task,
        @NotNull Locale locale,
        @NotNull Log log,
        @NotNull Writer logStream,
        @NotNull DBTTaskExecutionListener listener) throws DBException
    {
        SQLScriptExecuteSettings settings = new SQLScriptExecuteSettings();
        settings.loadConfiguration(runnableContext, task.getProperties());
        executeWithSettings(runnableContext, locale, log, listener, settings);
    }

    private void executeWithSettings(@NotNull DBRRunnableContext runnableContext, @NotNull Locale locale, @NotNull Log log, @NotNull DBTTaskExecutionListener listener, SQLScriptExecuteSettings settings) throws DBException {
        // Start consumers
        listener.taskStarted(settings);

        log.debug("SQL Script Execute");

        DBPDataSourceContainer dataSourceContainer = settings.getDataSourceContainer();
        Throwable error = null;
        try {
            runnableContext.run(true, true, monitor -> {
                try {
                    runScripts(monitor, dataSourceContainer, settings, log);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            });
        } catch (InvocationTargetException e) {
            error = e.getTargetException();
        } catch (InterruptedException e) {
            log.debug("Task canceled");
        }

        listener.taskFinished(settings, error);

        log.debug("SQL script execute completed");
    }

    private void runScripts(DBRProgressMonitor monitor, DBPDataSourceContainer dataSourceContainer, SQLScriptExecuteSettings settings, Log log) throws DBException {
        if (!dataSourceContainer.isConnected()) {
            dataSourceContainer.connect(monitor, true, true);
        }
        DBPDataSource dataSource = dataSourceContainer.getDataSource();
        if (dataSource == null) {
            throw new DBException("Can't obtain data source connection");
        }
        DBCExecutionContext executionContext = dataSource.getDefaultInstance().getDefaultContext(monitor, false);

        for (String filePath : settings.getScriptFiles()) {
            IFile sqlFile = SQLScriptExecuteSettings.getWorkspaceFile(filePath);
            try (InputStream sqlStream = sqlFile.getContents(true)) {
                try (Reader fileReader = new InputStreamReader(sqlStream, sqlFile.getCharset())) {
                    String sqlScriptContent = IOUtils.readToString(fileReader);
                    try {
                        processScript(monitor, dataSource, executionContext, filePath, sqlScriptContent, log);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            } catch (Exception e) {
                throw new DBException("Error executing script '" + filePath + "'",
                    e instanceof InvocationTargetException ? ((InvocationTargetException) e).getTargetException() : e);
            }
        }
    }

    private void processScript(DBRProgressMonitor monitor, DBPDataSource dataSource, DBCExecutionContext executionContext, String filePath, String sqlScriptContent, Log log) throws DBException {
        DBPContextProvider contextProvider = () -> executionContext;

        SQLSyntaxManager syntaxManager = new SQLSyntaxManager();
        syntaxManager.init(dataSource);
        SQLRuleManager ruleManager = new SQLRuleManager(syntaxManager);
        ruleManager.loadRules(dataSource, false);

        Document sqlDocument = new Document(sqlScriptContent);

        SQLParserContext parserContext = new SQLParserContext(contextProvider, syntaxManager, ruleManager, sqlDocument);
        List<SQLScriptElement> scriptElements = SQLScriptParser.extractScriptQueries(parserContext, 0, sqlScriptContent.length(), true, false, true);
        for (SQLScriptElement element : scriptElements) {
            log.debug("===============================");
            log.debug(element.toString());
        }
    }

}
