package org.apache.fulcrum.security.impl.db.entity;

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

// JDK classes
import java.util.Vector;
import org.apache.fulcrum.security.impl.db.entity.map.TurbineGroupMapBuilder;
import org.apache.torque.util.BasePeer;
import org.apache.torque.pool.DBConnection;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.SecurityEntity;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.DataBackendException;

/**
 * This class handles all the database access for the Group table.
 * This table contains all the Groups that a given member can play.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id$
 */
public class TurbineGroupPeer
    extends org.apache.fulcrum.security.impl.db.entity.BaseTurbineGroupPeer
{
    /** The column name for the name field. */
    public static final String NAME = GROUP_NAME;

    /**
     * Returns the full name of a column.
     *
     * @return A String with the full name of the column.
     */
    public static String getColumnName(String name)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(TurbineGroupMapBuilder.getTable());
        sb.append(".");
        sb.append(name);
        return sb.toString();
    }

    /**
     * Checks if a Group is defined in the system. The name
     * is used as query criteria.
     *
     * @param permission The Group to be checked.
     * @return <code>true</code> if given Group exists in the system.
     * @throws DataBackendException when more than one Group with
     *         the same name exists.
     * @throws Exception, a generic exception.
     */
    public static boolean checkExists(Group group)
        throws DataBackendException, Exception
    {
        Criteria criteria = new Criteria();
        criteria.addSelectColumn(GROUP_ID);
        criteria.add(NAME, ((SecurityEntity)group).getName());
        Vector results = BasePeer.doSelect(criteria);
        if(results.size() > 1)
        {
            throw new DataBackendException("Multiple groups named '" +
                ((TurbineGroup)group).getName() + "' exist!");
        }
        return (results.size()==1);
    }

    /**
     * Get the name of this table.
     *
     * @return A String with the name of the table.
     */
    public static String getTableName()
    {
        return TABLE_NAME;
    }

    /**
     * Builds a criteria object based upon an Group object
     */
    public static Criteria buildCriteria(Group group) {
        return BaseTurbineGroupPeer.buildCriteria((TurbineGroup)group);
    }

    /**
     * Retrieves/assembles a GroupSet of all of the Groups.
     *
     * @param criteria The criteria to use.
     * @return A GroupSet.
     * @exception Exception, a generic exception.
     */
    public static GroupSet retrieveSet() throws Exception
    {
        return retrieveSet(new Criteria());
    }

    /**
     * Retrieves/assembles a GroupSet based on the Criteria passed in
     */
    public static GroupSet retrieveSet(Criteria criteria) throws Exception
    {
        Vector results = doSelect(criteria);
        GroupSet rs = new GroupSet();
        for (int i = 0; i < results.size(); i++)
        {
            rs.add((Group)results.elementAt(i));
        }
        return rs;
    }

}
