
package org.jkiss.dbeaver.ext.mssql.model.plan.schemas.sql2005sp2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConditionalType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConditionalType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Condition" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ScalarExpressionType"/>
 *         &lt;element name="Then" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ScalarExpressionType"/>
 *         &lt;element name="Else" type="{http://schemas.microsoft.com/sqlserver/2004/07/showplan}ScalarExpressionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConditionalType", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", propOrder = {
    "condition",
    "then",
    "_else"
})
public class ConditionalType_sql2005sp2 {

    @XmlElement(name = "Condition", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected ScalarExpressionType_sql2005sp2 condition;
    @XmlElement(name = "Then", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected ScalarExpressionType_sql2005sp2 then;
    @XmlElement(name = "Else", namespace = "http://schemas.microsoft.com/sqlserver/2004/07/showplan", required = true)
    protected ScalarExpressionType_sql2005sp2 _else;

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public ScalarExpressionType_sql2005sp2 getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public void setCondition(ScalarExpressionType_sql2005sp2 value) {
        this.condition = value;
    }

    /**
     * Gets the value of the then property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public ScalarExpressionType_sql2005sp2 getThen() {
        return then;
    }

    /**
     * Sets the value of the then property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public void setThen(ScalarExpressionType_sql2005sp2 value) {
        this.then = value;
    }

    /**
     * Gets the value of the else property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public ScalarExpressionType_sql2005sp2 getElse() {
        return _else;
    }

    /**
     * Sets the value of the else property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarExpressionType_sql2005sp2 }
     *     
     */
    public void setElse(ScalarExpressionType_sql2005sp2 value) {
        this._else = value;
    }

}
