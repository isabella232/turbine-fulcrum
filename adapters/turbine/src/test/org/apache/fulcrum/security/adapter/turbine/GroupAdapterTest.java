package org.apache.fulcrum.security.adapter.turbine;
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

import junit.framework.TestCase;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.model.dynamic.entity.DynamicGroup;
/**
 * Test that we can use a GroupAdapter with a Group that has 
 * various types of Id objects.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class GroupAdapterTest extends TestCase
{
    /**
     * Constructor for GroupAdapterTest.
     * @param arg0
     */
    public GroupAdapterTest(String arg0)
    {
        super(arg0);
    }
    public void testWithInteger()
    {
        Group group = new DynamicGroup();
        group.setId(new Integer(56));
        GroupAdapter ga = new GroupAdapter(group);
        assertEquals(56, ga.getId());
        assertEquals(new Integer(56), ga.getIdAsObj());

    }
    public void testWithLong()
    {
        Group group = new DynamicGroup();
        group.setId(new Long(56));
        GroupAdapter ga = new GroupAdapter(group);
        assertEquals(56, ga.getId());
        assertEquals(new Integer(56), ga.getIdAsObj());

    }
    public void testWithString()
    {
        Group group = new DynamicGroup();
        group.setId("56");
        GroupAdapter ga = new GroupAdapter(group);
        assertEquals(56, ga.getId());
        assertEquals(new Integer(56), ga.getIdAsObj());

    }
}
