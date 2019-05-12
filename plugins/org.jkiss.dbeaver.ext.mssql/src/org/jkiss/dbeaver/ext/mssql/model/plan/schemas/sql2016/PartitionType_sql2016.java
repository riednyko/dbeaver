
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2016;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PartitionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PartitionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Broadcast"/>
 *     &lt;enumeration value="Demand"/>
 *     &lt;enumeration value="Hash"/>
 *     &lt;enumeration value="NoPartitioning"/>
 *     &lt;enumeration value="Range"/>
 *     &lt;enumeration value="RoundRobin"/>
 *     &lt;enumeration value="CloneLocation"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PartitionType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
@XmlEnum
public enum PartitionType_sql2016 {

    @XmlEnumValue("Broadcast")
    BROADCAST("Broadcast"),
    @XmlEnumValue("Demand")
    DEMAND("Demand"),
    @XmlEnumValue("Hash")
    HASH("Hash"),
    @XmlEnumValue("NoPartitioning")
    NO_PARTITIONING("NoPartitioning"),
    @XmlEnumValue("Range")
    RANGE("Range"),
    @XmlEnumValue("RoundRobin")
    ROUND_ROBIN("RoundRobin"),
    @XmlEnumValue("CloneLocation")
    CLONE_LOCATION("CloneLocation");
    private final String value;

    PartitionType_sql2016(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PartitionType_sql2016 fromValue(String v) {
        for (PartitionType_sql2016 c: PartitionType_sql2016 .values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
