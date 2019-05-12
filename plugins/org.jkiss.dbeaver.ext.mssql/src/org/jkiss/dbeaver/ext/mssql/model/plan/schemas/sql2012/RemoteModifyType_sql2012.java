
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2012;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RemoteModifyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteModifyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RemoteType">
 *       &lt;sequence>
 *         &lt;element name="SetPredicate" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ScalarExpressionType" minOccurs="0"/>
 *         &lt;element name="RelOp" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RelOpType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteModifyType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "setPredicate",
    "relOp"
})
public class RemoteModifyType_sql2012
    extends RemoteType_sql2012
{

    @XmlElement(name = "SetPredicate", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected ScalarExpressionType_sql2012 setPredicate;
    @XmlElement(name = "RelOp", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected RelOpType_sql2012 relOp;

    /**
     * Gets the value of the setPredicate property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarExpressionType_sql2012 }
     *     
     */
    public ScalarExpressionType_sql2012 getSetPredicate() {
        return setPredicate;
    }

    /**
     * Sets the value of the setPredicate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarExpressionType_sql2012 }
     *     
     */
    public void setSetPredicate(ScalarExpressionType_sql2012 value) {
        this.setPredicate = value;
    }

    /**
     * Gets the value of the relOp property.
     * 
     * @return
     *     possible object is
     *     {@link RelOpType_sql2012 }
     *     
     */
    public RelOpType_sql2012 getRelOp() {
        return relOp;
    }

    /**
     * Sets the value of the relOp property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelOpType_sql2012 }
     *     
     */
    public void setRelOp(RelOpType_sql2012 value) {
        this.relOp = value;
    }

}
