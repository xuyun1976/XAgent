package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ebay.platform.xagent.rmi.AgentRmiClient;
import com.ebay.platform.xagent.rmi.ClassInfo;
import com.ebay.platform.xagent.rmi.MethodInfo;
import com.ebay.platform.xagent.rmi.NodeInfo;

@SuppressWarnings("serial")
public class ClassListPanel extends JPanel
{
	private DefaultMutableTreeNode root = new InvisibleNode("Root");
	
	private AgentRmiClient agentRmiClient;
	private JTextField classFilter;
	private JTree classTree;
	private ImageIcon javaIcon = new ImageIcon(ClassListPanel.class.getClassLoader().getResource("java.png"));
	
	private ClassSelectedListener listener;
	
	public ClassListPanel(AgentRmiClient agentRmiClient)
	{
		this.agentRmiClient = agentRmiClient;
		
		init();
	}
	
	public void setListener(ClassSelectedListener listener) 
	{
		this.listener = listener;
	}

	private void init()
	{
		setLayout(new BorderLayout(5, 5));
		
		Dimension minimumSize = new Dimension(200, 50);
        setMinimumSize(minimumSize);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(400, 80));
        
        classFilter = createFilterTextField();
        classTree = createClassTree();
        
        JScrollPane listScroller = new JScrollPane(classTree);
        listScroller.setPreferredSize(new Dimension(400, 80));
        
