/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fulcrum.yaafi.interceptor.util;

import java.lang.reflect.Method;

import org.apache.fulcrum.yaafi.framework.util.StringUtils;

/**
 * Creates a string representation of java.lang.reflect.Method
 * 
 * @author <a href="mailto:siegfried.goeschl@it20one.at">Siegfried Goeschl</a>
 */
public class MethodToStringBuilder
{
    /** initial size for the StringBuffer */
    private static final int BUF_SIZE = 1024;
    
    /** the method we are dumping */
    private Method method;
    
    /** 
     * Constructor
     * 
     * @param method the method to print 
     */
    public MethodToStringBuilder(Method method)
    {
        this.method = method;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
	    try
        {
            StringBuffer buffer = new StringBuffer(BUF_SIZE);
            
            Class returnType = method.getReturnType();
            Class declaringClass = method.getDeclaringClass();
            Class[] params = method.getParameterTypes();
            
            buffer.append( this.stripDefaultPackage(returnType.getName()));
            buffer.append( ' ');
            buffer.append( this.stripDefaultPackage(declaringClass.getName()));
            buffer.append( '.');
            buffer.append( method.getName() );
            buffer.append( '(');
            
            for (int i = 0; i < params.length; i++) 
            {
                buffer.append( this.stripDefaultPackage(params[i].getName()) );	        
            	if (i < (params.length - 1))
            	{
            	    buffer.append(",");
            	}
            }
            
            buffer.append(")");
            
            return buffer.toString();
        }
        catch (Throwable t)
        {
            return "<" + t + ">";
        }
    }

    /**
     * Strips "java.lang" from the argument other than arrays
     */
	private String stripDefaultPackage( String arg )
	{
	    if( arg.charAt(0) == '[' )
	    {
	        // this is an array
	        return StringUtils.replaceChars(arg,';','#');	       
	    }
	    else
	    {
	        return StringUtils.replace( arg, "java.lang.", "");
	    }
	}
}
