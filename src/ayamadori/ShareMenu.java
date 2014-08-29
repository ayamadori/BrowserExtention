package ayamadori;

import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import org.tantalum.jme.RMSUtils;
import org.tantalum.util.StringUtils;


public class ShareMenu implements CommandListener {

	private MIDlet midlet;
	private Display display;
	private String text;
	private List shareList;
	private Command back;
	private Alert errorMessageAlert;
//	private OneNoteClient onenoteClient;
	private GoogleTranslateClient googleClient;
	private RMSUtils pref;
	private int index;

	public ShareMenu(MIDlet midlet, String text) {
		this.midlet = midlet;
		this.text = text;
//		onenoteClient = new OneNoteClient();
		googleClient = new GoogleTranslateClient();
		
		pref = RMSUtils.getInstance();
		String id = null;
		try {
			id = new String(pref.read("langid"));
		} catch (Exception e) {
		} finally {
			if(id == null) id = GoogleTranslateClient.ID_JAPANESE; // default is JP
			index = (id == null)? 40 : Integer.parseInt(id); // default is 40(=JP)
		}

		errorMessageAlert = new Alert("Error", null, null, AlertType.ERROR);
		errorMessageAlert.setTimeout(5000);
	}
	
	public void openBrowserMenu(){
		try {
			shareList = new List("BrowserExtention", List.IMPLICIT);
//			shareList.append("Clip to OneNote", Image.createImage("/icon/onenote.png"));
			shareList.append("Translate", Image.createImage("/icon/google_translate.png"));
			shareList.append("Mobile view", Image.createImage("/icon/google.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		back = new Command("Back", Command.BACK, 1);
		shareList.addCommand(back);
		shareList.setCommandListener(this);
		display = Display.getDisplay(midlet);
		display.setCurrent(shareList);
	}

	public void commandAction(Command command, Displayable displayable) {
		if (command == List.SELECT_COMMAND) {
			try {
				switch (shareList.getSelectedIndex()) {
					case 1:
//						launch(googleClient.getAccessURL(index, StringUtils.urlEncode(text)));
						launch("http://www.google.co.jp/gwt/x?source=wax&ie=UTF-8&oe=UTF-8&u="
								+ StringUtils.urlEncode(text));
						midlet.notifyDestroyed();
						break;
//					case 2:
//						launch("http://www.google.co.jp/gwt/x?source=wax&ie=UTF-8&oe=UTF-8&u="
//								+ StringUtils.urlEncode(text));
//						midlet.notifyDestroyed();
//						break;
					default:
//						onenoteClient.Clip(text);
//						onenoteClient.Clip(display, text);
//						launch("mailto:me@onenote.com?subject=" + StringUtils.urlEncode(text) + "&body=" + StringUtils.urlEncode(text));
						launch(googleClient.getAccessURL(index, StringUtils.urlEncode(text)));
						midlet.notifyDestroyed();
						break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (command == back) {
			midlet.notifyDestroyed();
		}
    }
	
	private void launch(String url) {
		try {
			midlet.platformRequest(url);
		} catch (Exception e) {
			errorMessageAlert.setString("Failed to launch");
			display.setCurrent(errorMessageAlert, shareList);
			e.printStackTrace();
		}
	}

}
