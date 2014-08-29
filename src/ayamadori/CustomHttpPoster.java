/**
 * 
 */
package ayamadori;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.tantalum.CancellationException;
import org.tantalum.Task;
import org.tantalum.TimeoutException;

/**
 * @author owner
 */
public class CustomHttpPoster extends Task {

	private String url;
	private Vector propertyKeys;
	private Vector propertyValues;
	private byte[] postData;
	private int resCode = -1;

	/**
	 * @param priority
	 */
	public CustomHttpPoster(String url) {
		super();
		// TODO Auto-generated constructor stub
		this.url = url;
		propertyKeys = new Vector();
		propertyValues = new Vector();
	}

	public void setRequestProperty(String key, String value) {
		propertyKeys.addElement(key);
		propertyValues.addElement(value);
	}

	public void setPostData(byte[] postData) {
		this.postData = postData;
	}

	public int getResponseCode() {
		return resCode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.tantalum.Task#exec(java.lang.Object)
	 */
	protected Object exec(Object arg0) throws CancellationException, TimeoutException, InterruptedException {
		// TODO Auto-generated method stub
		HttpConnection c = null;
		InputStream is = null;
		OutputStream os = null;
		byte[] data = null;

		try {
			c = (HttpConnection) Connector.open(url);
			c.setRequestMethod(HttpConnection.POST);
			
			// Set headers
			int size = propertyKeys.size();
			for (int i = 0; i < size; i++) {
				c.setRequestProperty((String) propertyKeys.elementAt(i), (String) propertyValues.elementAt(i));
			}
//			c.setRequestProperty("Content-Length", Integer.toString(postData.length));

			// Getting the output stream may flush the headers
			os = c.openOutputStream();
			os.write(postData);
			// os.flush(); // Optional, getResponseCode will flush

			// Getting the response code will open the connection,
			// send the request, and read the HTTP response headers.
			// The headers are stored until requested.
			resCode = c.getResponseCode();
			if (resCode != HttpConnection.HTTP_OK) {
				throw new IOException("HTTP response code: " + resCode);
			}

			is = c.openInputStream();

			// Get the ContentType
			// String type = c.getType();
			// System.out.println("type: " + type);

			// Get the length and process the data
			int len = (int) c.getLength();
			if (len > 0) {
				int actual = 0;
				int bytesread = 0;
				data = new byte[len];
				while ((bytesread != len) && (actual != -1)) {
					actual = is.read(data, bytesread, len - bytesread);
					bytesread += actual;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
				if (os != null) os.close();
				if (c != null) c.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return data;
	}

}
