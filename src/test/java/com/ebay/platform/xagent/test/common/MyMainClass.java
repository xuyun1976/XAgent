package com.ebay.platform.xagent.test.common;

public class MyMainClass 
{
	 public static void main(String[] args) throws InterruptedException 
	 {
		 MyUser myUser = new MyUser();
		 for (int i = 0; i < 10000; i++)
		 {
			 System.out.println(myUser.getName(String.valueOf(2000), 2000) + ";" +System.currentTimeMillis());
			 Thread.sleep(1000);
		 }
	 }
}
