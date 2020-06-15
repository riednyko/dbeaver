package org.jkiss.dbeaver.ext.mssql.tasks;

import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.sql.task.SQLToolExecuteHandler;
import org.jkiss.dbeaver.model.sql.task.SQLToolExecuteSettings;
import org.jkiss.dbeaver.model.sql.task.SQLToolRunStatisticsGenerator;
import org.jkiss.dbeaver.model.sql.task.SQLToolStatistics;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

public abstract class SQLServerToolWithStatus<OBJECT_TYPE extends DBSObject, SETTINGS extends SQLToolExecuteSettings<OBJECT_TYPE>>
        extends SQLToolExecuteHandler<OBJECT_TYPE, SETTINGS> implements SQLToolRunStatisticsGenerator<OBJECT_TYPE, SETTINGS, DBEPersistAction> {

    @Override
    public List<ToolStatus> getExecuteStatistics(OBJECT_TYPE object, SETTINGS settings, DBEPersistAction action, DBCSession session, DBCStatement dbStat) throws DBCException {
        List<ToolStatus> statusList = new ArrayList<>();
        try {
            int warnNum = 0;
            SQLWarning warning = ((JDBCStatement) dbStat).getWarnings();
            while (warning != null) {
                statusList.add(new ToolStatus(object, warning.getMessage()));
                warnNum++;
                warning = warning.getNextWarning();
            }
            if (warnNum == 0) {
                statusList.add(new ToolStatus(object, "Done"));
            }
        } catch (SQLException e) {
            // ignore
        }
        return statusList;
    }

    public class ToolStatus extends SQLToolStatistics<OBJECT_TYPE> {
        private final String message;

        ToolStatus(OBJECT_TYPE object, String message) {
            super(object);
            this.message = message;
        }

        @Property(viewable = true, order = 1)
        @Override
        public OBJECT_TYPE getObject() {
            return super.getObject();
        }

        @Property(viewable = true, order = 2)
        public String getMessage() {
            return message;
        }
    }
}
