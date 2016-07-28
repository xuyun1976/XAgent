package com.ebay.platform.xagent.runtime.decompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.benf.cfr.reader.PluginRunner;

import com.ebay.platform.xagent.AgentConstants;

public class DecompilerByCFR 
{
	public static String decompile(Map<String, byte[]> classMap)
	{
		if (classMap == null)
			return null;
		
		Iterator<String> it = classMap.keySet().iterator();
		
		File mainClassFile = null;
		List<File> classFiles = new ArrayList<File>();
		
		while (it.hasNext())
		{
			try
			{
				String className = it.next();
				File tempDir = new File(AgentConstants.DEFAULT_TEMP_DIR);
				if (!tempDir.exists())
					tempDir.mkdirs();
				
				File classFile = new File(AgentConstants.DEFAULT_TEMP_DIR + File.separator + className);
				classFiles.add(classFile);
				
				FileOutputStream fos = new FileOutputStream(classFile);
				
				fos.write(classMap.get(className));
				fos.close();
			
				if (!className.contains("$"))
					mainClassFile = classFile;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		String java = decompile(mainClassFile);
		
		for (File file : classFiles)
			file.delete();
		
		return java;
	}
	
	public static String decompile(File file)
	{
		if (file == null)
			return null;
		
		return new PluginRunner().getDecompilationFor(file.getAbsolutePath());
	}
	
	public static void main(String[] args)
	{
		
		System.out.println(DecompilerByCFR.decompile(new File("C:\\Users\\yunxu\\.xagent\\temp\\ClassEditorTabbedPanel.class")));
	}

}
