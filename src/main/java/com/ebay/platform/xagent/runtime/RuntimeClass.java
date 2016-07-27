package com.ebay.platform.xagent.runtime;

import java.io.File;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.ebay.platform.xagent.AgentUtils;

public class RuntimeClass 
{
	private File classFile;
	private String className;
	private byte[] classfileBuffer;
	
	public RuntimeClass(File classFile) throws Exception
	{
		this.classFile = classFile;
		classfileBuffer = AgentUtils.getBytesByFile(this.classFile);
		
		ClassReader reader = new ClassReader(classfileBuffer);
		reader.accept(new RuntimeClassAdapter(), 0);
	}

	public String getClassName() 
	{
		return className;
	}

	public byte[] getClassfileBuffer() throws Exception 
	{
		return classfileBuffer;
	}

	class RuntimeClassAdapter extends ClassVisitor
	{
		public RuntimeClassAdapter() 
		{
			super(Opcodes.ASM4);
		}
		
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
		{
			className = name;
		}
	}
	
}
