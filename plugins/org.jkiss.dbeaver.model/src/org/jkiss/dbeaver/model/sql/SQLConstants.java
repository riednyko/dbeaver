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

package org.jkiss.dbeaver.model.sql;

import org.jkiss.dbeaver.ModelPreferences;

/**
 * SQL editor constants
 */
public class SQLConstants {

    public static final String NULL_VALUE = "NULL";

    public final static String SQL_CONTENT_TYPE = "org.jkiss.dbeaver.sql";

    public final static String SQL_COMMENT = "sql_comment";

    public static final String SHORT_MESSAGE = "short_message"; //$NON-NLS-1$

    /**
     * Marker type contant for SQL portability targets.
     */
    public static final String PORTABILITY_MARKER_TYPE = ModelPreferences.PLUGIN_ID + ".portabilitytask";           //$NON-NLS-1$
    /**
     * Marker type contant for SQL syntax errors.
     */
    public static final String SYNTAX_MARKER_TYPE      = ModelPreferences.PLUGIN_ID + ".syntaxproblem";             //$NON-NLS-1$

    public static final String STR_QUOTE_SINGLE = "'";
    public static final String STR_QUOTE_DOUBLE = "\"";
    public static final String ML_COMMENT_START = "/*";
    public static final String ML_COMMENT_END = "*/";

    public static final String[] TABLE_KEYWORDS = {
        "FROM",
        "UPDATE",
        "INTO",
        "TABLE",
        "JOIN"
    }; //$NON-NLS$
    public static final String[] COLUMN_KEYWORDS = {
        "SELECT",
        "WHERE",
        "SET",
        "ON",
        "AND",
        "OR",
        "BY",
        "HAVING"
    };

