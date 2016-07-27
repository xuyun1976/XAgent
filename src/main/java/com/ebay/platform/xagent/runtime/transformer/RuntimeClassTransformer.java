package com.ebay.platform.xagent.runtime.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.platform.xagent.runtime.RuntimeClass;

public class RuntimeClassTransformer implements ClassFileTransformer
{
	private Map<String, RuntimeClass> runtimeClassMap = new HashMap<String, RuntimeClass>();
	
	public void setRuntimeClasses(List<RuntimeClass> runtimeClasses)
	{
		runtimeClassMap.clear();
		
		if (runtimeClasses == null)
			return;
		
		for (RuntimeClass runtimeClass : runtimeClasses)
			runtimeClassMap.put(runtimeClass.getClassName(), runtimeClass);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException 
	{
		if (!runtimeClassMap.containsKey(className))
			return null;
		
		try
		{
			return runtimeClassMap.get(className).getClassfileBuffer();
		}
		catch(Exception ex)
		{
			throw new IllegalClassFormatException(ex.getMessage());
		}
	}

}
