/*
 * Copyright (C) 2010-2015 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.controls.resultset;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCStatistics;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSDataManipulator;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.utils.ArrayUtils;
import org.jkiss.utils.CommonUtils;

import java.util.*;

/**
 * Result set model
 */
public class ResultSetModel {

    static final Log log = Log.getLog(ResultSetModel.class);

    // Columns
    private DBDAttributeBinding[] columns = new DBDAttributeBinding[0];
    private List<DBDAttributeBinding> visibleColumns = new ArrayList<DBDAttributeBinding>();
    private DBDDataFilter dataFilter;
    private boolean singleSourceCells;
    //private boolean refreshDynamicMeta;


    // Data
    private List<ResultSetRow> curRows = new ArrayList<ResultSetRow>();
    private int changesCount = 0;
    private volatile boolean hasData = false;
    // Flag saying that edited values update is in progress
    private volatile boolean updateInProgress = false;

    // Edited rows and cells
    private DBCStatistics statistics;
    private transient boolean metadataChanged;
    private transient boolean sourceChanged;
    private transient boolean metadataDynamic;

    public ResultSetModel()
    {
        dataFilter = createDataFilter();
    }

    @NotNull
    public DBDDataFilter createDataFilter()
    {
        List<DBDAttributeConstraint> constraints = new ArrayList<DBDAttributeConstraint>(columns.length);
        for (DBDAttributeBinding binding : columns) {
            addConstraints(constraints, binding);
        }
        return new DBDDataFilter(constraints);
    }

    private void addConstraints(List<DBDAttributeConstraint> constraints, DBDAttributeBinding binding) {
        DBDAttributeConstraint constraint = new DBDAttributeConstraint(binding);
        constraint.setVisible(visibleColumns.contains(binding));
        constraints.add(constraint);
        List<DBDAttributeBinding> nestedBindings = binding.getNestedBindings();
        if (nestedBindings != null) {
            for (DBDAttributeBinding nested : nestedBindings) {
                addConstraints(constraints, nested);
            }
        }
    }

    public boolean isMetadataChanged() {
        return metadataChanged;
    }

    public boolean isSingleSource()
    {
        return singleSourceCells;
    }

    /**
     * Returns single source of this result set. Usually it is a table.
     * If result set is a result of joins or contains synthetic columns then
     * single source is null. If driver doesn't support meta information
     * for queries then is will null.
     * @return single source entity
     */
    @Nullable
    public DBSEntity getSingleSource()
    {
        if (!singleSourceCells) {
            return null;
        }
        for (DBDAttributeBinding attr : columns) {
            DBDRowIdentifier rowIdentifier = attr.getRowIdentifier();
            if (rowIdentifier != null) {
                return rowIdentifier.getEntity();
            }
        }
        return null;
    }

    public void resetCellValue(DBDAttributeBinding attr, ResultSetRow row)
    {
        if (row.changes != null && row.changes.containsKey(attr)) {
            DBUtils.resetValue(getCellValue(attr, row));
            updateCellValue(attr, row, row.changes.get(attr), false);
            row.resetChange(attr);
            if (row.getState() == ResultSetRow.STATE_NORMAL) {
                changesCount--;
            }
        }
    }

    public void refreshChangeCount()
    {
        changesCount = 0;
        for (ResultSetRow row : curRows) {
            if (row.getState() != ResultSetRow.STATE_NORMAL) {
                changesCount++;
            } else if (row.changes != null) {
                changesCount += row.changes.size();
            }
        }
    }

    @NotNull
    public DBDAttributeBinding[] getColumns()
    {
        return columns;
    }

    public int getColumnCount()
    {
        return columns.length;
    }

    @NotNull
    public DBDAttributeBinding getColumn(int index)
    {
        return columns[index];
    }

    public int getVisibleColumnIndex(@NotNull DBDAttributeBinding column)
    {
        return visibleColumns.indexOf(column);
    }

    @NotNull
    public List<DBDAttributeBinding> getVisibleColumns()
    {
        return visibleColumns;
    }

    public int getVisibleColumnCount()
    {
        return visibleColumns.size();
    }

    @NotNull
    public DBDAttributeBinding getVisibleColumn(int index)
    {
        return visibleColumns.get(index);
    }

