package com.ebay.platform.xagent.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface AgentRmiService extends Remote 
{
	 public List<ClassInfo> getAllLoadedClasses() throws RemoteException;
	 public Map<String, byte[]> getClassfileBuffer(String className) throws RemoteException;
	 public List<MethodInfo> getMethods(String className) throws RemoteException;
	 
	 public void cache(MethodInfo method) throws RemoteException;
}
