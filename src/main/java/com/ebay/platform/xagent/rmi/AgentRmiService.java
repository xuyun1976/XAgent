package com.ebay.platform.xagent.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AgentRmiService extends Remote 
{
	 public List<String> getAllLoadedClasses() throws RemoteException;
	 public byte[] getClassfileBuffer(String className) throws RemoteException;
}
