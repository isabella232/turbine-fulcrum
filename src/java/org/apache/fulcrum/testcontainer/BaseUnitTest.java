package org.apache.fulcrum.testcontainer;
/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import junit.framework.TestCase;

import org.apache.avalon.framework.component.ComponentException;
/**
 * Base class for unit tests for components. This version doesn't load the container until the
 * first request for a component. This allows the tester to populate the configurationFileName and
 * roleFileName, possible one per test.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class BaseUnitTest extends TestCase
{
  
    public static final String CONTAINER_ECM="CONTAINER_ECM";
    public static final String CONTAINER_YAAFI="CONTAINER_YAAFI";
    /** Key used in the context for defining the application root */
    public static String COMPONENT_APP_ROOT = Container.COMPONENT_APP_ROOT;

    /** Pick the default container to be Yaafi **/
    public static String containerType = CONTAINER_YAAFI;
    /** Container for the components */
    private Container container;
    /** Setup our default configurationFileName */
    private String configurationFileName = "src/test/TestComponentConfig.xml";
    /** Setup our default roleFileName */
    private String roleFileName = "src/test/TestRoleConfig.xml";
    /** Setup our default parameterFileName */
    private String parameterFileName = null;
    
    /**
	 * Gets the configuration file name for the container should use for this test. By default it
	 * is src/test/TestComponentConfig.
	 * 
	 * @param configurationFileName
	 */
    protected void setConfigurationFileName(String configurationFileName)
    {
        this.configurationFileName = configurationFileName;
    }

    /**
	 * Override the role file name for the container should use for this test. By default it is
	 * src/test/TestRoleConfig.
	 * 
	 * @param roleFileName
	 */
    protected void setRoleFileName(String roleFileName)
    {
        this.roleFileName = roleFileName;
    }

    /**
	 * Constructor for test.
	 * 
	 * @param testName name of the test being executed
	 */
    public BaseUnitTest(String testName)
    {
        super(testName);
    }
    
    /**
	 * Clean up after each test is run.
	 */
    protected void tearDown()
    {
        if (container != null)
        {
            container.dispose();
        }
        container = null;
    }
    /**
	 * Gets the configuration file name for the container should use for this test.
	 * 
	 * @return The filename of the configuration file
	 */
    protected String getConfigurationFileName()
    {
        return configurationFileName;
    }
    /**
	 * Gets the role file name for the container should use for this test.
	 * 
	 * @return The filename of the role configuration file
	 */
    protected String getRoleFileName()
    {
        return roleFileName;
    }
    /**
     * Gets the parameter file name for the container should use for this test.
     * 
     * @return The filename of the role configuration file
     */
    protected String getParameterFileName()
    {
        return parameterFileName;
    }    
    /**
	 * Returns an instance of the named component. Starts the container if it hasn't been started.
	 * 
	 * @param roleName Name of the role the component fills.
	 * @throws ComponentException generic exception
	 */
    protected Object lookup(String roleName) throws ComponentException
    {
        if (container == null)
        {
            if(containerType.equals(CONTAINER_ECM)){
                container = new ECMContainer();
            }
            else {
                container = new YAAFIContainer();
            }
            container.startup(getConfigurationFileName(), getRoleFileName(),getParameterFileName());
        }
        return container.lookup(roleName);
    }
    
    /**
     * Helper method for converting to and from Merlin Unit TestCase.
     * @param roleName
     * @return
     * @throws ComponentException
     */
    protected Object resolve(String roleName) throws ComponentException
    {
        return lookup(roleName);
    }    
    /**
	 * Releases the component
	 * 
	 * @param component
	 */
    protected void release(Object component)
    {
        if (container != null)
        {
            container.release(component);
        }
    }
}
