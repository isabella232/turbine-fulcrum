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

import org.apache.commons.fileupload.FileItem;
import org.apache.fulcrum.intake.IntakeException;
import org.apache.fulcrum.intake.IntakeRuntimeException;
import org.apache.fulcrum.intake.validator.FileValidator;
import org.apache.fulcrum.intake.validator.ValidationException;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.parser.ValueParser;

/**
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class FileItemField
        extends Field<FileItem>
{
    /** Serial version */
	private static final long serialVersionUID = -963692413506822188L;

	/**
     * Constructor.
     *
     * @param field xml field definition object
     * @param group xml group definition object
     * @throws IntakeException thrown by superclass
     */
    public FileItemField(XmlField field, Group group)
            throws IntakeException
    {
        super(field, group);
    }

    /**
     * It is not possible to set the default value for this field type.
     * Calling this method with a non-null parameter will result in a
     * TurbineRuntimeException
     *
     * @param prop Parameter for the default values
     * @throws TurbineRuntimeException
     */
    @Override
	public void setDefaultValue(String prop)
    {
        if (prop != null)
        {
            throw new IntakeRuntimeException(
                    "Default values are not valid for "
                    + this.getClass().getName());
        }

        defaultValue = null;
    }

    /**
     * It is not possible to set the empty value for this field type.
     * Calling this method with a non-null parameter will result in a
     * TurbineRuntimeException
     *
     * @param prop Parameter for the empty values
     * @throws TurbineRuntimeException
     */
    @Override
	public void setEmptyValue(String prop)
    {
        if (prop != null)
        {
            throw new IntakeRuntimeException(
                    "Empty values are not valid for "
                    + this.getClass().getName());
        }

        emptyValue = null;
    }

    /**
     * A suitable validator.
     *
     * @return A suitable validator
     */
    @Override
	protected String getDefaultValidator()
    {
        return FileValidator.class.getName();
    }

    /**
     * Method called when this field (the group it belongs to) is
     * pulled from the pool.  The request data is searched to determine
     * if a value has been supplied for this field.  if so, the value
     * is validated.
     *
     * @param vp a <code>ValueParser</code> value
     * @return a <code>Field</code> value
     * @exception IntakeException if an error occurs
     */
    @Override
	public Field<FileItem> init(ValueParser vp)
            throws IntakeException
    {
        super.parser = vp;

        if (!(vp instanceof ParameterParser))
        {
            throw new IntakeException(
                    "FileItemFields can only be used with ParameterParser");
        }

        validFlag = true;

        if (parser.containsKey(getKey()))
        {
            setFlag = true;
            validate();
        }

        initialized = true;
        return this;
    }

    /**
     * Compares request data with constraints and sets the valid flag.
     *
     * @return the valid flag
     */
    @Override
	public boolean validate()
    {
        ParameterParser pp = (ParameterParser) super.parser;
        if (isMultiValued)
        {
            FileItem[] ss = pp.getFileItems(getKey());
            // this definition of not set might need refined.  But
            // not sure the situation will arise.
            if (ss.length == 0)
            {
                setFlag = false;
            }

            if (validator != null)
            {
                for (int i = 0; i < ss.length; i++)
                {
                    try
                    {
                        ((FileValidator) validator).assertValidity(ss[i]);
                    }
                    catch (ValidationException ve)
                    {
                        setMessage(ve.getMessage());
                    }
                }
            }

            if (setFlag && validFlag)
            {
                doSetValue();
            }
        }
        else
        {
            FileItem s = pp.getFileItem(getKey());
            if (s == null || s.getSize() == 0)
            {
                setFlag = false;
            }

            if (validator != null)
            {
                try
                {
                    ((FileValidator) validator).assertValidity(s);

                    if (setFlag)
                    {
                        doSetValue();
                    }
                }
                catch (ValidationException ve)
                {
                    setMessage(ve.getMessage());
                }
            }
            else if (setFlag)
            {
                doSetValue();
            }
        }

        return validFlag;
    }

    /**
     * Sets the value of the field from data in the parser.
     */
    @Override
	protected void doSetValue()
    {
        ParameterParser pp = (ParameterParser) super.parser;
        if (isMultiValued)
        {
            setTestValue(pp.getFileItems(getKey()));
        }
        else
        {
            setTestValue(pp.getFileItem(getKey()));
        }
    }
}