package com.ebay.platform.xagent.cache;

import java.lang.instrument.Instrumentation;

import com.ebay.platform.xagent.XAgentClassLoader;

public class CacheAgentFactory 
{
	public static CacheAgent createDefaultCacheAgent()
	{
		return createJCSCacheAgent();
	}
	
	public static CacheAgent createEhCacheAgent()
	{
		return new EhCacheAgent();
	}
	
	public static CacheAgent createJCSCacheAgent()
	{
		try
		{
			XAgentClassLoader xAgentClassLoader = new XAgentClassLoader();
			Thread.currentThread().setContextClassLoader(xAgentClassLoader);
    	
			Class c = xAgentClassLoader.loadClass("com.ebay.platform.xagent.cache.InfinispanCacheAgent");
    	
			return (CacheAgent)c.getConstructor().newInstance();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

}
