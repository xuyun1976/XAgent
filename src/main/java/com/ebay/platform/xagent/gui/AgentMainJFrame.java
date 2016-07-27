package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import com.ebay.platform.xagent.AgentConstants;
import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.rmi.AgentRmiClient;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings("serial")
public class AgentMainJFrame extends JFrame implements VMSelectedListener
{
	private ClassListPanel classListPanel = new ClassListPanel();
	private ConsolePanel consolePanel = new ConsolePanel();
	private ClassEditorTabbedPanel classEditorTabbedPanel;
	private AgentRmiClient agentRmiClient;
	
	public AgentMainJFrame()
	{
		agentRmiClient = createAgentRmiClient();
		classEditorTabbedPanel = createClassEditorTabbedPanel();
		
		init();
		addMenu();
		showJvmAttachDialog();
	}
	
	private void init()
	{  
        setVisible(true);  
        setLocationRelativeTo(null);  
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        classListPanel.setListener(classEditorTabbedPanel);
        
        JSplitPane splitVPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, classEditorTabbedPanel, consolePanel);
        splitVPane.setOneTouchExpandable(true);
        splitVPane.setDividerLocation(2000);
        
        JSplitPane splitHPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, classListPanel, splitVPane);
        splitHPane.setOneTouchExpandable(true);
        splitHPane.setDividerLocation(300);

        getContentPane().add(splitHPane);
    }
	
	private AgentRmiClient createAgentRmiClient()
	{
		int port = AgentUtils.getAvailablePort();
		return new AgentRmiClient(port);
	}
	
	private ClassEditorTabbedPanel createClassEditorTabbedPanel()
	{
		return new ClassEditorTabbedPanel(this.agentRmiClient);
	}
	
	private void addMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem attachMenuItem = new JMenuItem("Attach VM...");
		fileMenu.add(attachMenuItem);
		attachMenuItem.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					showJvmAttachDialog();
				}
			});

		fileMenu.addSeparator();
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		fileMenu.add(exitMenuItem);

		exitMenuItem.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					System.exit(0);
				}
			});
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}
	
	
	private void showJvmAttachDialog()
	{
		JvmAttachDialog jvmAttachDialog = new JvmAttachDialog(this);
        jvmAttachDialog.setListener(this);
        
        jvmAttachDialog.setAlwaysOnTop(true);
        jvmAttachDialog.setVisible(true);
	}

	@Override
	public void selected(VirtualMachineDescriptor vmd) 
	{
		AgentUtils.attachVM(vmd, AgentConstants.ARG_RMI_PORT + "=" + agentRmiClient.getRmiPort());
		
		List<String> classes = agentRmiClient.getAllLoadedClasses();
		
		classListPanel.refreshClassList(classes);
	}
	
	
}
