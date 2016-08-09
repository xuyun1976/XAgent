package com.ebay.platform.xagent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.cache.transformer.MethodCacheXAgentService;
import com.ebay.platform.xagent.rmi.RmiXAgentService;
import com.ebay.platform.xagent.runtime.RuntimeXAgentService;

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
		Properties p = new Properties();
		p.load(this.getClass().getClassLoader().getResourceAsStream("xagent.properties"));
		
		String xagentServices = p.getProperty("xagent.services");
		if (xagentServices == null)
			return;
		
		String[] serviceClasses = xagentServices.split(";");
		
		for (String serviceClass : serviceClasses)
		{
			 XAgentService service = (XAgentService)Class.forName(serviceClass).getConstructor(new Class[]{}).newInstance();
			
			service.setArgs(args);
			service.setInstrumentation(inst);
			service.setAgentmain(isAgentmain);
			
			service.start();
		}
	}
}
