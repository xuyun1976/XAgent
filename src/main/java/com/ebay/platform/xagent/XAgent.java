package com.ebay.platform.xagent;


import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;


public class XAgent 
{
    /**
     * JVM hook to statically load the javaagent at startup.
     * 
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception 
    {
    	System.out.println("---------------agent premain---------------");
    	exec(args, inst, false);
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     * 
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) throws Exception 
    {
    	System.out.println("---------------agent exec---------------");
        exec(args, inst, true);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private static void exec(String args, Instrumentation inst, boolean isAgentmain) throws Exception 
    {
    	ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    	XAgentClassLoader xAgentClassLoader = new XAgentClassLoader();
    	Thread.currentThread().setContextClassLoader(xAgentClassLoader);
    	
    	Class c = xAgentClassLoader.loadClass("com.ebay.platform.xagent.XAgentServiceImpl");
    	
    	Object service = c.getConstructor(String.class, Instrumentation.class, boolean.class).newInstance(args, inst, isAgentmain);
    	
    	Method method = c.getMethod("start", new Class[]{});
    	method.invoke(service, new Object[]{});
    	
    	Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

}
