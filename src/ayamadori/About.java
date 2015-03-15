package ayamadori;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

public class About implements CommandListener {
	
	private final String TEXT =
			"URLTranslator\n" +
			"\n" +
			"Version 1.1.0\n" +
			"\n" +
			"Copyright (c) ayamadori all rights reserved.\n" +
			"<https://sites.google.com/site/ayamadori/>\n";
	
	private MIDlet midlet;
	private Command cmdBack;
	private Form window;
	private Displayable parent;
	
	public About(MIDlet midlet, Displayable parent) {
		window = new Form("About");		
		window.append(TEXT);
		
		cmdBack = new Command("Back", Command.BACK, 0);
		window.addCommand(cmdBack);
	
		this.midlet = midlet;
		this.parent = parent;
		Display.getDisplay(this.midlet).setCurrent(window);
		
		window.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == cmdBack) {
			Display.getDisplay(midlet).setCurrent(parent);
		}
	}
}
