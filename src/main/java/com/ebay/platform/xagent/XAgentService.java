package com.ebay.platform.xagent;

import java.lang.instrument.Instrumentation;

public interface XAgentService 
{
	public void setArgs(String args);
	public void setInstrumentation(Instrumentation inst);
	public void setAgentmain(boolean isAgentmain);
	
	public void start() throws Exception;
	
}
