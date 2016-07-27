package com.ebay.platform.xagent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.transformer.MethodCacheTransformerFactory;
import com.ebay.platform.xagent.rmi.AgentRmiServiceImpl;
import com.ebay.platform.xagent.runtime.RuntimeClassDetect;

public class XAgent 
{
	private static Instrumentation instrumentation;

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
    
    private static void exec(String args, Instrumentation inst, boolean isAgentmain) throws Exception 
    {
    	Properties cmd = AgentUtils.parseArgs(args);
    	
    	String cacheMethodFile = cmd.getProperty(AgentConstants.ARG_CACHE_FILE, AgentConstants.DEFAULT_XCACHE_METHOD_FILE);
    	List<AgentMethod> methods = AgentUtils.getCacheMethods(cacheMethodFile);//new ArrayList<Method>();//
    	ClassFileTransformer classFileTransformer = MethodCacheTransformerFactory.create(methods, isAgentmain);
    	
//    	String runtimeDir = cmd.getProperty(AgentConstants.ARG_RUNTIME_DIR, AgentConstants.DEFAULT_RUNTIME_DIR);
//    	
//    	List<RuntimeClass> runtimeClasses = AgentUtils.getRuntimeClasses(runtimeDir);
//    	ClassFileTransformer runtimeClassTransformer = new RuntimeClassTransformer(runtimeClasses);
    	
    	instrumentation = inst;
    	instrumentation.addTransformer(classFileTransformer, true);
    	//instrumentation.addTransformer(runtimeClassTransformer, true);
    	
    	String rmiPort = cmd.getProperty(AgentConstants.ARG_RMI_PORT);
    	if (rmiPort != null)
    		new AgentRmiServiceImpl(inst, Integer.valueOf(rmiPort)).start();
    	
    	new RuntimeClassDetect(cmd, inst).apply();
    	
    	if (!isAgentmain)
    		return;
    	
    	for (AgentMethod method : methods)
        	instrumentation.retransformClasses(Class.forName(method.getClassName().replaceAll("/", ".")));
    	
//    	for (RuntimeClass runtimeClass : runtimeClasses)
//    		instrumentation.retransformClasses(Class.forName(runtimeClass.getClassName().replaceAll("/", ".")));
    }

}
