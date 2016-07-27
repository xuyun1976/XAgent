package com.ebay.platform.xagent.cache;

import java.util.Arrays;
import java.util.List;

public class CacheManager 
{
	private static CacheAgent cacheAgent = CacheAgentFactory.createDefaultCacheAgent();
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					try
					{
						CacheManager.shutdown();
						
						System.out.println("Cache Manager Shutdown");
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
			});
	}
	
	public static Object generateKey(Object...objects)
	{
		List<Object> arrayKey = Arrays.asList(objects);
		
		return arrayKey;
	}
	
	public static void put(Object key, Object value)
	{
		cacheAgent.put(key, value);
	}
	
	public static Object get(Object key)
	{
		Object value = cacheAgent.get(key);
		
		System.out.println(String.format("key=%s, value=%s", key, value));
		return value;
	}
	
	public static void shutdown()
	{
		cacheAgent.shutdown();
	}
	
}
