package com.ebay.platform.xagent.cache.transformer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.cache.AgentMethod;
import com.ebay.platform.xagent.cache.AgentMethod.AgentParameter;
import com.ebay.platform.xagent.cache.CacheManager;

public class MethodCacheTransformer implements ClassFileTransformer
{
	protected List<AgentMethod> methods = new ArrayList<AgentMethod>();

	public MethodCacheTransformer(List<AgentMethod> methods)
	{
		this.methods = methods;
	}

	public void setMethods(List<AgentMethod> methods) {
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
	

	protected ClassVisitor getClassVisitor(ClassWriter cw, String className) 
	{
		return new PremainClassAdapter(Opcodes.ASM4, cw, className);
	}
	
	class PremainClassAdapter extends ClassVisitor implements Opcodes
	{
		private String className;
		private List<CacheMethod> cacheMethods = new ArrayList<CacheMethod>();
		
		
		public PremainClassAdapter(int api, ClassVisitor cv, String className) 
		{
			super(api, cv);
			this.className = className;
		}
		
		@Override  
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			AgentMethod method = getMethod(className, name, access, desc);
			if (method == null)
				return super.visitMethod(access, name, desc, signature, exceptions);
			
			String rename = name + AgentUtils.getRandomString(8);
			
			cacheMethods.add(new CacheMethod(access, name, desc, signature, exceptions, rename, method));
			
			return renameMethod(access, name, desc, signature, exceptions, rename);
		}
	
		@Override
		public void visitEnd()
		{
			for (CacheMethod cacheMethod : cacheMethods)
			{
				insertCacheMethod(cacheMethod.getAccess(), cacheMethod.getName(), cacheMethod.getDesc(), cacheMethod.getSignature(), cacheMethod.getExceptions(), cacheMethod.getRename(), cacheMethod.getMethod());
			}
			
			super.visitEnd();
		}
		
		private MethodVisitor renameMethod(int access, String name,String desc, String signature, String[] exceptions, String rename)
		{
			int access1 = (access | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL) & (Opcodes.ACC_PUBLIC ^ 0xFF) & (Opcodes.ACC_PROTECTED ^ 0xFF);
			
			return cv.visitMethod(access1, rename, desc, signature, exceptions);
		}
		
		private void insertCacheMethod(int access, String name, String desc, String signature, String[] exceptions, String rename, AgentMethod method)
		{
			String cacheManagerName = CacheManager.class.getName().replaceAll("\\.", "/");
			
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			int paramCount = method.getParameters().length;
			boolean isStatic = method.isStaticMethod();
			int varIndex = isStatic ? 0 : 1;
			String className = method.getClassName();
			
			mv.visitCode();
			
			Label l0 = new Label();
			mv.visitLabel(l0);
			
			mv.visitIntInsn(BIPUSH, paramCount + 1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			
			mv.visitInsn(DUP);
			mv.visitIntInsn(BIPUSH, 0);
			mv.visitLdcInsn(method.getClassName().replaceAll("/", ".") + "." + method.getMethodName());
			mv.visitInsn(AASTORE);
			
			for (int i = 0; i < paramCount; i++)
			{
				mv.visitInsn(DUP);
				mv.visitIntInsn(BIPUSH, i + 1);
				
				AgentParameter parameter = method.getParameters()[i];
				if (parameter.isPrimitive())
				{
					String typeStr = parameter.getType();
					if (typeStr.equals("Z"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
					}
					else if (typeStr.equals("C"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
					}
					else if (typeStr.equals("B"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
					}
					else if (typeStr.equals("S"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
					}
					else if (typeStr.equals("I"))
					{	
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					}
					else if (typeStr.equals("F"))
					{
						mv.visitVarInsn(FLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
					}
					else if (typeStr.equals("J"))
					{
						mv.visitVarInsn(LLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
					}
					else if (typeStr.equals("D"))
					{
						mv.visitVarInsn(DLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
					}
				}
				else
				{
					mv.visitVarInsn(ALOAD, varIndex + i);
				}
				
				mv.visitInsn(AASTORE);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, cacheManagerName, "generateKey", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount);
			
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, varIndex + paramCount);
			mv.visitMethodInsn(INVOKESTATIC, cacheManagerName, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 1);
			
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 1);
			
			Label l3 = new Label();
			mv.visitJumpInsn(IFNONNULL, l3);
			
			Label l4 = new Label();
			mv.visitLabel(l4);
			if (!isStatic)
				mv.visitVarInsn(ALOAD, 0);
			
			for (int i = 0; i < paramCount; i++)
			{
				AgentParameter parameter = method.getParameters()[i];
				
				if (parameter.isPrimitive())
				{
					String typeStr = parameter.getType();
					
					if (typeStr.equals("I") || typeStr.equals("B") || typeStr.equals("C") || typeStr.equals("Z") || typeStr.equals("S"))
						mv.visitVarInsn(ILOAD, varIndex + i);
					else if (typeStr.equals("F"))
						mv.visitVarInsn(FLOAD, varIndex + i);
					else if (typeStr.equals("J"))
						mv.visitVarInsn(LLOAD, varIndex + i);
					else if (typeStr.equals("D"))
						mv.visitVarInsn(DLOAD, varIndex + i);
				}
				else
				{
					mv.visitVarInsn(ALOAD, varIndex + i);
				}
			}
			
			mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, className, rename, desc, false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 1);
			
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitVarInsn(ALOAD, varIndex + paramCount);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 1);
			mv.visitMethodInsn(INVOKESTATIC, cacheManagerName, "put", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"java/util/List", "java/lang/Object"}, 0, null);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 1);
			
			AgentParameter returnParameter = method.getReturnParameter();
			String typeStr = returnParameter.getType();
			if (returnParameter.isPrimitive())
			{
				mv.visitTypeInsn(CHECKCAST, typeStr);
				if (typeStr.equals("I") || typeStr.equals("Z") || typeStr.equals("C") || typeStr.equals("B") || typeStr.equals("S"))
					mv.visitInsn(IRETURN);
				else if (typeStr.equals("F"))
					mv.visitInsn(FRETURN);
				else if (typeStr.equals("J"))
					mv.visitInsn(LRETURN);
				else if (typeStr.equals("D"))
					mv.visitInsn(DRETURN);
			}
			else
			{
				typeStr = (String)AgentUtils.getAsmClassText(returnParameter);
				mv.visitTypeInsn(CHECKCAST, typeStr);
				mv.visitInsn(ARETURN);
			}
			
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
/*		
		private void insertCacheMethod(int access, String name, String desc, String signature, String[] exceptions, String rename, AgentMethod method)
		{
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			int paramCount = method.getParameters().length;
			boolean isStatic = method.isStaticMethod();
			int varIndex = isStatic ? 0 : 1;
			String className = method.getClassName();
			
			mv.visitCode();
			
			Label l0 = new Label();
			mv.visitLabel(l0);
			
			mv.visitIntInsn(BIPUSH, paramCount + 1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			
			mv.visitInsn(DUP);
			mv.visitIntInsn(BIPUSH, 0);
			mv.visitLdcInsn(method.getClassName().replaceAll("/", ".") + "." + method.getMethodName());
			mv.visitInsn(AASTORE);
			
			for (int i = 0; i < paramCount; i++)
			{
				mv.visitInsn(DUP);
				mv.visitIntInsn(BIPUSH, i + 1);
				
				AgentParameter parameter = method.getParameters()[i];
				if (parameter.isPrimitive())
				{
					String typeStr = parameter.getType();
					if (typeStr.equals("Z"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
					}
					else if (typeStr.equals("C"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
					}
					else if (typeStr.equals("B"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
					}
					else if (typeStr.equals("S"))
					{
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
					}
					else if (typeStr.equals("I"))
					{	
						mv.visitVarInsn(ILOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					}
					else if (typeStr.equals("F"))
					{
						mv.visitVarInsn(FLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
					}
					else if (typeStr.equals("J"))
					{
						mv.visitVarInsn(LLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
					}
					else if (typeStr.equals("D"))
					{
						mv.visitVarInsn(DLOAD, varIndex + i);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
					}
				}
				else
				{
					mv.visitVarInsn(ALOAD, varIndex + i);
				}
				
				mv.visitInsn(AASTORE);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount);
			
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getContextClassLoader", "()Ljava/lang/ClassLoader;", false);
			mv.visitLdcInsn("ehcache.xml");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "getResource", "(Ljava/lang/String;)Ljava/net/URL;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 1);
			
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 1);
			mv.visitMethodInsn(INVOKESTATIC, "net/sf/ehcache/CacheManager", "create", "(Ljava/net/URL;)Lnet/sf/ehcache/CacheManager;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 2);
			
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 2);
			mv.visitLdcInsn("authCache1");
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/CacheManager", "getCache", "(Ljava/lang/String;)Lnet/sf/ehcache/Cache;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 3);
			
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 3);
			mv.visitVarInsn(ALOAD, varIndex + paramCount);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/Cache", "get", "(Ljava/lang/Object;)Lnet/sf/ehcache/Element;", false);
			
			Label l5 = new Label();
			mv.visitJumpInsn(IFNONNULL, l5);
			
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 3);
			mv.visitTypeInsn(NEW, "net/sf/ehcache/Element");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, varIndex + paramCount);
			
			if (!isStatic)
				mv.visitVarInsn(ALOAD, 0);
			
			for (int i = 0; i < paramCount; i++)
			{
				AgentParameter parameter = method.getParameters()[i];
				
				if (parameter.isPrimitive())
				{
					String typeStr = parameter.getType();
					
					if (typeStr.equals("I") || typeStr.equals("B") || typeStr.equals("C") || typeStr.equals("Z") || typeStr.equals("S"))
						mv.visitVarInsn(ILOAD, varIndex + i);
					else if (typeStr.equals("F"))
						mv.visitVarInsn(FLOAD, varIndex + i);
					else if (typeStr.equals("J"))
						mv.visitVarInsn(LLOAD, varIndex + i);
					else if (typeStr.equals("D"))
						mv.visitVarInsn(DLOAD, varIndex + i);
				}
				else
				{
					mv.visitVarInsn(ALOAD, varIndex + i);
				}
			}
			
			mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, className, rename, desc, false);
			mv.visitMethodInsn(INVOKESPECIAL, "net/sf/ehcache/Element", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/Cache", "put", "(Lnet/sf/ehcache/Element;)V", false);
			mv.visitLabel(l5);
			
			List<Object> paramArrayObjects = new ArrayList<Object>();
			
			if (!isStatic)
				paramArrayObjects.add(className);
			
			for (int i = 0; i < paramCount; i++)
			{
				AgentParameter parameter = method.getParameters()[i];
				paramArrayObjects.add(AgentUtils.getAsmClassText(parameter));
			}
			
			paramArrayObjects.add("java/util/List");
			paramArrayObjects.add("java/net/URL");
			paramArrayObjects.add("net/sf/ehcache/CacheManager");
			paramArrayObjects.add("net/sf/ehcache/Cache");
			
			mv.visitFrame(Opcodes.F_FULL, paramArrayObjects.size(), paramArrayObjects.toArray(new Object[]{}), 0, new Object[] {});
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/Cache", "flush", "()V", false);
			
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 3);
			mv.visitVarInsn(ALOAD, varIndex + paramCount);
			
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/Cache", "get", "(Ljava/lang/Object;)Lnet/sf/ehcache/Element;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/Element", "getObjectValue", "()Ljava/lang/Object;", false);
			mv.visitVarInsn(ASTORE, varIndex + paramCount + 4);
			
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/sf/ehcache/CacheManager", "shutdown", "()V", false);
			
			
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitVarInsn(ALOAD, varIndex + paramCount + 4);
			
			AgentParameter returnParameter = method.getReturnParameter();
			String typeStr = returnParameter.getType();
			if (returnParameter.isPrimitive())
			{
				mv.visitTypeInsn(CHECKCAST, typeStr);
				if (typeStr.equals("I") || typeStr.equals("Z") || typeStr.equals("C") || typeStr.equals("B") || typeStr.equals("S"))
					mv.visitInsn(IRETURN);
				else if (typeStr.equals("F"))
					mv.visitInsn(FRETURN);
				else if (typeStr.equals("J"))
					mv.visitInsn(LRETURN);
				else if (typeStr.equals("D"))
					mv.visitInsn(DRETURN);
			}
			else
			{
				typeStr = (String)AgentUtils.getAsmClassText(returnParameter);
				mv.visitTypeInsn(CHECKCAST, typeStr);
				mv.visitInsn(ARETURN);
			}
			
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
*/		
		class CacheMethod
		{
			int access;
			String name;
			String desc;
			String signature; 
			String[] exceptions;
			String rename;
			AgentMethod method;
			
			public CacheMethod(int access, String name, String desc, String signature, String[] exceptions, String rename, AgentMethod method) 
			{
				this.access = access;
				this.name = name;
				this.desc = desc;
				this.signature = signature;
				this.exceptions = exceptions;
				this.rename = rename;
				this.method = method;
			}

			public int getAccess() {
				return access;
			}

			public String getName() {
				return name;
			}

			public String getDesc() {
				return desc;
			}

			public String getSignature() {
				return signature;
			}

			public String[] getExceptions() {
				return exceptions;
			}

			public String getRename() {
				return rename;
			}

			public AgentMethod getMethod() {
				return method;
			}
			
		}
	}
	
	
	
	public static void main(String[] args) throws IllegalClassFormatException
	{
		Properties cmd = AgentUtils.parseArgs("");
    	
    	String cacheMethodFile = cmd.getProperty(AgentConstants.ARG_CACHE_FILE, AgentConstants.DEFAULT_XCACHE_METHOD_FILE);
    	List<AgentMethod> methods = AgentUtils.getCacheMethods(cacheMethodFile);//new ArrayList<Method>();//

		MethodCacheTransformer aa = new MethodCacheTransformer(methods);
		aa.transform(null, "com/ebay/platform/cache/MyUser1", null, null, null);
	}

}
