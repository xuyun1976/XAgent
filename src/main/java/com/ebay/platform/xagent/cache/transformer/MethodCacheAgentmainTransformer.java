package com.ebay.platform.xagent.cache.transformer;


import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.ebay.platform.xagent.cache.AgentMethod;

public class MethodCacheAgentmainTransformer extends MethodCacheAbstractTransformer
{
    public MethodCacheAgentmainTransformer(List<AgentMethod> methods) 
    {
		super(methods);
	}

	@Override
	protected ClassVisitor getClassVisitor(ClassWriter cw, String className) 
	{
		return new AgentmainClassAdapter(Opcodes.ASM4, cw, className);
	}
	
	class AgentmainClassAdapter extends ClassVisitor implements Opcodes
	{
		@SuppressWarnings("unused")
		private String className;
		
		public AgentmainClassAdapter(int paramInt,	ClassVisitor paramClassVisitor, String className) 
		{
			super(paramInt, paramClassVisitor);
			this.className = className;
		}
		
	}
}
