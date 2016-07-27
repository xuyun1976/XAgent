package com.ebay.platform.xagent;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

class ExtensionFileFilter implements FileFilter 
{
	private List<String> extensions;
	private List<String> excludeDirs;
	
	public ExtensionFileFilter(String extension, List<String> excludeDirs)
	{
		this(Arrays.asList(new String[]{extension}), excludeDirs);
	}
	
	public ExtensionFileFilter(List<String> extensions, List<String> excludeDirs)
	{
		this.extensions = extensions;
		this.excludeDirs = excludeDirs;
	}
	
	public boolean accept(File file) 
	{
		String name = file.getName();
		
		
		if (file.isDirectory())
		{
			if (excludeDirs == null)
				return true;
			
			return !excludeDirs.contains(name);
		}
		
		if (extensions == null)
			return true;
		
		for (String extension : extensions)
		{
			if (file.getName().endsWith(extension))
				return true;
		}
		
	    return false;
	}
}
