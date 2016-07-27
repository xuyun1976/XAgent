package com.ebay.platform.xagent.test.common;

import java.util.List;
import java.util.Scanner;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class MyJavaAgentLoader 
{

    private static final String jarFilePath = "C:\\GitHub\\XAgent\\target\\XAgent-0.0.1-SNAPSHOT-jar-with-dependencies.jar";

    /*
    public static void loadAgent() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFilePath, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */
    
    private static List<VirtualMachineDescriptor> listVirtualMachines()
    {
    	List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
    	
    	System.out.println("All the java processes:");
    	for (VirtualMachineDescriptor vmd : vmds)
    		System.out.println(vmd);
    	
    	return vmds;
    }
    
    private static VirtualMachineDescriptor chooseVirtualMachine(List<VirtualMachineDescriptor> vmds)
    {
    	System.out.println("Please input a java process ID:");
    	
    	do
    	{
    		Scanner sc = new Scanner(System.in);
    		
    		try
    		{
    			int tmp = sc.nextInt();
    			sc.close();
    			
    			for (VirtualMachineDescriptor vmd : vmds)
    			{
    				if (Integer.valueOf(vmd.id()) == tmp)
    					return vmd;
    			}
    			
    			throw new Exception();
    		}
    		catch(Exception ex)
    		{
    			System.out.println("Please input correct PID:");
    		}
    		
    	} while (true);
    	
    }
    
    private static void attachVirtualMachine(VirtualMachineDescriptor vmd)
    {
    	System.out.println("choosed:" + vmd.id());
    	
    	try 
    	{
            VirtualMachine vm = VirtualMachine.attach(vmd);
            vm.loadAgent(jarFilePath, "");
            Thread.sleep(100000);
            //vm.detach();
        } 
    	catch (Exception e) 
    	{
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args)
    {
    	String classFilePath = MyJavaAgentLoader.class.getResource("").getPath();  
    	//String path = MyJavaAgentLoader.class.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    	
    	List<VirtualMachineDescriptor> vmds = MyJavaAgentLoader.listVirtualMachines();
    	VirtualMachineDescriptor vmd = MyJavaAgentLoader.chooseVirtualMachine(vmds);
    	MyJavaAgentLoader.attachVirtualMachine(vmd);
    }
}
