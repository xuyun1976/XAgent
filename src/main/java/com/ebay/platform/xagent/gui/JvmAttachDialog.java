package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;


@SuppressWarnings("serial")
public class JvmAttachDialog extends JDialog 
{
	private JTable jvmTable;
	private VMSelectedListener listener;
	
	public JvmAttachDialog(Frame frame) 
	{
		super(frame, "Choose JVM", true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(500,300); 
		centerScreen();
		
		addTable();
		addButtons();
		
		//pack();
	}
	
	public void setListener(VMSelectedListener listener) {
		this.listener = listener;
	}

	private void centerScreen()   
	{   
		Dimension dim = getToolkit().getScreenSize();   
		Rectangle bounds = getBounds();   
	    setLocation((dim.width - bounds.width) / 2, (dim.height - bounds.height) / 2);   
	    requestFocus();   
	}
	
	private void addTable()
	{
		JPanel mainPanel = new JPanel();
		//mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		//mainPanel.setLayout(new GridLayout(2,1));
		
		//mainPanel.setSize(300,300);
		mainPanel.setLayout(new BorderLayout());
		
		getContentPane().setLayout(new BorderLayout(5, 5));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JLabel label = new JLabel("Please Choose a Java Process:");
		label.setBorder(new EmptyBorder(10, 5, 10, 5));
		mainPanel.add(label, BorderLayout.NORTH);
		
		TableVMValue tv = new TableVMValue();
		List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
		for (VirtualMachineDescriptor vmd : vmds)
		{
			if (vmd.displayName().length() == 0)
				continue;
			
			tv.addElement(new VM(vmd)); 
		}
		
		jvmTable = new JTable(tv);
		jvmTable.setRowHeight(25);
		jvmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jvmTable.addMouseListener(new MouseAdapter()
			{
		    	public void mouseClicked(MouseEvent e) 
		    	{
		    		if(SwingUtilities.isLeftMouseButton(e) &&  e.getClickCount() == 2)
		    		{
		    			if (selected())
							JvmAttachDialog.this.dispose();      
		    		}
		    	}
		   });
		
		TableColumnModel tcm = jvmTable.getColumnModel();
		TableColumn firstColumn = tcm.getColumn(0); 
		firstColumn.setPreferredWidth(50);
		firstColumn.setMinWidth(50);
		
		TableColumn secondColumn = tcm.getColumn(1); 
		secondColumn.setPreferredWidth(350);
		secondColumn.setMinWidth(100);
		
		JScrollPane scrollPane = new JScrollPane(jvmTable);
		scrollPane.setSize(400, 250);
		
		mainPanel.add(scrollPane);
	}
	
	private void addButtons()
	{
		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (selected())
					JvmAttachDialog.this.dispose();
				
//				//VM vm = (VM)jvmComboBox.getSelectedItem();
//				
//				if (listener != null)
//					listener.selected(vm.getVmd());
			}
		});
		
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				JvmAttachDialog.this.dispose();
			}
		});
		
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}
	
	private boolean selected()
	{
		int row = jvmTable.getSelectedRow();
		
		if (row == -1 || listener == null)
			return false;
		
		TableVMValue tv = (TableVMValue)jvmTable.getModel();
		VM vm = tv.getValueAt(row);
		listener.selected(vm.getVmd(), vm.getPort());
		
		return true;
	}
	
	class VM 
	{
		private VirtualMachineDescriptor vmd;
		private int port = 0;
		
		public VM(VirtualMachineDescriptor vmd)
		{
			this.vmd = vmd;
		}
		
		public VirtualMachineDescriptor getVmd() {
			return vmd;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getProcessId()
		{
			return vmd.id();
		}
		
		public String getCommand()
		{
			return vmd.displayName();
		}
		
		@Override
		public String toString()
		{
			return vmd.displayName() + " " + vmd.id();
		}
	}
	
	class TableVMValue extends AbstractTableModel
	{  
        public String[] columnNames = new String[]{"Process ID", "Command", "Port"};  
        public List<VM> vms = new ArrayList<VM>();  
        
        public void addElement(VM vm) 
        {
        	vms.add(vm);
		}
		
        public int getColumnCount() 
        {  
            return columnNames.length;  
        }  
        
		public int getRowCount() 
		{  
        	return vms.size();  
        }
        
        public VM getValueAt(int row)
        {
        	if (row < vms.size())
        		return vms.get(row);
        	
        	return null;
        }
        
        public Object getValueAt(int rowIndex, int col) 
        {
        	VM vm = vms.get(rowIndex);
        	if (col == 0)
        		return vm.getProcessId();
        	else if (col == 1)
        		return vm.getCommand();
        	else
                return vm.getPort() == 0 ? "" : vm.getPort();  
        }
        
        public String getColumnName(int col)
        {  
        	return columnNames[col];  
        }
        
        public boolean isCellEditable(int row, int col)
        {
        	if (col == 2)
        	   return true;
        
        	return false;
        }
        
        public void setValueAt(Object value, int row, int col)
        { 
        	try
        	{
        		VM vm = vms.get(row);
        		vm.setPort(Integer.valueOf(value.toString()));
        	}
        	catch(Exception ex)
        	{
        	}
        	
        	this.fireTableCellUpdated(row, col);     
       }     
	}  
}
