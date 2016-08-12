package com.ebay.platform.xagent.cache;

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
		return new InfinispanCacheAgent();
	}

}
