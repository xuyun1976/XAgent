package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.ebay.platform.xagent.AgentUtils;
import com.ebay.platform.xagent.rmi.AgentRmiClient;
import com.ebay.platform.xagent.runtime.decompiler.Decompiler;

@SuppressWarnings("serial")
public class ClassEditorTabbedPanel extends JTabbedPane implements ClassSelectedListener
{
	private AgentRmiClient agentRmiClient;
	
	public ClassEditorTabbedPanel(AgentRmiClient agentRmiClient)
	{
		this.agentRmiClient = agentRmiClient;
		
		init();
	}
	
	private void init()
	{
		addMouseListener(new MouseListenerImpl());
	}
	
	public void addClassEditorTab(String simpleClassName, String className)
	{
		JTextArea classTextArea = new JTextArea();
		
		JScrollPane listScroller = new JScrollPane(classTextArea);
        listScroller.setPreferredSize(new Dimension(400, 80));
        
        add(listScroller, BorderLayout.CENTER);
        addTab(simpleClassName, null, listScroller, className);
        
        byte[] buffer = agentRmiClient.getClassfileBuffer(className);
        
        String java = Decompiler.decompile(buffer);
        
        classTextArea.setText(java);
    }

	@Override
	public void selected(String className) 
	{
		int index = className.lastIndexOf(".");
        String simpleClassName = className.substring(index + 1);
        
        index = indexOfTab(simpleClassName);
        
        if (index == -1)
        {
        	addClassEditorTab(simpleClassName, className);
        	index = this.getTabCount() - 1;
        }
        
        setSelectedIndex(index);
	}
	
	public void save(int index)
	{
		try
		{
			JScrollPane scrollPane = (JScrollPane)getComponentAt(index);
			JTextArea classTextArea = (JTextArea)scrollPane.getViewport().getView();
			String fileName = getTitleAt(index) + ".java";
			
			AgentUtils.save(fileName, classTextArea.getText());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	class MouseListenerImpl implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			if(!SwingUtilities.isRightMouseButton(e))
				return;
			
			showPopupMenu(e);
		}

		@Override
		public void mousePressed(MouseEvent e) 
		{
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
		
		private void showPopupMenu(final MouseEvent e) 
		{
	        {
				final int index = ClassEditorTabbedPanel.this.indexAtLocation(e.getX(), e.getY());
				final int count = ClassEditorTabbedPanel.this.getTabCount();
				
				if (index == -1)
					return;

	            JPopupMenu pop = new JPopupMenu();
	            JMenuItem closeCurrent = new JMenuItem("Close");
	            closeCurrent.addMouseListener(new MouseAdapter() 
	            {
	                public void mouseReleased(MouseEvent e) 
	                {
	                	ClassEditorTabbedPanel.this.removeTabAt(index);  
	                }
	            });
	            pop.add(closeCurrent);
	             
	            JMenuItem closeOthers = new JMenuItem("Close Others");
	            closeOthers.addMouseListener(new MouseAdapter() 
	            {
	                public void mouseReleased(MouseEvent e) 
	                {
	                    for(int j = count - 1;j >= 0; j--) 
	                    {
	                    	if (j == index)
	                    		continue;
	                    	
	                    	ClassEditorTabbedPanel.this.removeTabAt(j);  
	                    }
	                }
	            });
	            pop.add(closeOthers);
	             
	            JMenuItem closeAll = new JMenuItem("Close All");
	            closeAll.addMouseListener(new MouseAdapter() 
	            {
	                public void mouseReleased(MouseEvent e) 
	                {
	                    ClassEditorTabbedPanel.this.removeAll();;  
	                }
	            });
	            pop.add(closeAll);
	            
	            pop.addSeparator();
	            
	            JMenuItem save = new JMenuItem("Save");
	            save.addMouseListener(new MouseAdapter() 
	            {
	                public void mouseReleased(MouseEvent e) 
	                {
	                    save(index);  
	                }
	            });
	            pop.add(save);
	             
	            pop.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}
	
}
