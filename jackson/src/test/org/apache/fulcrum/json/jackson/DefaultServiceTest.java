package org.apache.fulcrum.json.jackson;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fulcrum.json.JsonService;
import org.apache.fulcrum.json.TestClass;
import org.apache.fulcrum.testcontainer.BaseUnitTest;


/**
 * Jackson 2 JSON Test
 * 
 * @author gk
 * @version $Id$
 */
public class DefaultServiceTest extends BaseUnitTest {
    private JsonService sc = null;
    private final String preDefinedOutput = "{\"container\":{\"cf\":\"Config.xml\"},\"configurationName\":\"Config.xml\",\"name\":\"mytest\"}";

    /**
     * Constructor for test.
     * 
     * @param testName
     *            name of the test being executed
     */
    public DefaultServiceTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        super.setUp();
        sc = (JsonService) this.lookup(JsonService.ROLE);
    }

    public void testSerialize() throws Exception {
        String serJson = sc.ser(new TestClass("mytest"));
        assertEquals("Serialization failed ", preDefinedOutput, serJson);
    }

    public void testSerializeExcludeNothing() throws Exception {
        String serJson = sc.serializeAllExceptFilter(new TestClass("mytest"),
                (Class) null, (String[]) null);
        assertEquals(
                "Serialization failed ",
                "{\"container\":{\"cf\":\"Config.xml\"},\"configurationName\":\"Config.xml\",\"name\":\"mytest\"}",
                serJson);
    }


    public void ignoreTestSerializeExcludeClass() throws Exception {
        // jackson 1 could yet not exclude classes 
    }

    public void ignoreTestSerializeExcludeClassAndField() throws Exception {
     // no deep exclude
    }

    public void ignoreTestSerializeExcludeClassAndFields() throws Exception {
        // no deep exclude
    }

    public void ignoreTestSerializeExcludeField() throws Exception {

        // no deep ignore??
    }

    public void testSerializeDate() throws Exception {
        final SimpleDateFormat MMddyyyy = new SimpleDateFormat("MM/dd/yyyy");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("date", Calendar.getInstance().getTime());

        sc.setDateFormat(MMddyyyy);
        String serJson = sc.ser(map);
        System.out.println("serJson:" + serJson);
        assertTrue("Serialize with Adapter failed ",
                serJson.matches("\\{\"date\":\"\\d\\d/\\d\\d/\\d{4}\"\\}"));
    }

    // jackson serializes size too
    public void testSerializeCollection() throws Exception {
        List<org.apache.fulcrum.json.Rectangle> rectList = new ArrayList<org.apache.fulcrum.json.Rectangle>();
        for (int i = 0; i < 10; i++) {
            org.apache.fulcrum.json.Rectangle filteredRect = new org.apache.fulcrum.json.Rectangle(i, i, "rect" + i);
            rectList.add(filteredRect);
        }
        String adapterSer = sc.ser(rectList);
        assertEquals(
                "collect ser",
                "[{'w':0,'h':0,'name':'rect0','size':0},{'w':1,'h':1,'name':'rect1','size':1},{'w':2,'h':2,'name':'rect2','size':4},{'w':3,'h':3,'name':'rect3','size':9},{'w':4,'h':4,'name':'rect4','size':16},{'w':5,'h':5,'name':'rect5','size':25},{'w':6,'h':6,'name':'rect6','size':36},{'w':7,'h':7,'name':'rect7','size':49},{'w':8,'h':8,'name':'rect8','size':64},{'w':9,'h':9,'name':'rect9','size':81}]",
                adapterSer.replace('"', '\''));
    }
    
    public void testSerializationCollectioPrimitiveWrapper() throws Exception {

        List<Integer> intList = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            Integer integer = new Integer(i*i);
            intList.add(integer);
        }
        String result = sc.serializeOnlyFilter(intList, Integer.class, null);
        assertEquals(
                "Serialization of beans failed ",
                "[0,1,4,9,16,25,36,49,64,81]",
                result);
    }

//    public void testSerializeTypeAdapterForCollection() throws Exception {
//        TestSerializer tser = new TestSerializer();
//        CustomModuleWrapper<List> cmw = new CustomModuleWrapper<List>(tser, null);
//        sc.addAdapter("Collection Adapter", ArrayList.class, cmw);
//        List<Rectangle> rectList = new ArrayList<Rectangle>();
//        for (int i = 0; i < 10; i++) {
//            Rectangle filteredRect = new Rectangle(i, i, "rect" + i);
//            rectList.add(filteredRect);
//        }
//        String adapterSer = sc.ser(rectList);
//        assertEquals(
//                "collect ser",
//                "{'rect0':0,'rect1':1,'rect2':4,'rect3':9,'rect4':16,'rect5':25,'rect6':36,'rect7':49,'rect8':64,'rect9':81}",
//                adapterSer.replace('"', '\''));
//    }
    
//    public void testMixinAdapter() throws Exception {
//        TestJsonSerializer tser = new TestJsonSerializer();
//        CustomModuleWrapper<TestClass> cmw = new CustomModuleWrapper<TestClass>(
//                tser, null);
//        sc.addAdapter("Collection Adapter", TestClass.class, cmw);
//        String adapterSer = sc.ser(new TestClass("mytest"));
//        assertEquals("failed adapter serialization:",
//                "{\"n\":\"mytest\",\"p\":\"Config.xml\",\"c\":[]}", adapterSer);
//    }
    
    public void testDeSerialize() throws Exception {
        String serJson = sc.ser(new TestClass("mytest"));
        Object deson = sc.deSer(serJson, TestClass.class);
        assertEquals("Serialization failed ", TestClass.class, deson.getClass());
    }

    
    public void testDeserializationCollection() throws Exception {
        List<Rectangle> rectList = new ArrayList<Rectangle>(); 
        for (int i = 0; i < 10; i++) {
            Rectangle filteredRect = new Rectangle(i, i, "rect" + i);
            rectList.add(filteredRect);
        }
        String serColl = sc.ser(rectList);
        // we need to set size to Ignore -> UnrecognizedPropertyException
        Collection<Rectangle> resultList0 =  sc.deSerCollection(serColl, new ArrayList<Rectangle>(), Rectangle.class);
        for (int i = 0; i < 10; i++) {
            Rectangle rect = ((List<Rectangle>)resultList0)
                            .get(i);
            assertEquals("deser reread size failed", (i * i), rect.getSize());
        }
    }

    public void testDeserializationTypeAdapterForCollection() throws Exception {
        // not implemented
    }
    
    public void testSerializeWithMixinAndFilter() throws Exception {
        Bean filteredBean = new Bean();
        filteredBean.setName("joe");
        //
        sc.addAdapter("M4RBeanMixin", Bean.class,
                BeanMixin.class);
        // profession was already set to ignore, does not change
        String bean = sc.serializeOnlyFilter(filteredBean, Bean.class, "profession");
        assertEquals(
                "Ser filtered Bean failed ",
                "{}",
                bean);
    }
      
    public void testSerializeWithOnlyFilter() throws Exception {
        // no deep include ??
        Bean filteredBean = new Bean();
        filteredBean.setName("joe");
        String bean = sc.serializeOnlyFilter(filteredBean, Bean.class, "profession");
        assertEquals(
                "Ser filtered Bean failed ",
                "{\"profession\":null}",
                bean);
    }

}