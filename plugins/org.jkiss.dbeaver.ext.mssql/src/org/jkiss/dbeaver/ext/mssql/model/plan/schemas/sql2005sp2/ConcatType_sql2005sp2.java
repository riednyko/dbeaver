
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2005sp2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConcatType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConcatType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RelOpBaseType">
 *       &lt;sequence>
 *         &lt;element name="RelOp" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RelOpType" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConcatType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "relOp"
})
public class ConcatType_sql2005sp2
    extends RelOpBaseType_sql2005sp2
{

    @XmlElement(name = "RelOp", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected List<RelOpType_sql2005sp2> relOp;

    /**
     * Gets the value of the relOp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relOp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelOp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelOpType_sql2005sp2 }
     * 
     * 
     */
    public List<RelOpType_sql2005sp2> getRelOp() {
        if (relOp == null) {
            relOp = new ArrayList<RelOpType_sql2005sp2>();
        }
        return this.relOp;
    }

}
