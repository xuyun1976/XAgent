package com.ebay.platform.xagent.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.ebay.platform.xagent.AgentUtils;

public class AgentRmiClient implements AgentRmiService
{
	private int rmiPort;
	
	public AgentRmiClient()
	{
		assignPort();
	}
	
	public int getRmiPort() {
		return rmiPort;
	}

	public void setRmiPort(int rmiPort) {
		this.rmiPort = rmiPort;
	}
	
	public void assignPort()
	{
		rmiPort = AgentUtils.getAvailablePort();
	}

	@Override
	public List<ClassInfo> getAllLoadedClasses() 
	{
		int retry = 0;
		
		while(retry++ < 3)
		{
			try
			{
				AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
				List<ClassInfo> classes = agentRmiService.getAllLoadedClasses();
		        
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
	public Map<String, byte[]> getClassfileBuffer(String className)
	{
		try
		{
			AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
			Map<String, byte[]> classMap = agentRmiService.getClassfileBuffer(className);
		        
			return classMap;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	public List<MethodInfo> getMethods(String className) throws RemoteException 
	{
		try
		{
			AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
			List<MethodInfo> methods = agentRmiService.getMethods(className);
		        
			return methods;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void cache(MethodInfo method) throws RemoteException 
	{
		try
		{
			AgentRmiService agentRmiService = (AgentRmiService)Naming.lookup("rmi://127.0.0.1:" + rmiPort + "/AgentRmiService");  
			agentRmiService.cache(method);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new AgentRmiClient().getAllLoadedClasses();
	}


}
