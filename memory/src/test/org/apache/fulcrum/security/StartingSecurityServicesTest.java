package org.apache.fulcrum.security;
/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import org.apache.avalon.framework.service.ServiceException;

import org.apache.fulcrum.security.model.basic.BasicModelManager;
import org.apache.fulcrum.security.model.dynamic.DynamicModelManager;
import org.apache.fulcrum.security.memory.MemoryGroupManagerImpl;
import org.apache.fulcrum.security.memory.MemoryPermissionManagerImpl;
import org.apache.fulcrum.security.memory.MemoryRoleManagerImpl;
import org.apache.fulcrum.security.memory.MemoryUserManagerImpl;
import org.apache.fulcrum.testcontainer.BaseUnitTest;

/**
 * @author <a href="mailto:marco@intermeta.de">Marco Kn&uuml;ttel</a>
 * @version $Id$
 */

public class StartingSecurityServicesTest extends BaseUnitTest
{
    private SecurityService securityService = null;
    public StartingSecurityServicesTest(String name)
    {
        super(name);
    }
    public void testStartingDynamicModel() throws Exception
    {

        this.setConfigurationFileName("src/test/DynamicMemory.xml");
        securityService = (SecurityService) lookup(SecurityService.ROLE);
        assertTrue(
            securityService.getUserManager() instanceof MemoryUserManagerImpl);
        assertTrue(
            securityService.getRoleManager() instanceof MemoryRoleManagerImpl);
        assertTrue(
            securityService.getPermissionManager()
                instanceof MemoryPermissionManagerImpl);
        assertTrue(
            securityService.getGroupManager()
                instanceof MemoryGroupManagerImpl);
        assertTrue(
            securityService.getModelManager() instanceof DynamicModelManager);
    }

    public void testStartingTurbineModel() throws Exception
    {

        this.setConfigurationFileName("src/test/TurbineMemory.xml");
        securityService = (SecurityService) lookup(SecurityService.ROLE);
        assertTrue(
            securityService.getUserManager()
                instanceof org.apache.fulcrum.security.memory.turbine
                    .MemoryTurbineUserManagerImpl);
        assertTrue(
            securityService.getRoleManager() instanceof MemoryRoleManagerImpl);
        assertTrue(
            securityService.getPermissionManager()
                instanceof MemoryPermissionManagerImpl);
		assertTrue(
			securityService.getGroupManager()
				instanceof MemoryGroupManagerImpl);
        assertTrue(
            securityService.getModelManager()
                instanceof org.apache.fulcrum.security.memory.turbine
                    .MemoryTurbineModelManagerImpl);
        assertTrue(
            securityService.getModelManager() instanceof DynamicModelManager);
    }

    public void testStartingBasicModel() throws Exception
    {

        this.setConfigurationFileName("src/test/BasicMemory.xml");
        securityService = (SecurityService) lookup(SecurityService.ROLE);
        assertTrue(
            securityService.getUserManager() instanceof MemoryUserManagerImpl);
        assertTrue(
            securityService.getGroupManager()
                instanceof MemoryGroupManagerImpl);
        assertTrue(
            securityService.getModelManager() instanceof BasicModelManager);
    }

    public void testLazyLoadingOfServices() throws Exception
    {
        this.setRoleFileName(null);
        this.setConfigurationFileName("src/test/LazyLoadServices.xml");
        securityService = (SecurityService) lookup(SecurityService.ROLE);
        assertTrue(
            securityService.getUserManager() instanceof MemoryUserManagerImpl);
        try
        {
            securityService.getModelManager();
            fail("Should have throw runtime error");
        }
        catch (RuntimeException re)
        {
            assertTrue(
                "Type was " + re.getCause().getClass().getName(),
                re.getCause() instanceof ServiceException);
        }
    }

}
