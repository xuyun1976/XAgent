package com.ebay.platform.xagent.rmi;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.XAgentService;
import com.ebay.platform.xagent.rmi.AgentRmiServiceImpl;

public class RmiXAgentService implements XAgentService 
{
	private String args;
	private Instrumentation inst;
	@SuppressWarnings("unused")
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
    	
    	String rmiPort = cmd.getProperty(AgentConstants.ARG_RMI_PORT);
    	if (rmiPort != null)
    		new AgentRmiServiceImpl(inst, Integer.valueOf(rmiPort)).start();
 	}
}
