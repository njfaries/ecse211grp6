package gui;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import universal.Universal;

import gui.defaults.DPMPanel;

@SuppressWarnings("serial")
public class SendSelectPanel extends DPMPanel implements ActionListener{
	private CheckboxGroup sendSelect;
	private Checkbox sendBoth, sendB, sendG;

	public SendSelectPanel(MainWindow mw) {
		super(mw);
		this.layoutPanel();
	}


	private void layoutPanel() {
		GridBagConstraints c = new GridBagConstraints();
		this.setFont(new Font("Helvetica", Font.PLAIN, 14));
		this.setLayout(new GridBagLayout());
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.ipady = 1;
		Label sendTo = new Label("Connect to:", Label.CENTER);
		this.add(sendTo, c);
		
		//add checkbox group
		sendSelect = new CheckboxGroup();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		Checkbox sendBoth = new Checkbox("B and G", sendSelect, true);
		this.add(sendBoth, c);
		c.gridx = 1;
		sendB = new Checkbox("B only", sendSelect, false);
		this.add(sendB, c);
		c.gridx = 2;
		sendG = new Checkbox("G only", sendSelect, false);
		this.add(sendG, c);
	}
	
	public Universal.TransmitRule getSendSelction(){
		Checkbox selection = this.sendSelect.getSelectedCheckbox();
		if(selection.equals(this.sendBoth))return Universal.TransmitRule.BOTH;
		else if(selection.equals(this.sendB))return Universal.TransmitRule.BUILDER_ONLY;
		else if(selection.equals(this.sendG))return Universal.TransmitRule.GARBAGECOLLECTOR_ONLY;
		else return Universal.TransmitRule.BOTH;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
