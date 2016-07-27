package com.ebay.platform.xagent.cache.transformer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.cache.AgentMethod;

public abstract class MethodCacheAbstractTransformer implements ClassFileTransformer
{
	protected List<AgentMethod> methods;
	
	public MethodCacheAbstractTransformer(List<AgentMethod> methods)
	{
		this.methods = methods;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException 
    {
    	for (AgentMethod method : methods)
    	{
    		if (className.equals(method.getClassName()))
    		{ 
    			try
    			{
	    			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	    			ClassReader reader = new ClassReader(classfileBuffer);
	    			reader.accept(getClassVisitor(cw, className), 0);
	    			
	    			if (AgentConstants.ENABLE_DEBUG)
	    				debugFileOutput(cw, className);
	    			
	    			return cw.toByteArray();
    			}
    			catch(Exception ex)
    			{
    				ex.printStackTrace();
    				throw new IllegalClassFormatException(ex.toString());
    			}
    		}
    	}
    	
        return classfileBuffer;
    }
	
	protected abstract ClassVisitor getClassVisitor(ClassWriter cw, String className);
	
	protected AgentMethod getMethod(String className, String methodName, int access, String desc)
	{
		for (AgentMethod method : methods)
		{
			 if (method.getClassName().equals(className) && method.getMethodName().equals(methodName)
				&& !desc.startsWith("()") && !desc.endsWith("V"))
				 return method.parse(access, desc);
		}
		
		return null;
	}
	
	private void debugFileOutput(ClassWriter cw, String className) throws IOException
	{
		byte[] data = cw.toByteArray();
        
		File dir = new File(AgentConstants.DEFAULT_DEBUG_DIR);
		if (!dir.exists())
			dir.mkdirs();
		
		String fileName = className.substring(className.lastIndexOf("/") + 1);
        FileOutputStream fout = new FileOutputStream(dir +  File.separator + fileName + ".class");
        fout.write(data);
        fout.close();

	}
	
}
