//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.19 at 07:08:19 PM CET 
//


package sk.tuke.fei.kpi.nosal.milan.bte.metaconfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for targetElement-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="targetElement-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="element" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="generic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="printType" type="{http://kpi.fei.tuke.sk/Nosal/Milan/BTE/metaconfiguration}printType-type"/>
 *         &lt;element name="targetConfiguration" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "targetElement-type", propOrder = {
    "element",
    "generic",
    "name",
    "printType",
    "targetConfiguration"
})
public class TargetElementType {

    @XmlElement(defaultValue = "false")
    protected boolean element;
    @XmlElement(defaultValue = "false")
    protected boolean generic;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true, defaultValue = "CONTEXT")
    protected PrintTypeType printType;
    @XmlElement(required = true, defaultValue = "")
    protected String targetConfiguration;

    /**
     * Gets the value of the element property.
     * 
     */
    public boolean isElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     */
    public void setElement(boolean value) {
        this.element = value;
    }

    /**
     * Gets the value of the generic property.
     * 
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Sets the value of the generic property.
     * 
     */
    public void setGeneric(boolean value) {
        this.generic = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the printType property.
     * 
     * @return
     *     possible object is
     *     {@link PrintTypeType }
     *     
     */
    public PrintTypeType getPrintType() {
        return printType;
    }

    /**
     * Sets the value of the printType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrintTypeType }
     *     
     */
    public void setPrintType(PrintTypeType value) {
        this.printType = value;
    }

    /**
     * Gets the value of the targetConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetConfiguration() {
        return targetConfiguration;
    }

    /**
     * Sets the value of the targetConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetConfiguration(String value) {
        this.targetConfiguration = value;
    }

}
