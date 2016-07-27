package com.ebay.platform.xagent.gui;

import javax.swing.SwingUtilities;

public class XAgentGuiLauncher 
{
	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{  
            public void run() 
            {  
            	new AgentMainJFrame();
            }                 
        });

	}

}
