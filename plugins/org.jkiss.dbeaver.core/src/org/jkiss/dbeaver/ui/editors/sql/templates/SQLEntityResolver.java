package org.jkiss.dbeaver.ui.editors.sql.templates;

import org.eclipse.jface.text.templates.TemplateContext;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.DBSObjectSelector;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;
import java.util.List;

/**
 * Entity resolver
 */
public class SQLEntityResolver extends SQLObjectResolver<DBSEntity> {

    public SQLEntityResolver()
    {
        super("table", "Database table");
    }

    @Override
    protected void resolveObjects(DBRProgressMonitor monitor, DBPDataSource dataSource, TemplateContext context, List<DBSEntity> entities) throws DBException
    {
        resolveTables(monitor, dataSource, context, entities);
    }

    static void resolveTables(DBRProgressMonitor monitor, DBPDataSource dataSource, TemplateContext context, List<DBSEntity> entities) throws DBException
    {
        String catalogName = context.getVariable(SQLContainerResolver.VAR_NAME_CATALOG);
        String schemaName = context.getVariable(SQLContainerResolver.VAR_NAME_SCHEMA);
        DBSObjectContainer objectContainer = DBUtils.getAdapter(DBSObjectContainer.class, dataSource);
        if (objectContainer == null) {
            return;
        }
        if (!CommonUtils.isEmpty(catalogName) || !CommonUtils.isEmpty(schemaName)) {
            // Find container for specified schema/catalog
            objectContainer = (DBSObjectContainer)DBUtils.getObjectByPath(monitor, objectContainer, catalogName, schemaName, null);
        } else {
            DBSObjectSelector objectSelector = DBUtils.getAdapter(DBSObjectSelector.class, dataSource);
            if (objectSelector != null) {
                objectContainer = DBUtils.getAdapter(DBSObjectContainer.class, objectSelector.getSelectedObject());
            }
        }
        if (objectContainer != null) {
            makeProposalsFromChildren(monitor, objectContainer, entities);
        }
    }

    static void makeProposalsFromChildren(DBRProgressMonitor monitor, DBSObjectContainer container, List<DBSEntity> names) throws DBException
    {
        Collection<? extends DBSObject> children = container.getChildren(monitor);
        if (CommonUtils.isEmpty(children)) {
            return;
        }
        for (DBSObject child : children) {
            if (child instanceof DBSEntity) {
                names.add((DBSEntity) child);
            }
        }
    }
}
