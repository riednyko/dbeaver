
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2014sp2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScalarInsertType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScalarInsertType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RowsetType">
 *       &lt;sequence>
 *         &lt;element name="SetPredicate" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ScalarExpressionType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DMLRequestSort" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScalarInsertType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "setPredicate"
})
public class ScalarInsertType_sql2014sp2
    extends RowsetType_sql2014sp2
{

    @XmlElement(name = "SetPredicate", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
    protected ScalarExpressionType_sql2014sp2 setPredicate;
    @XmlAttribute(name = "DMLRequestSort")
    protected Boolean dmlRequestSort;

    /**
     * Gets the value of the setPredicate property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarExpressionType_sql2014sp2 }
     *     
     */
    public ScalarExpressionType_sql2014sp2 getSetPredicate() {
        return setPredicate;
    }

    /**
     * Sets the value of the setPredicate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarExpressionType_sql2014sp2 }
     *     
     */
    public void setSetPredicate(ScalarExpressionType_sql2014sp2 value) {
        this.setPredicate = value;
    }

    /**
     * Gets the value of the dmlRequestSort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getDMLRequestSort() {
        return dmlRequestSort;
    }

    /**
     * Sets the value of the dmlRequestSort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDMLRequestSort(Boolean value) {
        this.dmlRequestSort = value;
    }

}
