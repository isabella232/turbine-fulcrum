package org.apache.fulcrum.security.hibernate.turbine;
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
import java.util.Iterator;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.hibernate.PersistenceHelper;
import org.apache.fulcrum.security.model.turbine.AbstractTurbineModelManager;
import org.apache.fulcrum.security.model.turbine.TurbineModelManager;
import org.apache.fulcrum.security.model.turbine.entity.TurbineGroup;
import org.apache.fulcrum.security.model.turbine.entity.TurbinePermission;
import org.apache.fulcrum.security.model.turbine.entity.TurbineRole;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
/**
 * This implementation persists to a database via Hibernate.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class HibernateModelManagerImpl extends AbstractTurbineModelManager implements TurbineModelManager
{
    private PersistenceHelper persistenceHelper;
    /**
	 * Grants a Role a Permission
	 * 
	 * @param role the Role.
	 * @param permission the Permission.
	 * @throws DataBackendException if there was an error accessing the data backend.
	 * @throws UnknownEntityException if role or permission is not present.
	 */
    public synchronized void grant(Role role, Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        boolean permissionExists = false;
        try
        {
            roleExists = getRoleManager().checkExists(role);
            permissionExists = getPermissionManager().checkExists(permission);
            
            if (roleExists && permissionExists)
            {
                ((TurbineRole) role).addPermission(permission);
                ((TurbinePermission) permission).addRole(role);
                getPersistenceHelper().updateEntity(permission);
                getPersistenceHelper().updateEntity(role);                
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("grant(Role,Permission) failed", e);
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" + role.getName() + "'");
        }
        if (!permissionExists)
        {
            throw new UnknownEntityException("Unknown permission '" + permission.getName() + "'");
        }
    }
    /**
	 * Revokes a Permission from a Role.
	 * 
	 * @param role the Role.
	 * @param permission the Permission.
	 * @throws DataBackendException if there was an error accessing the data backend.
	 * @throws UnknownEntityException if role or permission is not present.
	 */
    public synchronized void revoke(Role role, Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        boolean permissionExists = false;
        try
        {
            roleExists = getRoleManager().checkExists(role);
            permissionExists = getPermissionManager().checkExists(permission);
            if (roleExists && permissionExists)
            {
                ((TurbineRole) role).removePermission(permission);
                ((TurbinePermission) permission).removeRole(role);
                getPersistenceHelper().updateEntity(role);
                getPersistenceHelper().updateEntity(permission);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("revoke(Role,Permission) failed", e);
        }
        finally
        {
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" + role.getName() + "'");
        }
        if (!permissionExists)
        {
            throw new UnknownEntityException("Unknown permission '" + permission.getName() + "'");
        }
    }  

    /**
	 * @return Returns the persistenceHelper.
	 */
	public PersistenceHelper getPersistenceHelper() throws DataBackendException
	{
		if (persistenceHelper == null)
		{
			persistenceHelper = (PersistenceHelper)resolve(PersistenceHelper.ROLE);
		}
		return persistenceHelper;
	}
    
     public void grant(User user, Group group, Role role) throws DataBackendException, UnknownEntityException {
            boolean roleExists = false;
            boolean userExists = false;
            boolean groupExists = false;
            try {
                roleExists = getRoleManager().checkExists(role);
                userExists = getUserManager().checkExists(user);
                groupExists = getGroupManager().checkExists(group);
                if (roleExists && groupExists && userExists) {
                    TurbineUserGroupRole ugr = new TurbineUserGroupRole();
                    ugr.setGroup(group);
                    ugr.setRole(role);
                    ugr.setUser(user);
                    ((TurbineUser) user).addUserGroupRole(ugr);
                    ((TurbineGroup) group).addUserGroupRole(ugr);
                    ((TurbineRole) role).addUserGroupRole(ugr);
                    getPersistenceHelper().updateEntity(user);
                    getPersistenceHelper().updateEntity(group);
                    getPersistenceHelper().updateEntity(role);
                    return;
                }
            } catch (Exception e) {
                throw new DataBackendException("grant(Role,Permission) failed", e);
            }

            if (!roleExists) {
                throw new UnknownEntityException("Unknown role '" + role.getName() + "'");
            }
            if (!groupExists) {
                throw new UnknownEntityException("Unknown group '" + group.getName() + "'");
            }
            if (!userExists) {
                throw new UnknownEntityException("Unknown user '" + user.getName() + "'");
            }

        }

        public void revoke(User user, Group group, Role role) throws DataBackendException, UnknownEntityException {
            boolean roleExists = false;
            boolean userExists = false;
            boolean groupExists = false;
            try {
                roleExists = getRoleManager().checkExists(role);
                userExists = getUserManager().checkExists(user);
                groupExists = getGroupManager().checkExists(group);
                if (roleExists && groupExists && userExists) {
                    boolean ugrFound = false;
                    TurbineUserGroupRole ugr = null;
                    for(Iterator i = ((TurbineUser) user).getUserGroupRoleSet().iterator();i.hasNext();){
                        ugr = (TurbineUserGroupRole)i.next();
                        if(ugr.getUser().equals(user)&& ugr.getGroup().equals(group) && ugr.getRole().equals(role)){
                            ugrFound=true;
                            break;
                        }
                    }
                    if(!ugrFound){
                        throw new UnknownEntityException("Could not find User/Group/Role");
                    }

                    ((TurbineUser) user).removeUserGroupRole(ugr);
                    ((TurbineGroup) group).removeUserGroupRole(ugr);
                    ((TurbineRole) role).removeUserGroupRole(ugr);
                    getPersistenceHelper().updateEntity(user);
                    getPersistenceHelper().updateEntity(group);
                    getPersistenceHelper().updateEntity(role);
                    return;
                }
            } catch (Exception e) {
                throw new DataBackendException("grant(Role,Permission) failed", e);
            }

            if (!roleExists) {
                throw new UnknownEntityException("Unknown role '" + role.getName() + "'");
            }
            if (!groupExists) {
                throw new UnknownEntityException("Unknown group '" + group.getName() + "'");
            }
            if (!userExists) {
                throw new UnknownEntityException("Unknown user '" + user.getName() + "'");
            }

        } 
}