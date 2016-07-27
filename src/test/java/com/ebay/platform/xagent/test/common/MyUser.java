package com.ebay.platform.xagent.test.common;

public class MyUser 
{
	InnerClass in = new InnerClass();
	 public String getName(String time, int i) throws InterruptedException 
	 {
		 Thread.sleep(Integer.valueOf(time) + i);
		 return "foo11" + in.getValue();
	 }
	 
	 class InnerClass
	 {
		 public String getValue()
		 {
			 return "-----------";
		 }
	 }

}
