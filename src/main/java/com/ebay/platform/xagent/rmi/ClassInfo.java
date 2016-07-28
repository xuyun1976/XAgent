package com.ebay.platform.xagent.rmi;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ClassInfo implements NodeInfo, Serializable, Comparable<ClassInfo>
{
	private String className;
	
	public ClassInfo(String className)
	{
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int compareTo(ClassInfo o) 
	{
		return className.compareTo(o.getClassName());
	}
	
	@Override
	public String toString()
	{
		return className;
	}
}
