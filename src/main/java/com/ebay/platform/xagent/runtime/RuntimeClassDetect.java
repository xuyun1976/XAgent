package com.ebay.platform.xagent.runtime;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;

public class RuntimeClassDetect 
{
	private Properties args;
	private Instrumentation inst;
	private String classpath;
	
	public RuntimeClassDetect(Properties args, Instrumentation inst, String classpath)
	{
		this.args = args;
		this.inst = inst;
		this.classpath = classpath;
	}
	
	public void apply()
	{
		DetecteTask detecteTask = new DetecteTask();
		detecteTask.start();
	}
	
	class DetecteTask extends Thread
	{
		public DetecteTask()
		{
			this.setDaemon(true);
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public void run() 
		{
			while(true)
			{
				try
				{
					File runtimeDir = new File(args.getProperty(AgentConstants.ARG_RUNTIME_DIR, AgentConstants.DEFAULT_RUNTIME_DIR));
					if (!runtimeDir.exists())
						runtimeDir.mkdirs();
					
		    		List<RuntimeClass> runtimeClasses = AgentUtils.getRuntimeClasses(runtimeDir, classpath);
					
					for (RuntimeClass runtimeClass : runtimeClasses)
					{
						Class clz = AgentUtils.getClassFromInstrumention(inst, runtimeClass.getClassName().replaceAll("/", "."));
						if (clz != null)
							inst.redefineClasses(new ClassDefinition(clz, runtimeClass.getClassfileBuffer()));
						else
							System.out.println("Can not find the class :" + runtimeClass.getClassName());
					}
					
					Thread.sleep(10000);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
	}
	
}
