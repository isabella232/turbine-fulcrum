package org.apache.fulcrum.intake.validator;

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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Validates Floats with the following constraints in addition to those
 * listed in NumberValidator and DefaultValidator.
 *
 * <table>
 * <caption>Validation rules</caption>
 * <tr><th>Name</th><th>Valid Values</th><th>Default Value</th></tr>
 * <tr><td>minValue</td><td>greater than Float.MIN_VALUE</td>
 * <td>&nbsp;</td></tr>
 * <tr><td>maxValue</td><td>less than Float.MAX_VALUE</td>
 * <td>&nbsp;</td></tr>
 * <tr><td>invalidNumberMessage</td><td>Some text</td>
 * <td>Entry was not a valid number</td></tr>
 * </table>
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:Colin.Chalmers@maxware.nl">Colin Chalmers</a>
 * @author <a href="mailto:jh@byteaction.de">J&uuml;rgen Hoffmann</a>
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id$
 */
public class FloatValidator
        extends NumberValidator<Float>
{
    /**
     * Default Constructor
     */
    public FloatValidator()
    {
        super();
        invalidNumberMessage = "Entry was not a valid Float";
    }

    /**
     * @see org.apache.fulcrum.intake.validator.NumberValidator#parseNumber(java.lang.String, java.util.Locale)
     */
    @Override
    protected Float parseNumber(String stringValue, Locale locale) throws NumberFormatException
    {
        NumberFormat nf = NumberFormat.getInstance(locale);

        try
        {
            return Float.valueOf(nf.parse(stringValue).floatValue());
		}
        catch (ParseException e)
        {
        	throw new NumberFormatException(e.getMessage());
		}
    }
}
