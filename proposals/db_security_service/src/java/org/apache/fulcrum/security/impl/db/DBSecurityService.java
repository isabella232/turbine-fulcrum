package org.apache.fulcrum.security.impl.db;


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


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.fulcrum.InitializationException;

import org.apache.fulcrum.security.BaseSecurityService;
import org.apache.fulcrum.security.TurbineSecurity;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;

import org.apache.fulcrum.security.entity.User;

import org.apache.fulcrum.security.impl.db.entity.TurbineRolePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;

import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.UnknownEntityException;

import org.apache.commons.configuration.Configuration;

import org.apache.log4j.Category;

import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;

import org.apache.torque.util.Criteria;

/**
 * An implementation of SecurityService that uses a database as backend.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:marco@intermeta.de">Marco Kn&uuml;ttel</a>
 * @version $Id$
 */
public class DBSecurityService
    extends BaseSecurityService
{
    /** Log4j category */
    private Category category = 
        Category.getInstance(getClass().getName());

    /**
     * Initializes the DBSecurityService, loading the various class objects
     * representing the security entity peer classes
     *
     * @exception InitializationException A problem occured during initialization
     */

    public void init()
        throws InitializationException
    {
        Configuration conf = getConfiguration();

        UserPeerManager.init(conf);
        GroupPeerManager.init(conf);
        RolePeerManager.init(conf);
        PermissionPeerManager.init(conf);

        /* At the end, because it calls setInit(true)! */
        super.init();
    }
  

    /*-----------------------------------------------------------------------
      Creation of AccessControlLists
      -----------------------------------------------------------------------*/

    /**
     * Constructs an AccessControlList for a specific user.
     *
     * This method creates a snapshot of the state of security information
     * concerning this user, at the moment of invocation and stores it
     * into an AccessControlList object.
     *
     * @param user the user for whom the AccessControlList are to be retrieved
     *
     * @return A new AccessControlList object.
     *
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if user account is not present.
     */
    public AccessControlList getACL(User user)
        throws DataBackendException, UnknownEntityException
    {
        if (!TurbineSecurity.accountExists(user))
        {
            throw new UnknownEntityException("The account '" +
                                             user.getUserName() + "' does not exist");
        }
        try
        {
            Hashtable roles = new Hashtable();
            Hashtable permissions = new Hashtable();
            // notify the state modifiers (writers) that we want to create the
            // snapshot.
            lockShared();

            // construct the snapshot:

            // foreach group in the system
            Iterator groupsIterator = getAllGroups().elements();
            while (groupsIterator.hasNext())
            {
                Group group = (Group) groupsIterator.next();
                // get roles of user in the group
                RoleSet groupRoles = RolePeerManager.retrieveSet(user, group);
                // put the Set into roles(group)
                roles.put(group, groupRoles);
                // collect all permissions in this group
                PermissionSet groupPermissions = new PermissionSet();
                // foreach role in Set
                Iterator rolesIterator = groupRoles.elements();
                while (rolesIterator.hasNext())
                {
                    Role role = (Role) rolesIterator.next();
                    // get permissions of the role
                    PermissionSet rolePermissions =
                        PermissionPeerManager.retrieveSet(role);
                    groupPermissions.add(rolePermissions);
                }
                // put the Set into permissions(group)
                permissions.put(group, groupPermissions);
            }
            return getAclInstance(roles, permissions);
        }
        catch (Exception e)
        {
            throw new DataBackendException("Failed to build ACL for user '" +
                                           user.getUserName() + "'" , e);
        }
        finally
        {
            // notify the state modifiers that we are done creating the snapshot.
            unlockShared();
        }
    }

    /*-----------------------------------------------------------------------
      Security management
      -----------------------------------------------------------------------*/

    /**
     * Grant an User a Role in a Group.
     *
     * @param user the user.
     * @param group the group.
     * @param role the role.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if user account, group or role is not
     *         present.
     */
    public synchronized void grant(User user, Group group, Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean userExists = false;
        boolean groupExists = false;
        boolean roleExists = false;
        try
        {
            lockExclusive();
            userExists = TurbineSecurity.accountExists(user);
            groupExists = checkExists(group);
            roleExists = checkExists(role);
            if (userExists && groupExists && roleExists)
            {
                Criteria criteria = new Criteria();
                criteria.add(TurbineUserGroupRolePeer.USER_ID,
                             ((Persistent) user).getPrimaryKey());
                criteria.add(TurbineUserGroupRolePeer.GROUP_ID,
                             ((Persistent) group).getPrimaryKey());
                criteria.add(TurbineUserGroupRolePeer.ROLE_ID,
                             ((Persistent) role).getPrimaryKey());
                TurbineUserGroupRolePeer.doInsert(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("grant(User,Group,Role) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        if (!userExists)
        {
            throw new UnknownEntityException("Unknown user '" +
                                             user.getUserName() + "'");
        }
        if (!groupExists)
        {
            throw new UnknownEntityException("Unknown group '" +
                                             group.getName() + "'");
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" +
                                             role.getName() + "'");
        }
    }

    /**
     * Revoke a Role in a Group from an User.
     *
     * @param user the user.
     * @param group the group.
     * @param role the role.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if user account, group or role is not
     *         present.
     */
    public synchronized void revoke(User user, Group group, Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean userExists = false;
        boolean groupExists = false;
        boolean roleExists = false;
        try
        {
            lockExclusive();
            userExists = TurbineSecurity.accountExists(user);
            groupExists = checkExists(group);
            roleExists = checkExists(role);
            if (userExists && groupExists && roleExists)
            {
                Criteria criteria = new Criteria();
                criteria.add(TurbineUserGroupRolePeer.USER_ID,
                             ((Persistent) user).getPrimaryKey());
                criteria.add(TurbineUserGroupRolePeer.GROUP_ID,
                             ((Persistent) group).getPrimaryKey());
                criteria.add(TurbineUserGroupRolePeer.ROLE_ID,
                             ((Persistent) role).getPrimaryKey());
                TurbineUserGroupRolePeer.doDelete(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("revoke(User,Role,Group) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        if (!userExists)
        {
            throw new UnknownEntityException("Unknown user '" +
                                             user.getUserName() + "'");
        }
        if (!groupExists)
        {
            throw new UnknownEntityException("Unknown group '" +
                                             group.getName() + "'");
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" +
                                             role.getName() + "'");
        }
    }

    /**
     * Revokes all roles from an User.
     *
     * This method is used when deleting an account.
     *
     * @param user the User.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the account is not present.
     */
    public synchronized void revokeAll(User user)
        throws DataBackendException, UnknownEntityException
    {
        boolean userExists = false;
        try
        {
            lockExclusive();
            userExists = TurbineSecurity.accountExists(user);
            if (userExists)
            {
                // The following would not work, due to an annoying misfeature
                // of Village.
                // Village allows only a single row to be deleted at a time.
                // I wish that it was possible to disable this behaviour!

                // Criteria criteria = new Criteria();
                // criteria.add(UserGroupRolePeer.USER_ID,
                //           ((Persistent) user).getPrimaryKey());
                // UserGroupRolePeer.doDelete(criteria);
                ObjectKey key = ((Persistent)  user).getPrimaryKey();
                TurbineUserGroupRolePeer.deleteAll(
                    TurbineUserGroupRolePeer.TABLE_NAME,
                    TurbineUserGroupRolePeer.USER_ID,
                    ((BigDecimal) key.getValue()).intValue());
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("revokeAll(User) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown user '" +
                                         user.getUserName() + '\'');
    }

    /**
     * Grants a Role a Permission
     *
     * @param role the Role.
     * @param permission the Permission.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if role or permission is not present.
     */
    public synchronized void grant(Role role, Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        boolean permissionExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            permissionExists = checkExists(permission);
            if (roleExists && permissionExists)
            {
                Criteria criteria = new Criteria();
                criteria.add(TurbineRolePermissionPeer.ROLE_ID,
                             ((Persistent) role).getPrimaryKey());
                criteria.add(TurbineRolePermissionPeer.PERMISSION_ID,
                             ((Persistent) permission).getPrimaryKey());
                TurbineUserGroupRolePeer.doInsert(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("grant(Role,Permission) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" +
                                             role.getName() + "'");
        }
        if (!permissionExists)
        {
            throw new UnknownEntityException("Unknown permission '" +
                                             permission.getName() + "'");
        }
    }

    /**
     * Revokes a Permission from a Role.
     *
     * @param role the Role.
     * @param permission the Permission.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if role or permission is not present.
     */
    public synchronized void revoke(Role role, Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        boolean permissionExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            permissionExists = checkExists(permission);
            if (roleExists && permissionExists)
            {
                Criteria criteria = new Criteria();
                criteria.add(TurbineRolePermissionPeer.ROLE_ID,
                             ((Persistent) role).getPrimaryKey());
                criteria.add(TurbineRolePermissionPeer.PERMISSION_ID,
                             ((Persistent) permission).getPrimaryKey());
                TurbineRolePermissionPeer.doDelete(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("revoke(Role,Permission) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        if (!roleExists)
        {
            throw new UnknownEntityException("Unknown role '" +
                                             role.getName() + "'");
        }
        if (!permissionExists)
        {
            throw new UnknownEntityException("Unknown permission '" +
                                             permission.getName() + "'");
        }
    }

    /**
     * Revokes all permissions from a Role.
     *
     * This method is user when deleting a Role.
     *
     * @param role the Role
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws  UnknownEntityException if the Role is not present.
     */
    public synchronized void revokeAll(Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            if (roleExists)
            {
                // The following would not work, due to an annoying misfeature
                // of Village.
                // see revokeAll(user)

                // Criteria criteria = new Criteria();
                // criteria.add(RolePermissionPeer.ROLE_ID, role.getPrimaryKey());
                // RolePermissionPeer.doDelete(criteria);

                ObjectKey key = ((Persistent) role).getPrimaryKey();
                TurbineRolePermissionPeer.deleteAll(
                    TurbineRolePermissionPeer.TABLE_NAME,
                    TurbineRolePermissionPeer.ROLE_ID,
                    ((BigDecimal) key.getValue()).intValue());
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("revokeAll(Role) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown role '" +
                                         role.getName() + "'");
    }

    /*-----------------------------------------------------------------------
      Group/Role/Permission management
      -----------------------------------------------------------------------*/

    /**
     * Retrieve a set of Groups that meet the specified Criteria.
     *
     * @param criteria A Criteria of Group selection.
     * @return a set of Groups that meet the specified Criteria.
     *
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     */
    public GroupSet getGroups(Criteria criteria)
        throws DataBackendException
    {
        Criteria dbCriteria = new Criteria();
        Iterator keys = criteria.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            dbCriteria.put(GroupPeerManager.getColumnName(key),
                           criteria.get(key));
        }
        List groups = new ArrayList(0);
        try
        {
            groups = GroupPeerManager.doSelect(criteria);
        }
        catch (Exception e)
        {
            throw new DataBackendException("getGroups(Criteria) failed", e);
        }
        return new GroupSet(groups);
    }

    /**
     * Retrieve a set of Roles that meet the specified Criteria.
     *
     * @param criteria A Criteria of Roles selection.
     * @return a set of Roles that meet the specified Criteria.
     *
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     */
    public RoleSet getRoles(Criteria criteria)
        throws DataBackendException
    {
        Criteria dbCriteria = new Criteria();
        Iterator keys = criteria.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            dbCriteria.put(RolePeerManager.getColumnName(key),
                           criteria.get(key));
        }
        List roles = new ArrayList(0);
        try
        {
            roles = RolePeerManager.doSelect(criteria);
        }
        catch (Exception e)
        {
            throw new DataBackendException("getRoles(Criteria) failed", e);
        }
        return new RoleSet(roles);
    }

    /**
     * Retrieve a set of Permissions that meet the specified Criteria.
     *
     * @param criteria A Criteria of Permissions selection.
     * @return a set of Permissions that meet the specified Criteria.
     *
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     */
    public PermissionSet getPermissions(Criteria criteria)
        throws DataBackendException
    {
        Criteria dbCriteria = new Criteria();
        Iterator keys = criteria.keySet().iterator();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            dbCriteria.put(PermissionPeerManager.getColumnName(key),
                           criteria.get(key));
        }
        List permissions = new ArrayList(0);
        try
        {
            permissions = PermissionPeerManager.doSelect(criteria);
        }
        catch (Exception e)
        {
            throw new DataBackendException("getPermissions(Criteria) failed", e);
        }
        return new PermissionSet(permissions);
    }

    /**
     * Retrieves all permissions associated with a role.
     *
     * @param role the role name, for which the permissions are to be retrieved.
     *
     * @return A Permission set for the Role.
     *
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the role is not present.
     */
    public PermissionSet getPermissions(Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        try
        {
            lockShared();
            roleExists = checkExists(role);
            if (roleExists)
            {
                return PermissionPeerManager.retrieveSet(role);
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("getPermissions(Role) failed", e);
        }
        finally
        {
            unlockShared();
        }
        throw new UnknownEntityException("Unknown role '" +
                                         role.getName() + "'");
    }

    /**
     * Stores Group's attributes. The Groups is required to exist in the system.
     *
     * @param group The Group to be stored.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public void saveGroup(Group group)
        throws DataBackendException, UnknownEntityException
    {
        boolean groupExists = false;
        try
        {
            groupExists = checkExists(group);
            if (groupExists)
            {
                Criteria criteria = GroupPeerManager.buildCriteria(group);
                GroupPeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("saveGroup(Group) failed" , e);
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
    }

    /**
     * Stores Role's attributes. The Roles is required to exist in the system.
     *
     * @param role The Role to be stored.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the role does not exist.
     */
    public void saveRole(Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        try
        {
            roleExists = checkExists(role);
            if (roleExists)
            {
                Criteria criteria = RolePeerManager.buildCriteria(role);
                RolePeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("saveRole(Role) failed", e);
        }
        throw new UnknownEntityException("Unknown role '" + role + "'");
    }

    /**
     * Stores Permission's attributes. The Permissions is required to exist in
     * the system.
     *
     * @param permission The Permission to be stored.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the permission does not exist.
     */
    public void savePermission(Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean permissionExists = false;
        try
        {
            permissionExists = checkExists(permission);
            if (permissionExists)
            {
                Criteria criteria = PermissionPeerManager.buildCriteria(permission);
                PermissionPeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("savePermission(Permission) failed", e);
        }
        throw new UnknownEntityException("Unknown permission '" + permission + "'");
    }

    /**
     * @deprecated Use getGroupInstance(String name) instead.
     */
    public Group getNewGroup(String groupName)
    {
        try
        {
            return getGroupInstance(groupName);
        }
        catch (UnknownEntityException uee)
        {
            uee.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated Use getRoleInstance(String name) instead.
     */
    public Role getNewRole(String roleName)
    {
        try
        {
            return getRoleInstance(roleName);
        }
        catch (UnknownEntityException uee)
        {
            return null;
        }
    }

    /**
     * @deprecated Use getPermissionInstance(String name) instead.
     */
    public Permission getNewPermission(String permissionName)
    {
        try
        {
            return getPermissionInstance(permissionName);
        }
        catch (UnknownEntityException uee)
        {
            return null;
        }
    }

    /**
     * Creates a new group with specified attributes.
     *
     * @param group the object describing the group to be created.
     * @return a new Group object that has id set up properly.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws EntityExistsException if the group already exists.
     */
    public synchronized Group addGroup(Group group)
        throws DataBackendException, EntityExistsException
    {
        boolean groupExists = false;
        try
        {
            lockExclusive();
            groupExists = checkExists(group);
            if (!groupExists)
            {
                // add a row to the table
                Criteria criteria = GroupPeerManager.buildCriteria(group);
                GroupPeerManager.doInsert(criteria);
                // try to get the object back using the name as key.
                criteria = new Criteria();
                criteria.add(GroupPeerManager.getNameColumn(),
                             group.getName());
                List results = GroupPeerManager.doSelect(criteria);
                if (results.size() != 1)
                {
                    throw new DataBackendException(
                        "Internal error - query returned " +
                        results.size() + " rows");
                }
                Group newGroup = (Group) results.get(0);
                // add the group to system-wide cache
                getAllGroups().add(newGroup);
                // return the object with correct id
                return newGroup;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("addGroup(Group) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        // the only way we could get here without return/throw tirggered
        // is that the groupExists was true.
        throw new EntityExistsException("Group '" + group +
                                        "' already exists");
    }

    /**
     * Creates a new role with specified attributes.
     *
     * @param role the object describing the role to be created.
     * @return a new Role object that has id set up properly.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws EntityExistsException if the role already exists.
     */
    public synchronized Role addRole(Role role)
        throws DataBackendException, EntityExistsException
    {
        boolean roleExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            if (!roleExists)
            {
                // add a row to the table
                Criteria criteria = RolePeerManager.buildCriteria(role);
                RolePeerManager.doInsert(criteria);
                // try to get the object back using the name as key.
                criteria = new Criteria();
                criteria.add(RolePeerManager.getNameColumn(), role.getName());
                List results = RolePeerManager.doSelect(criteria);
                if (results.size() != 1)
                {
                    throw new DataBackendException(
                        "Internal error - query returned " +
                        results.size() + " rows");
                }
                Role newRole = (Role) results.get(0);
                // add the role to system-wide cache
                getAllRoles().add(newRole);
                // return the object with correct id
                return newRole;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("addRole(Role) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        // the only way we could get here without return/throw tirggered
        // is that the roleExists was true.
        throw new EntityExistsException("Role '" + role + "' already exists");
    }

    /**
     * Creates a new permission with specified attributes.
     *
     * @param permission the object describing the permission to be created.
     * @return a new Permission object that has id set up properly.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws EntityExistsException if the permission already exists.
     */
    public synchronized Permission addPermission(Permission permission)
        throws DataBackendException, EntityExistsException
    {
        boolean permissionExists = false;
        try
        {
            lockExclusive();
            permissionExists = checkExists(permission);
            if (!permissionExists)
            {
                // add a row to the table
                Criteria criteria = PermissionPeerManager.buildCriteria(permission);
                PermissionPeerManager.doInsert(criteria);
                // try to get the object back using the name as key.
                criteria = new Criteria();
                criteria.add(PermissionPeerManager.getNameColumn(),
                             permission.getName());
                List results = PermissionPeerManager.doSelect(criteria);
                if (results.size() != 1)
                {
                    throw new DataBackendException(
                        "Internal error - query returned " +
                        results.size() + " rows");
                }
                Permission newPermission = (Permission) results.get(0);
                // add the permission to system-wide cache
                getAllPermissions().add(newPermission);
                // return the object with correct id
                return newPermission;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("addPermission(Permission) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        // the only way we could get here without return/throw tirggered
        // is that the permissionExists was true.
        throw new EntityExistsException("Permission '" + permission +
                                        "' already exists");
    }

    /**
     * Removes a Group from the system.
     *
     * @param group The object describing group to be removed.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public synchronized void removeGroup(Group group)
        throws DataBackendException, UnknownEntityException
    {
        boolean groupExists = false;
        try
        {
            lockExclusive();
            groupExists = checkExists(group);
            if (groupExists)
            {
                Criteria criteria = GroupPeerManager.buildCriteria(group);
                GroupPeerManager.doDelete(criteria);
                getAllGroups().remove(group);
                return;
            }
        }
        catch (Exception e)
        {
            category.error("Failed to delete a Group");
            category.error(e);
            throw new DataBackendException("removeGroup(Group) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
    }

    /**
     * Removes a Role from the system.
     *
     * @param role The object describing role to be removed.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the role does not exist.
     */
    public synchronized void removeRole(Role role)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            if (roleExists)
            {
                // revoke all permissions from the role to be deleted
                revokeAll(role);
                Criteria criteria = RolePeerManager.buildCriteria(role);
                RolePeerManager.doDelete(criteria);
                getAllRoles().remove(role);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("removeRole(Role)" , e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown role '" + role + "'");
    }

    /**
     * Removes a Permission from the system.
     *
     * @param permission The object describing permission to be removed.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the permission does not exist.
     */
    public synchronized void removePermission(Permission permission)
        throws DataBackendException, UnknownEntityException
    {
        boolean permissionExists = false;
        try
        {
            lockExclusive();
            permissionExists = checkExists(permission);
            if (permissionExists)
            {
                Criteria criteria = PermissionPeerManager.buildCriteria(permission);
                PermissionPeerManager.doDelete(criteria);
                getAllPermissions().remove(permission);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("removePermission(Permission)" , e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown permission '" +
                                         permission + "'");
    }

    /**
     * Renames an existing Group.
     *
     * @param group The object describing the group to be renamed.
     * @param name the new name for the group.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public synchronized void renameGroup(Group group, String name)
        throws DataBackendException, UnknownEntityException
    {
        boolean groupExists = false;
        try
        {
            lockExclusive();
            groupExists = checkExists(group);
            if (groupExists)
            {
                group.setName(name);
                Criteria criteria = GroupPeerManager.buildCriteria(group);
                GroupPeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("renameGroup(Group,String)" , e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
    }

    /**
     * Renames an existing Role.
     *
     * @param role The object describing the role to be renamed.
     * @param name the new name for the role.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the role does not exist.
     */
    public synchronized void renameRole(Role role, String name)
        throws DataBackendException, UnknownEntityException
    {
        boolean roleExists = false;
        try
        {
            lockExclusive();
            roleExists = checkExists(role);
            if (roleExists)
            {
                role.setName(name);
                Criteria criteria = RolePeerManager.buildCriteria(role);
                RolePeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("renameRole(Role,String)" , e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown role '" + role + "'");
    }

    /**
     * Renames an existing Permission.
     *
     * @param permission The object describing the permission to be renamed.
     * @param name the new name for the permission.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the permission does not exist.
     */
    public synchronized void renamePermission(Permission permission, String name)
        throws DataBackendException, UnknownEntityException
    {
        boolean permissionExists = false;
        try
        {
            lockExclusive();
            permissionExists = checkExists(permission);
            if (permissionExists)
            {
                permission.setName(name);
                Criteria criteria = PermissionPeerManager.buildCriteria(permission);
                PermissionPeerManager.doUpdate(criteria);
                return;
            }
        }
        catch (Exception e)
        {
            throw new DataBackendException("renamePermission(Permission,name)", e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown permission '" +
                                         permission + "'");
    }

    /** Service specific implementation methods */

    /**
     * Determines if the <code>Group</code> exists in the security system.
     *
     * @param group a <code>Group</code> value
     * @return true if the group exists in the system, false otherwise
     * @throws DataBackendException when more than one Group with
     *         the same name exists.
     * @throws Exception A generic exception.
     */
    protected boolean checkExists(Group group)
        throws DataBackendException, Exception
    {
        return GroupPeerManager.checkExists(group);
    }

    /**
     * Determines if the <code>Role</code> exists in the security system.
     *
     * @param role a <code>Role</code> value
     * @return true if the role exists in the system, false otherwise
     * @throws DataBackendException when more than one Role with
     *         the same name exists.
     * @throws Exception A generic exception.
     */
    protected boolean checkExists(Role role)
        throws DataBackendException, Exception
    {
        return RolePeerManager.checkExists(role);
    }

    /**
     * Determines if the <code>Permission</code> exists in the security system.
     *
     * @param permission a <code>Permission</code> value
     * @return true if the permission exists in the system, false otherwise
     * @throws DataBackendException when more than one Permission with
     *         the same name exists.
     * @throws Exception A generic exception.
     */
    protected boolean checkExists(Permission permission)
        throws DataBackendException, Exception
    {
        return PermissionPeerManager.checkExists(permission);
    }

}