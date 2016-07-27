package com.ebay.platform.xagent.test.common;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.ebay.platform.xagent.cache.CacheManager;


public class MyUser1 
{
	  public static String getNameI69d6mz2(String time, int i)
			    throws InterruptedException
			  {
			    Thread.sleep(Integer.valueOf(time).intValue() + i);
			    return "foo----";
			  }
			  
			  public static String getName(String paramString, int paramInt)
			    throws InterruptedException
			  {
			    Object localList = CacheManager.generateKey(new Object[] { "com/ebay/platform/cache/MyUsergetName", paramString, Integer.valueOf(paramInt) });//Arrays.asList(new Object[] { "com/ebay/platform/cache/MyUsergetName", paramString, Integer.valueOf(paramInt) });
			    //URL localURL = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
			    //CacheManager localCacheManager = CacheManager.create(localURL);
			    //Cache localCache = localCacheManager.getCache("authCache1");
			    Object localObject = CacheManager.get(localList);
			    if (localObject == null) 
			    {
			    	localObject = getNameI69d6mz2(paramString, paramInt);
			    	CacheManager.put(localList, localObject);
			    }
			    
			    return (String)localObject;
			  }
			  


}
