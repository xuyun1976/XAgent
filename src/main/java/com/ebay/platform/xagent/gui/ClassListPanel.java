package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class ClassListPanel extends JPanel
{
	private JTextField classFilter;
	private JList<String> classList;
	
	private List<String> classes;
	
	private ClassSelectedListener listener;
	
	public ClassListPanel()
	{
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
		classList = createClassList();
        
        JScrollPane listScroller = new JScrollPane(classList);
        listScroller.setPreferredSize(new Dimension(400, 80));
        
        add(classFilter, BorderLayout.NORTH);
		add(listScroller, BorderLayout.CENTER);
	}
	
	private JList<String> createClassList()
	{
		JList<String> classList = new JList<String>(new DefaultListModel<String>());
        classList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        classList.setLayoutOrientation(JList.VERTICAL);
        classList.setVisibleRowCount(-1);
        
        classList.addMouseListener(new MouseAdapter() 
        {
            @SuppressWarnings("unchecked")
			public void mouseClicked(MouseEvent evt) 
            {
                JList<String> list = (JList<String>)evt.getSource();
                if (evt.getClickCount() == 2) 
                {
                	int index = list.locationToIndex(evt.getPoint());
                	
                	listener.selected(list.getModel().getElementAt(index));
                }          
            }
        });

		return classList;
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
                filterModel((DefaultListModel<String>)classList.getModel(), filter);
            }
        });
        return field;
    }
	
	public void filterModel(DefaultListModel<String> model, String filter) 
	{
		String filterLC = filter.toLowerCase();
		
        for (String s : classes) 
        {
            if (!s.toLowerCase().contains(filterLC))
            {
                if (model.contains(s)) 
                {
                    model.removeElement(s);
                }
            } 
            else 
            {
                if (!model.contains(s)) 
                {
                    model.addElement(s);
                }
            }
        }
        
    }
	
	public void refreshClassList(List<String> classes)
	{
		if (classes == null)
			return;
		
		this.classes = classes;
		
		DefaultListModel<String> model = (DefaultListModel<String>)classList.getModel();
		
		model.removeAllElements();
		
		for (String clz : classes)
			model.addElement(clz);
	}
}
