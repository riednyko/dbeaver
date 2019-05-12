
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2012;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The cursor type that might have one or more cursor operations, used in DECLARE CURSOR, OPEN CURSOR and FETCH CURSOR
 * 
 * <p>Java class for StmtCursorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StmtCursorType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}BaseStmtInfoType">
 *       &lt;sequence>
 *         &lt;element name="CursorPlan" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}CursorPlanType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StmtCursorType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "cursorPlan"
})
public class StmtCursorType_sql2012
    extends BaseStmtInfoType_sql2012
{

    @XmlElement(name = "CursorPlan", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected CursorPlanType_sql2012 cursorPlan;

    /**
     * Gets the value of the cursorPlan property.
     * 
     * @return
     *     possible object is
     *     {@link CursorPlanType_sql2012 }
     *     
     */
    public CursorPlanType_sql2012 getCursorPlan() {
        return cursorPlan;
    }

    /**
     * Sets the value of the cursorPlan property.
     * 
     * @param value
     *     allowed object is
     *     {@link CursorPlanType_sql2012 }
     *     
     */
    public void setCursorPlan(CursorPlanType_sql2012 value) {
        this.cursorPlan = value;
    }

}
