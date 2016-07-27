package com.ebay.platform.xagent.rmi;

import java.rmi.Naming;
import java.util.List;

public class AgentRmiClient implements AgentRmiService
{
	private int rmiPort;
	
	public AgentRmiClient(int rmiPort)
	{
		this.rmiPort = rmiPort;
	}
	
	public int getRmiPort() {
		return rmiPort;
	}

	public void setRmiPort(int rmiPort) {
		this.rmiPort = rmiPort;
	}

	@Override
	public List<String> getAllLoadedClasses() 
	{
		int retry = 0;
		
		while(retry++ < 3)
		{
			try
			{
				AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
				List<String> classes = agentRmiService.getAllLoadedClasses();
		        
				return classes;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				
				try 
				{
					Thread.sleep(2000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	@Override
	public byte[] getClassfileBuffer(String className)
	{
		try
		{
			AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
			byte[] buffer = agentRmiService.getClassfileBuffer(className);
		        
			return buffer;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new AgentRmiClient(6600).getAllLoadedClasses();
	}

}
