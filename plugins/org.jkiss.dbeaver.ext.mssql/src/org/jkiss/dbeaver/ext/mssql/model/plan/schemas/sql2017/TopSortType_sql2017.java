
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2017;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TopSortType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TopSortType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}SortType">
 *       &lt;attribute name="Rows" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="WithTies" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TopSortType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan")
public class TopSortType_sql2017
    extends SortType_sql2017
{

    @XmlAttribute(name = "Rows", required = true)
    protected int rows;
    @XmlAttribute(name = "WithTies")
    protected Boolean withTies;

    /**
     * Gets the value of the rows property.
     * 
     */
    public int getRows() {
        return rows;
    }

    /**
     * Sets the value of the rows property.
     * 
     */
    public void setRows(int value) {
        this.rows = value;
    }

    /**
     * Gets the value of the withTies property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getWithTies() {
        return withTies;
    }

    /**
     * Sets the value of the withTies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWithTies(Boolean value) {
        this.withTies = value;
    }

}
