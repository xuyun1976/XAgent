package com.ebay.platform.xagent.rmi;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;

@SuppressWarnings("serial")
public class AgentRmiServiceImpl extends UnicastRemoteObject implements AgentRmiService
{
	private Instrumentation inst;
	private int port;
	
	public AgentRmiServiceImpl(Instrumentation inst, int port) throws RemoteException 
	{
		super();
		
		this.inst = inst;
		this.port = port;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<String> getAllLoadedClasses() throws RemoteException 
	{
		if (inst == null)
			return null;
		
		Class[] classes = inst.getAllLoadedClasses();
		List<String> clzz = new ArrayList<String>();
		
		for (Class clz : classes)
		{
			String className = clz.getName();
			
			boolean excluded = false;
			for (String exclude : AgentConstants.EXCLUDE_CLASS_NAMES)
			{	
				if (className.startsWith(exclude))
				{
					excluded = true;
					break;
				}
			}
			
			if (!excluded)
				clzz.add(className);
		}
		
		Collections.sort(clzz);
		
		return clzz;
	}
	
	public void start() 
	{  
        try 
        {  
            LocateRegistry.createRegistry(port);  
            Naming.rebind("rmi://127.0.0.1:" + port + "/AgentRmiService", this);  
        } 
        catch (Exception e) 
        {  
            e.printStackTrace();  
        }  
    } 

	@Override
	@SuppressWarnings("rawtypes")
	public byte[] getClassfileBuffer(String className) throws RemoteException 
	{
		try
		{
			Class c = getClass(className);
			String classAsPath = className.replace('.', '/') + ".class";
			InputStream is = c.getClassLoader().getResourceAsStream(classAsPath);
			
			return AgentUtils.getBytesByFile(is);
		}
		catch(Exception ex)
		{
			throw new RemoteException(ex.getMessage(), ex);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Class getClass(String className)
	{
		Class[] classes = inst.getAllLoadedClasses();
		
		for (Class clz : classes)
		{
			if (clz.getName().equals(className))
				return clz;
		}
		
		return null;
	}

}
