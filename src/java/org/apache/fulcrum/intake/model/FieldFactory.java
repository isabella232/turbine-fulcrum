package org.apache.fulcrum.intake.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.fulcrum.intake.IntakeException;
import org.apache.fulcrum.intake.xmlmodel.XmlField;

/**
 * Creates Field objects.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:Colin.Chalmers@maxware.nl">Colin Chalmers</a>
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id$
 */
public abstract class FieldFactory
{
    private static Map fieldCtors = initFieldCtors();

    private static Map initFieldCtors()
    {
        fieldCtors = new HashMap();

        fieldCtors.put("int", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new IntegerField(f, g);
            }
        }
        );
        fieldCtors.put("boolean", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new BooleanField(f, g);
            }
        }
        );
        fieldCtors.put("String", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new StringField(f, g);
            }
        }
        );
        fieldCtors.put("BigDecimal", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new BigDecimalField(f, g);
            }
        }
        );
        fieldCtors.put("FileItem", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new FileItemField(f, g);
            }
        }
        );
        fieldCtors.put("DateString", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new DateStringField(f, g);
            }
        }
        );
        fieldCtors.put("float", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new FloatField(f, g);
            }
        }
        );
        fieldCtors.put("double", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new DoubleField(f, g);
            }
        }
        );
        fieldCtors.put("short", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new ShortField(f, g);
            }
        }
        );
        fieldCtors.put("long", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                return new LongField(f, g);
            }
        }
        );
        fieldCtors.put("custom", new FieldFactory.FieldCtor()
        {
            public Field getInstance(XmlField f, Group g)
                    throws IntakeException
            {
                String fieldClass = f.getFieldClass();

                if (fieldClass != null
                        && fieldClass.indexOf('.') == -1)
                {
                    fieldClass = Field.defaultFieldPackage + fieldClass;
                }

                if (fieldClass != null)
                {
                    Class field;

                    try
                    {
                        field = Class.forName(fieldClass);
                        Constructor constructor =
                            field.getConstructor(new Class[] { XmlField.class, Group.class });

                        return (Field)constructor.newInstance(new Object[] { f, g });
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new IntakeException(
                                "Could not load Field class("
                                + fieldClass + ")", e);
                    }
                    catch (Exception e)
                    {
                        throw new IntakeException(
                                "Could not create new instance of Field("
                                + fieldClass + ")", e);
                    }
                }
                else
                {
                    throw new IntakeException(
                            "Custom field types must define a fieldClass");
                }
            }
        }
        );
        return fieldCtors;
    }

    protected static abstract class FieldCtor
    {
        public Field getInstance(XmlField f, Group g) throws IntakeException
        {
            return null;
        }
    }

    /**
     * Creates a Field object appropriate for the type specified
     * in the xml file.
     *
     * @param xmlField a <code>XmlField</code> value
     * @return a <code>Field</code> value
     * @throws IntakeException indicates that an unknown type was specified for a field.
     */
    public static final Field getInstance(XmlField xmlField, Group xmlGroup)
            throws IntakeException
    {
        FieldCtor fieldCtor = null;
        Field field = null;
        String type = xmlField.getType();

        fieldCtor = (FieldCtor) fieldCtors.get(type);
        if (fieldCtor == null)
        {
            throw new IntakeException("An Unsupported type has been specified for " +
                    xmlField.getName() + " in group " + xmlGroup.getIntakeGroupName() + " type = " + type);
        }
        else
        {
            field = fieldCtor.getInstance(xmlField, xmlGroup);
        }

        return field;
    }
}