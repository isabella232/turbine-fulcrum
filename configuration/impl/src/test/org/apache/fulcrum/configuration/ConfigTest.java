package org.apache.fulcrum.configuration;
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.merlin.unit.AbstractMerlinTestCase;
import org.apache.commons.configuration.Configuration;

/**
 * Basic testing of the Container
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Id$
 */
public class ConfigTest extends AbstractMerlinTestCase
{
    private Configuration config = null;
    
    /**
     * Constructor for test.
     *
     * @param testName name of the test being executed
     */
    public ConfigTest(String name)
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        super.setUp();
        try
        {
            config = (Configuration) this.resolve( "/test/config" );

        }
        catch (ComponentException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Verifies that the ConfigurationFactory works properly.
     *
     */
    public void testLoad()
    {
	  assertEquals(10.25,config.getDouble("test.double"),0);
	  assertEquals(
          "I'm complex!",config.getString("element2.subelement.subsubelement"));
    }
   
}
