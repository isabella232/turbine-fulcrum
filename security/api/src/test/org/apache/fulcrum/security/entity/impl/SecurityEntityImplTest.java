package org.apache.fulcrum.security.entity.impl;
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
import junit.framework.TestCase;

import org.apache.fulcrum.security.entity.SecurityEntity;
/**
 * Test the SecurityEntityImple
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class SecurityEntityImplTest extends TestCase
{

    /**
     * Constructor for SecurityEntityImplTest.
     * @param arg0
     */
    public SecurityEntityImplTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Make sure lowercasing logic works properly.
     * @throws Exception
     */
    public void testSettingGettingName() throws Exception
    {
        SecurityEntity se = new SecurityEntityImpl();
        se.setName("hello");
        assertEquals("hello",se.getName());
        se.setName("HelLo");
		assertEquals("hello",se.getName());
		try
		{
		    se.setName(null);
		    fail("Should throw an InvalidParameterException");
		}
		catch(IllegalArgumentException ipe)
		{
		    //good
		}
    }
}
