package gui;
import gui.defaults.*;

import java.awt.*;

@SuppressWarnings("serial")
public class BluetoothOutput extends DPMPanel {

	private TextArea output;
	
	public BluetoothOutput(MainWindow mw) {
		super(mw);
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		c.gridx = 0;
		c.gridy = 0;
		Label lbl = new Label("Bluetooth Output", Label.CENTER);
		lbl.setFont(new Font("Serif", Font.BOLD, 32));
		this.add(lbl, c);
		
		c.gridx = 0;
		c.gridy = 1;
		output = new TextArea("",20,70, TextArea.SCROLLBARS_BOTH);
		output.setEditable(false);
		this.add(output, c);
	}
	
	public void append(String string, boolean secondNewline) {
		if (secondNewline)
			output.append("\n" + string + "\n");
		else
			output.append("\n" + string);
	}
	
	public void clear() {
		this.output.replaceRange("", 0, this.output.getCaretPosition());
	}

}
