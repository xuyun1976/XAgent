package com.ebay.platform.xagent;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.transformer.MethodCacheTransformer;
import com.ebay.platform.xagent.rmi.AgentRmiServiceImpl;
import com.ebay.platform.xagent.runtime.RuntimeClassDetect;

public class XAgentServiceImpl implements XAgentService 
{
	private String args;
	private Instrumentation inst;
	private boolean isAgentmain;
	
	public XAgentServiceImpl(String args, Instrumentation inst, boolean isAgentmain)
	{
		this.args = args;
		this.inst = inst;
		this.isAgentmain = isAgentmain;
	}
	
	@Override
	public void setArgs(String args) 
	{
		this.args = args;
	}
	
	@Override
	public void setInstrumentation(Instrumentation inst) 
	{
		this.inst = inst;
	}

	@Override
	public void setAgentmain(boolean isAgentmain) 
	{
		this.isAgentmain = isAgentmain;
	}
	
	@Override
	public void start() throws Exception
	{
    	Properties cmd = AgentUtils.parseArgs(args);
    	
    	String cacheMethodFile = cmd.getProperty(AgentConstants.ARG_CACHE_FILE, AgentConstants.DEFAULT_XCACHE_METHOD_FILE);
    	String classpath = cmd.getProperty(AgentConstants.ARG_CLASSPATH);
    	
    	List<AgentMethod> methods = AgentUtils.getCacheMethods(cacheMethodFile);
    	MethodCacheTransformer methodCacheTransformer = new MethodCacheTransformer(methods);
    	
    	System.out.println(methodCacheTransformer.getClass().getClassLoader());
    	
    	inst.addTransformer(methodCacheTransformer, true);
    	
    	String rmiPort = cmd.getProperty(AgentConstants.ARG_RMI_PORT);
    	if (rmiPort != null)
    		new AgentRmiServiceImpl(inst, methodCacheTransformer, Integer.valueOf(rmiPort)).start();
    	
    	new RuntimeClassDetect(cmd, inst, classpath).apply();
    	
    	if (!isAgentmain)
    		return;
    	
    	for (AgentMethod method : methods)
    		inst.retransformClasses(Class.forName(method.getClassName().replaceAll("/", ".")));
		
	}


	
}
