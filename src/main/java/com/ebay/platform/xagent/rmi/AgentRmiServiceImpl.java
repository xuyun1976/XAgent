package com.ebay.platform.xagent.rmi;

import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.transformer.MethodCacheTransformer;

@SuppressWarnings("serial")
public class AgentRmiServiceImpl extends UnicastRemoteObject implements AgentRmiService
{
	private Instrumentation inst;
	private MethodCacheTransformer methodCacheTransformer;
	private int port;
	
	public AgentRmiServiceImpl(Instrumentation inst, int port) throws RemoteException 
	{
		super();
		
		this.inst = inst;
		this.methodCacheTransformer = new MethodCacheTransformer();
		this.port = port;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ClassInfo> getAllLoadedClasses() throws RemoteException 
	{
		if (inst == null)
			return null;
		
		Class[] classes = inst.getAllLoadedClasses();
		List<ClassInfo> clzz = new ArrayList<ClassInfo>();
		
		for (Class clz : classes)
		{
			String className = clz.getName();
			
			boolean excluded = false;
			for (String exclude : AgentConstants.EXCLUDE_CLASS_NAMES)
			{	
				if (className.startsWith(exclude) || className.indexOf("$") != -1)
				{
					excluded = true;
					break;
				}
			}
			
			if (!excluded)
				clzz.add(new ClassInfo(className));
		}
		
		Collections.sort(clzz);
		
		return clzz;
	}
	
	public void start() 
	{  
        try 
        {  
            LocateRegistry.createRegistry(port);  
            Naming.rebind("rmi://127.0.0.1:" + port + "/AgentRmiService", this);  
        } 
        catch (Exception e) 
        {  
            e.printStackTrace();  
        }  
    } 

	@Override
	@SuppressWarnings("rawtypes")
	public Map<String, byte[]> getClassfileBuffer(String className) throws RemoteException 
	{
		Map<String, byte[]> classMap = new HashMap<String, byte[]>();
		List<Class> classes = getClassAndInnerClass(className);
		
		for (Class c : classes)
		{
			try
			{
				
				String classAsPath = c.getName().replace('.', '/') + ".class";
				InputStream is = c.getClassLoader().getResourceAsStream(classAsPath);
				byte[] buffer = AgentUtils.getBytesByFile(is);
				
				int index = classAsPath.lastIndexOf("/");
				classMap.put(classAsPath.substring(index + 1), buffer);
			}
			catch(Exception ex)
			{
				throw new RemoteException(ex.getMessage(), ex);
			}
		}
		
		return classMap;
	}
	
	@SuppressWarnings("rawtypes")
	private List<Class> getClassAndInnerClass(String className)
	{
		int index = className.indexOf("$");
		if (index != -1)
			className = className.substring(0, index);
		
		String innerClassName = className + "$";
		List<Class> clzz = new ArrayList<Class>();
		Class[] classes = inst.getAllLoadedClasses();
		
		for (Class clz : classes)
		{
			if (clz.getName().equals(className) || clz.getName().startsWith(innerClassName))
				clzz.add(clz);
		}
		
		return clzz;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<MethodInfo> getMethods(String className) throws RemoteException 
	{
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		Class clz = getClass(className);
		
		if (clz == null)
			return methods;
		
		for (Method m : clz.getDeclaredMethods())
		{
			methods.add(new MethodInfo(className, m.getName(), m.toString()));
		}
		
		return methods;
	}
	
	@SuppressWarnings("rawtypes")
	private Class getClass(String className)
	{
		Class[] classes = inst.getAllLoadedClasses();
		for (Class clz : classes)
		{
			if (clz.getName().equals(className) )
				return clz;
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void cache(MethodInfo method) throws RemoteException 
	{
		try
		{
			String className = method.getClassName();
			String classPath = className.replace(".", "/");
			int index = className.lastIndexOf(".");
			String classKey = className.substring(index + 1) + ".class";
			
			Map<String, byte[]> classMap = getClassfileBuffer(className);
			
			Class clz = AgentUtils.getClassFromInstrumention(inst, className);
			
			methodCacheTransformer.setMethods(Arrays.asList(new AgentMethod[]{new AgentMethod(classPath, method.getMethodName())}));
			byte[] buffer = methodCacheTransformer.transform(clz.getClassLoader(), classPath, null, clz.getProtectionDomain(), classMap.get(classKey));
			
			inst.redefineClasses(new ClassDefinition(clz, buffer));
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
