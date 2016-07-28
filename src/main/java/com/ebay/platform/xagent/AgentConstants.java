package com.ebay.platform.xagent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class AgentConstants 
{
	public static final boolean ENABLE_DEBUG = true;
	
	public static final String AGNET_NAME = "xagent";
	
	public static final String ARG_RMI_PORT = "port";
	
	public static final String ARG_CACHE_FILE = "cacheFile";
	public static final String DEFAULT_XCACHE_METHOD_FILE = "xcache_method.txt";
	
	
	public static final String ARG_RUNTIME_DIR = "runtimeDir";
	public static final String RUNTIME_SUCCESS_DIR = "success";
	public static final String RUNTIME_FAILED_DIR = "failed";
	public static final String RUNTIME_COMPILE_OUT_DIR = "bin";
	
	public static final String DEAFULT_XAGENT_DIR = System.getProperty("user.home") + File.separator + "." + AGNET_NAME;
	public static final String DEFAULT_DEBUG_DIR = DEAFULT_XAGENT_DIR + File.separator + "debug";
	public static final String DEFAULT_RUNTIME_DIR = DEAFULT_XAGENT_DIR + File.separator + "runtime";
	public static final String DEFAULT_TEMP_DIR = DEAFULT_XAGENT_DIR + File.separator + "temp";

	public static final List<String> EXCLUDE_DIRS = Arrays.asList(new String[]{RUNTIME_SUCCESS_DIR, RUNTIME_FAILED_DIR});
	
	public static final String[] EXCLUDE_CLASS_NAMES = new String[]{"[", "java.", "javax.", "com.sun.", "jdk.", "sun."};
	
	public static final String jarFilePath = "C:\\GitHub\\XAgent\\target\\XAgent-0.0.1-SNAPSHOT-jar-with-dependencies.jar";

}
