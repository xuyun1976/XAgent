package com.ebay.platform.xagent.cli;

import java.util.List;
import java.util.Scanner;

import com.ebay.platform.xagent.AgentUtils;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class XAgentCLILauncher 
{
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
            AgentUtils.attachVM(vmd, "");
            
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
    	//String classFilePath = XAgentCLILauncher.class.getResource("").getPath();  
    	//String path = MyJavaAgentLoader.class.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    	
    	List<VirtualMachineDescriptor> vmds = XAgentCLILauncher.listVirtualMachines();
    	VirtualMachineDescriptor vmd = XAgentCLILauncher.chooseVirtualMachine(vmds);
    	XAgentCLILauncher.attachVirtualMachine(vmd);
    }
}
