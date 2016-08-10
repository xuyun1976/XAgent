package com.ebay.platform.xagent.cache.transformer;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.XAgentService;
import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.transformer.MethodCacheTransformer;

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
	
	@SuppressWarnings("rawtypes")
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
    	{
    		try
    		{
    			Class clz = AgentUtils.getClassFromInstrumention(inst, method.getClassName().replaceAll("/", "."));
    			
    			inst.retransformClasses(clz);
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
	}
	
}
