package com.ebay.platform.xagent.gui;

import com.sun.tools.attach.VirtualMachineDescriptor;

public interface VMSelectedListener 
{
	public void selected(VirtualMachineDescriptor vmd);
}