        add(classFilter, BorderLayout.NORTH);
		add(listScroller, BorderLayout.CENTER);
	}
	
	private JTree createClassTree()
	{
		InvisibleTreeModel ml = new InvisibleTreeModel(root);
		ml.activateFilter(true);
		 
		final JTree classTree = new JTree(ml);
		classTree.setRootVisible(false);
		classTree.setCellRenderer(new ClassTreeRender());
		
		classTree.addMouseListener(new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent evt) 
            {
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode)classTree.getLastSelectedPathComponent();
                if (node == null) 
                	return;
                
                NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
                
                if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2 && nodeInfo instanceof ClassInfo) 
            	{
            		listener.selected(nodeInfo.toString());
            	}
            	else if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1 && nodeInfo instanceof ClassInfo)
            	{
            		if (node.getChildCount() == 0)
            			loadMethods(node, ((ClassInfo)nodeInfo).getClassName());
            		
            		
            		expandNode(new TreePath(node.getPath()), true);
            	}
            	else if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() == 1 && nodeInfo instanceof MethodInfo)
            	{
            		popupMenu(node, evt);
            	}
                    
            }
        });

		return classTree;
	}
	
	@SuppressWarnings("rawtypes")
	private void expandNode(TreePath parent, boolean expand)
    {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0)
        {
            Enumeration e = node.children();
            while(e.hasMoreElements())
            {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandNode(path, expand);
            }
        }
        if(expand)
        	classTree.expandPath(parent);
        else
        	classTree.collapsePath(parent);
    }
	
	private void popupMenu(final DefaultMutableTreeNode node, MouseEvent e)
	{
		JPopupMenu pop = new JPopupMenu();
        JMenuItem cacheEnable = new JMenuItem("Cache Enable");
        cacheEnable.addMouseListener(new MouseAdapter() 
        {
            public void mouseReleased(MouseEvent e) 
            {
            	cacheEnable(node);  
            }
        });
        pop.add(cacheEnable);
        
        pop.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void cacheEnable(DefaultMutableTreeNode node)
	{
		try
		{
			MethodInfo methodInfo = (MethodInfo)node.getUserObject();
			agentRmiClient.cache(methodInfo);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void loadMethods(DefaultMutableTreeNode node, String className)
	{
		try
		{
			List<MethodInfo> methods = agentRmiClient.getMethods(className);
			
			for (MethodInfo method : methods)
				node.add(new InvisibleNode(method));
			
			DefaultTreeModel model = (DefaultTreeModel)classTree.getModel();
			model.reload(root);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private JTextField createFilterTextField() 
	{
        final JTextField field = new JTextField(15);
        field.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override 
            public void insertUpdate(DocumentEvent e) 
            { 
            	filter(); 
            }
            
            @Override 
            public void removeUpdate(DocumentEvent e) 
            { 
            	filter(); 
            }
            
            @Override 
            public void changedUpdate(DocumentEvent e) 
            {
            	
            }
            
            private void filter() 
            {
                String filter = field.getText();
                filterModel(root, filter.toLowerCase());
            }
        });
        return field;
    }
	
	public void filterModel(DefaultMutableTreeNode root, String filter) 
	{
		for (int i = 0; i < root.getChildCount(); i++)
		{
			InvisibleNode node = (InvisibleNode)root.getChildAt(i);
			
			NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
			if (nodeInfo instanceof ClassInfo)
			{
				String className = ((ClassInfo)nodeInfo).getClassName();
				if (!className.toLowerCase().contains(filter))
					node.setVisible(false);
				else
					node.setVisible(true);
			}
		}
		
		DefaultTreeModel model = (DefaultTreeModel)classTree.getModel();
		model.reload();
    }
	
	public void refreshClassList(List<ClassInfo> classes)
	{
		if (classes == null)
			return;
		
		root.removeAllChildren();
		
		for (ClassInfo clz : classes)
			root.add(new InvisibleNode(clz));
		
		DefaultTreeModel model = (DefaultTreeModel)classTree.getModel();
		model.reload();
	}
	
	class ClassTreeRender extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) 
		{
		      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		      
		      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		      if (!(node.getUserObject() instanceof NodeInfo))
		    	  return this;
		    	
		      NodeInfo nodeInfo = (NodeInfo)(node.getUserObject());
		      if (nodeInfo instanceof ClassInfo)
		      {
		    	  this.setIcon(javaIcon);
		    	  this.setToolTipText(((ClassInfo)nodeInfo).getClassName());
		      }
		   
		      return this;
		}
	}
	
	class InvisibleTreeModel extends DefaultTreeModel 
	{
		protected boolean filterIsActive;
		
		public InvisibleTreeModel(TreeNode root) 
		{
			this(root, false);
		}

		public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren) 
		{
			this(root, false, false);
		}

		public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren, boolean filterIsActive) 
		{
		    super(root, asksAllowsChildren);
		    this.filterIsActive = filterIsActive;
		}

		public void activateFilter(boolean newValue) 
		{
		    filterIsActive = newValue;
		}

		public boolean isActivatedFilter() 
		{
		    return filterIsActive;
		}

		public Object getChild(Object parent, int index) 
		{
			if (filterIsActive) 
			{
		    	if (parent instanceof InvisibleNode) 
		    	{
		    		return ((InvisibleNode) parent).getChildAt(index, filterIsActive);
		    	}
		    }
			
		    return ((TreeNode) parent).getChildAt(index);
		}

		public int getChildCount(Object parent) 
		{
		    if (filterIsActive) 
		    {
		    	if (parent instanceof InvisibleNode) 
		    	{
		    		return ((InvisibleNode) parent).getChildCount(filterIsActive);
		    	}
		    }
		    
		    return ((TreeNode) parent).getChildCount();
		}
	}
	
	class InvisibleNode extends DefaultMutableTreeNode 
	{
		protected boolean isVisible;
		
		public InvisibleNode() 
		{
			this(null);
		}

		public InvisibleNode(Object userObject) 
		{
		    this(userObject, true, true);
		}

		public InvisibleNode(Object userObject, boolean allowsChildren, boolean isVisible) 
		{
		    super(userObject, allowsChildren);
		    this.isVisible = isVisible;
		}

		@SuppressWarnings("rawtypes")
		public TreeNode getChildAt(int index, boolean filterIsActive) 
		{
			if (!filterIsActive) 
			{
		      return super.getChildAt(index);
		    }
		    
			if (children == null) 
			{
		      throw new ArrayIndexOutOfBoundsException("node has no children");
		    }

		    
			int realIndex = -1;
		    int visibleIndex = -1;
		    Enumeration e = children.elements();
		    
		    while (e.hasMoreElements()) 
		    {
		    	InvisibleNode node = (InvisibleNode) e.nextElement();
		    	if (node.isVisible()) 
		    	{
		    		visibleIndex++;
		    	}
		    	realIndex++;
		    	if (visibleIndex == index) 
		    	{
		    		return (TreeNode) children.elementAt(realIndex);
		    	}
		    }

		    throw new ArrayIndexOutOfBoundsException("index unmatched");
		}
		
		@SuppressWarnings("rawtypes")
		public int getChildCount(boolean filterIsActive) 
		{
			if (!filterIsActive) 
				return super.getChildCount();
		    
			if (children == null) 
		      return 0;
		    
		    int count = 0;
		    Enumeration e = children.elements();
		    
		    while (e.hasMoreElements()) 
		    {
		    	InvisibleNode node = (InvisibleNode) e.nextElement();
		    	if (node.isVisible())
		    		count++;
		    }

		    return count;
		}

		public void setVisible(boolean visible) 
		{
			this.isVisible = visible;
		}

		public boolean isVisible() 
		{
			return isVisible;
		}
	}
}
