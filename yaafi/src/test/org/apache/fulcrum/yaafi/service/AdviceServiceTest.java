package org.apache.fulcrum.yaafi.service;

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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.fulcrum.yaafi.DependentTestComponent;
import org.apache.fulcrum.yaafi.framework.container.ServiceContainer;
import org.apache.fulcrum.yaafi.framework.factory.ServiceContainerConfiguration;
import org.apache.fulcrum.yaafi.framework.factory.ServiceContainerFactory;
import org.apache.fulcrum.yaafi.interceptor.logging.LoggingInterceptorService;
import org.apache.fulcrum.yaafi.service.advice.AdviceService;

/**
 * Test suite for the ServiceManagereService.
 *
 * @author <a href="mailto:siegfried.goeschl@it20one.at">Siegfried Goeschl</a>
 */

public class AdviceServiceTest extends TestCase implements DependentTestComponent
{
    private AdviceService service;
    private DependentTestComponent advisedThis;
    private ServiceContainer container;

    
    /**
     * Constructor
     * @param name the name of the test case
     */
    public AdviceServiceTest( String name )
    {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ServiceContainerConfiguration config = new ServiceContainerConfiguration();
        config.loadContainerConfiguration( "./src/test/TestYaafiContainerConfig.xml" );
        this.container = ServiceContainerFactory.create( config );
        service = (AdviceService) this.container.lookup(AdviceService.class.getName());
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        ServiceContainerFactory.dispose(this.container);
        super.tearDown();
    }

    public static Test suite()
    {
        TestSuite suite= new TestSuite();

        suite.addTest( new AdviceServiceTest("testSimpleObject") );
        suite.addTest( new AdviceServiceTest("testDefaultAdvice") );
        suite.addTest( new AdviceServiceTest("testChainedAdvices") );
        suite.addTest( new AdviceServiceTest("testMultipleProxies") );

        
        return suite;
    }

    /**
     * Advice a StringBuffer based on the CharSequence interface
     */
    public void testSimpleAdvice() throws Exception
    {
        String[] interceptorList = { LoggingInterceptorService.class.getName() };
        StringBuffer unadvicedObject = new StringBuffer("foo");
        CharSequence advicedObject = (CharSequence) this.service.advice(unadvicedObject );
        
        int length = advicedObject.length();
        assertTrue(this.service.isAdviced(advicedObject));
        assertFalse(this.service.isAdviced(unadvicedObject));
        assertTrue(unadvicedObject.length() == length);
    }

    /**
     * Advice a StringBuffer based on the CharSequence interface
     */
    public void testSimpleObject() throws Exception
    {
        String[] interceptorList = { LoggingInterceptorService.class.getName() };
        StringBuffer unadvicedObject = new StringBuffer("foo");
        CharSequence advicedObject = (CharSequence) this.service.advice("adviced", interceptorList, unadvicedObject );
        
        int length = advicedObject.length();
        assertTrue(this.service.isAdviced(advicedObject));
        assertFalse(this.service.isAdviced(unadvicedObject));
        assertTrue(unadvicedObject.length() == length);
    }

    /**
     * Advice a StringBuffer based on the CharSequenceInterface with default interceptors
     */
    public void testDefaultAdvice() throws Exception
    {
        StringBuffer unadvicedObject = new StringBuffer("foo");
        CharSequence advicedObject = (CharSequence) this.service.advice("default adviced", unadvicedObject );
        
        advicedObject.length();
    }

    /**
     * The test implements the DependentTestComponent interface therefore we
     * are able to intercept the invocation of test(). Whereas test() invokes
     * another advised component.
     */
    public void testChainedAdvices() throws Exception
    {
        String[] interceptorList = { LoggingInterceptorService.class.getName() };
        this.advisedThis = (DependentTestComponent) this.service.advice(interceptorList, this);
        this.advisedThis.test();
    }

    /**
     * Advice a StringBuffer based on the CharSequenceInterface
     */
    public void testMultipleProxies() throws Exception
    {
        String[] interceptorList = { LoggingInterceptorService.class.getName() };
        StringBuffer unadvicedObject = new StringBuffer("foo");
        CharSequence advicedObject = (CharSequence) this.service.advice("first advice", interceptorList, unadvicedObject);
        CharSequence advicedAdvicedObject = (CharSequence) this.service.advice("second advice", interceptorList, advicedObject );
        
        advicedAdvicedObject.length();
        assertTrue(this.service.isAdviced(advicedAdvicedObject));
    }

    /**
     * Advice a StringBuffer based on the CharSequenceInterface
     */
    public void test()
    {
        try
        {
            DependentTestComponent testComponent = (DependentTestComponent) this.container.lookup(
                DependentTestComponent.class.getName()
                );
            
            testComponent.test();
        }
        catch (ServiceException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
}
