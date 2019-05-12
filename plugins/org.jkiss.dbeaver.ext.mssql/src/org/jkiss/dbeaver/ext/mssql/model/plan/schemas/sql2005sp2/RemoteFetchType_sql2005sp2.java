
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2005sp2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RemoteFetchType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteFetchType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}RemoteType">
 *       &lt;sequence>
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
@XmlType(name = "RemoteFetchType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "relOp"
})
public class RemoteFetchType_sql2005sp2
    extends RemoteType_sql2005sp2
{

    @XmlElement(name = "RelOp", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected RelOpType_sql2005sp2 relOp;

    /**
     * Gets the value of the relOp property.
     * 
     * @return
     *     possible object is
     *     {@link RelOpType_sql2005sp2 }
     *     
     */
    public RelOpType_sql2005sp2 getRelOp() {
        return relOp;
    }

    /**
     * Sets the value of the relOp property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelOpType_sql2005sp2 }
     *     
     */
    public void setRelOp(RelOpType_sql2005sp2 value) {
        this.relOp = value;
    }

}
