package org.apache.fulcrum;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import org.apache.fulcrum.ServiceException;
import org.apache.log4j.Category;
//!! this has to go away. we want to use a Configuration
// interface.
import org.apache.velocity.runtime.configuration.Configuration;

// NOTE:
// initClass is taking the name of the service now not
// the name of the class. not sure why this is necessary
// yet.

/**
 * A generic implementation of a <code>ServiceBroker</code>.
 *
 * Functionality that <code>ServiceBroker</code> provides:
 *
 * <ul>
 *
 * <li>Maintaining service name to class name mapping, allowing
 * plugable service implementations.</li>
 *
 * <li>Providing <code>Services</code> with a <code>Configuration</code>
 * based on system wide configuration mechanism.</li>
 *
 * </ul>
 *
 * @author <a href="mailto:burton@apache.org">Kevin Burton</a>
 * @author <a href="mailto:krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class BaseServiceBroker implements ServiceBroker
{
    /** 
     * Mapping of Service names to class names. 
     */
    protected Configuration mapping = new Configuration();

    /** 
     * A repository of Service instances. 
     */
    protected Hashtable services = new Hashtable();

    /**
     * Configuration for the services broker.
     * The configuration should be set by the application
     * in which the services framework is running.
     */
    protected Configuration configuration;

    /**
     * A prefix for <code>Service</code> properties in
     * TurbineResource.properties.
     */
    public static final String SERVICE_PREFIX = "services.";

    /**
     * A <code>Service</code> property determining its implementing
     * class name .
     */
    public static final String CLASSNAME_SUFFIX = ".classname";

    /** 
     * True if logging should go throught 
     * LoggingService, false if not. 
     */
    protected boolean loggingEnabled = false;

    /**
     * These are objects that the parent application
     * can provide so that application specific
     * services have a mechanism to retrieve specialized
     * information. For example, in Turbine there are services
     * that require the RunData object: these services can
     * retrieve the RunData object that Turbine has placed
     * in the service manager. This alleviates us of 
     * the requirement of having init(Object) all
     * together.
     */
    protected Hashtable serviceObjects = new Hashtable();

    /**
     * This is the log4j category that the parent application
     * has provided for logging. If a Category is not set
     * than all messages are sent to stout.
     */
    protected Category category;

    protected Hashtable categoryTable;

    /**
     * Application root path as set by the
     * parent application.
     */
    protected String applicationRoot;

    /**
     * Default constructor of InitableBorker.
     *
     * This constructor does nothing.
     */
    protected BaseServiceBroker()
    {
    }
    
    /**
     * Set the configuration object for the services broker.
     * This is the configuration that contains information
     * about all services in the care of this service
     * manager.
     *
     * @param Configuration
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Get the configuration for this service manager.
     *
     * @return Configuration
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Set the log4j Category that will be used for
     * logging.
     *
     * @param Category
     */
    public void setCategory(Category category)
    {
        this.category = category;
        loggingEnabled = true;
    }

    /**
     * Get the log4j Category used for logging.
     *
     * @return Category
     */
    public Category getCategory()
    {
        return category;
    }        
    
    /**
     * Get a log4j Category by name.
     *
     * @param name log4j category to return
     * @return Category log4j category
     */
    public Category getCategory(String name)
    {
        return Category.getInstance(name);
    }        

    public void setCategoryTable(Hashtable categoryTable)
    {
        this.categoryTable = categoryTable;
    }
    
    public Hashtable getCategoryTable()
    {
        return categoryTable;
    }        

    /**
     * Initialize this service manager.
     */
    public void init() 
        throws InitializationException
    {
        // Check:
        //
        // 1. The configuration has been set.
        // 2. Logging category has been set.
        // 3. Make sure the application root has been set.
        
        //!! We should make some service framework exceptions
        //   to throw in the event these requirements
        //   aren't satisfied.
        
        // Create the mapping between service names
        // and their classes.
        initMapping();
        
        Log.setCategory(getCategory());
        Log.setCategoryTable(getCategoryTable());
        
        // Start services that have their 'earlyInit'
        // property set to 'true'.
        initServices(false);
    }
    
    /**
     * Set an application specific service object
     * that can be used by application specific
     * services.
     *
     * @param String name of service object
     * @param Object value of service object
     */
    public void setServiceObject(String name, Object value)
    {
        serviceObjects.put(name, value);
    }
    
    /**
     * Get an application specific service object.
     *
     * @return Object application specific service object
     */
    public Object getServiceObject(String name)
    {
        return serviceObjects.get(name);
    }        

    /**
     * Creates a mapping between Service names and class names.
     *
     * The mapping is built according to settings present in
     * TurbineResources.properties.  The entries should have the
     * following form:
     *
     * <pre>
     * services.MyService.classname=com.mycompany.MyServiceImpl
     * services.MyOtherService.classname=com.mycompany.MyOtherServiceImpl
     * </pre>
     *
     * <br>
     *
     * Generic ServiceBroker provides no Services.
     */
    protected void initMapping()
    {
        int pref = SERVICE_PREFIX.length();
        int suff = CLASSNAME_SUFFIX.length();
        
        /*
         * These keys returned in an order that corresponds
         * to the order the services are listed in
         * the TR.props.
         *
         * When the mapping is created we use a Configuration
         * object to ensure that the we retain the order
         * in which the order the keys are returned.
         *
         * There's no point in retrieving an ordered set
         * of keys if they aren't kept in order :-)
         */
        Iterator keys = configuration.getKeys();
        
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            
            if(key.startsWith(SERVICE_PREFIX) && key.endsWith(CLASSNAME_SUFFIX))
            {
                String serviceKey = key.substring(pref, key.length() - suff);
                notice ("Added Mapping for Service: " + serviceKey);
                
                if (! mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey, 
                                        configuration.getString(key));
                }                    
            }
        }
    }

    /**
     * Performs early initialization of an Service class.
     *
     * @param className The name of the class to be initailized.
     * @param data An Object to be used for initialization activities.
     * @exception InitializationException Initialization was not successful.
     */
    public void initClass(String className)
        throws InitializationException
    {
        Service instance = getServiceInstance(className);

        if (!instance.getInit())
        {
            // this call might result in an indirect recursion
            instance.init();
        }
    }

    /**
        * Shuts down an <code>Initable</code>.
        *
        * This method is used to release resources allocated by an
        * <code>Initable</code>, and return it to its initial (uninitailized)
        * state.
        *
        * @param className The name of the class to be uninitialized.
        */
    public void shutdownClass(String className)
    {
        try
        {
            Service initable = getServiceInstance(className);
            if (initable.getInit())
            {
                initable.shutdown();
                ((BaseService) initable).setInit(false);
            }
        }
        catch (InstantiationException e)
        {
            // Shutdown of a nonexistent class was requested.
            // This does not hurt anything, so we log the error and continue.
            error(new ServiceException(
                "Shutdown of a nonexistent class " + className + 
                    " was requested", e));
        }
    }

    /**
     * Determines whether a service is registered in the configured
     * <code>TurbineResources.properties</code>.
     *
     * @param serviceName The name of the service whose existance to check.
     * @return Registration predicate for the desired services.
     */
    public boolean isRegistered(String serviceName)
    {
        return (services.get(serviceName) != null);
    }

    /**
     * Returns an Iterator over all known service names.
     *
     * @return An Iterator of service names.
     */
    public Iterator getServiceNames()
    {
        return mapping.getKeys();
    }

    /**
     * Returns an Iterator over all known service names beginning with
     * the provided prefix.
     *
     * @param prefix The prefix against which to test.
     * @return An Iterator of service names which match the prefix.
     */
    public Iterator getServiceNames(String prefix)
    {
        return mapping.getKeys(prefix);
    }

    /**
     * Performs early initialization of specified service.
     *
     * @param name The name of the service (generally the
     * <code>SERVICE_NAME</code> constant of the service's interface
     * definition).
     * @param data An object to use for initialization activities.
     * @exception InitializationException Initilaization of this
     * service was not successful.
     */
    public void initService(String name)
        throws InitializationException
    {
        String className = (String) mapping.get(name);
        
        if (className == null || className.trim().length() == 0)
        {
            throw new InitializationException(
                "ServiceBroker: initialization of unknown service " +
                    name + " requested.");
        }
        
        initClass(name);
    }

    /**
     * Performs early initialization of all services.  Failed early
     * initialization of a Service may be non-fatal to the system,
     * thuss the exceptions are logged and then discarded.
     *
     * @param data An Object to use for initialization activities.
     */
    public void initServices()
    {
        try
        {
            initServices(false);
        }
        catch (InstantiationException notThrown)
        {
        }
        catch (InitializationException notThrown)
        {
        }
    }

    /**
     * Performs early initiailzation of all services. You can decide
     * to handle failed initizalizations if you wish, but then
     * after one service fails, the other will not have the chance
     * to initialize.
     *
     * @param data An Object to use for initialization activities.
     * @param report <code>true</code> if you want exceptions thrown.
     */
    public void initServices(boolean report) 
        throws InstantiationException, InitializationException
    {
        Iterator names = getServiceNames();
        // throw exceptions
        if (report)
        {
            while (names.hasNext())
            {
                doInitService((String) names.next());
            }
        }
        // eat exceptions
        else
        {
            while (names.hasNext())
            {
                try
                {
                    doInitService((String) names.next());
                }
                // In case of an exception, file an error message; the
                // system may be still functional, though.
                catch (InstantiationException e)
                {
                    error(e);
                }
                catch (InitializationException e)
                {
                    error(e);
                }
            }
        }
        notice("Finished initializing all services!");
    }

    /**
     * Internal utility method for use in initServices()
     * to prevent duplication of code.
     */
    private void doInitService(String name) 
        throws InstantiationException, 
               InitializationException
    {
        /*
         * We only want to start up services that have their
         * earlyInit flag set. Don't waste resources if we
         * don't have to.
         */
        if (getConfiguration(name).getBoolean("earlyInit", false) == false)
        {
            return;
        }            
            
        notice("Start Initializing service (early): " + name);

        // Make sure the service has it's name and broker
        // reference set before initialization.
        getServiceInstance(name);

        /* 
         * We are using the name of the service now.
         */
        initClass(name);

        notice("Finish Initializing service (early): " + name);
    }

    /**
     * Shuts down a <code>Service</code>.
     *
     * This method is used to release resources allocated by a
     * Service, and return it to its initial (uninitailized) state.
     *
     * @param name The name of the <code>Service</code> to be uninitialized.
     */
    public void shutdownService(String name)
    {
        String className = (String) mapping.get(name);
                
        if (className != null)
        {
            shutdownClass(className);
        }
    }

    /**
     * Shuts down all Turbine services, releasing allocated resources and
     * returning them to their initial (uninitailized) state.
     */
    public void shutdownServices()
    {
        notice("Shutting down all services!");

        Iterator serviceNames = getServiceNames();
        String serviceName = null;

        /*
         * Now we want to reverse the order of
         * this list. This functionality should be added to
         * the ExtendedProperties in the commons but
         * this will fix the problem for now.
         */

        ArrayList reverseServicesList = new ArrayList();

        while (serviceNames.hasNext())
        {
            serviceName = (String) serviceNames.next();
            reverseServicesList.add(0, serviceName);
        }

        serviceNames = reverseServicesList.iterator();

        while (serviceNames.hasNext())
        {
            serviceName = (String) serviceNames.next();
            notice("Shutting down service: " + serviceName);
            shutdownService(serviceName);
        }
    }

    /**
     * Returns an instance of requested Service.
     *
     * @param name The name of the Service requested.
     * @return An instance of requested Service.
     * @exception InstantiationException, if the service is unknown or
     * can't be initialized.
     */
    public Service getService(String name) throws InstantiationException
    {
        Service service;
        try
        {
            service = getServiceInstance(name);
            if (!service.getInit())
            {
                synchronized (service.getClass())
                {
                    if (!service.getInit())
                    {
                        notice("Start Initializing service (late): " + name);
                        service.init();
                        notice("Finish Initializing service (late): " + name);
                    }
                }
            }
            if (!service.getInit())
            {
                // this exception will be caught & rethrown by this very method.
                // getInit() returning false indicates some initialization issue,
                // which in turn prevents the InitableBroker from passing a
                // reference to a working instance of the initable to the client.
                throw new InitializationException(
                    "init() failed to initialize service " + name);
            }
            return service;
        }
        catch (InitializationException e)
        {
            throw new InstantiationException(
                "Service " + name + " failed to initialize", e);
        }
    }

    /**
     * Retrieves an instance of a Service without triggering late
     * initialization.
     *
     * Early initialization of a Service can require access to Service
     * properties.  The Service must have its name and serviceBroker
     * set by then.  Therefore, before calling
     * Initable.initClass(Object), the class must be instantiated with
     * InitableBroker.getInitableInstance(), and
     * Service.setServiceBroker() and Service.setName() must be
     * called.  This calls for two - level accesing the Services
     * instances.
     *
     * @param name The name of the service requested.
     * @exception InstantiationException, if the service is unknown or
     * can't be initialized.
     */
    protected Service getServiceInstance(String name)
        throws InstantiationException
    {
        Service service = (Service) services.get(name);

        if (service == null)
        {
            String className = mapping.getString(name);
            
            if (className == null)
            {
                throw new InstantiationException(
                    "ServiceBroker: unknown service " + name + " requested");
            }
            try
            {
                service = (Service) services.get(className);

                if (service == null)
                {
                    try
                    {
                        service = (Service) Class.forName(className).newInstance();
                    }
                    // those two errors must be passed to the VM
                    catch (ThreadDeath t)
                    {
                        throw t;
                    }
                    catch (OutOfMemoryError t)
                    {
                        throw t;
                    }
                    catch (Throwable t)
                    {
                        // Used to indicate error condition.
                        String msg = null;

                        if (t instanceof NoClassDefFoundError)
                        {
                            msg = "A class referenced by " + className +
                                " is unavailable. Check your jars and classes.";
                        }
                        else if (t instanceof ClassNotFoundException)
                        {
                            msg = "Class " + className + 
                                " is unavailable. Check your jars and classes.";
                        }
                        else if (t instanceof ClassCastException)
                        {
                            msg = "Class " + className + 
                                " doesn't implement Initable.";
                        }
                        else
                        {
                            msg = "Failed to instantiate " + className;
                        }
                        
                        throw new InstantiationException(msg, t);
                    }
                }
            }
            catch (ClassCastException e)
            {
                throw new InstantiationException("ServiceBroker: class " 
                    + className + " does not implement Service interface.", e);
            }
            catch (InstantiationException e)
            {
                throw new InstantiationException(
                    "Failed to instantiate service " + name, e);
            }
            service.setServiceBroker(this);
            service.setName(name);
            services.put(name, service);
        }

        return service;
    }

    /**
     * Returns the Configuration for the specified service.
     *
     * @param name The name of the service.
     */
    public Configuration getConfiguration( String name )
    {
        return configuration.subset(SERVICE_PREFIX + name);
    }

    /**
     * Output a diagnostic notice.
     *
     * This method is used by the service framework classes for producing
     * tracing mesages that might be useful for debugging.
     *
     * <p>Standard Turbine logging facilities are used.
     *
     * @param msg the message to print.
     */
    public void notice(String msg)
    {
        if (loggingEnabled)
        {
            category.info(msg);
        }            
        else
        {
            System.out.println("NOTICE: " + msg);
        }            
    }

    /**
     * Output an error message.
     *
     * This method is used by the service framework classes for displaying
     * stacktraces of any exceptions that might be caught during processing.
     *
     * <p>Standard Turbine logging facilities are used.
     *
     * @param msg the message to print.
     */
    public void error(Throwable t)
    {
        if (loggingEnabled)
        {
            category.info(t);
            //!! The stack trace is not making it into
            // the log which isn't good.
        }
        else
        {
            System.out.println("ERROR: " + t.getMessage());
            t.printStackTrace();
        }            
    }

    /**
     * Set the application root.
     *
     * @param String application root
     */
    public void setApplicationRoot(String applicationRoot)
    {
        this.applicationRoot = applicationRoot;
    }        

    /**
     * Get the application root as set by
     * the parent application.
     *
     * @return String application root
     */
    public String getApplicationRoot()
    {
        return applicationRoot;
    }

    public String getRealPath(String path)
    {
        return getApplicationRoot() + '/' + path;
    }
}
