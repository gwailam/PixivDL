package net.imyeyu.pdl.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.imyeyu.pdl.PixivDL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析器
 *
 * 夜雨 创建于 2021/4/10 21:56
 */
public class Parser {

	private static final Pattern PATTERN = Pattern.compile("(id=\"meta-preload-data\" content=').*?>"); // JSON 数据正则

	private int port;
	private String ip;

	/**
	 * 获取页面
	 *
	 * @param url 地址
	 * @return 页面 HTML
	 * @throws Exception 异常
	 */
	public String getPage(String url) throws Exception {
		SocketAddress address = new InetSocketAddress(ip, port);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

		URL uri = new URL(url);
		HttpURLConnection connect = (HttpURLConnection) uri.openConnection(proxy);
		// SSL 请求
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, new TrustManager[]{X509}, null);
		if (connect instanceof HttpsURLConnection) {
			((HttpsURLConnection) connect).setSSLSocketFactory(sslcontext.getSocketFactory());
		}

		connect.setRequestMethod("GET");
		setHeader(connect);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connect.getInputStream(),
				StandardCharsets.UTF_8
		));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\r\n");
		}

		br.close();
		connect.disconnect();
		return sb.toString();
	}

	/**
	 * 获取图片信息，重要信息如下：
	 * urls: {
	 *     "original": "https://i.pximg.net/img-original/img/....."
	 * },
	 * "pageCount": 10,
	 *
	 * @param pid  图片 ID
	 * @param page 页面数据
	 * @return 图片数据
	 */
	public JsonObject getData(String pid, String page) {
		Matcher matcherText = PATTERN.matcher(page);
		if (matcherText.find()) {
			String jsonString = matcherText.group();
			jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.lastIndexOf('}') + 1);

			JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
			JsonObject illust = root.get("illust").getAsJsonObject();
			return illust.get(String.valueOf(pid)).getAsJsonObject();
		}
		return null;
	}

	/**
	 * 设置请求头
	 *
	 * @param connect 请求
	 */
	public void setHeader(HttpURLConnection connect) {
		connect.setRequestProperty("accept", "*/*");
		connect.setRequestProperty("referer", "https://www.pixiv.net/");
		connect.setRequestProperty("connection", "Keep-Alive");
		connect.setRequestProperty(
				"user-agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400"
		);
		connect.setRequestProperty("Cookie", PixivDL.config.getString("cookie"));
		connect.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connect.setRequestProperty("Accept-Charset", "UTF-8");
		connect.setConnectTimeout(8000);
	}

	/**
	 * X509 SSL
	 *
	 * 夜雨 创建于 2021/4/10 18:59
	 */
	private final TrustManager X509 = new X509TrustManager() {

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}
	};

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
