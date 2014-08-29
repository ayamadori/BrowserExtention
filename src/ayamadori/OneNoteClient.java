package ayamadori;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.content.ResponseListener;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.midlet.MIDlet;
import org.tantalum.CancellationException;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
import org.tantalum.jme.RMSUtils;
import org.tantalum.net.HttpPoster;
import org.tantalum.net.json.JSONModel;
import org.tantalum.util.LOR;
import org.tantalum.util.StringUtils;

public class OneNoteClient implements ResponseListener {

	private static final String CLIENT_ID = "000000004012434F";
	private static final String CLIENT_SECRET = "QTn2Ft59aRNh0u6t7Adcls2mv4qrarw7";
	// Redirect URL for the authentication
	private static final String REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf";
	// http://msdn.microsoft.com/JA-JP/library/office/dn575435(v=office.15).aspx
	// http://gihyo.jp/dev/serial/01/wl-sdk/0045?page=2
	private static final String ACCESS_URL = "https://login.live.com/oauth20_authorize.srf?client_id=" + CLIENT_ID
			+ "&scope=office.onenote_create%20wl.offline_access&response_type=code&redirect_uri=";

	// http://msdn.microsoft.com/ja-jp/library/office/dn575431(v=office.15).aspx
	private static final String POST_HTML_START = "<!DOCTYPE html><html><body><img data-render-src=\"";
	private static final String POST_HTML_END = "\"/></body></html>";
	
	private static final String API_URL = "https://www.onenote.com/api/v1.0/pages/";
	
	// http://msdn.microsoft.com/ja-jp/library/hh243647
	private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
	private RMSUtils pref;
	private MIDlet midlet;
	private Alert progress;

	public OneNoteClient() {
		pref = RMSUtils.getInstance();
	}
	
	public void setListener(MIDlet midlet) {
		this.midlet = midlet;
		Registry.getRegistry(midlet.getClass().getName()).setListener(this);
	}
	
    /* In order to use MS Live authentication, the app needs to be registered
     * and the client id for the API needs to be obtained.
     * Information about how to register the app and obtain the client id can be found from: 
     * http://msdn.microsoft.com/en-us/library/bb676626.aspx
     */
//    private static final String client_id = ""; // Please paste the Live id here
	
    // URL for the authentication service
//    private static final String accessURL = "https://login.live.com/oauth20_authorize.srf?client_id="
//        + client_id + "&scope=wl.basic&response_type=token&redirect_uri=";

