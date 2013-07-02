package org.jkiss.dbeaver.core.eclipse.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.DefaultProgressMonitor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.search.IObjectSearchListener;
import org.jkiss.dbeaver.ui.search.IObjectSearchQuery;

import java.util.Collection;

/**
 * Search query adapter
 */
public class SearchQueryAdapter implements ISearchQuery, IObjectSearchListener {

    private final IObjectSearchQuery source;
    private SearchResultAdapter result;

    public SearchQueryAdapter(IObjectSearchQuery source)
    {
        this.source = source;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException
    {
        result = new SearchResultAdapter(this);
        try {
            source.runQuery(new DefaultProgressMonitor(monitor), this);
        } catch (DBException e) {
            return RuntimeUtils.makeExceptionStatus(e);
        }
        return Status.OK_STATUS;
    }

    @Override
    public String getLabel()
    {
        return source.getLabel();
    }

    @Override
    public boolean canRerun()
    {
        return true;
    }

    @Override
    public boolean canRunInBackground()
    {
        return true;
    }

    @Override
    public ISearchResult getSearchResult()
    {
        return result;
    }

    @Override
    public void searchStarted()
    {

    }

    @Override
    public boolean objectsFound(DBRProgressMonitor monitor, Collection<?> objects)
    {
        result.addObjects(objects);
        return true;
    }

    @Override
    public void searchFinished()
    {

    }
}
