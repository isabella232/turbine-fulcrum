package org.apache.fulcrum.cache;

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

import java.io.IOException;
import org.apache.fulcrum.Service;
import org.apache.fulcrum.TurbineServices;

/**
 * This is a Facade class for GlobalCacheService.
 *
 * This class provides static methods that call related methods of the
 * implementation of the GlobalCacheService used by the System, according to
 * the settings in TurbineResources.
 *
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @version $Id$
 */
public abstract class TurbineGlobalCache
{
    /**
     * Utility method for accessing the service
     * implementation
     *
     * @return a GlobalCacheService implementation instance
     */
    protected static GlobalCacheService getService()
    {
        return (GlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);
    }


    /**
     * Gets a cached object given its id (a String).
     *
     * @param id The String id for the object.
     * @return A CachedObject.
     * @exception ObjectExpiredException, if the object has expired in
     * the cache.
     */
    public static CachedObject getObject(String id)
        throws ObjectExpiredException
    {
        return getService().getObject(id);
    }

    /**
     * Adds an object to the cache.
     *
     * @param id The String id for the object.
     * @param o The object to add to the cache.
     */
    public static void addObject(String id,
                          CachedObject o)
    {
        getService().addObject(id, o);
    }

    /**
     * Removes an object from the cache.
     *
     * @param id The String id for the object.
     */
    public static void removeObject(String id)
    {
        getService().removeObject(id);
    }

    /**
     * Returns the current size of the cache.
     * @return int representing current cache size in number of bytes
     */
    public static int getCacheSize()
        throws IOException
    {
        return getService().getCacheSize();
    }

    /**
     * Returns the number of objects in the cache.
     * @return int The current number of objects in the cache.
     */
    public static int getNumberOfObjects()
    {
        return getService().getNumberOfObjects();
    }
}
