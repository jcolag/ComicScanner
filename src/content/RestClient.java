package content;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class RestClient {
	private String base = "";
	private DefaultHttpClient httpclient;
	public List<Cookie> cookies;
	public static final String urlDocs = "/documents.json";
	public static final String urlSubmit = "/documents/new.json";

	/**
	 * @param entity
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	private static String extractContentString(HttpEntity entity, String encoding)
			throws IOException {
		long length = entity.getContentLength();
		byte b[] = new byte[(int)length];
		entity.getContent().read(b);
		return new String(b, Charset.forName(encoding));
	}

	/**
	 * @param urlBase
	 */
	public RestClient(String urlBase) {
		base = urlBase;
		httpclient = new DefaultHttpClient();
	}

	/**
	 * @param urlDocs
	 * @return
	 * @throws java.net.ConnectException
	 */
	public String getFromUrl(String urlDocs) throws java.net.ConnectException {
		String resp = "";
		try {
			HttpGet httpget = new HttpGet(base + urlDocs);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			cookies = httpclient.getCookieStore().getCookies();
			if (entity != null) {
				Header enc = entity.getContentEncoding();
				String encoding = "UTF-8";
				if (enc != null) {
					encoding = enc.getValue();
				}
				resp = extractContentString(entity, encoding);
				entity.consumeContent();
			}
		} catch (java.net.ConnectException e) {
			throw e;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String postToUrl(String urlSubmit, List<NameValuePair> nvps)
			throws java.net.ConnectException {
		String resp = "";
		try {
			HttpPost httpost = new HttpPost(base + urlSubmit);
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			cookies = httpclient.getCookieStore().getCookies();
			if (entity != null) {
				Header enc = entity.getContentEncoding();
				String encoding = "UTF-8";
				if (enc != null) {
					encoding = enc.getValue();
				}
				resp = extractContentString(entity, encoding);
				entity.consumeContent();
			}
		} catch (java.net.ConnectException e) {
			throw e;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}
}
