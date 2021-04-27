package net.imyeyu.pdl.core;

import com.google.gson.JsonObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.betterfx.service.ByteSpeed;
import net.imyeyu.pdl.PixivDL;
import net.imyeyu.pdl.bean.PixivImage;

import javax.naming.NoPermissionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.List;

/**
 * 下载线程
 *
 * 夜雨 创建于 2021/4/11 01:34
 */
public class DownloadThread extends Service<Integer> {

	private static final Parser PARSER = new Parser();
	private static final String URL_PREFIX = "https://www.pixiv.net/artworks/"; // URL 前缀
	private final List<PixivImage> list;

	private final String path;
	private final int delay;
	private String threadName;
	private boolean isShutdown = false; // 中止
	private PixivImage nowImage; // 当前执行图像

	public DownloadThread(List<PixivImage> list) {
		this.list = list;
		// 代理
		PARSER.setIp(PixivDL.config.getString("ip"));
		PARSER.setPort(PixivDL.config.getInt("port"));
		// 保存位置
		path = PixivDL.config.getString("path");
		// 延时
		delay = PixivDL.config.getInt("delay");

		// 自监听，把状态数据返回到下载列表对象中
		// 当前下载第几个
		valueProperty().addListener((obs, o, i) -> {
			if (nowImage != null && i != null) {
				nowImage.setImgI(i);
			}
		});
		// 当前作品数量
		titleProperty().addListener((obs, o, l) -> {
			if (nowImage != null && l != null) {
				nowImage.setImgCount(Integer.parseInt(l));
			}
		});
		// 当前下载图像进度
		progressProperty().addListener((obs, o, p) -> {
			if (nowImage != null && p != null) {
				nowImage.setPercent(p.doubleValue());
			}
		});
	}

	@Override
	protected Task<Integer> createTask() {
		return new Task<>() {
			@Override
			protected Integer call() throws Exception {
				threadName = '[' + Thread.currentThread().getName() + "]";

				for (PixivImage pixivImage : list) {
					nowImage = pixivImage;
					String pid = pixivImage.getId();
					try {
						// 单品下载
						int pid_ = pid.indexOf('_');
						boolean isSingle = pid_ != -1;
						int singleI = -1;
						if (isSingle) {
							// 取第几张
							singleI = Integer.parseInt(pid.substring(pid_ + 2));
							// 取 ID
							pid = pid.substring(0, pid_);
						}
						// 请求页面
						updateMessage(threadName + "[请求] " + URL_PREFIX + pid);
						String page = PARSER.getPage(URL_PREFIX + pid);
						// 解析 JSON
						updateMessage(threadName + "[成功] 正在解析");
						JsonObject imageData = PARSER.getData(pid, page);
						// 首张原图 URL
						String url = imageData.get("urls").getAsJsonObject().get("original").getAsString();
						// 切出格式
						String format = url.substring(url.lastIndexOf('.'));
						// 切出 URL 前缀
						url = url.substring(0, url.lastIndexOf('/') + 1);
						// 作品图片数量
						int count = imageData.get("pageCount").getAsInt();

						updateValue(0);
						updateMessage(threadName + "[成功] 共 " + count + " 张，URL 前缀：" + url);
						updateTitle(isSingle ? String.valueOf(1) : String.valueOf(count));

						// 下载图片
						for (int i = 0; i < count; i++) {
							if (isSingle && singleI != i) {
								continue;
							}

							String name = pid + "_p" + i + format;
							try {
								updateValue(isSingle ? 1 : i + 1);
								updateMessage(threadName + "[下载] " + url + name);
								downloadFile(url + name, path, name);
								updateMessage(threadName + "[完成] " + path + '/' + name);

								if (isShutdown) {
									return 0;
								}

								if (delay != 0) {
									Thread.sleep(delay);
								}
							} catch (Exception e) {
								updateMessage(threadName + "[错误] " + e.getMessage());
								updateMessage(threadName + "[错误] 下载失败（已跳过）：" + path + '/' + name);
								pixivImage.addFail(name);
								e.printStackTrace();
								throw e;
							}
						}
					} catch (Exception e) {
						updateMessage(threadName + "[错误] " + e.getMessage());
						updateMessage(threadName + "[错误] 无法解析页面，PID = " + pid);
						pixivImage.addFail(pid);
						e.printStackTrace();
						throw e;
					}
				}
				return null;
			}

			/**
			 * 下载文件，脱离 BetterJava 以便计算网速
			 *
			 * @param url        下载地址
			 * @param path       文件存放路径
			 * @param fileName   文件名
			 * @throws Exception 异常
			 */
			private void downloadFile(String url, String path, String fileName) throws Exception {
				File dir = new File(path);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						throw new NoPermissionException("没有权限创建文件夹：" + dir.getAbsolutePath());
					}
				}

				SocketAddress address = new InetSocketAddress(PARSER.getIp(), PARSER.getPort());
				Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

				HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection(proxy);
				connect.setRequestProperty("accept", "*/*");
				connect.setRequestProperty("referer", "https://www.pixiv.net/");
				connect.setRequestProperty("connection", "Keep-Alive");
				connect.setRequestProperty(
						"user-agent",
						"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400"
				);
				connect.setRequestProperty("Cookie", PixivDL.config.getString("cookie"));
				connect.setConnectTimeout(8000);
				long lengthTotal = connect.getContentLength();
				double length = 0;

				InputStream is = connect.getInputStream();
				byte[] buffer = new byte[1024];
				int l;
				File file = new File(dir + File.separator + fileName);
				FileOutputStream fos = new FileOutputStream(file);
				while ((l = is.read(buffer)) != -1) {
					ByteSpeed.BUFFER += l;
					updateProgress((length += l), lengthTotal);
					fos.write(buffer, 0, l);
				}
				updateProgress(1, 1);

				fos.close();
				is.close();
			}
		};
	}

	public void shutdown() {
		isShutdown = true;
	}

	public String getThreadName() {
		return threadName;
	}

	public PixivImage getNowImage() {
		return nowImage;
	}

	public List<PixivImage> getList() {
		return list;
	}
}