    public static final String[] SQL2003_RESERVED_KEYWORDS = {
        "ABS",
        "ALL",
        "ALLOCATE",
        "ALTER",
        "AND",
        "ANY",
        "ARE",
        "ARRAY",
        "AS",
        "ASENSITIVE",
        "ASYMMETRIC",
        "AT",
        "ATOMIC",
        "AUTHORIZATION",
        "AVG",
        "BEGIN",
        "BETWEEN",
        "BIGINT",
        "BINARY",
        "BLOB",
        "BOOLEAN",
        "BOTH",
        "BY",
        "CALL",
        "CALLED",
        "CARDINALITY",
        "CASCADED",
        "CASE",
        "CAST",
        "CEIL",
        "CEILING",
        "CHAR",
        "CHAR_LENGTH",
        "CHARACTER",
        "CHARACTER_LENGTH",
        "CHECK",
        "CLOB",
        "CLOSE",
        "COALESCE",
        "COLLATE",
        "COLLECT",
        "COLUMN",
        "COMMIT",
        "CONDITION",
        "CONNECT",
        "CONSTRAINT",
        "CONVERT",
        "CORR",
        "CORRESPONDING",
        "COUNT",
        "COVAR_POP",
        "COVAR_SAMP",
        "CREATE",
        "CROSS",
        "CUBE",
        "CUME_DIST",
        "CURRENT",
        "CURRENT_DATE",
        "CURRENT_DEFAULT_TRANSFORM_GROUP",
        "CURRENT_PATH",
        "CURRENT_ROLE",
        "CURRENT_TIME",
        "CURRENT_TIMESTAMP",
        "CURRENT_TRANSFORM_GROUP_FOR_TYPE",
        "CURRENT_USER",
        "CURSOR",
        "CYCLE",
        "DATE",
        "DAY",
        "DEALLOCATE",
        "DEC",
        "DECIMAL",
        "DECLARE",
        "DEFAULT",
        "DELETE",
        "DENSE_RANK",
        "DEREF",
        "DESCRIBE",
        "DETERMINISTIC",
        "DISCONNECT",
        "DISTINCT",
        "DOUBLE",
        "DROP",
        "DYNAMIC",
        "EACH",
        "ELEMENT",
        "ELSE",
        "END",
        "END-EXEC",
        "ESCAPE",
        "EVERY",
        "EXCEPT",
        "EXEC",
        "EXECUTE",
        "EXISTS",
        "EXP",
        "EXTERNAL",
        "EXTRACT",
        "FALSE",
        "FETCH",
        "FILTER",
        "FLOAT",
        "FLOOR",
        "FOR",
        "FOREIGN",
        "FREE",
        "FROM",
        "FULL",
        "FUNCTION",
        "FUSION",
        "GET",
        "GLOBAL",
        "GRANT",
        "GROUP",
        "GROUPING",
        "HAVING",
        "HOLD",
        "HOUR",
        "IDENTITY",
        "IN",
        "INDICATOR",
        "INNER",
        "INOUT",
        "INSENSITIVE",
        "INSERT",
        "INT",
        "INTEGER",
        "INTERSECT",
        "INTERSECTION",
        "INTERVAL",
        "INTO",
        "IS",
        "JOIN",
        "LANGUAGE",
        "LARGE",
        "LATERAL",
        "LEADING",
        "LEFT",
        "LIKE",
        "LN",
        "LOCAL",
        "LOCALTIME",
        "LOCALTIMESTAMP",
        "LOWER",
        "MATCH",
        "MAX",
        "MEMBER",
        "MERGE",
        "METHOD",
        "MIN",
        "MINUTE",
        "MOD",
        "MODIFIES",
        "MODULE",
        "MONTH",
        "MULTISET",
        "NATIONAL",
        "NATURAL",
        "NCHAR",
        "NCLOB",
        "NEW",
        "NO",
        "NONE",
        "NORMALIZE",
        "NOT",
        "NULL",
        "NULLIF",
        "NUMERIC",
        "OCTET_LENGTH",
        "OF",
        "OLD",
        "ON",
        "ONLY",
        "OPEN",
        "OR",
        "ORDER",
        "OUT",
        "OUTER",
        "OVER",
        "OVERLAPS",
        "OVERLAY",
        "PARAMETER",
        "PARTITION",
        "PERCENT_RANK",
        "PERCENTILE_CONT",
        "PERCENTILE_DISC",
        "POSITION",
        "POWER",
        "PRECISION",
        "PREPARE",
        "PRIMARY",
        "PROCEDURE",
        "RANGE",
        "RANK",
        "READS",
        "REAL",
        "RECURSIVE",
        "REF",
        "REFERENCES",
        "REFERENCING",
        "REGR_AVGX",
        "REGR_AVGY",
        "REGR_COUNT",
        "REGR_INTERCEPT",
        "REGR_R2",
        "REGR_SLOPE",
        "REGR_SXX",
        "REGR_SXY",
        "REGR_SYY",
        "RELEASE",
        "RESULT",
        "RETURN",
        "RETURNS",
        "REVOKE",
        "RIGHT",
        "ROLLBACK",
        "ROLLUP",
        "ROW",
        "ROW_NUMBER",
        "ROWS",
        "SAVEPOINT",
        "SCOPE",
        "SCROLL",
        "SEARCH",
        "SECOND",
        "SELECT",
        "SENSITIVE",
        "SESSION_USER",
        "SET",
        "SIMILAR",
        "SMALLINT",
        "SOME",
        "SPECIFIC",
        "SPECIFICTYPE",
        "SQL",
        "SQLEXCEPTION",
        "SQLSTATE",
        "SQLWARNING",
        "SQRT",
        "START",
        "STATIC",
        "STDDEV_POP",
        "STDDEV_SAMP",
        "SUBMULTISET",
        "SUBSTRING",
        "SUM",
        "SYMMETRIC",
        "SYSTEM",
        "SYSTEM_USER",
        "TABLE",
        "TABLESAMPLE",
        "THEN",
        "TIME",
        "TIMESTAMP",
        "TIMEZONE_HOUR",
        "TIMEZONE_MINUTE",
        "TO",
        "TRAILING",
        "TRANSLATE",
        "TRANSLATION",
        "TREAT",
        "TRIGGER",
        "TRIM",
        "TRUE",
        "UESCAPE",
        "UNION",
        "UNIQUE",
        "UNKNOWN",
        "UNNEST",
        "UPDATE",
        "UPPER",
        "USER",
        "USING",
        "VALUE",
        "VALUES",
        "VAR_POP",
        "VAR_SAMP",
        "VARCHAR",
        "VARYING",
        "WHEN",
        "WHENEVER",
        "WHERE",
        "WIDTH_BUCKET",
        "WINDOW",
        "WITH",
        "WITHIN",
        "WITHOUT",
        "YEAR",
    };

