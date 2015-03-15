package ayamadori;

import javax.microedition.content.ContentHandlerException;
import javax.microedition.content.ContentHandlerServer;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.tantalum.PlatformUtils;
import org.tantalum.jme.RMSUtils;
import org.tantalum.util.L;

public class BrowserExtention extends MIDlet {

    private Display display = null;
 // Current invocation, null if no Invocation.
    private Invocation invocation;
 // ContentHandlerServer from which to get requests.
    private ContentHandlerServer handler;
//    public Form settingForm;
    private ShareMenu menu;
    private boolean isLaunchByInvocation;
    private String url;
	private RMSUtils pref;
	private MIDlet midlet = this;
	private TextField urlField;
    
	public BrowserExtention() {
		// TODO Auto-generated constructor stub
	}

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		L.i("destroyApp", "unconditional=" + unconditional);
		System.out.println("Destroyed-->");
//		PlatformUtils.getInstance().shutdown(unconditional, "Shutting down.");
		PlatformUtils.getInstance().shutdown("Shutting down.");
//		notifyDestroyed();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	protected void startApp() throws MIDletStateChangeException {
		// enable tantalum library
		PlatformUtils.getInstance().setProgram(this, 3);
		pref = RMSUtils.getInstance();
		
		// TODO Auto-generated method stub
		isLaunchByInvocation = false;
		try {
			handler = Registry.getServer(getClass().getName());
			invocation = handler.getRequest(false);
	        if (invocation != null) {
	        	isLaunchByInvocation = true;
	        	System.out.println("Launch by Invocation-->");
	        	String args[] = invocation.getArgs();
	        	for(int i=0; i<args.length; i++) {
	        		System.out.println(args[i]);
	        		if(args[i].startsWith("text=")) {
	        			url = args[i].substring(5);
	        			break;
	        		}
	        	}
//	        	handler.finish(invocation, Invocation.OK);
	        } else {
	        	System.out.println("Launch normal-->");
	        }
	        
		} catch (ContentHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
        if (this.display == null) {
        	this.display = Display.getDisplay(this);
            if(isLaunchByInvocation) {
            	if(url.startsWith("http")) {
            		this.menu = new ShareMenu(this, url);
            		this.menu.openBrowserMenu();
            	} else {
            		Alert al = new Alert("Info", "Invalid URL", null, AlertType.WARNING);
            		al.addCommand(new Command("Back", Command.BACK, 0));
            		al.setCommandListener(new CommandListener() {						
						public void commandAction(Command c, Displayable d) {
							// TODO Auto-generated method stub
							notifyDestroyed();
						}
					});
            		display.setCurrent(al);            		
            	}
            } else {
        		final OneNoteClient client = new OneNoteClient();
        		client.setListener(this);
        		
        		final Form settingForm = new Form("URLTranslator");

//            	StringItem authButton = new StringItem("Microsoft Account", "Authenticate", Item.BUTTON);
//            	authButton.setDefaultCommand(new Command("Authenticate", Command.ITEM, 0));
//				authButton.setItemCommandListener(new ItemCommandListener() {
//					public void commandAction(Command c, Item item) {
//						client.authenticate();
//					}
//				});
//				settingForm.append(authButton);
				
				final ChoiceGroup choice = new ChoiceGroup("Translate language", Choice.POPUP, GoogleTranslateClient.LANGUAGE, null);
				settingForm.append(choice);
            	String id = null;
				try {
					id = new String(pref.read("langid"));
				} catch (Exception e) {
				} finally {
					if(id == null) id = GoogleTranslateClient.ID_JAPANESE; // default is JP
				}
            	
            	choice.setSelectedIndex(Integer.parseInt(id), true);
            	settingForm.setItemStateListener(new ItemStateListener() {				
					public void itemStateChanged(Item item) {
						if(item.equals(choice)) try {
							pref.write("langid", new Integer(choice.getSelectedIndex()).toString().getBytes());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}
				});
//              	settingForm.setTicker(new Ticker("This app can be used in [Share] menu only"));
            	settingForm.append(new Spacer(0, 30));
//            	settingForm.append(new StringItem("**NOTICE**", "This app can be used in [Share] menu only."));
            	urlField = new TextField("Enter URL", null, 100, TextField.URL);
            	settingForm.append(urlField);
            	
            	final Command exit = new Command("Exit", Command.EXIT, 3);
            	final Command go = new Command("Go", Command.OK, 1);
              	final Command about = new Command("About", Command.OK, 2);             	
              	settingForm.addCommand(exit);
              	settingForm.addCommand(go);
              	settingForm.addCommand(about);
				settingForm.setCommandListener(new CommandListener() {
					public void commandAction(Command c, Displayable d) {
						if (c == go) {
							url = urlField.getString();
							if(url.startsWith("http")) {
			            		menu = new ShareMenu(midlet, url);
			            		menu.openBrowserMenu();
			            	} else {
			            		Alert al = new Alert("Info", "Invalid URL", null, AlertType.WARNING);
			            		al.addCommand(new Command("Back", Command.BACK, 0));
			            		display.setCurrent(al);            		
			            	}
						} else if (c == exit) {
							System.out.println("Destroyed-->");
							// PlatformUtils.getInstance().shutdown(false, "Shutting down.");
							PlatformUtils.getInstance().shutdown("Shutting down.");
							notifyDestroyed();
						} else if (c == about) {
							new About(midlet, settingForm);
						}
					}
				});

            	this.display.setCurrent(settingForm);
            }   
            
        }
	}

	public void invocationRequestNotify(ContentHandlerServer handler) {
		// TODO Auto-generated method stub
		invocation = handler.getRequest(false);
        if (invocation != null) {
        	isLaunchByInvocation = true;
        	System.out.println("Launch by Invocation-->");
        	String args[] = invocation.getArgs();
        	for(int i=0; i<args.length; i++) {
//        		System.out.println(args[i]);
        		if(args[i].startsWith("text=")) url = args[i].substring(5); 
        	}        	
        	this.menu = new ShareMenu(this, url);
        	this.menu.openBrowserMenu();
        } else {
        	System.out.println("Launch normal-->");
        }
	}
	
}
