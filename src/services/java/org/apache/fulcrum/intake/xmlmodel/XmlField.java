package org.apache.fulcrum.intake.xmlmodel;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.util.StringUtils;
import org.xml.sax.Attributes;

/**
 * A Class for holding data about a property used in an Application.
 *
 * @author <a href="mailto:jmcnally@collab.net>John McNally</a>
 * @version $Id$
 */
public class XmlField
    implements java.io.Serializable
{
    private static final String DEFAULT_VALIDATOR =
        "org.apache.fulcrum.intake.validatore.DefaultValidator";

    private String baseClass;
    private String name;
    private String key;
    private String type;
    private String displayName;
    private String onError;
    private String multiValued;
    private XmlGroup parent;
    private List rules;
    private Map ruleMap;
    private String ifRequiredMessage;
    private String mapToObject;
    private String mapToProperty;
    private String validator;

    private static HashMap defaultOnErrors;
    private static HashMap convertHash;
    private static HashMap convertArrayHash;

    // static
    {
        populateDefaults();
    }

    private static void populateDefaults()
    {
        defaultOnErrors = new HashMap(15);
        convertHash = new HashMap(15);
        convertArrayHash = new HashMap(15);

        defaultOnErrors.put("boolean", "false");
        defaultOnErrors.put("byte", "-1");
        defaultOnErrors.put("short", "-1");
        defaultOnErrors.put("int", "-1");
        defaultOnErrors.put("long", "-1");
        defaultOnErrors.put("float", "-1.0f");
        defaultOnErrors.put("double", "-1.0");
        defaultOnErrors.put("BigDecimal", "new BigDecimal(\"-1.0\")");
        // defaultOnErrors.put("BigInteger", "new BigInteger(\"-1\")");

        convertHash.put("boolean", "getBoolean");
        convertHash.put("byte", "getByte");
        convertHash.put("short", "getShort");
        convertHash.put("int", "getInt");
        convertHash.put("long", "getLong");
        convertHash.put("float", "getFloat");
        convertHash.put("double", "getDouble");
        convertHash.put("Date", "getDate");
        convertHash.put("BigDecimal", "getBigDecimal");
        // convertHash.put("BigInteger", "getBigInteger");

        convertHash.put("boolean[]",
                        "Boolean.valueOf(stringValue[i]).booleanValue()");
        convertArrayHash.put("byte[]",
                             "Byte.valueOf(stringValue[i]).byteValue()");
        convertArrayHash.put("short[]",
                             "Short.valueOf(stringValue[i]).shortValue()");
        convertArrayHash.put("int[]", "Integer.parseInt(stringValue[i])");
        convertArrayHash.put("long[]", "Long.parseLong(stringValue[i])");
        convertArrayHash.put("float[]",
                             "Float.valueOf(stringValue[i]).floatValue()");
        convertArrayHash.put("double[]",
                             "Double.valueOf(stringValue[i]).doubleValue()");
        convertArrayHash.put("Date[]", "FIXME!!");
        convertArrayHash.put("BigDecimal[]", "new BigDecimal(stringValue[i])");
        // convertHash.put("BigInteger", "new BigInteger(stringValue)");
    }

    /**
     * Default Constructor
     */
    public XmlField()
    {
        rules = new ArrayList();
        ruleMap = new HashMap();
    }


    /**
     * Creates a new column and set the name
     */
    public XmlField(String name)
    {
        this.name = name;
        rules = new ArrayList();
        ruleMap = new HashMap();
    }

    /**
     * Imports a column from an XML specification
     */
    public void loadFromXML (Attributes attrib)
    {
        setBaseClass(attrib.getValue("baseClass"));
        setName(attrib.getValue("name"));
        key = attrib.getValue("key");
        type = attrib.getValue("type");
        displayName = attrib.getValue("displayName");
        //setOnError(attrib.getValue("onError"));
        setMultiValued(attrib.getValue("multiValued"));

        String mapObj = attrib.getValue("mapToObject");
        if ( mapObj != null && mapObj.length() != 0 )
        {
            setMapToObject(mapObj);
        }

        String mapProp = attrib.getValue("mapToProperty");
        if ( mapProp != null )
        {
            setMapToProperty(mapProp);
        }
        setValidator(attrib.getValue("validator"));
    }


    /**
     * Get the name of the property
     */
    public String getRawName()
    {
        return name;
    }

    /**
     * Get the name of the property
     */
    public String getName()
    {
        return StringUtils.replace(name, "_", "");
    }

    /**
     * Set the name of the property
     */
    public void setName(String newName)
    {
        name = newName;
    }

    /**
     * Get the display name of the property
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Set the display name of the property
     */
    public void setDisplayName(String newDisplayName)
    {
        displayName = newDisplayName;
    }

    /**
     * Set the parameter key of the property
     */
    public void setKey(String newKey)
    {
        key = newKey;
    }
    /**
     * Get the parameter key of the property
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Set the type of the property
     */
    public void setType(String newType)
    {
        type = newType;
    }

    /**
     * Get the type of the property
     */
    public String getType()
    {
        /*
        if ( isMultiValued() )
        {
            return type + "[]";
        }
        */
        return type;
    }

    /**
     * Set the base class of the field
     */
    public void setBaseClass(String newBaseClass)
    {
        baseClass = newBaseClass;
    }

    /**
     * Get the base class of the field
     */
    public String getBaseClass()
    {
        return baseClass;
    }

    /* *
     * Set the value of the property, if a conversion error occurs.
     * /
    public void setOnError(String newOnError)
    {
        onError = newOnError;
    }

    /* *
     * Get the value of the property, if a conversion error occurs.
     * /
    public String getOnError()
    {
        if ( onError == null && defaultOnErrors.containsKey(getType()) )
        {
            onError = (String)defaultOnErrors.get(getType());
        }
        return onError;
    }
    */

    /**
     * Set whether this class can have multiple values
     */
    public void setMultiValued(String newMultiValued)
    {
        multiValued = newMultiValued;
    }

    /**
     * can this field have several values?
     */
    public boolean isMultiValued()
    {
        if ( multiValued != null && multiValued.equals("true") )
        {
            return true;
        }
        return false;
    }

    /**
     * Set the name of the object that takes this input
     */
    public void setMapToObject(String obj)
    {
        mapToObject = obj;
    }

    /**
     * Get the name of the object that takes this input
     */
    public String getMapToObject()
    {
        return mapToObject;
    }

    /**
     * Set the property method that takes this input
     */
    public void setMapToProperty(String prop)
    {
        mapToProperty = prop;
    }

    /**
     * Get the property method that takes this input
     */
    public String getMapToProperty()
    {
        if ( mapToProperty == null )
        {
            return getName();
        }
        else
        {
            return mapToProperty;
        }
    }

    /**
     * Set the class name of the validator
     */
    public void setValidator(String prop)
    {
        validator = prop;
    }

    /**
     * Get the className of the validator
     */
    public String getValidator()
    {
        return validator;
    }

    /**
     * The name of the field making sure the first letter is lowercase.
     *
     * @return a <code>String</code> value
     */
    public String getVariable()
    {
        String firstChar = getName().substring(0,1).toLowerCase();
        return firstChar + getName().substring(1);
    }

    public String getPPMethod()
    {
        String result = null;
        if ( convertHash.containsKey(getType()))
        {
            result = (String)convertHash.get(getType());
        }
        return result;
    }

    public String getArrayConvert()
    {
        String result = null;
        if ( convertArrayHash.containsKey(getType()))
        {
            result = (String)convertArrayHash.get(getType());
        }
        return result;
    }

    /**
     * Set the parent XmlGroup of the property
     */
    public void setGroup(XmlGroup parent)
    {
        this.parent = parent;
        if ( mapToObject != null && mapToObject.length() != 0 )
        {
            mapToObject = parent.getAppData().getBasePackage() + mapToObject;
        }
    }

    /**
     * Get the parent XmlGroup of the property
     */
    public XmlGroup getGroup()
    {
        return parent;
    }

    /**
     * Get the value of ifRequiredMessage.
     * @return value of ifRequiredMessage.
     */
    public String getIfRequiredMessage()
    {
        return ifRequiredMessage;
    }

    /**
     * Set the value of ifRequiredMessage.
     * @param v  Value to assign to ifRequiredMessage.
     */
    public void setIfRequiredMessage(String  v)
    {
        this.ifRequiredMessage = v;
    }

    /**
     * A utility function to create a new input parameter
     * from attrib and add it to this property.
     */
    public Rule addRule(Attributes attrib)
    {
        Rule rule = new Rule();
        rule.loadFromXML(attrib);
        addRule(rule);

        return rule;
    }

    /**
     * Adds a new rule to the parameter Map and set the
     * parent property of the Rule to this property
     */
    public void addRule(Rule rule)
    {
        rule.setField(this);
        rules.add(rule);
        ruleMap.put(rule.getName(), rule);
    }

    /**
     * The collection of rules for this field.
     *
     * @return a <code>List</code> value
     */
    public List getRules()
    {
        return rules;
    }

    /**
     * The collection of rules for this field keyed by
     * parameter name.
     *
     * @return a <code>Map</code> value
     */
    public Map getRuleMap()
    {
        return ruleMap;
    }

    /**
     * String representation of the column. This
     * is an xml representation.
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(" <field name=\""+name+"\"");
        result.append(" key=\""+key+"\"");
        result.append(" type=\""+type+"\"");

        if (displayName != null)
        {
            result.append(" displayName=\""+displayName+"\"");
        }
        if (onError != null)
        {
            result.append(" onError=\""+onError+"\"");
        }
        if (mapToObject != null)
        {
            result.append(" mapToObject=\""+mapToObject+"\"");
        }
        if (mapToProperty != null)
        {
            result.append(" mapToProperty=\""+mapToProperty+"\"");
        }
        if (validator != null)
        {
            result.append(" validator=\""+validator+"\"");
        }


        if ( rules.size() == 0 )
        {
            result.append(" />\n");
        }
        else
        {
            result.append(">\n");
            for (Iterator i = rules.iterator() ; i.hasNext() ;)
            {
                result.append(i.next());
            }
            result.append("</field>\n");
        }

        return result.toString();
    }

    // this methods are called during serialization
    private void writeObject(java.io.ObjectOutputStream stream)
        throws java.io.IOException
    {
        stream.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        populateDefaults();
    }


}



