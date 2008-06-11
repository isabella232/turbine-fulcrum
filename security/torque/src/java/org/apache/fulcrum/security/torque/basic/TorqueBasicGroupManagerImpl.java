package org.apache.fulcrum.security.torque.basic;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.sql.Connection;
import java.util.List;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.torque.TorqueAbstractGroupManager;
import org.apache.fulcrum.security.torque.om.TorqueBasicGroupPeer;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
/**
 * This implementation persists to a database via Torque.
 *
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id:$
 */
public class TorqueBasicGroupManagerImpl extends TorqueAbstractGroupManager
{
    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager#doSelectAllGroups(java.sql.Connection)
     */
    protected List doSelectAllGroups(Connection con)
        throws TorqueException
    {
        Criteria criteria = new Criteria(TorqueBasicGroupPeer.DATABASE_NAME);

        return TorqueBasicGroupPeer.doSelect(criteria, con);
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager#doSelectById(java.lang.Integer, java.sql.Connection)
     */
    protected Group doSelectById(Integer id, Connection con)
        throws NoRowsException, TooManyRowsException, TorqueException
    {
        return TorqueBasicGroupPeer.retrieveByPK(id, con);
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager#doSelectByName(java.lang.String, java.sql.Connection)
     */
    protected Group doSelectByName(String name, Connection con)
        throws NoRowsException, TooManyRowsException, TorqueException
    {
        Criteria criteria = new Criteria(TorqueBasicGroupPeer.DATABASE_NAME);
        criteria.add(TorqueBasicGroupPeer.GROUP_NAME, name);
        criteria.setIgnoreCase(true);
        criteria.setSingleRecord(true);

        List groups = TorqueBasicGroupPeer.doSelect(criteria, con);

        if (groups.isEmpty())
        {
            throw new NoRowsException(name);
        }

        return (Group)groups.get(0);
    }
}