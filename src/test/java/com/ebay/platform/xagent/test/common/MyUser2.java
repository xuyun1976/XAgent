package com.ebay.platform.xagent.test.common;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class MyUser2 
{

	  
	 public String getName(int time) throws InterruptedException 
	 {
		 Thread.sleep(time);
		 return "foo2";
	 }

}
