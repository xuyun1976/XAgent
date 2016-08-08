package com.ebay.platform.xagent;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class XAgentClassLoader extends URLClassLoader
{
	static URL xagentUrl = XAgentClassLoader.class.getProtectionDomain().getCodeSource().getLocation();
	
	public XAgentClassLoader() 
	{
		super(new URL[]{xagentUrl}, null);
		
		try
		{
			List<File> jars = searchJarForDev();
			if (jars != null)
			{
				for (File jar : jars)
					this.addURL(jar.toURI().toURL());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private List<File> searchJarForDev() throws Exception
	{
		File file = new File(xagentUrl.getFile());
		if (file.isDirectory())
		{
			File parent = file.getParentFile();
			if (parent != null && "target".equals(parent.getName()))
			{
				return AgentUtils.getFilesInDirectory(parent, new ExtensionFileFilter(".jar", null));
			}
		}
		
		return null;
	}

}