    public static final String[] SQL2003_NON_RESERVED_KEYWORDS = {
        "A",
        "ABSOLUTE",
        "ACTION",
        "ADA",
        "ADD",
        "ADMIN",
        "AFTER",
        "ALWAYS",
        "ASC",
        "ASSERTION",
        "ASSIGNMENT",
        "ATTRIBUTE",
        "ATTRIBUTES",
        "BEFORE",
        "BERNOULLI",
        "BREADTH",
        "C",
        "CASCADE",
        "CATALOG",
        "CATALOG_NAME",
        "CHAIN",
        "CHARACTER_SET_CATALOG",
        "CHARACTER_SET_NAME",
        "CHARACTER_SET_SCHEMA",
        "CHARACTERISTICS",
        "CHARACTERS",
        "CLASS_ORIGIN",
        "COBOL",
        "COLLATION",
        "COLLATION_CATALOG",
        "COLLATION_NAME",
        "COLLATION_SCHEMA",
        "COLUMN_NAME",
        "COMMAND_FUNCTION",
        "COMMAND_FUNCTION_CODE",
        "COMMITTED",
        "CONDITION_NUMBER",
        "CONNECTION",
        "CONNECTION_NAME",
        "CONSTRAINT_CATALOG",
        "CONSTRAINT_NAME",
        "CONSTRAINT_SCHEMA",
        "CONSTRAINTS",
        "CONSTRUCTOR",
        "CONTAINS",
        "CONTINUE",
        "CURSOR_NAME",
        "DATA",
        "DATETIME_INTERVAL_CODE",
        "DATETIME_INTERVAL_PRECISION",
        "DEFAULTS",
        "DEFERRABLE",
        "DEFERRED",
        "DEFINED",
        "DEFINER",
        "DEGREE",
        "DEPTH",
        "DERIVED",
        "DESC",
        "DESCRIPTOR",
        "DIAGNOSTICS",
        "DISPATCH",
        "DOMAIN",
        "DYNAMIC_FUNCTION",
        "DYNAMIC_FUNCTION_CODE",
        "EQUALS",
        "EXCEPTION",
        "EXCLUDE",
        "EXCLUDING",
        "FINAL",
        "FIRST",
        "FOLLOWING",
        "FORTRAN",
        "FOUND",
        "G",
        "GENERAL",
        "GENERATED",
        "GO",
        "GOTO",
        "GRANTED",
        "HIERARCHY",
        "IMMEDIATE",
        "IMPLEMENTATION",
        "INCLUDING",
        "INCREMENT",
        "INITIALLY",
        "INPUT",
        "INSTANCE",
        "INSTANTIABLE",
        "INVOKER",
        "ISOLATION",
        "K",
        "KEY",
        "KEY_MEMBER",
        "KEY_TYPE",
        "LAST",
        "LENGTH",
        "LEVEL",
        "LOCATOR",
        "M",
        "MAP",
        "MATCHED",
        "MAXVALUE",
        "MESSAGE_LENGTH",
        "MESSAGE_OCTET_LENGTH",
        "MESSAGE_TEXT",
        "MINVALUE",
        "MORE",
        "MUMPS",
        "NAME",
        "NAMES",
        "NESTING",
        "NEXT",
        "NORMALIZED",
        "NULLABLE",
        "NULLS",
        "NUMBER",
        "OBJECT",
        "OCTETS",
        "OPTION",
        "OPTIONS",
        "ORDERING",
        "ORDINALITY",
        "OTHERS",
        "OUTPUT",
        "OVERRIDING",
        "PAD",
        "PARAMETER_MODE",
        "PARAMETER_NAME",
        "PARAMETER_ORDINAL_POSITION",
        "PARAMETER_SPECIFIC_CATALOG",
        "PARAMETER_SPECIFIC_NAME",
        "PARAMETER_SPECIFIC_SCHEMA",
        "PARTIAL",
        "PASCAL",
        "PATH",
        "PLACING",
        "PLI",
        "PRECEDING",
        "PRESERVE",
        "PRIOR",
        "PRIVILEGES",
        "PUBLIC",
        "READ",
        "RELATIVE",
        "REPEATABLE",
        "RESTART",
        "RESTRICT",
        "RETURNED_CARDINALITY",
        "RETURNED_LENGTH",
        "RETURNED_OCTET_LENGTH",
        "RETURNED_SQLSTATE",
        "ROLE",
        "ROUTINE",
        "ROUTINE_CATALOG",
        "ROUTINE_NAME",
        "ROUTINE_SCHEMA",
        "ROW_COUNT",
        "SCALE",
        "SCHEMA",
        "SCHEMA_NAME",
        "SCOPE_CATALOG",
        "SCOPE_NAME",
        "SCOPE_SCHEMA",
        "SECTION",
        "SECURITY",
        "SELF",
        "SEQUENCE",
        "SERIALIZABLE",
        "SERVER_NAME",
        "SESSION",
        "SETS",
        "SIMPLE",
        "SIZE",
        "SOURCE",
        "SPACE",
        "SPECIFIC_NAME",
        "STATE",
        "STATEMENT",
        "STRUCTURE",
        "STYLE",
        "SUBCLASS_ORIGIN",
        "TABLE_NAME",
        "TEMPORARY",
        "TIES",
        "TOP_LEVEL_COUNT",
        "TRANSACTION",
        "TRANSACTION_ACTIVE",
        "TRANSACTIONS_COMMITTED",
        "TRANSACTIONS_ROLLED_BACK",
        "TRANSFORM",
        "TRANSFORMS",
        "TRIGGER_CATALOG",
        "TRIGGER_NAME",
        "TRIGGER_SCHEMA",
        "TYPE",
        "UNBOUNDED",
        "UNCOMMITTED",
        "UNDER",
        "UNNAMED",
        "USAGE",
        "USER_DEFINED_TYPE_CATALOG",
        "USER_DEFINED_TYPE_CODE",
        "USER_DEFINED_TYPE_NAME",
        "USER_DEFINED_TYPE_SCHEMA",
        "VIEW",
        "WORK",
        "WRITE",
        "ZONE"
    };
    