    public void setColumnVisibility(@NotNull DBDAttributeBinding attribute, boolean visible)
    {
        DBDAttributeConstraint constraint = dataFilter.getConstraint(attribute);
        if (constraint != null && constraint.isVisible() != visible) {
            constraint.setVisible(visible);
            if (visible) {
                visibleColumns.add(attribute);
            } else {
                visibleColumns.remove(attribute);
            }
        }
    }

    @Nullable
    public DBDAttributeBinding getAttributeBinding(@NotNull DBSAttributeBase attribute)
    {
        return DBUtils.findBinding(columns, attribute);
    }

    @Nullable
    DBDAttributeBinding getAttributeBinding(@Nullable DBSEntity entity, @NotNull String columnName)
    {
        for (DBDAttributeBinding column : visibleColumns) {
            DBDRowIdentifier rowIdentifier = column.getRowIdentifier();
            if ((entity == null || (rowIdentifier != null && rowIdentifier.getEntity() == entity)) &&
                column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    public boolean isEmpty()
    {
        return getRowCount() <= 0 || visibleColumns.size() <= 0;
    }

    public int getRowCount()
    {
        return curRows.size();
    }

    @NotNull
    public List<ResultSetRow> getAllRows() {
        return curRows;
    }

    @NotNull
    public Object[] getRowData(int index)
    {
        return curRows.get(index).values;
    }

    @NotNull
    public ResultSetRow getRow(int index)
    {
        return curRows.get(index);
    }

    @Nullable
    public Object getCellValue(@NotNull DBDAttributeBinding column, @NotNull ResultSetRow row) {
        int depth = column.getLevel();
        if (depth == 0) {
            return row.values[column.getOrdinalPosition()];
        }
        Object curValue = row.values[column.getTopParent().getOrdinalPosition()];

        for (int i = 0; i < depth; i++) {
            if (curValue == null) {
                break;
            }
            DBDAttributeBinding attr = column.getParent(depth - i - 1);
            assert attr != null;
            try {
                curValue = attr.extractNestedValue(curValue);
            } catch (DBCException e) {
                log.debug("Error reading nested value of [" + attr.getName() + "]", e);
                curValue = null;
                break;
            }
        }

        return curValue;
    }

    /**
     * Updates cell value. Saves previous value.
     *
     * @param attr Attribute
     * @param row row index
     * @param value new value
     * @return true on success
     */
    public boolean updateCellValue(@NotNull DBDAttributeBinding attr, @NotNull ResultSetRow row, @Nullable Object value)
    {
        return updateCellValue(attr, row, value, true);
    }

    public boolean updateCellValue(@NotNull DBDAttributeBinding attr, @NotNull ResultSetRow row, @Nullable Object value, boolean updateChanges)
    {
        int depth = attr.getLevel();
        int rootIndex;
        if (depth == 0) {
            rootIndex = attr.getOrdinalPosition();
        } else {
            rootIndex = attr.getTopParent().getOrdinalPosition();
        }
        Object rootValue = row.values[rootIndex];
        Object ownerValue = depth > 0 ? rootValue : null;
        {
            // Obtain owner value and create all intermediate values
            for (int i = 0; i < depth; i++) {
                if (ownerValue == null) {
                    // Create new owner object
                    log.warn("Null owner value");
                    return false;
                }
                if (i == depth - 1) {
                    break;
                }
                DBDAttributeBinding ownerAttr = attr.getParent(depth - i - 1);
                assert ownerAttr != null;
                try {
                    ownerValue = ownerAttr.extractNestedValue(ownerValue);
                } catch (DBCException e) {
                    log.warn("Error getting field [" + ownerAttr.getName() + "] value", e);
                    return false;
                }
            }
        }
        // Get old value
        Object oldValue = rootValue;
        if (ownerValue != null) {
            try {
                oldValue = attr.extractNestedValue(ownerValue);
            } catch (DBCException e) {
                log.error("Error getting [" + attr.getName() + "] value", e);
            }
        }
        if ((value instanceof DBDValue && value == oldValue) || !CommonUtils.equalObjects(oldValue, value)) {
            // If DBDValue was updated (kind of LOB?) or actual value was changed
            if (ownerValue == null && DBUtils.isNullValue(oldValue) && DBUtils.isNullValue(value)) {
                // Both nulls - nothing to update
                return false;
            }
            // Do not add edited cell for new/deleted rows
            if (row.getState() == ResultSetRow.STATE_NORMAL) {

                boolean cellWasEdited = row.changes != null && row.changes.containsKey(attr);
                Object oldOldValue = !cellWasEdited ? null : row.changes.get(attr);
                if (cellWasEdited && !CommonUtils.equalObjects(oldValue, oldOldValue)) {
                    // Value rewrite - release previous stored old value
                    DBUtils.releaseValue(oldValue);
                } else if (updateChanges) {
                    row.addChange(attr, oldValue);
                }
                if (updateChanges && row.getState() == ResultSetRow.STATE_NORMAL && !cellWasEdited) {
                    changesCount++;
                }
            }
            if (ownerValue != null) {
                if (ownerValue instanceof DBDStructure) {
                    try {
                        ((DBDStructure) ownerValue).setAttributeValue(attr.getAttribute(), value);
                    } catch (DBCException e) {
                        log.error("Error setting [" + attr.getName() + "] value", e);
                    }
                } else {
                    log.warn("Value [" + ownerValue + "] edit is not supported");
                }
            } else {
                row.values[rootIndex] = value;
            }
            return true;
        }
        return false;
    }

    boolean isDynamicMetadata() {
        return metadataDynamic;
    }

    /**
     * Sets new metadata of result set
     * @param newColumns columns metadata
     * @return true if new metadata differs from old one, false otherwise
     */
    public void setMetaData(@NotNull DBDAttributeBinding[] newColumns)
    {
        boolean update = false;
        if (this.columns == null || this.columns.length == 0 || this.columns.length != newColumns.length || isDynamicMetadata()) {
            update = true;
        } else {
/*
            if (dataFilter != null && dataFilter.hasFilters()) {
                // This is a filtered result set so keep old metadata.
                // Filtering modifies original query (adds sub-query)
                // and it may change metadata (depends on driver)
                // but actually it doesn't change any column or table names/types
                // so let's keep old info
                return false;
            }
*/

            for (int i = 0; i < this.columns.length; i++) {
                if (!ResultSetUtils.equalAttributes(this.columns[i].getMetaAttribute(), newColumns[i].getMetaAttribute())) {
                    update = true;
                    break;
                }
            }
        }

        if (update) {
            if (!ArrayUtils.isEmpty(this.columns) && !ArrayUtils.isEmpty(newColumns) && isDynamicMetadata() &&
                this.columns[0].getTopParent().getMetaAttribute().getSource() == newColumns[0].getTopParent().getMetaAttribute().getSource())
            {
                // the same source
                sourceChanged = false;
            } else {
                sourceChanged = true;
            }
            this.clearData();
            this.columns = newColumns;
            fillVisibleColumns();
        }
        metadataChanged = update;
        metadataDynamic = this.columns != null &&
            this.columns.length > 0 &&
            this.columns[0].getTopParent().getDataSource().getInfo().isDynamicMetadata();
    }

    public void setData(@NotNull List<Object[]> rows)
    {
        // Clear previous data
        this.clearData();

        if (metadataChanged) {
            if (columns.length == 1 && columns[0].getDataKind() == DBPDataKind.STRUCT) {
                List<DBDAttributeBinding> nested = columns[0].getNestedBindings();
                if (!CommonUtils.isEmpty(nested)) {
                    columns = nested.toArray(new DBDAttributeBinding[nested.size()]);
                    fillVisibleColumns();
                }
            }
        }

        // Add new data
        appendData(rows);

        if (sourceChanged) {
            this.dataFilter = createDataFilter();
        } else {
            DBDDataFilter prevFilter = dataFilter;
            this.dataFilter = createDataFilter();
            updateDataFilter(prevFilter);
        }

        if (metadataChanged) {
            // Check single source flag
            this.singleSourceCells = true;
            DBSEntity sourceTable = null;
            for (DBDAttributeBinding column : visibleColumns) {
//                if (isColumnReadOnly(column)) {
//                    singleSourceCells = false;
//                    break;
//                }
                DBDRowIdentifier rowIdentifier = column.getRowIdentifier();
                if (rowIdentifier != null) {
                    if (sourceTable == null) {
                        sourceTable = rowIdentifier.getEntity();
                    } else if (sourceTable != rowIdentifier.getEntity()) {
                        singleSourceCells = false;
                        break;
                    }
                } else {
                    singleSourceCells = false;
                    break;
                }
            }
        }

        hasData = true;
        metadataChanged = false;
        sourceChanged = false;
    }

    public void appendData(@NotNull List<Object[]> rows)
    {
        int rowCount = rows.size();
        List<ResultSetRow> newRows = new ArrayList<ResultSetRow>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            newRows.add(
                new ResultSetRow(curRows.size() + i, rows.get(i)));
        }
        curRows.addAll(newRows);
    }

    void clearData()
    {
        // Refresh all rows
        this.releaseAll();
        this.curRows = new ArrayList<ResultSetRow>();

        hasData = false;
    }

    public boolean hasData()
    {
        return hasData;
    }

    public boolean isDirty()
    {
        return changesCount != 0;
    }

    private void fillVisibleColumns() {
        this.visibleColumns.clear();
        for (DBDAttributeBinding binding : this.columns) {
            DBDPseudoAttribute pseudoAttribute = binding.getMetaAttribute().getPseudoAttribute();
            if (pseudoAttribute == null) {
                // Make visible "real" attributes
                this.visibleColumns.add(binding);
            }
        }
    }

    boolean isColumnReadOnly(@NotNull DBDAttributeBinding column)
    {
        if (column.getMetaAttribute().isReadOnly()) {
            return true;
        }
        DBDRowIdentifier rowIdentifier = column.getRowIdentifier();
        if (rowIdentifier == null || !(rowIdentifier.getEntity() instanceof DBSDataManipulator) ||
            (column.getValueHandler().getFeatures() & DBDValueHandler.FEATURE_COMPOSITE) != 0) {
            return true;
        }
        DBSDataManipulator dataContainer = (DBSDataManipulator) rowIdentifier.getEntity();
        return (dataContainer.getSupportedFeatures() & DBSDataManipulator.DATA_UPDATE) == 0;
    }

    public boolean isUpdateInProgress()
    {
        return updateInProgress;
    }

    void setUpdateInProgress(boolean updateInProgress)
    {
        this.updateInProgress = updateInProgress;
    }

    void addNewRow(int rowNum, @NotNull Object[] data)
    {
        ResultSetRow newRow = new ResultSetRow(curRows.size(), data);
        newRow.setVisualNumber(rowNum);
        newRow.setState(ResultSetRow.STATE_ADDED);
        shiftRows(newRow, 1);
        curRows.add(rowNum, newRow);
        changesCount++;
    }

    /**
     * Removes row with specified index from data
     * @param row row
     * @return true if row was physically removed (only in case if this row was previously added)
     * or false if it just marked as deleted
     */
    boolean deleteRow(@NotNull ResultSetRow row)
    {
        if (row.getState() == ResultSetRow.STATE_ADDED) {
            cleanupRow(row);
            return true;
        } else {
            // Mark row as deleted
            row.setState(ResultSetRow.STATE_REMOVED);
            changesCount++;
            return false;
        }
    }

    void cleanupRow(@NotNull ResultSetRow row)
    {
        row.release();
        this.curRows.remove(row.getVisualNumber());
        this.shiftRows(row, -1);
    }

    boolean cleanupRows(Collection<ResultSetRow> rows)
    {
        if (rows != null && !rows.isEmpty()) {
            // Remove rows (in descending order to prevent concurrent modification errors)
            List<ResultSetRow> rowsToRemove = new ArrayList<ResultSetRow>(rows);
            Collections.sort(rowsToRemove, new Comparator<ResultSetRow>() {
                @Override
                public int compare(ResultSetRow o1, ResultSetRow o2) {
                    return o1.getVisualNumber() - o2.getVisualNumber();
                }
            });
            for (ResultSetRow row : rowsToRemove) {
                cleanupRow(row);
            }
            return true;
        } else {
            return false;
        }
    }

    private void shiftRows(@NotNull ResultSetRow relative, int delta)
    {
        for (ResultSetRow row : curRows) {
            if (row.getVisualNumber() >= relative.getVisualNumber()) {
                row.setVisualNumber(row.getVisualNumber() + delta);
            }
            if (row.getRowNumber() >= relative.getRowNumber()) {
                row.setRowNumber(row.getRowNumber() + delta);
            }
        }
    }

    private void releaseAll()
    {
        for (ResultSetRow row : curRows) {
            row.release();
        }
    }

    public DBDDataFilter getDataFilter()
    {
        return dataFilter;
    }

    /**
     * Sets new data filter
     * @param dataFilter data filter
     * @return true if visible columns were changed. Spreadsheet has to be refreshed
     */
    boolean setDataFilter(DBDDataFilter dataFilter)
    {
        this.dataFilter = dataFilter;
        // Check if filter misses some columns
        List<DBDAttributeConstraint> newConstraints = new ArrayList<DBDAttributeConstraint>();
        for (DBDAttributeBinding binding : columns) {
            if (dataFilter.getConstraint(binding) == null) {
                addConstraints(newConstraints, binding);
            }
        }
        if (!newConstraints.isEmpty()) {
            dataFilter.addConstraints(newConstraints);
        }

        List<DBDAttributeBinding> newBindings = new ArrayList<DBDAttributeBinding>();

        for (DBSAttributeBase attr : this.dataFilter.getOrderedVisibleAttributes()) {
            DBDAttributeBinding binding = getAttributeBinding(attr);
            if (binding != null) {
                newBindings.add(binding);
            }
        }
        if (!newBindings.equals(visibleColumns)) {
            visibleColumns = newBindings;
            return true;
        }
        return false;
    }

    void updateDataFilter(DBDDataFilter filter)
    {
        for (DBDAttributeConstraint constraint : filter.getConstraints()) {
            DBDAttributeConstraint filterConstraint = this.dataFilter.getConstraint(constraint.getAttribute(), metadataChanged);
            if (filterConstraint == null) {
                //log.warn("Constraint for attribute [" + constraint.getAttribute().getName() + "] not found");
                continue;
            }
            if (constraint.getOperator() != null) {
                filterConstraint.setOperator(constraint.getOperator());
                filterConstraint.setReverseOperator(constraint.isReverseOperator());
                filterConstraint.setValue(constraint.getValue());
            } else {
                filterConstraint.setCriteria(constraint.getCriteria());
            }
            if (constraint.getOrderPosition() > 0) {
                filterConstraint.setOrderPosition(constraint.getOrderPosition());
                filterConstraint.setOrderDescending(constraint.isOrderDescending());
            }
        }
        this.dataFilter.setWhere(filter.getWhere());
        this.dataFilter.setOrder(filter.getOrder());
        this.dataFilter.setAnyConstraint(filter.isAnyConstraint());
    }

    void resetOrdering()
    {
        final boolean hasOrdering = dataFilter.hasOrdering();
        // Sort locally
        final List<DBDAttributeConstraint> orderConstraints = dataFilter.getOrderConstraints();
        Collections.sort(curRows, new Comparator<ResultSetRow>() {
            @Override
            public int compare(ResultSetRow row1, ResultSetRow row2)
            {
                if (!hasOrdering) {
                    return row1.getRowNumber() - row2.getRowNumber();
                }
                int result = 0;
                for (DBDAttributeConstraint co : orderConstraints) {
                    final DBDAttributeBinding binding = getAttributeBinding(co.getAttribute());
                    if (binding == null) {
                        continue;
                    }
                    Object cell1 = getCellValue(binding, row1);
                    Object cell2 = getCellValue(binding, row2);
                    if (cell1 == cell2) {
                        result = 0;
                    } else if (DBUtils.isNullValue(cell1)) {
                        result = 1;
                    } else if (DBUtils.isNullValue(cell2)) {
                        result = -1;
                    } else if (cell1 instanceof Comparable) {
                        result = ((Comparable)cell1).compareTo(cell2);
                    } else {
                        String str1 = String.valueOf(cell1);
                        String str2 = String.valueOf(cell2);
                        result = str1.compareTo(str2);
                    }
                    if (co.isOrderDescending()) {
                        result = -result;
                    }
                    if (result != 0) {
                        break;
                    }
                }
                return result;
            }
        });
        for (int i = 0; i < curRows.size(); i++) {
            curRows.get(i).setVisualNumber(i);
        }
    }

    public DBCStatistics getStatistics()
    {
        return statistics;
    }

    public void setStatistics(DBCStatistics statistics)
    {
        this.statistics = statistics;
    }
}
