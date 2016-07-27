package com.ebay.platform.xagent.cache;

public interface CacheAgent 
{
	public void shutdown();
	
	public void put(Object key, Object value);
	public Object get(Object key);
	
}
