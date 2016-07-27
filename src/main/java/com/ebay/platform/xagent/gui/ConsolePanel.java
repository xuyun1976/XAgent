package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class ConsolePanel extends JPanel
{
	private JTextArea consoleTextArea = new JTextArea();
	
	public ConsolePanel()
	{
		init();
	}
	
	private void init()
	{
		setLayout(new BorderLayout(5, 5));
		
		Dimension minimumSize = new Dimension(200, 50);
        setMinimumSize(minimumSize);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane listScroller = new JScrollPane(consoleTextArea);
        listScroller.setPreferredSize(new Dimension(400, 80));
        
        add(listScroller, BorderLayout.CENTER);
    }
}
