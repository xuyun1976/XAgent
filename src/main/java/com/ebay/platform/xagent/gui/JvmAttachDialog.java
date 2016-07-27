package com.ebay.platform.xagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;


@SuppressWarnings("serial")
public class JvmAttachDialog extends JDialog 
{
	private JComboBox<VM> jvmComboBox;
	private VMSelectedListener listener;
	
	public JvmAttachDialog(Frame frame) 
	{
		super(frame, "Choose JVM", true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(300,400); 
		centerScreen();
		
		addList();
		addButtons();
		
		pack();
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
	
	private void addList()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new GridLayout(2,1));
		getContentPane().setLayout(new BorderLayout(5, 5));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JLabel label = new JLabel("Please Choose a Java Process:");
		mainPanel.add(label);
		
		DefaultComboBoxModel<VM> model = new DefaultComboBoxModel<VM>();
		List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
		for (VirtualMachineDescriptor vmd : vmds)
		{
			if (vmd.displayName().length() == 0)
				continue;
			
			model.addElement(new VM(vmd));
		}
		
		jvmComboBox = new JComboBox<VM>(model);
	
		mainPanel.add(jvmComboBox);
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
				JvmAttachDialog.this.dispose();
				
				VM vm = (VM)jvmComboBox.getSelectedItem();
				
				if (listener != null)
					listener.selected(vm.getVmd());
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
	
	class VM 
	{
		private VirtualMachineDescriptor vmd;
		
		public VM(VirtualMachineDescriptor vmd)
		{
			this.vmd = vmd;
		}
		
		public VirtualMachineDescriptor getVmd() {
			return vmd;
		}

		@Override
		public String toString()
		{
			return vmd.displayName() + " " + vmd.id();
		}
	}
}
