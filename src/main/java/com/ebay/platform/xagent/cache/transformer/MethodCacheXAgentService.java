package com.ebay.platform.xagent.cache.transformer;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.XAgentService;
import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.transformer.MethodCacheTransformer;
import com.ebay.platform.xagent.rmi.AgentRmiServiceImpl;

public class MethodCacheXAgentService implements XAgentService 
{
	private String args;
	private Instrumentation inst;
	private boolean isAgentmain;
	
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
    	List<AgentMethod> methods = AgentUtils.getCacheMethods(cacheMethodFile);
    	MethodCacheTransformer methodCacheTransformer = new MethodCacheTransformer();
    	methodCacheTransformer.setMethods(methods);
    	
    	inst.addTransformer(methodCacheTransformer, true);
    	
    	if (!isAgentmain)
    		return;
    	
    	for (AgentMethod method : methods)
    		inst.retransformClasses(Class.forName(method.getClassName().replaceAll("/", ".")));
		
	}
	
}
