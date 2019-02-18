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
package org.jkiss.dbeaver.ext.mysql.model.plan;

import org.jkiss.dbeaver.model.exec.plan.DBCPlanCostNode;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.plan.AbstractExecutionPlanNode;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL execution plan node
 */
public class MySQLPlanNode extends AbstractExecutionPlanNode implements DBCPlanCostNode {

    protected Integer id;
    protected String selectType;
    protected String table;
    protected String type;
    protected String possibleKeys;
    protected String key;
    protected String keyLength;
    protected String ref;
    protected Long rowCount;
    protected Long filtered;
    protected String extra;

    protected MySQLPlanNode parent;
    protected List<MySQLPlanNode> nested;

    public MySQLPlanNode(List<MySQLPlanNode> nodes) {
        // Root node
        type = "<plan>";
        if (!nodes.isEmpty()) {
            this.rowCount = nodes.get(0).rowCount;
        }
        this.nested = nodes;
    }

    public MySQLPlanNode(MySQLPlanNode parent, ResultSet dbResult) {
        this.parent = parent;
        this.id = JDBCUtils.safeGetInteger(dbResult, "id");
        this.selectType = JDBCUtils.safeGetString(dbResult, "select_type");
        this.table = JDBCUtils.safeGetString(dbResult, "table");
        this.type = JDBCUtils.safeGetString(dbResult, "type");
        this.possibleKeys = JDBCUtils.safeGetString(dbResult, "possible_keys");
        this.key = JDBCUtils.safeGetString(dbResult, "key");
        this.keyLength = JDBCUtils.safeGetString(dbResult, "key_len");
        this.ref = JDBCUtils.safeGetString(dbResult, "ref");
        this.rowCount = JDBCUtils.safeGetLongNullable(dbResult, "rows");
        this.filtered = JDBCUtils.safeGetLongNullable(dbResult, "filtered");
        this.extra = JDBCUtils.safeGetString(dbResult, "extra");
    }

    public MySQLPlanNode(MySQLPlanNode parent, String type) {
        this.parent = parent;
        this.type = type;
    }

    protected MySQLPlanNode(MySQLPlanNode parent, MySQLPlanNode source) {
        this.id = source.id;
        this.selectType = source.selectType;
        this.table = source.table;
        this.type = source.type;
        this.possibleKeys = source.possibleKeys;
        this.key = source.key;
        this.keyLength = source.keyLength;
        this.ref = source.ref;
        this.rowCount = source.rowCount;
        this.filtered = source.filtered;
        this.extra = source.extra;

        this.parent = parent;
        if (source.nested != null) {
            this.nested = new ArrayList<>(source.nested.size());
            for (MySQLPlanNode srcNode : source.nested) {
                this.nested.add(srcNode.copyNode(this));
            }
        }
    }

    @Override
    public MySQLPlanNode getParent() {
        return parent;
    }

    void setParent(MySQLPlanNode node) {
        if (this.parent != null && this.parent.nested != null) {
            this.parent.nested.remove(this);
        }
        this.parent = node;
        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }

    private void addChild(MySQLPlanNode node) {
        if (this.nested == null) {
            this.nested = new ArrayList<>();
        }
        this.nested.add(node);

    }

    @Override
    public String getNodeName() {
        return table;
    }

    @Override
    public String getNodeDescription() {
        return ref;
    }

    @Override
    @Property(order = 3, viewable = true)
    public String getNodeType() {
        return type;
    }

    @Override
    public List<MySQLPlanNode> getNested() {
        return nested;
    }

    @Property(order = 0, viewable = true)
    public Integer getId() {
        return id;
    }

    @Property(order = 1, viewable = true)
    public String getSelectType() {
        return selectType;
    }

    @Property(order = 2, viewable = true)
    public String getTable() {
        return table;
    }

    @Property(order = 4, viewable = true)
    public String getPossibleKeys() {
        return possibleKeys;
    }

    @Property(order = 5, viewable = true)
    public String getKey() {
        return key;
    }

    @Property(order = 6, viewable = true)
    public String getKeyLength() {
        return keyLength;
    }

    @Property(order = 7, viewable = true)
    public String getRef() {
        return ref;
    }

    @Property(order = 8, viewable = true)
    public Long getRowCount() {
        return rowCount;
    }

    @Property(order = 9, viewable = true)
    public Long getFiltered() {
        return filtered;
    }

    @Property(order = 10, viewable = true)
    public String getExtra() {
        return extra;
    }

    @Override
    public Number getNodeCost() {
        return null;
    }

    @Override
    public Number getNodePercent() {
        return null;
    }

    @Override
    public Number getNodeDuration() {
        return null;
    }

    @Override
    public Number getNodeRowCount() {
        return rowCount;
    }

    public boolean isCompositeNode() {
        return "PRIMARY".equals(selectType);
    }

    @Override
    public String toString() {
        return id + " " + selectType + " " + table;
    }

    void computeStats() {
        if (rowCount == null) {
            if (nested != null) {
                long calcCount = 0;
                for (MySQLPlanNode child : nested) {
                    calcCount += CommonUtils.toLong(child.getRowCount());
                }
                this.rowCount = calcCount;
            }
        }

        if (nested != null) {
            for (MySQLPlanNode child : nested) {
                child.computeStats();
            }
        }
    }

    MySQLPlanNode copyNode(MySQLPlanNode parent) {
        return new MySQLPlanNode(parent, this);
    }
}
