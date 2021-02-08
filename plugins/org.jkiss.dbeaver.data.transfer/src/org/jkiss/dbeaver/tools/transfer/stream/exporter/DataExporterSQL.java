/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
package org.jkiss.dbeaver.tools.transfer.stream.exporter;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPIdentifierCase;
import org.jkiss.dbeaver.model.DBPNamedObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLConstants;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSDataManipulator;
import org.jkiss.dbeaver.tools.transfer.DTUtils;
import org.jkiss.dbeaver.tools.transfer.stream.IStreamDataExporterSite;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

/**
 * SQL Exporter
 */
public class DataExporterSQL extends StreamExporterAbstract {

    private static final Log log = Log.getLog(DataExporterSQL.class);

    private static final String PROP_INCLUDE_AUTO_GENERATED = "includeAutoGenerated";
    private static final String PROP_OMIT_SCHEMA = "omitSchema";
    private static final String PROP_ROWS_IN_STATEMENT = "rowsInStatement";
    private static final String PROP_DATA_FORMAT = "nativeFormat";
    private static final char STRING_QUOTE = '\'';
    private static final String PROP_LINE_BEFORE_ROWS = "lineBeforeRows";
    private static final String PROP_KEYWORD_CASE = "keywordCase";
    private static final String PROP_UPSERT = "upsertKeyword";
    private static final String PROP_ON_CONFLICT = "insertOnConflict";

    private boolean includeAutoGenerated;
    private String rowDelimiter;
    private boolean omitSchema;
    private int rowsInStatement;
    private boolean useNativeDataFormat = true;
    private boolean lineBeforeRows = true;
    private String tableName;
    private DBDAttributeBinding[] columns;
    private boolean oneLineEntry;

    private final String KEYWORD_INSERT_INTO = "INSERT INTO";
    private final String KEYWORD_VALUES = "VALUES";
    private final String KEYWORD_INTO = "INTO";
    private final static String KEYWORD_INSERT_ALL = "INSERT ALL";
    private final String KEYWORD_SELECT_FROM_DUAL = "SELECT 1 FROM DUAL";
    private final static String KEYWORD_UPDATE_OR = "UPDATE OR";
    private final static String KEYWORD_UPSERT_INTO = "UPSERT INTO";
    private final static String KEYWORD_REPLACE_INTO = "REPLACE INTO";
    private final static String KEYWORD_DUPLICATE_KEY = "ON DUPLICATE KEY UPDATE";
    private final static String KEYWORD_ON_CONFLICT = "ON CONFLICT";

    private DBPIdentifierCase identifierCase;
    private static String onConflictExpression;

    private transient StringBuilder sqlBuffer = new StringBuilder(100);
    private transient long rowCount;
    private SQLDialect dialect;

    enum InsertKeyword {
        INSERT("INSERT"),
        INSERT_ALL(KEYWORD_INSERT_ALL),
        UPDATE(KEYWORD_UPDATE_OR),
        UPSERT(KEYWORD_UPSERT_INTO),
        REPLACE(KEYWORD_REPLACE_INTO),
        ON_DUPLICATE(KEYWORD_DUPLICATE_KEY),
        ON_CONFLICT(KEYWORD_ON_CONFLICT);
        private String value;
        InsertKeyword(String v) {
            this.value = v;
        }
        public String value() {
            return value;
        }
        public static InsertKeyword fromValue(String v) {
            for (InsertKeyword s : InsertKeyword.values()) {
                if (s.value.equals(v)) {
                    return s;
                }
            }
            return INSERT;
        }
    }

    private InsertKeyword insertKeyword;

    private boolean isSkipColumn(DBDAttributeBinding attr) {
        return attr.isPseudoAttribute() || (!includeAutoGenerated && attr.isAutoGenerated()) ||
            attr instanceof DBDAttributeBindingCustom;
    }

