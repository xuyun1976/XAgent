package com.ebay.platform.xagent.cache;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;

public class AgentMethod implements Cloneable
{
	private final String primitiveTypes = "IBCZSFJD";
	private String className;
	private String methodName;
	private boolean staticMethod;
	private AgentParameter returnParameter;
	private AgentParameter[] parameters;
	
	public AgentMethod(String className, String methodName)
	{
		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public AgentParameter getReturnParameter() {
		return returnParameter;
	}

	public AgentParameter[] getParameters() {
		return parameters;
	}

	public boolean isStaticMethod() {
		return staticMethod;
	}

	public AgentMethod parse(int access, String desc)
	{
		AgentMethod clone = this.clone();
		
		clone.staticMethod = (access & Opcodes.ACC_STATIC) != 0;
		
		List<AgentParameter> parameters = new ArrayList<AgentParameter>();
		
		int index = 0;
		while (index < desc.length())
		{
			char ch = desc.charAt(index);
			if (primitiveTypes.indexOf(ch) != -1)
			{
				parameters.add(new AgentParameter(String.valueOf(ch)));
			}
			else if (ch == 'L' || ch == '[')
			{
				int end = desc.indexOf(';', index);
				parameters.add(new AgentParameter(desc.substring(index, end + 1)));
				index = end;
			}
			else if (ch == ')')
			{
				clone.returnParameter = new AgentParameter(desc.substring(index + 1));
				break;
			}
			
			index++;
		}
		
		clone.parameters = parameters.toArray(new AgentParameter[]{});
		
		return clone;
	}
	
	@Override
	public AgentMethod clone()
	{
		try 
		{
			AgentMethod clone = (AgentMethod)super.clone();
			
			return clone;
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public class AgentParameter
	{
		private boolean primitive;
		private String type;
		
		public AgentParameter(String type)
		{
			this.type = type;
			this.primitive = type.length() == 1 && primitiveTypes.indexOf(type) != -1;
		}

		public boolean isPrimitive() {
			return primitive;
		}

		public String getType() {
			return type;
		}
	}
}
