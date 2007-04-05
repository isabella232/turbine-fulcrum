package org.apache.fulcrum.security.torque.basic;
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
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.model.basic.entity.BasicGroup;
import org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity;
import org.apache.fulcrum.security.torque.om.TorqueBasicGroupPeer;
import org.apache.fulcrum.security.torque.om.TorqueBasicUser;
import org.apache.fulcrum.security.torque.om.TorqueBasicUserGroup;
import org.apache.fulcrum.security.torque.om.TorqueBasicUserGroupPeer;
import org.apache.fulcrum.security.util.UserSet;
import org.apache.torque.TorqueException;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;
/**
 * This abstract class provides the SecurityInterface to the managers.
 *
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id:$
 */
public abstract class TorqueAbstractBasicGroup extends TorqueAbstractSecurityEntity
    implements BasicGroup
{
    /** a cache of user objects */
    private Set userSet = null;
    
    /**
     * Forward reference to generated code
     * 
     * Get a list of association objects, pre-populated with their TorqueBasicUser 
     * objects.
     * 
     * @param criteria Criteria to define the selection of records
     * @throws TorqueException
     * 
     * @return a list of User/Group relations
     */
    protected abstract List getTorqueBasicUserGroupsJoinTorqueBasicUser(Criteria criteria) 
        throws TorqueException;

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#addUser(org.apache.fulcrum.security.entity.User)
     */
    public void addUser(User user)
    {
        getUsers().add(user);
    }

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#getUsers()
     */
    public UserSet getUsers()
    {
        if (userSet == null)
        {
            userSet = new UserSet();
        }
        else if(!(userSet instanceof UserSet))
        {
            userSet = new UserSet(userSet);
        }

        return (UserSet)userSet;
    }

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#getUsersAsSet()
     */
    public Set getUsersAsSet()
    {
        return userSet;
    }

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#removeUser(org.apache.fulcrum.security.entity.User)
     */
    public void removeUser(User user)
    {
        getUsers().remove(user);
    }

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#setUsers(org.apache.fulcrum.security.util.UserSet)
     */
    public void setUsers(UserSet userSet)
    {
        if(userSet != null)
        {
            this.userSet = userSet;
        }
        else
        {
            this.userSet = new UserSet();
        }
    }

    /**
     * @see org.apache.fulcrum.security.model.basic.entity.BasicGroup#setUsersAsSet(java.util.Set)
     */
    public void setUsersAsSet(Set users)
    {
        setUsers(new UserSet(users));
    }
    
    /**
     * Retrieve attached objects such as users, permissions,....
     */
    public void retrieveAttachedObjects(Connection con) throws TorqueException
    {
        this.userSet = new UserSet();
        
        // the generated method that allows a Connection parameter is missing
        List usergroups = getTorqueBasicUserGroupsJoinTorqueBasicUser(new Criteria());

        for (Iterator i = usergroups.iterator(); i.hasNext();)
        {
            TorqueBasicUserGroup tbug = (TorqueBasicUserGroup)i.next(); 
            userSet.add(tbug.getTorqueBasicUser());
        }
    }
    
    /**
     * Update this instance to the database with all dependend objects
     * 
     * @param con A database connection 
     */
    public void update(Connection con) throws TorqueException
    {
        if (userSet != null)
        {
            Criteria criteria = new Criteria();
            
            /* remove old entries */
            criteria.add(TorqueBasicUserGroupPeer.GROUP_ID, getEntityId());
            TorqueBasicUserGroupPeer.doDelete(criteria, con);

            for (Iterator i = userSet.iterator(); i.hasNext();)
            {
                TorqueBasicUser user = (TorqueBasicUser)i.next();

                TorqueBasicUserGroup ug = new TorqueBasicUserGroup();
                ug.setUserId(user.getEntityId());
                ug.setGroupId(getEntityId());
                ug.save(con);
            }
        }
        
        try
        {
            save(con);
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
    }

    /**
     * Get the name of the connnection pool associated to this object
     * 
     * @return the logical Torque database name 
     */
    public String getDatabaseName()
    {
        return TorqueBasicGroupPeer.DATABASE_NAME;
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity#delete()
     */
    public void delete() throws TorqueException
    {
        TorqueBasicGroupPeer.doDelete(SimpleKey.keyFor(getEntityId()));
    }
}