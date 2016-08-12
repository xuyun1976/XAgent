package com.ebay.platform.xagent.cache;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;


public class InfinispanCacheAgent implements CacheAgent
{
	Cache<Object, Object> cache ;
	
	public InfinispanCacheAgent()
    {
		try 
		{
			cache = new DefaultCacheManager("infinispan.xml").getCache("fileCache");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
    }
    
	@Override
	public void shutdown() 
	{
	}

	@Override
	public void put(Object key, Object value) 
	{
		cache.put(key, value);
	}

	@Override
	public Object get(Object key) 
	{
		try
		{
			return cache.get(key);
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
}
