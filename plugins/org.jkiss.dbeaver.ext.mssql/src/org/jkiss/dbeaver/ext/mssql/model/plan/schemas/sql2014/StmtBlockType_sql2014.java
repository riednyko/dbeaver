
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2014;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * The statement block that contains many statements
 * 
 * <p>Java class for StmtBlockType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StmtBlockType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="StmtSimple" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}StmtSimpleType"/>
 *           &lt;element name="StmtCond" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}StmtCondType"/>
 *           &lt;element name="StmtCursor" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}StmtCursorType"/>
 *           &lt;element name="StmtReceive" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}StmtReceiveType"/>
 *           &lt;element name="StmtUseDb" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}StmtUseDbType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StmtBlockType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "stmtSimpleOrStmtCondOrStmtCursor"
})
public class StmtBlockType_sql2014 {

    @XmlElements({
        @XmlElement(name = "StmtSimple", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", type = StmtSimpleType_sql2014 .class),
        @XmlElement(name = "StmtCond", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", type = StmtCondType_sql2014 .class),
        @XmlElement(name = "StmtCursor", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", type = StmtCursorType_sql2014 .class),
        @XmlElement(name = "StmtReceive", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", type = StmtReceiveType_sql2014 .class),
        @XmlElement(name = "StmtUseDb", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", type = StmtUseDbType_sql2014 .class)
    })
    protected List<BaseStmtInfoType_sql2014> stmtSimpleOrStmtCondOrStmtCursor;

    /**
     * Gets the value of the stmtSimpleOrStmtCondOrStmtCursor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stmtSimpleOrStmtCondOrStmtCursor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStmtSimpleOrStmtCondOrStmtCursor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StmtSimpleType_sql2014 }
     * {@link StmtCondType_sql2014 }
     * {@link StmtCursorType_sql2014 }
     * {@link StmtReceiveType_sql2014 }
     * {@link StmtUseDbType_sql2014 }
     * 
     * 
     */
    public List<BaseStmtInfoType_sql2014> getStmtSimpleOrStmtCondOrStmtCursor() {
        if (stmtSimpleOrStmtCondOrStmtCursor == null) {
            stmtSimpleOrStmtCondOrStmtCursor = new ArrayList<BaseStmtInfoType_sql2014>();
        }
        return this.stmtSimpleOrStmtCondOrStmtCursor;
    }

}
