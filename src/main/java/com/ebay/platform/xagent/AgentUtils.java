package com.ebay.platform.xagent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.objectweb.asm.Opcodes;

import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.AgentMethod.AgentParameter;
import com.ebay.platform.xagent.runtime.RuntimeClass;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class AgentUtils 
{
	public static Properties parseArgs(String args)
	{
		Properties cmd = new Properties();
		
		if (args == null)
			return cmd;
		
		String[] list = args.split(",");
		for (String arg : list)
		{
			String[] tmp = arg.split("=");
			if (tmp.length != 2)
				continue;
			
			cmd.put(tmp[0], tmp[1]);
		}
		
		return cmd;
	}
	
	public static List<String> readText(String fileName) 
	{
		List<String> contents = new ArrayList<String>();
		
		BufferedReader reader = null;
	    String line = null;
	    
	    try 
	    {
	    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    	//InputStream is = classLoader.getResourceAsStream(fileName);
	    	//if (is == null)
	    	//{
	    		classLoader = classLoader.getClass().getClassLoader();
	    		InputStream is = classLoader.getResourceAsStream(fileName);
	    		if (is == null)
	    			return contents;
	    	//}
	    	
	        reader = new BufferedReader(new InputStreamReader(is));
	        
	        while ((line = reader.readLine()) != null) 
	        {
	            line = line.trim();
	            if (line.startsWith("#"))
	            	continue;
	            
	            contents.add(line);
	        }
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	    finally
	    {
	        if(reader != null)
	        {
	            try 
	            {
	                reader.close();
	            } 
	            catch (Exception e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    return contents;
    }
	
	public static List<AgentMethod> getCacheMethods(String cacheMethodFile)
	{
		List<AgentMethod> methods = new ArrayList<AgentMethod>();
		List<String> contents = readText(cacheMethodFile);
		
		for (String line : contents)
		{
			try
			{
				int index = line.lastIndexOf(".");
				if (index == -1)
					continue;
				
				String className = line.substring(0, index);
				String methodName = line.substring(index + 1);
				
				if (className.length() == 0 || methodName.length() == 0)
					continue;
				
				className = className.replaceAll("\\.","/");
				methods.add(new AgentMethod(className, methodName));
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return methods;
	}
	
	public static String getRandomString(int length) 
	{ 
		String base = "abcdefghijklmnopqrstuvwxyz0123456789ACDEFGHIJKLMNOPQRSTUVWXYZ";   
	    Random random = new Random();
	    StringBuffer sb = new StringBuffer();   
	    
	    for (int i = 0; i < length; i++) 
	    {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }
	    
	    return sb.toString();   
	}
	
	public static Object getAsmClassText(AgentParameter parameter)
	{
		String type = parameter.getType();
		
		if (parameter.isPrimitive())
		{
			if (type.equals("I") || type.equals("Z") || type.equals("C") || type.equals("B") || type.equals("S"))
				return Opcodes.INTEGER;
			else if (type.equals("F"))
				return Opcodes.FLOAT;
			else if (type.equals("J"))
				return Opcodes.LONG;
			else if (type.equals("D"))
				return Opcodes.DOUBLE;
		}
		else
		{
			if (!type.startsWith("[") && type.startsWith("L") && type.endsWith(";"))
				type = type.substring(1, type.length() -1);
			
				return type;
		}
		
		throw new RuntimeException("getAsmClassText failed for : " + type);
	}
	
	public static void save(String fileName, String content) throws Exception
	{
		save(new File(AgentConstants.DEFAULT_RUNTIME_DIR + File.separator + fileName), content);
		
	}
	
	public static void save(File file, String content) throws Exception
	{
		FileWriter fileWritter = new FileWriter(file, false);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        bufferWritter.write(content);
        bufferWritter.close();
	}
	
	public static byte[] getBytesByFile(File fileName) throws Exception
	{  
        FileInputStream fis = new FileInputStream(fileName);  
        
        return getBytesByFile(fis);
    }
	
	public static byte[] getBytesByFile(InputStream is) throws Exception
	{  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        byte[] b = new byte[1024];  
        int n;  
        
        while ((n = is.read(b)) != -1) 
        	bos.write(b, 0, n);  
        
        is.close();  
        bos.close();  

        return bos.toByteArray();  
    }
	
	public static List<File> getFilesInDirectory(File dir, FileFilter filter) throws Exception 
	{
		List<File> outputFiles = new ArrayList<File>();
		
		getFilesInDirectory(dir, filter, outputFiles);
		
		return outputFiles;
	}
	
	private static void getFilesInDirectory(File dir, FileFilter filter, List<File> files) throws Exception 
	{
		if (!dir.exists())
			return;
		
		if (dir.isDirectory()) 
        {
			File[] fileList = dir.listFiles(filter);
                
			for (File file : fileList) 
			{
				if (file.isDirectory())
					getFilesInDirectory(file, filter, files);
				else
				    files.add(file);
            }
       }
       else
       {
    	   files.add(dir);
       }
	}
	
	public static List<RuntimeClass> getRuntimeClasses(File runtimeDir, String classpath) throws Exception
	{
		List<RuntimeClass> runtimeClasses = new ArrayList<RuntimeClass>();
		
		File outputDir = compile(runtimeDir, classpath);
		
		if (outputDir == null)
			return runtimeClasses;
		
		List<File> classFiles = getFilesInDirectory(runtimeDir, new ExtensionFileFilter(".class", AgentConstants.EXCLUDE_DIRS));
		
		for (File f : classFiles)
			runtimeClasses.add(new RuntimeClass(f));
		
		deleteDir(outputDir);
		
		moveToSuccess(runtimeDir);
		
		return runtimeClasses;
	}
	
	private static boolean deleteDir(File dir) 
	{
        if (dir.isDirectory()) 
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) 
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) 
                {
                    return false;
                }
            }
        }
        
        return dir.delete();
    }
	
	public static void moveToSuccess(File runtimeDir) throws Exception
	{
		List<File> files = getFilesInDirectory(runtimeDir, new ExtensionFileFilter((List<String>)null, AgentConstants.EXCLUDE_DIRS));
		
		for (File file : files)
		{
			File dest;
			if (file.getParent().equals(runtimeDir.getAbsolutePath()))
				dest = new File(runtimeDir.getAbsolutePath() + File.separator + AgentConstants.RUNTIME_SUCCESS_DIR);
			else
			{
				String path = file.getParent();
				dest = new File(runtimeDir + File.separator + AgentConstants.RUNTIME_SUCCESS_DIR + File.separator + path.substring(runtimeDir.getAbsolutePath().length() + 1));
			}
			
			if (!dest.exists())
				dest.mkdirs();
			
			dest = new File(dest.getAbsolutePath() + File.separator + file.getName());
			if (dest.exists())
				dest.delete();
			
			file.renameTo(dest);
		}
	}
	
	public static File compile(File runtimeDir, String classpath) throws Exception
	{
		List<File> javaFiles = getFilesInDirectory(runtimeDir, new ExtensionFileFilter(".java", AgentConstants.EXCLUDE_DIRS));
		if (javaFiles.isEmpty())
			return null;
		
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

	    File outputDir = new File(runtimeDir.getAbsolutePath() + File.separator + AgentConstants.RUNTIME_COMPILE_OUT_DIR);
	    if (!outputDir.exists())
	    	outputDir.mkdirs();
	    
	    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputDir));
	    fileManager.setLocation(StandardLocation.CLASS_PATH, getJarsInClasspath(classpath));
	    
	    List<String> options = new ArrayList<String>();
	    options.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
	    
	    // Compile the file
	    compiler.getTask(null, fileManager, null, options, null, fileManager.getJavaFileObjectsFromFiles(javaFiles)).call();
	    fileManager.close();
	    
	    return outputDir;
	}
	
	public static List<File> getJarsInClasspath(String classpath) throws Exception
	{
		List<File> jars = new ArrayList<File>();
		
		if (classpath == null)
			return jars;
		
		String[] paths = classpath.split(",|;");
		
		for (String path : paths)
		{
			List<File> files = getFilesInDirectory(new File(path), new ExtensionFileFilter(".jar", null));
			jars.addAll(files);
		}
		
		return jars;
	}
	
	public static int getAvailablePort()
	{
		for (int port = 6600; port < 10000; port++)
		{
			ServerSocket socket = null;
			try 
			{
				socket = new ServerSocket(port);
				return port;
			} 
			catch (Exception e) 
			{
		    } 
			finally 
			{ 
				if (socket != null)
				{
					try 
					{
						socket.close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} 
				}
			}
		}
		
		return 0;
	}
	
	public static void attachVM(VirtualMachineDescriptor vmd, String options)
	{
		try
		{
			VirtualMachine vm = VirtualMachine.attach(vmd);
			
			System.out.println(AgentUtils.class.getProtectionDomain().getCodeSource().getLocation());
			
			vm.loadAgent(AgentConstants.jarFilePath, options);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Class getClassFromInstrumention(Instrumentation inst, String className)
	{
		Class[] classes = inst.getAllLoadedClasses();
		for (Class clz : classes)
		{
			if (clz.getName().equals(className) )
				return clz;
		}
		
		return null;
	}
	
	public static void main(String[] args) throws Exception
	{
		File runtimeDir = new File(AgentConstants.DEFAULT_RUNTIME_DIR);
		if (!runtimeDir.exists())
			runtimeDir.mkdirs();
		
		System.out.println(runtimeDir.getAbsolutePath());
		
		List<RuntimeClass> runtimeClasses = getRuntimeClasses(runtimeDir, "C:\\v3app\\core\\shared\\lib");
		
		System.out.println(runtimeClasses);
	}
}