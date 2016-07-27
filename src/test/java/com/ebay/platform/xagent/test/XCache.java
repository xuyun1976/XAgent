package com.ebay.platform.xagent.test;

import java.net.URL;

import com.ebay.platform.xagent.cache.CacheManager;


public class XCache 
{
	public static void main(String[] args)
	{
		for (int i = 0; i < 10; i++)
        {
			CacheManager.put(i, i);
			
			System.out.println(i);
        }
        
	}
}

class Student
{
	int id;
	
	public Student(int id)
	{
		this.id = id;
	}
}