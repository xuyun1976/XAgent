package com.ebay.platform.xagent.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class CacheManager 
{
	private static CacheAgent cacheAgent = CacheAgentFactory.createDefaultCacheAgent();
	private static XStream xstream = new XStream(new JettisonMappedXmlDriver());
	
	static
	{
		xstream.setMode(XStream.NO_REFERENCES);
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	classLoader = classLoader.getClass().getClassLoader();
		xstream.setClassLoader(classLoader);
		
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
		if (isSerializable(key, value))
			cacheAgent.put(key, value);
		else
			cacheAgent.put(objectToJson(key), objectToJson(value));
	}
	
	public static Object get(Object key)
	{
		Object value = null;
		
		if (isSerializable(key))
			value = cacheAgent.get(key);
		
		if (!isSerializable(key) || value == null)
		{
			String jsonValue = (String)cacheAgent.get(objectToJson(key));
			
			value = jsonToObject(jsonValue);
		}
			
		System.out.println(String.format("key=%s, value=%s", key, value));
		
		return value;
	}
	
	public static boolean isSerializable(Object... objs)
	{
		if (objs == null)
			return false;
		
		for (Object obj : objs)
		{
			if (!(obj instanceof Serializable))
				return false;
		}
		
		return true;
	}
	
	public static String objectToJson(Object obj)
	{
		String json = xstream.toXML(obj);
		
		return json;
	}
	
	public static Object jsonToObject(String json)
	{
		if (json == null)
			return null;
		
		return xstream.fromXML(json);
	}
	
//	public static <T> T get1(Object key)
//	{
//		Object value = cacheAgent.get(key);
//		
//		System.out.println(String.format("key=%s, value=%s", key, value));
//		return value;
//	}
	
	public static void shutdown()
	{
		cacheAgent.shutdown();
	}
	
}
