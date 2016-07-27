package com.ebay.platform.xagent.cache;


import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

public class OSCacheAgent implements CacheAgent
{
	private GeneralCacheAdministrator cache = new GeneralCacheAdministrator();
    
    public OSCacheAgent()
    {
    	
    }
    
	@Override
	public void shutdown() 
	{
		
	}

	@Override
	public void put(Object key, Object value) 
	{
		if (key == null || value == null)
			return;
		
		cache.putInCache(key.toString(), value);
	}

	@Override
	public Object get(Object key) 
	{
		if (key == null)
			return null;
		
		try 
		{
			return cache.getFromCache(key.toString());
		} 
		catch (NeedsRefreshException e) 
		{
			return null;
		}
	}
	
}