    public static final String[] SQL_EX_KEYWORDS = {
        "CHANGE",
        "MODIFY",
    };
    public static final String[] DEFAULT_TYPES = {
        "CHAR",
        "VARCHAR",
        "VARBINARY",
        "INTEGER",
        "SMALLINT",
        "BIGINT",
        "NUMBER",
        "NUMERIC",
        "DECIMAL",
        "FLOAT",
        "DOUBLE",
        "DATE",
        "TIME",
        "TIMESTAMP",
        "CLOB",
        "BLOB",
    };

    public static final String BLOCK_BEGIN = "BEGIN";
    public static final String BLOCK_END = "END";

    /**
     * Pseudo variables - these are not dynamic parameters
     */
    public static final String[] PSEUDO_VARIABLES = {
        ":NEW",
        ":OLD",
    };

    public static final char STRUCT_SEPARATOR = '.'; //$NON-NLS-1$
    public static final String DEFAULT_STATEMENT_DELIMITER = ";";
    public static final String CONFIG_COLOR_KEYWORD = "org.jkiss.dbeaver.sql.editor.color.keyword.foreground";
    public static final String CONFIG_COLOR_DATATYPE = "org.jkiss.dbeaver.sql.editor.color.datatype.foreground";
    public static final String CONFIG_COLOR_STRING = "org.jkiss.dbeaver.sql.editor.color.string.foreground";
    public static final String CONFIG_COLOR_NUMBER = "org.jkiss.dbeaver.sql.editor.color.number.foreground";
    public static final String CONFIG_COLOR_COMMENT = "org.jkiss.dbeaver.sql.editor.color.comment.foreground";
    public static final String CONFIG_COLOR_DELIMITER = "org.jkiss.dbeaver.sql.editor.color.delimiter.foreground";
    public static final String CONFIG_COLOR_PARAMETER = "org.jkiss.dbeaver.sql.editor.color.parameter.foreground";
    public static final String CONFIG_COLOR_TEXT = "org.jkiss.dbeaver.sql.editor.color.text.foreground";
    public static final String CONFIG_COLOR_BACKGROUND = "org.jkiss.dbeaver.sql.editor.color.text.background";
    public static final String CONFIG_COLOR_DISABLED = "org.jkiss.dbeaver.sql.editor.color.disabled.background";
    public static final String CONFIG_FONT_OUTPUT = "org.jkiss.dbeaver.sql.editor.font.output";

    public static final char DEFAULT_PARAMETER_MARK = '?';
    public static final char DEFAULT_PARAMETER_PREFIX = ':';
    public static final String DEFAULT_IDENTIFIER_QUOTE = "\"";
}
