package com.ebay.platform.xagent.cache;

import java.io.Serializable;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheAgent implements CacheAgent
{
	private CacheManager cacheManager;
    private Cache cache;
    
    public EhCacheAgent()
    {
    	System.setProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
    	URL url = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
		cacheManager = CacheManager.create(url);
		cache = cacheManager.getCache("xagentCache");
    }
    
	@Override
	public void shutdown() 
	{
		if (cacheManager == null)
			return;
		
		cacheManager.shutdown();
	}

	@Override
	public void put(Object key, Object value) 
	{
		cache.put(new Element(key, value));
		
		if (key instanceof Serializable && value instanceof Serializable)
			cache.flush();
	}

	@Override
	public Object get(Object key) 
	{
		Element el = cache.get(key);
		if (el == null)
			return null;
		
		return  el.getObjectValue();
	}
	
}
