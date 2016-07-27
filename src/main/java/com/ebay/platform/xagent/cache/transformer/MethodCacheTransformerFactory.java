package com.ebay.platform.xagent.cache.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

import com.ebay.platform.xagent.cache.AgentMethod;

public class MethodCacheTransformerFactory 
{
	public static ClassFileTransformer create(List<AgentMethod> methods, boolean isAgentmain)
	{
		if (isAgentmain)
			return new MethodCachePremainTransformer(methods);
		else
			return new MethodCachePremainTransformer(methods);
	}
}
