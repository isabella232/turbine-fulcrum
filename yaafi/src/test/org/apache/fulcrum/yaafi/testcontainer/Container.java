package org.apache.fulcrum.yaafi.testcontainer;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fulcrum.yaafi.framework.container.ServiceContainer;
import org.apache.fulcrum.yaafi.framework.container.ServiceContainerImpl;
import org.apache.fulcrum.yaafi.framework.factory.ServiceManagerFactory;
import org.apache.fulcrum.yaafi.service.servicemanager.ServiceManagerService;


/**
 * This is a simple YAAFI based container that can be used in unit test
 * of the fulcrum components.
 *
 * @author <a href="mailto:siegfried.goeschl@it20one.at">Siegfried Goeschl</a> 
 */
public class Container extends AbstractLogEnabled implements Initializable, Disposable
{
	/** Key used in the context for defining the application root */
    public static String COMPONENT_APP_ROOT = "componentAppRoot";

    /** Alternate Merlin Friendly Key used in the context for defining the application root */
    public static String URN_AVALON_HOME = "urn:avalon:home";    

    /** Alternate Merlin Friendly Key used in the context for defining the application root */
    public static String URN_AVALON_TEMP = "urn:avalon:temp";    

    /** Component manager */
    private ServiceContainer manager;
    
    /** Configuration file name */
    private String configFileName;
    
    /** Role file name */
    private String roleFileName;
    
    /** Parameters file name */
    private String parametersFileName;
    
    
    /** 
     * Constructor
     */
    public Container()
    {
        // org.apache.log4j.BasicConfigurator.configure();
        this.manager = new ServiceContainerImpl();
        this.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG ) );
    }
        
    /**
     * Starts up the container and initializes it.
     *
     * @param configFileName Name of the component configuration file
     * @param roleFileName Name of the role configuration file
     */
    public void startup(String configFileName, String roleFileName, String parametersFileName )
    {
        getLogger().debug("Starting container...");        
        
        this.configFileName = configFileName;
        this.roleFileName = roleFileName;
        this.parametersFileName = parametersFileName;
        
        File configFile = new File(configFileName);        
        
        if (!configFile.exists())
        {            
            throw new RuntimeException(
                "Could not initialize the container because the config file could not be found:" + configFile);
        }

        try
        {
            initialize();
            getLogger().info("YaffiContainer ready.");
        }
        catch (Exception e)
        {
            getLogger().error("Could not initialize the container", e);
            throw new RuntimeException("Could not initialize the container");
        }    
    }
    
    // -------------------------------------------------------------
    // Avalon lifecycle interfaces
    // -------------------------------------------------------------
    /**
     * Initializes the container
     *
     * @throws Exception generic exception
     */
    public void initialize() throws Exception
    {
        DefaultContext context = new DefaultContext();
        String absolutePath = new File("").getAbsolutePath();
        context.put(COMPONENT_APP_ROOT, absolutePath);
        context.put(URN_AVALON_HOME, new File( new File("").getAbsolutePath() ) );
        
        Logger logger = new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG );
        
        this.manager = ServiceManagerFactory.create(
            logger,
            this.roleFileName,
            this.configFileName,
            this.parametersFileName,
            context
            );
    }

    /**
     * Disposes of the container and releases resources
     */
    public void dispose()
    {
        getLogger().debug("Disposing of container...");
        this.manager.dispose();
        getLogger().info("YaffiContainer has been disposed.");
    }
    /**
     * Returns an instance of the named component
     *
     * @param roleName Name of the role the component fills.
     * @throws ComponentException generic exception
     */
    public Object lookup(String roleName) throws ComponentException
    {
        try
        {
            return ServiceManagerService.getServiceManager().lookup(roleName);
        }
        catch( Exception e )
        {
            String msg = "Failed to lookup role " + roleName;
            throw new ComponentException(roleName,msg,e);
        }
    }
    /**
     * Releases the component implementing the Component interface. This
     * interface is depracted but still around in Fulcrum
     *
     * @param component
     */
    public void release(Component component)
    {
        ServiceManagerService.getServiceManager().release(component);
    }
    /**
     * Releases the component
     *
     * @param component
     */
    public void release(Object component)
    {
        ServiceManagerService.getServiceManager().release(component);
    }
}
