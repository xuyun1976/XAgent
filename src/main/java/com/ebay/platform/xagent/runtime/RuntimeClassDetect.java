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
	
	public RuntimeClassDetect(Properties args, Instrumentation inst)
	{
		this.args = args;
		this.inst = inst;
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
					
		    		List<RuntimeClass> runtimeClasses = AgentUtils.getRuntimeClasses(runtimeDir);
					
		    		//runtimeClassTransformer.setRuntimeClasses(runtimeClasses);
		    		
					for (RuntimeClass runtimeClass : runtimeClasses)
			    		inst.redefineClasses(new ClassDefinition(Class.forName(runtimeClass.getClassName().replaceAll("/", ".")), runtimeClass.getClassfileBuffer()));
					
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
