package org.apache.fulcrum.intake;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import org.apache.turbine.om.OMTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.transform.XmlToAppData;
import org.apache.fulcrum.intake.xmlmodel.AppData;
import org.apache.fulcrum.intake.xmlmodel.XmlField;
import org.apache.fulcrum.intake.xmlmodel.XmlGroup;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.pool.BoundedBuffer;
import org.apache.fulcrum.pool.Recyclable;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;

/**
 * This service provides access to input processing objects based
 * on an XML specification.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class TurbineIntakeService
    extends BaseService
    implements IntakeService
{
    /** Array of group names. */
    private String[] groupNames;

    /** The cache of group names. */
    private Map groupNameMap;

    /** The cache of group keys. */
    private Map groupKeyMap;

    /** The cache of PropertyDescriptor[]. */
    private Map beanPropsMap;

    /** The cache of property getters. */
    private Map getterMap;

    /** The cache of property setters. */
    private Map setterMap;

    /** Keep a OMTool to be able to retrieve objects */
    //private OMTool omTool;

    /** The top element of the object tree */
    private AppData appData;

    /**
     * The pool repository, one pool for each class.
     */
    private HashMap poolRepository = new HashMap();

    // a couple integers for a switch statement
    private static final int GETTER = 0;
    private static final int SETTER = 1;

    /**
     * Constructor.
     */
    public TurbineIntakeService()
    {
    }

    /**
     * Called the first time the Service is used.
     *
     * @param config A ServletConfig.
     */
    public void init()
        throws InitializationException
    {
        String xmlPath = getConfiguration().getString(XML_PATH);

        if ( xmlPath == null )
        {
            String pathError =
                "Path to intake.xml was not specified.  Check that the" +
                " property exists in TR.props and was loaded.";
            getCategory().error(pathError);
            throw new InitializationException(pathError);
        }

        //!! need a constant
        String appDataPath = "WEB-INF/appData.ser";
        try
        {
            // If possible, transform paths to be webapp root relative.
            xmlPath = getRealPath(xmlPath);
            appDataPath = getRealPath(appDataPath);
            File serialAppData = new File(appDataPath);
            File xmlFile = new File(xmlPath);
            if ( serialAppData.exists()
                 && serialAppData.lastModified() > xmlFile.lastModified() )
            {
                InputStream in = null;
                try
                {
                    in = new FileInputStream(serialAppData);
                    ObjectInputStream p = new ObjectInputStream(in);
                    appData = (AppData)p.readObject();
                }
                catch (Exception e)
                {
                    // We got a corrupt file for some reason
                    writeAppData(xmlPath, appDataPath, serialAppData);
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else
            {
                writeAppData(xmlPath, appDataPath, serialAppData);
            }

            groupNames = new String[appData.getGroups().size()];
            groupKeyMap = new HashMap();
            groupNameMap = new HashMap();
            beanPropsMap = new HashMap();
            getterMap = new HashMap();
            setterMap = new HashMap();
            // omTool = new OMTool();
            String pkg = appData.getBasePackage();

            List glist = appData.getGroups();
            for ( int i=glist.size()-1; i>=0; i-- )
            {
                XmlGroup g = (XmlGroup)glist.get(i);
                String groupName = g.getName();
                groupNames[i] = groupName;
                groupKeyMap.put(groupName, g.getKey());
                groupNameMap.put(g.getKey(), groupName);

                List classNames = g.getMapToObjects();
                Iterator iter2 = classNames.iterator();
                while (iter2.hasNext())
                {
                    String className = (String)iter2.next();
                    if ( !getterMap.containsKey(className) )
                    {
                        getterMap.put(className, new HashMap());
                        setterMap.put(className, new HashMap());
                    }
                }
            }

            setInit(true);
        }
        catch (Exception e)
        {
            throw new InitializationException(
                "TurbineIntakeService failed to initialize", e);
        }
    }

    /**
     * This method writes the appData file into Objects and stores
     * the information into this classes appData property
     */
    private void writeAppData(String xmlPath, String appDataPath, File serialAppData)
        throws Exception
    {
        XmlToAppData xmlApp = new XmlToAppData();
        appData = xmlApp.parseFile(xmlPath);
        OutputStream out = null;
        InputStream in = null;
        try
        {
            // write the appData file out
            out = new FileOutputStream(serialAppData);
            ObjectOutputStream p = new ObjectOutputStream(out);
            p.writeObject(appData);
            p.flush();

            // read the file back in. for some reason on OSX 10.1
            // this is necessary.
            in = new FileInputStream(serialAppData);
            ObjectInputStream pin = new ObjectInputStream(in);
            appData = (AppData)pin.readObject();
        }
        catch (Exception e)
        {
            getCategory().info(
                "Intake initialization could not be serialized " +
                "because writing to " + appDataPath + " was not " +
                "allowed.  This will require that the xml file be " +
                "parsed when restarting the application.");
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }
        }
    }

    private Method initializeBeanProp(String className, String propName,
                                        int getOrSet)
        throws Exception
    {
        // getter or setter method
        Method method = null;
        if ( className != null && propName != null )
        {
            // Uses bean introspection to set writable properties
            // of bean from the parameters, where a
            // (case-insensitive) name match between the bean
            // property and the parameter is looked for.
            PropertyDescriptor[] beanProps = 
                (PropertyDescriptor[])beanPropsMap.get(className);
            if ( beanProps == null ) 
            {
                beanProps = Introspector
                    .getBeanInfo(Class.forName(className))
                    .getPropertyDescriptors();
                beanPropsMap.put(className, beanProps);
            }

            boolean noMatch = true;
            for (int j=beanProps.length-1; j>=0; j--)
            {
                if (propName.equalsIgnoreCase(beanProps[j].getName()))
                {
                    switch ( getOrSet )
                    {
                        case GETTER:
                            method = beanProps[j].getReadMethod();
                            // it appears that the introspector misses some
                            // getters
                            if ( method == null ) 
                            {
                                Class cls = Class.forName(className);
                                try
                                {
                                    method = cls
                                        .getMethod("get" + propName, null);
                                    // we have to force the setter as well
                                    Class[] sig = new Class[1];
                                    sig[0] = method.getReturnType();
                                    Method setterMethod = null;
                                    try
                                    {
                                        setterMethod = cls
                                            .getMethod("set" + propName, sig);
                                    }
                                    catch(NoSuchMethodException e)
                                    {
                                        // ignore, setter was null;
                                    }
                                    
                                    beanProps[j] = new PropertyDescriptor(
                                        propName, method, setterMethod);
                                    System.out.println("reset descriptor");
                                }
                                catch(NoSuchMethodException e)
                                {
                                    // ignore, getter was really null
                                    // we should check for boolean "is"
                                    // prefix, though boolean less likely to 
                                    // override setters to cause problems
                                }
                            }
                            
                            ((HashMap)getterMap.get(className))
                                .put(propName, method);
                            break;
                        case SETTER:
                            method = beanProps[j].getWriteMethod();
                            ((HashMap)setterMap.get(className))
                                .put(propName, method);
                    }
                    noMatch = false;
                    break;
                }
            }
            if ( noMatch )
            {
                getCategory().error("Intake property: '" + propName + 
                          "' for class: '" +
                          className + "' could not be found.");
            }
        }
        return method;
    }

    /**
     * An inner class for group specific pools.
     */
    private class PoolBuffer
    {
        /**
         * A buffer for class instances.
         */
        private BoundedBuffer pool;

        /**
         * A cache for recycling methods.
         */
        private HashMap recyclers;

        /**
         * Contructs a new pool buffer with a specific capacity.
         *
         * @param capacity a capacity.
         */
        public PoolBuffer(int capacity)
        {
            pool = new BoundedBuffer(capacity);
        }

        /**
         * Polls for an instance from the pool.
         *
         * @return an instance or null.
         */
        public Group poll()
            throws ServiceException
        {
            Group instance = (Group)pool.poll();
            if ((instance != null) &&
                (instance instanceof Recyclable))
            {
                try
                {
                    ((Recyclable) instance).recycle();
                }
                catch (Exception x)
                {
                    throw new ServiceException("Recycling failed for " +
                        instance.getClass().getName(),x);
                }
            }
            return instance;
        }

        /**
         * Offers an instance to the pool.
         *
         * @param instance an instance.
         */
        public boolean offer(Group instance)
        {
            try
            {
                ((Recyclable) instance).dispose();
            }
            catch (Exception x)
            {
                return false;
            }
            return pool.offer(instance);
        }

        /**
         * Returns the capacity of the pool.
         *
         * @return the capacity.
         */
        public int capacity()
        {
            return pool.capacity();
        }

        /**
         * Returns the size of the pool.
         *
         * @return the size.
         */
        public int size()
        {
            return pool.size();
        }
    }

    /**
     * Gets an instance of a named group either from the pool
     * or by calling the Factory Service if the pool is empty.
     *
     * @param groupName the name of the group.
     * @return a Group instance.
     * @throws ServiceException if recycling fails.
     */
    public Group getGroup(String groupName)
            throws ServiceException
    {
        if (groupName == null)
        {
            throw new ServiceException (
                "Intake TurbineIntakeService.getGroup(groupName) is null");
        }
        Group instance = (Group)pollInstance(groupName);
        if ( instance == null )
        {
            try
            {
                instance = new Group(appData.getGroup(groupName));
            }
            catch (Exception e)
            {
                throw new ServiceException(e);
            }
        }
        return instance;
    }


    /**
     * Puts a Group back to the pool.
     *
     * @param instance the object instance to recycle.
     * @return true if the instance was accepted.
     */
    public boolean releaseGroup(Group instance)
    {
        if (instance != null)
        {
            HashMap repository = poolRepository;
            String name = instance.getIntakeGroupName();
            PoolBuffer pool = (PoolBuffer) repository.get(name);
            if (pool == null)
            {
                pool = new PoolBuffer(instance.getPoolCapacity());
                repository = (HashMap) repository.clone();
                repository.put(name,pool);
                poolRepository = repository;
            }
            return pool.offer(instance);
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets the capacity of the pool for a named group.
     *
     * @param name the name of the class.
     */
    public int getCapacity(String name)
        throws Exception
    {
        int capacity = DEFAULT_POOL_CAPACITY;
        PoolBuffer pool = (PoolBuffer) poolRepository.get(name);
        if ( pool == null )
        {
            try
            {
                capacity = Integer
                    .parseInt(appData.getGroup(name).getPoolCapacity());
            }
            catch (NumberFormatException nfe)
            {
                // Ignored
            }
        }
        else
        {
            capacity = pool.capacity();
        }
        return capacity;
    }

    /**
     * Sets the capacity of the pool for a group.
     * Note that the pool will be cleared after the change.
     *
     * @param name the name of the group.
     * @param capacity the new capacity.
     */
    public void setCapacity(String name,
                            int capacity)
    {
        HashMap repository = poolRepository;
        repository = repository != null ?
            (HashMap) repository.clone() : new HashMap();
        repository.put(name,new PoolBuffer(capacity));
        poolRepository = repository;
    }

    /**
     * Gets the current size of the pool for a group.
     *
     * @param name the name of the group.
     */
    public int getSize(String name)
    {
        PoolBuffer pool = (PoolBuffer) poolRepository.get(name);
        return pool != null ? pool.size() : 0;
    }

    /**
     * Clears instances of a group from the pool.
     *
     * @param name the name of the group.
     */
    public void clearPool(String name)
    {
        throw new Error("Not implemented");
        /* FIXME!! We need to worry about objects that are checked out

        HashMap repository = poolRepository;
        if (repository.get(name) != null)
        {
            repository = (HashMap) repository.clone();
            repository.remove(name);
            poolRepository = repository;
        }
        */
    }

    /**
     * Clears all instances from the pool.
     */
    public void clearPool()
    {
        throw new Error("Not implemented");
        /* FIXME!! We need to worry about objects that are checked out
        poolRepository = new HashMap();
        */
    }

    /**
     * Polls and recycles an object of the named group from the pool.
     *
     * @param groupName the name of the group.
     * @return the object or null.
     * @throws ServiceException if recycling fails.
     */
    private Object pollInstance(String groupName)
        throws ServiceException
    {
        PoolBuffer pool = (PoolBuffer) poolRepository.get(groupName);
        return pool != null ? pool.poll() : null;
    }

    /**
     * Names of all the defined groups.
     *
     * @return array of names.
     */
    public String[] getGroupNames()
    {
        return groupNames;
    }

    /**
     * Gets the key (usually a short identifier) for a group.
     *
     * @param groupName the name of the group.
     * @return the the key.
     */
    public String getGroupKey(String groupName)
    {
        return (String)groupKeyMap.get(groupName);
    }

    /**
     * Gets the group name given its key.
     *
     * @param the the key.
     * @return groupName the name of the group.
     */
    public String getGroupName(String groupKey)
    {
        return (String)groupNameMap.get(groupKey);
    }

    /**
     * Gets the Method that can be used to set a property.
     *
     * @param className the name of the object.
     * @param propName the name of the property.
     * @return the setter.
     */
    public Method getFieldSetter(String className, String propName)
    {
        Map settersForClassName = (Map)setterMap.get(className);
        Method method = (Method)settersForClassName.get(propName);

        if ( method == null )
        {
            synchronized(setterMap)
            {
                try
                {
                    method = initializeBeanProp(className, propName, SETTER);
                }
                catch (Exception e)
                {
                    getCategory().error(e);
                }
            }
        }
        return method;
    }

    /**
     * Gets the Method that can be used to get a property value.
     *
     * @param className the name of the object.
     * @param propName the name of the property.
     * @return the getter.
     */
    public Method getFieldGetter(String className, String propName)
    {
        Map gettersForClassName = (Map)getterMap.get(className);
        Method method = (Method)gettersForClassName.get(propName);

        if ( method == null )
        {
            synchronized(getterMap)
            {
                try
                {
                    method = initializeBeanProp(className, propName, GETTER);
                }
                catch (Exception e)
                {
                    getCategory().error(e);
                }
            }
        }
        return method;
    }

}
