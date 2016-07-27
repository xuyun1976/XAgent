package com.ebay.platform.xagent.cache;

public class CacheAgentFactory 
{
	public static CacheAgent createDefaultCacheAgent()
	{
		return createEhCacheAgent();
	}
	
	public static CacheAgent createEhCacheAgent()
	{
		return new EhCacheAgent();
	}
	
	public static CacheAgent createOSCacheAgent()
	{
		return new OSCacheAgent();
	}

}
