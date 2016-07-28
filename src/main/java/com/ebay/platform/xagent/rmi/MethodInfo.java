package com.ebay.platform.xagent.rmi;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MethodInfo implements NodeInfo, Serializable 
{
	private String className;
	private String methodName;
	private String methodFullName;
	
	public MethodInfo(String className, String methodName, String methodFullName)
	{
		this.className = className;
		this.methodName = methodName;
		this.methodFullName = methodFullName;
	}
	
	public String getClassName() {
		return className;
	}
	public String getMethodName() {
		return methodName;
	}
	public String getMethodFullName() {
		return methodFullName;
	}
	
	@Override
	public String toString() {
		return methodFullName;
	}
}