    /**
     * Creates Oauth invocation
     */
    public void authenticate() {
        Invocation inv = new Invocation();
        inv.setURL(ACCESS_URL + REDIRECT_URL);
        inv.setID("com.nokia.browser");
        inv.setArgs(new String[] { "mode=proxy", "redirect_intercept=" + REDIRECT_URL });
        inv.setResponseRequired(true);
        try {
            String my_class = midlet.getClass().getName();
            Registry.getRegistry(my_class).invoke(inv);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The listener is notified to indicate that an Invocation response is available.
     * @param registry the Registry with a new response
     */
    public void invocationResponseNotify(Registry registry) {
        Invocation response = registry.getResponse(true);
        if (response.getStatus() == Invocation.OK) {
            String redirectedURL = response.getURL();
			
			String code = parseCode(redirectedURL);
			System.out.println("Authenticated-->");
			System.out.println("code= " + code);
		    getToken(code);
        } 
        else {
            System.out.println("GetStatus = " + response.getStatus());
            /* handle cancelled invocation */
        }
    }

    /**
     * Parses authentication token from URL
     * @param input input URL
     * @return authentication code
     */
    private String parseCode(String input) {
        if (input == null) {
            return null;
        }
        String code = "code=";
        int start;
        int end;
        start = input.indexOf(code);
        if (start == -1) {
            return null;            
        }
        else {
            start += code.length();            
        }
        end = input.indexOf("&", start);

        String token = input.substring(start, (end != -1) ? end : input.length());
        return token;
    }

//	public void Clip(final String url) {
    public void Clip(final Display display, final String url) {
			System.out.println("Clip--> ");
			progress = new Alert(null, "Clipping...", null, null);
			progress.setTimeout(Alert.FOREVER);
			progress.setIndicator(new Gauge(null, false, Gauge.INDEFINITE, 0));
			progress.addCommand(new Command("Back", Command.BACK, 0));
			progress.setCommandListener(new CommandListener() {				
				public void commandAction(Command c, Displayable d) {
					// TODO Auto-generated method stub
					PlatformUtils.getInstance().shutdown("Shutting down.");
					commandAction(Alert.DISMISS_COMMAND, progress);
				}
			});
			display.setCurrent(progress);
//			refreshToken().chain(clipTask(url)).fork();
			try {
				System.out.println("RefreshToken--> ");
				String refresh_token = new String(pref.read("refresh_token"));
				System.out.println("refresh_token= " + refresh_token);
				final HttpPoster refreshTokenPoster = new HttpPoster(TOKEN_URL);
// 				final HttpPoster clipPoster = new HttpPoster(API_URL);
				refreshTokenPoster.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				byte[] postdata = ("client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URL+"&grant_type=refresh_token&refresh_token="+refresh_token).getBytes("UTF-8");
				refreshTokenPoster.setPostData(postdata);
				refreshTokenPoster.chain(new Task() {
					protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
						// TODO Auto-generated method stub			
						// Handle response
				         if (refreshTokenPoster.getResponseCode() == HttpPoster.HTTP_200_OK) {
				             try {
				                 // Convert byte array response to UTF-8 encoded String
//				                 String response = new String((byte[]) obj, "utf-8");
				            	 String response = new String(((LOR) obj).getBytes());
				                 System.out.println("response2-->\n" + response);
				                 JSONModel json = new JSONModel(response);
				                 String access_token = json.getString("access_token");
				                 System.out.println("access_token= " + access_token);
				                 String refresh_token = json.getString("refresh_token");
				                 System.out.println("refresh_token= " + refresh_token);
				                 pref.write("access_token", access_token.getBytes());
				                 pref.write("refresh_token", refresh_token.getBytes());
				                 
//				                 System.out.println("ClipTask--> ");
				 				// remove last "="
//				 				if(access_token.endsWith("=")) access_token = access_token.substring(0, access_token.length() - 1);

//				 				clipPoster.setRequestProperty("Content-Type", "text/html");
//				 				clipPoster.setRequestProperty("Content-Type", "multipart/form-data; boundary=NewPart");
//				 				clipPoster.setRequestProperty("Authorization", "Bearer " + access_token);
//				 				byte[] postdata = (POST_HTML_START + url + POST_HTML_END).getBytes("UTF-8");
//				 				byte[] postdata = StringUtils.urlEncode(POST_HTML_START + url + POST_HTML_END).getBytes();
//				 				byte[] postdata = StringUtils.urlEncode(StringUtils.readStringFromJAR("/page.html")).getBytes();
//				 				clipPoster.setPostData(postdata);

				                 doClip(url);
				             } catch (Exception ex) {
				                 ex.printStackTrace();
				             }
				         }
				         else {
				             // If response code not 200 / OK, print error note to std out
				             System.out.println("Error: " + refreshTokenPoster.getResponseCode());
				         }

						return obj;
					}
				}).fork();
//				}).chain(clipPoster).chain(new Task() {
//					protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
//						// TODO Auto-generated method stub			
//						// Handle response
//				         if (clipPoster.getResponseCode() == HttpPoster.HTTP_200_OK) {
//				             try {
//				                 // Convert byte array response to UTF-8 encoded String
//				                 String response = new String(((LOR) obj).getBytes());
//				                 // In this example we just print the response to the std out
//				                 System.out.println(response);
//				             } catch (Exception ex) {
//				                 ex.printStackTrace();
//				             }
//				         }
//				         else {
//				             // If response code not 200 / OK, print error note to std out
//				             System.out.println("Error: " + clipPoster.getResponseCode());
//				             Hashtable headers = clipPoster.getResponseHeaders();
//				             Enumeration enum = headers.keys();
//				             while (enum.hasMoreElements()) {
//				            	 String key = (String) enum.nextElement();
//				            	 System.out.println(key + " : " + headers.get(key) + "\n");
//				             }
//				         }
//
//						return obj;
//					}
//				}).fork();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/*
	 * http://msdn.microsoft.com/JA-JP/library/office/dn575435(v=office.15).aspx
	 * 
	 * POST https://www.onenote.com/api/v1.0/pages
	 * Content-Type:text/html
	 * Authorization:Bearer tokenString
	 * tokenString を、ログイン時にアプリが Live Connect REST エンドポイントから取得するベアラ トークンに置き換えます。
	 */
	private Task clipTask(final String url) {
		Task task = null;
		try {
			System.out.println("ClipTask--> ");
			String access_token = new String(pref.read("access_token"));
			// remove last "="
			if(access_token.endsWith("=")) access_token = access_token.substring(0, access_token.length() - 1);
			System.out.println("access_token= " + access_token);
			final HttpPoster poster = new HttpPoster(Task.IDLE_PRIORITY, API_URL);
			poster.setRequestProperty("Content-Type", "text/html");
			poster.setRequestProperty("Authorization", "Bearer " + access_token);
//			byte[] postdata = (POST_HTML_START + url + POST_HTML_END).getBytes("UTF-8");
			byte[] postdata = StringUtils.urlEncode(POST_HTML_START + url + POST_HTML_END).getBytes("UTF-8");
			poster.setPostData(postdata);
			poster.chain(new Task() {
				protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
					// TODO Auto-generated method stub			
					// Handle response
			         if (poster.getResponseCode() == HttpPoster.HTTP_200_OK) {
			             try {
			                 // Convert byte array response to UTF-8 encoded String
			                 String response = new String(((LOR) obj).getBytes());
			                 // In this example we just print the response to the std out
			                 System.out.println(response);
			             } catch (Exception ex) {
			                 ex.printStackTrace();
			             }
			         }
			         else {
			             // If response code not 200 / OK, print error note to std out
			             System.out.println("Error: " + poster.getResponseCode());
			         }

					return obj;
				}
			}).fork();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}
	
	/*
	 * http://msdn.microsoft.com/JA-JP/library/office/dn575435(v=office.15).aspx
	 * 
	 * POST https://www.onenote.com/api/v1.0/pages
	 * Content-Type:text/html
	 * Authorization:Bearer tokenString
	 * tokenString を、ログイン時にアプリが Live Connect REST エンドポイントから取得するベアラ トークンに置き換えます。
	 */
	private void doClip(final String url) {
		try {
			System.out.println("ClipTask--> ");
			String access_token = new String(pref.read("access_token"));
			// remove last "="
//			if(access_token.endsWith("=")) access_token = access_token.substring(0, access_token.length() - 1);
			System.out.println("access_token= " + access_token);
			final HttpPoster poster = new HttpPoster(API_URL);
//			poster.setRequestProperty("Content-Type", "text/html");
//			poster.setRequestProperty("Authorization", "bearer " + access_token);
//			byte[] postdata = (POST_HTML_START + url + POST_HTML_END).getBytes("UTF-8");
			byte[] postdata = StringUtils.urlEncode(POST_HTML_START + url + POST_HTML_END).getBytes("UTF-8");
			poster.setPostData(postdata);
			poster.chain(new Task() {
				protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
					// TODO Auto-generated method stub			
					// Handle response
			         if (poster.getResponseCode() == HttpPoster.HTTP_200_OK) {
			             try {
			                 // Convert byte array response to UTF-8 encoded String
			                 String response = new String(((LOR) obj).getBytes());
			                 // In this example we just print the response to the std out
			                 System.out.println(response);
			                 
			                 progress.setString("Clipped.");
			             } catch (Exception ex) {
			                 ex.printStackTrace();
			             }
			         }
			         else {
			             // If response code not 200 / OK, print error note to std out
			             System.out.println("Error: " + poster.getResponseCode());
			         }

					return obj;
				}
			}).fork();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * http://msdn.microsoft.com/JA-JP/library/office/dn575435(v=office.15).aspx
	 * 
	 * POST https://www.onenote.com/api/v1.0/pages Content-Type:text/html Authorization:Bearer tokenString
	 * tokenString を、ログイン時にアプリが Live Connect REST エンドポイントから取得するベアラ トークンに置き換えます。
	 */
	public void getToken(final String code) {
		System.out.println("GetToken-->");
		try {
			System.out.println("code= " + code);
			final HttpPoster poster = new HttpPoster(TOKEN_URL);
//			final CustomHttpPoster poster = new CustomHttpPoster(TOKEN_URL);
			poster.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			byte[] postData = ("client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URL + "&client_secret="
					+ CLIENT_SECRET + "&code=" + code + "&grant_type=authorization_code").getBytes("UTF-8");
			poster.setPostData(postData);
			poster.chain(new Task() {
				protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
					// TODO Auto-generated method stub			
					// Handle response
			         if (poster.getResponseCode() == HttpPoster.HTTP_200_OK) {
			             try {
			                 // Convert byte array response to UTF-8 encoded String
//			                 String response = new String((byte[]) obj, "utf-8");
			                 String response = new String(((LOR) obj).getBytes());
			                 System.out.println("response1-->\n" + response);
			                 JSONModel json = new JSONModel(response);
			                 String access_token = json.getString("access_token");
			                 System.out.println("access_token= " + access_token);
			                 String refresh_token = json.getString("refresh_token");
			                 System.out.println("refresh_token= " + refresh_token);
			                 pref.write("access_token", access_token.getBytes());
			                 pref.write("refresh_token", refresh_token.getBytes());

			             } catch (Exception ex) {
			                 ex.printStackTrace();
			             }
			         }
			         else {
			             // If response code not 200 / OK, print error note to std out
			             System.out.println("Error: " + poster.getResponseCode());
			         }

					return obj;
				}
			}).fork();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * http://msdn.microsoft.com/ja-jp/library/hh243647
	 * 
	 * POST https://login.live.com/oauth20_token.srf
	 * Content-type: application/x-www-form-urlencoded
	 * client_id=CLIENT_ID&redirect_uri=https://login.live.com/oauth20_desktop.srf&grant_type=refresh_token&refresh_token=REFRESH_TOKEN
	 */
	private Task refreshToken() {
		Task task = null;
		try {
			System.out.println("RefreshToken--> ");
			String refresh_token = new String(pref.read("refresh_token"));
			System.out.println("refresh_token= " + refresh_token);
			final HttpPoster poster = new HttpPoster(TOKEN_URL);
			poster.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			byte[] postdata = ("client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URL+"&grant_type=refresh_token&refresh_token="+refresh_token).getBytes("UTF-8");
			poster.setPostData(postdata);
			task = poster.chain(new Task(Task.HIGH_PRIORITY) {
				protected Object exec(Object obj) throws CancellationException, TimeoutException, InterruptedException {
					// TODO Auto-generated method stub			
					// Handle response
			         if (poster.getResponseCode() == HttpPoster.HTTP_200_OK) {
			             try {
			                 // Convert byte array response to UTF-8 encoded String
//			                 String response = new String((byte[]) obj, "utf-8");
			            	 String response = new String(((LOR) obj).getBytes());
			                 System.out.println("response2-->\n" + response);
			                 JSONModel json = new JSONModel(response);
			                 String access_token = json.getString("access_token");
			                 System.out.println("access_token= " + access_token);
			                 String refresh_token = json.getString("refresh_token");
			                 System.out.println("refresh_token= " + refresh_token);
			                 pref.write("access_token", access_token.getBytes());
			                 pref.write("refresh_token", refresh_token.getBytes());

			             } catch (Exception ex) {
			                 ex.printStackTrace();
			             }
			         }
			         else {
			             // If response code not 200 / OK, print error note to std out
			             System.out.println("Error: " + poster.getResponseCode());
			         }

					return obj;
				}
			});
//			}).fork();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}
}
