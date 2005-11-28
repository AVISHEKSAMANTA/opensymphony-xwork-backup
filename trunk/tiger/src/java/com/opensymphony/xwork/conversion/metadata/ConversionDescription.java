/*
 * Copyright (c) 2002-2005 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.conversion.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.opensymphony.xwork.conversion.annotations.ConversionRule;
import com.opensymphony.xwork.conversion.annotations.ConversionType;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * <code>ConversionDescription</code>
 *
 * @author Rainer Hermanns
 * @version $Id$
 */
public class ConversionDescription {

    /**
     * Jakarta commons-logging reference.
     */
    protected static Log log = null;


    /**
     * Key used for type conversion of collections.
     */
    String COLLECTION_PREFIX = "Collection_";

    /**
     * Key used for type conversion of maps.
     */
    String MAP_PREFIX = "Map_";

    public String property;
    public String typeConverter;
    public String rule = "";
    public String fullQualifiedClassName;
    public String type = null;

    public ConversionDescription() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Creates an ConversionDescription with the specified property name.
     *
     * @param property
     */
    public ConversionDescription(String property) {
        this.property = property;
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * <p>
     * Sets the property name to be inserted into the related conversion.properties file.<br/>
     * Note: Do not add COLLECTION_PREFIX or MAP_PREFIX keys to property names.
     * </p>
     *
     * @param property The property to be converted.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Sets the class name of the type converter to be used.
     *
     * @param typeConverter The class name of the type converter.
     */
    public void setTypeConverter(String typeConverter) {
        this.typeConverter = typeConverter;
    }

    /**
     * Sets the rule prefix for COLLECTION_PREFIX or MAP_PREFIX key.
     * Defaults to en emtpy String.
     *
     * @param rule
     */
    public void setRule(String rule) {
        if (rule != null && rule.length() > 0) {
            if (rule.equals(ConversionRule.COLLECTION.toString())) {
                this.rule = COLLECTION_PREFIX;
            } else if (rule.equals(ConversionRule.MAP.toString())) {
                this.rule = MAP_PREFIX;
            }
        }
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Returns the conversion description as property entry.
     * <p>
     * Example:<br/>
     * property.name = converter.className<br/>
     * Collection_property.name = converter.className<br/>
     * Map_property.name = converter.className
     * </p>
     *
     * @return the conversion description as property entry.
     */
    public String asProperty() {
        StringWriter sw = new StringWriter();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(sw);
            writer.print(rule);
            writer.print(property);
            writer.print("=");
            writer.print(typeConverter);

        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }

        return sw.toString();

    }

    /**
     * Returns the fullQualifiedClassName attribute is used to create the special <code>conversion.properties</code> file name.
     *
     * @return fullQualifiedClassName
     */
    public String getFullQualifiedClassName() {
        return fullQualifiedClassName;
    }

    /**
     * The fullQualifiedClassName attribute is used to create the special <code>conversion.properties</code> file name.
     *
     * @param fullQualifiedClassName
     */
    public void setFullQualifiedClassName(String fullQualifiedClassName) {
        this.fullQualifiedClassName = fullQualifiedClassName;
    }
}