    @Override
    public void init(IStreamDataExporterSite site) throws DBException {
        super.init(site);

        Map<String, Object> properties = site.getProperties();

        if (properties.containsKey(PROP_INCLUDE_AUTO_GENERATED)) {
            includeAutoGenerated = CommonUtils.toBoolean(properties.get(PROP_INCLUDE_AUTO_GENERATED));
        }
        if (properties.containsKey(PROP_OMIT_SCHEMA)) {
            omitSchema = CommonUtils.toBoolean(properties.get(PROP_OMIT_SCHEMA));
        }
        try {
            rowsInStatement = CommonUtils.toInt(properties.get(PROP_ROWS_IN_STATEMENT));
        } catch (NumberFormatException e) {
            rowsInStatement = 10;
        }
        useNativeDataFormat = CommonUtils.toBoolean(properties.get(PROP_DATA_FORMAT));
        lineBeforeRows = CommonUtils.toBoolean(properties.get(PROP_LINE_BEFORE_ROWS));
        rowDelimiter = GeneralUtils.getDefaultLineSeparator();
        dialect = SQLUtils.getDialectFromObject(site.getSource());

        if (properties.containsKey(DBSDataManipulator.OPTION_USE_CURRENT_DIALECT_SETTINGS)) {
            boolean useDBDefaultValueMode = CommonUtils.toBoolean(properties.get(DBSDataManipulator.OPTION_USE_CURRENT_DIALECT_SETTINGS));
            if (useDBDefaultValueMode && getDefaultMultiValueInsertMode() != SQLDialect.MultiValueInsertMode.GROUP_ROWS) {
                rowsInStatement = 1;
            }
        }

        String keywordCase = CommonUtils.toString(properties.get(PROP_KEYWORD_CASE));
        if (keywordCase.equalsIgnoreCase("lower")) {
            identifierCase = DBPIdentifierCase.LOWER;
        } else {
            identifierCase = DBPIdentifierCase.UPPER;
        }

        insertKeyword = InsertKeyword.fromValue(CommonUtils.toString(properties.get(PROP_UPSERT)));
        onConflictExpression = CommonUtils.toString(properties.get(PROP_ON_CONFLICT));
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void exportHeader(DBCSession session) throws DBException, IOException {
        if (useNativeDataFormat) {
            if (session instanceof DBDFormatSettingsExt) {
                ((DBDFormatSettingsExt) session).setUseNativeDateTimeFormat(true);
            }
        }
        columns = getSite().getAttributes();
        DBPNamedObject source = getSite().getSource();
        tableName = DTUtils.getTableName(session.getDataSource(), source, omitSchema);

        rowCount = 0;
    }

    @Override
    public void exportRow(DBCSession session, DBCResultSet resultSet, Object[] row) throws DBException, IOException {
        PrintWriter out = getWriter();
        oneLineEntry = rowsInStatement == 1;
        int columnsSize = columns.length;
        boolean firstRow = false;
        if (insertKeyword == InsertKeyword.INSERT_ALL) {
            sqlBuffer.append(identifierCase.transform(KEYWORD_INSERT_ALL));
        }
        if (oneLineEntry || insertKeyword == InsertKeyword.INSERT_ALL || rowCount % rowsInStatement == 0) {
            sqlBuffer.setLength(0);
            if (rowCount > 0) {
                //if (insertMode == SQLDialect.MultiValueInsertMode.PLAIN) {
                //    sqlBuffer.append(");");
                if (!oneLineEntry && insertKeyword != InsertKeyword.INSERT_ALL) {
                    if (!CommonUtils.isEmpty(onConflictExpression)) {
                        addOnConflictExpression(out);
                    }
                    sqlBuffer.append(";");
                } else if (insertKeyword == InsertKeyword.INSERT_ALL && rowCount % rowsInStatement == 0) {
                    sqlBuffer.append("\n").append(identifierCase.transform(KEYWORD_SELECT_FROM_DUAL)).append(";");
                }
                if (lineBeforeRows) {
                    sqlBuffer.append(rowDelimiter);
                }
            }
            switch (insertKeyword) {
                case UPDATE:
                    sqlBuffer.append(identifierCase.transform(KEYWORD_UPDATE_OR)).append(" ").append(identifierCase.transform(KEYWORD_INSERT_INTO));
                    break;
                case UPSERT:
                    sqlBuffer.append(identifierCase.transform(KEYWORD_UPSERT_INTO));
                    break;
                case REPLACE:
                    sqlBuffer.append(identifierCase.transform(KEYWORD_REPLACE_INTO));
                    break;
                default:
                    if (insertKeyword == InsertKeyword.INSERT_ALL) {
                        if (rowCount % rowsInStatement == 0) {
                            sqlBuffer.append(identifierCase.transform(KEYWORD_INSERT_ALL)).append("\n");
                        }
                        sqlBuffer.append("\t").append(identifierCase.transform(KEYWORD_INTO));
                    } else {
                        sqlBuffer.append(identifierCase.transform(KEYWORD_INSERT_INTO));
                    }
            }
            sqlBuffer.append(" ").append(tableName).append(" (");
            boolean hasColumn = false;
            for (DBDAttributeBinding column : columns) {
                if (isSkipColumn(column)) {
                    continue;
                }
                if (hasColumn) {
                    sqlBuffer.append(',');
                }
                hasColumn = true;
                sqlBuffer.append(DBUtils.getQuotedIdentifier(column));
            }
            sqlBuffer.append(") ");
            sqlBuffer.append(identifierCase.transform(KEYWORD_VALUES));
            if (oneLineEntry || insertKeyword == InsertKeyword.INSERT_ALL) {
                sqlBuffer.append(" (");
            }
            if (rowsInStatement > 1 && lineBeforeRows && insertKeyword != InsertKeyword.INSERT_ALL) {
                sqlBuffer.append(rowDelimiter);
            }
            out.write(sqlBuffer.toString());
            firstRow = true;
        }
        if (!oneLineEntry && !firstRow) {
            out.write(",");
            if (lineBeforeRows) {
                out.write(rowDelimiter);
            }
        }
        if (!oneLineEntry && insertKeyword != InsertKeyword.INSERT_ALL) {
            if (lineBeforeRows) {
                out.write("\t");
            }
            out.write(" (");
        }
        rowCount++;
        boolean hasValue = false;
        for (int i = 0; i < columnsSize; i++) {
            DBDAttributeBinding column = columns[i];
            if (isSkipColumn(column)) {
                continue;
            }

            if (hasValue) {
                out.write(',');
            }
            hasValue = true;
            Object value = row[i];

            if (DBUtils.isNullValue(value)) {
                // just skip it
                out.write(SQLConstants.NULL_VALUE);
            } else if (row[i] instanceof DBDContent) {
                DBDContent content = (DBDContent) row[i];
                try {
                    if (column.getValueHandler() instanceof DBDContentValueHandler) {
                        ((DBDContentValueHandler) column.getValueHandler()).writeStreamValue(session.getProgressMonitor(), session.getDataSource(), column, content, out);
                    } else {
                        // Content
                        // Inline textual content and handle binaries in some special way
                        DBDContentStorage cs = content.getContents(session.getProgressMonitor());
                        if (cs != null) {
                            if (ContentUtils.isTextContent(content)) {
                                try (Reader contentReader = cs.getContentReader()) {
                                    writeStringValue(contentReader);
                                }
                            } else {
                                getSite().writeBinaryData(cs);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn(e);
                } finally {
                    content.release();
                }
            } else if (value instanceof File) {
                out.write("@");
                out.write(((File) value).getAbsolutePath());
            } else {
                // If we have disabled "Native Date/Time format" option then we
                // use UI format + enquote value
                boolean needQuotes = false;
                DBDDisplayFormat displayFormat = DBDDisplayFormat.NATIVE;
                if (!useNativeDataFormat && column.getDataKind() == DBPDataKind.DATETIME) {
                    displayFormat = DBDDisplayFormat.UI;
                    needQuotes = true;
                }
                String sqlValue = SQLUtils.convertValueToSQL(
                    session.getDataSource(),
                    column,
                    column.getValueHandler(),
                    row[i],
                    displayFormat);
                if (needQuotes) out.write('\'');
                out.write(sqlValue);
                if (needQuotes) out.write('\'');
            }
        }
        //if (insertMode != SQLDialect.MultiValueInsertMode.PLAIN) {
            out.write(")");
        //}
        if (!CommonUtils.isEmpty(onConflictExpression) && oneLineEntry) {
            addOnConflictExpression(out);
        }
        if (oneLineEntry) {
            out.write(";");
        }
    }

    private void addOnConflictExpression(PrintWriter out) {
        if (insertKeyword == InsertKeyword.ON_CONFLICT) {
            out.write(" " + identifierCase.transform(KEYWORD_ON_CONFLICT) + " " + onConflictExpression);
        } else if (insertKeyword == InsertKeyword.ON_DUPLICATE) {
            out.write(" " + identifierCase.transform(KEYWORD_DUPLICATE_KEY) + " " + onConflictExpression);
        }
    }

    @Override
    public void exportFooter(DBRProgressMonitor monitor) {
        PrintWriter out = getWriter();
        if (insertKeyword == InsertKeyword.INSERT_ALL) {
            if (rowCount > 0) {
                out.write("\n" + identifierCase.transform(KEYWORD_SELECT_FROM_DUAL) + ";");
            }
        } else if (!oneLineEntry){
            if (rowCount > 0) {
                addOnConflictExpression(out);
                out.write(";");
            }
        }
    }

    private void writeStringValue(String value) {
        PrintWriter out = getWriter();
        out.write(STRING_QUOTE);
        if (dialect != null) {
            out.write(dialect.escapeString(value));
        } else {
            out.write(value);
        }
        out.write(STRING_QUOTE);
    }

    private void writeStringValue(Reader reader) throws IOException {
        try {
            PrintWriter out = getWriter();
            out.write(STRING_QUOTE);
            // Copy reader
            char buffer[] = new char[2000];
            for (; ; ) {
                int count = reader.read(buffer);
                if (count <= 0) {
                    break;
                }
                if (dialect != null) {
                    out.write(dialect.escapeString(String.valueOf(buffer, 0, count)));
                } else {
                    out.write(buffer, 0, count);
                }
            }
            out.write(STRING_QUOTE);
        } finally {
            ContentUtils.close(reader);
        }
    }

    private SQLDialect.MultiValueInsertMode getDefaultMultiValueInsertMode() {
        SQLDialect.MultiValueInsertMode insertMode = SQLDialect.MultiValueInsertMode.NOT_SUPPORTED;
        if (dialect != null && rowsInStatement != 1) {
            insertMode = dialect.getDefaultMultiValueInsertMode();
        }
        return insertMode;
    }

}
