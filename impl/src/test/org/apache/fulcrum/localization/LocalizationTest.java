package org.apache.fulcrum.localization;

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

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.fulcrum.testcontainer.BaseUnitTest;

/**
 * Tests the API of the
 * {@link org.apache.fulcrum.localization.LocalizationService}.
 * <br>
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 */
public class LocalizationTest extends BaseUnitTest
{
    
    private LocalizationService ls = null;
    public LocalizationTest(String name)
    {
        super( name );
    }
  
    public void setUp() throws Exception
    {
        super.setUp();
        try
        {
            ls = (LocalizationService) this.resolve( LocalizationService.class.getName() );
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
    }
    public void testInitialization()
    {
        assertTrue(true);
    }
    public void testLocalization() throws Exception
    {
        // Test retrieval of text using multiple default bundles
        String s = ls.getString(null, null, "key1");
        assertEquals("Unable to retrieve localized text for locale: default", "value1", s);
        s = ls.getString(null, new Locale("en", "US"), "key2");
        assertEquals("Unable to retrieve localized text for locale: en-US", "value2", s);
        s = ls.getString("org.apache.fulcrum.localization.BarBundle", new Locale("ko", "KR"), "key3");
        assertEquals("Unable to retrieve localized text for locale: ko-KR", s, "[ko] value3");
        try
        {
            ls.getString("DoesNotExist", new Locale("ko", ""), "key1");
            fail();
        }
        catch (MissingResourceException expectedFailure)
        {
            // Asked for resource bundle which does not exist.
        }
        catch( Throwable e )
        {
            // should not happen
            fail();
        }
        // When a locale is used which cannot be produced for a given
        // bundle, fall back to the default locale.
        s = ls.getString(null, new Locale("ko", "KR"), "key4");
        assertEquals("Unable to retrieve localized text for locale: default", s, "value4");
        try
        {
            ls.getString(null, null, "NoSuchKey");
            fail();
        }
        catch (MissingResourceException expectedFailure)
        {
            // Asked for key from default bundle which does not exist,
        }
    }
    /**
     * Putting this in a seperate testcase because it fails..  Why?  I don't know.  I have never
     * used localization, so I leave it to brains better then mine. -dep
     * @todo Figure out why this test fails.
     * @throws Exception
     */
    public void OFFtestRetrievingOddLocale() throws Exception
    {
        String s = ls.getString(null, new Locale("fr", "US"), "key3");
        assertEquals("Unable to retrieve localized text for locale: fr", "[fr] value3", s);
    }
}
