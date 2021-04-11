package net.imyeyu.pdl.ctrl;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.imyeyu.betterfx.service.ByteSpeed;
import net.imyeyu.betterjava.IO;
import net.imyeyu.betterjava.Tools;
import net.imyeyu.betterjava.config.Configer;
import net.imyeyu.pdl.PixivDL;
import net.imyeyu.pdl.bean.PixivImage;
import net.imyeyu.pdl.core.DownloadThread;
import net.imyeyu.pdl.view.ViewMain;
import net.imyeyu.pixelfx.dialog.Alert;

import java.awt.Desktop;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 主界面控制器
 *
 * 夜雨 创建于 2021/4/11 01:42
 */
public class Main extends ViewMain {

	private final ByteSpeed byteSpeed = new ByteSpeed(); // 网速计算
	private static final Image COOKIE_TIPS = new Image("cookie-tips.png");

	private DownloadThread[] threads; // 当前所有线程
	private final SimpleBooleanProperty isRunning = new SimpleBooleanProperty(false);

	@Override
	public void start(Stage stage) {
		super.start(stage);

		// 运行禁用
		ip.disableProperty().bind(isRunning);
		port.disableProperty().bind(isRunning);
		cookie.disableProperty().bind(isRunning);
		multiDL.disableProperty().bind(isRunning);
		delay.disableProperty().bind(isRunning);
		run.disableProperty().bind(isRunning);
		stop.disableProperty().bind(isRunning.not());
		pids.disableProperty().bind(isRunning);
		filter.disableProperty().bind(isRunning);

		// Cookie 提示
		cookie.hoverProperty().addListener((obs, o, isHover) -> {
			if (isHover) {
				popupTips.showImage(stage, COOKIE_TIPS);
			} else {
				popupTips.hide();
			}
		});
		cookie.setOnMouseMoved(event -> {
			popupTips.setX(event.getScreenX() + 6);
			popupTips.setY(event.getScreenY() + 6);
		});
		// 网速
		byteSpeed.valueProperty().addListener((obs, o, b) -> {
			if (b != null && b != 0) {
				netSpeed.setText(Tools.byteFormat(b, 2));
				netSpeed.addValue(b / 1024E2);
			} else {
				netSpeed.setText("");
				netSpeed.addValue(0);
			}
		});
		// 开始
		run.setOnAction(event -> {
			isRunning.set(true);

			PixivDL.config.bindUpdate();
			// 设置代理
			System.setProperty("http.proxyHost", ip.getText());
			System.setProperty("http.proxyPort", port.getText());

			if (super.pids.getText().length() < 1) {
				new Alert("没有任务");
				isRunning.set(false);
				return;
			}
			// 解析 ID
			String[] pids = super.pids.getText().trim().split(",");

			final int THREAD_SIZE = multiDL.getValue();
			// 线程完成监听
			SimpleIntegerProperty finishCount = new SimpleIntegerProperty(0);
			finishCount.addListener((obs, o, i) -> {
				if (i != null && i.intValue() == THREAD_SIZE) {
					isRunning.set(false);
					threads = null;
				}
			});
			// 多线程任务
			List<List<PixivImage>> datas = new ArrayList<>();
			// 初始化任务列表
			for (int i = 0; i < THREAD_SIZE; i++) {
				datas.add(new ArrayList<>());
			}
			// 分配任务
			list.getItems().clear();
			for (int i = 0; i < pids.length; i++) {
				if (!pids[i].trim().equals("")) {
					PixivImage pi = new PixivImage(pids[i].trim());
					datas.get(i % THREAD_SIZE).add(pi);
					list.getItems().add(pi);
				}
			}
			// 初始化线程
			threads = new DownloadThread[THREAD_SIZE];
			for (int i = 0; i < THREAD_SIZE; i++) {
				threads[i] = new DownloadThread(datas.get(i));
			}
			// 消息
			final SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss]");
			for (int i = 0; i < THREAD_SIZE; i++) {
				threads[i].messageProperty().addListener((obs, o, msg) -> {
					if (msg != null) {
						log.appendText(format.format(new Date()) + msg);
						log.appendText("\n");
					}
				});
			}
			// 异常
			for (int i = 0; i < THREAD_SIZE; i++) {
				final int j = i;
				threads[i].exceptionProperty().addListener((obs, o, e) -> {
					if (e != null) {
						String id = threads[j].getNowImage().getId();
						String name = threads[j].getThreadName();
						log.appendText(format.format(new Date()) + name + "[错误][" + id + "] " + e.getMessage());
						log.appendText("\n");
						finishCount.set(finishCount.get() + 1);
					}
				});
			}
			// 完成监听
			for (int i = 0; i < THREAD_SIZE; i++) {
				final int j = i;
				threads[i].setOnSucceeded(e -> {
					if (threads[j].getNowImage() != null) {
						threads[j].getNowImage().setPercent(1);
					}
					finishCount.set(finishCount.get() + 1);
				});
			}
			// 启动线程
			for (int i = 0; i < THREAD_SIZE; i++) {
				threads[i].start();
			}
		});
		// 停止
		stop.setOnAction(event -> {
			if (threads != null) {
				for (DownloadThread thread : threads) {
					thread.shutdown();
				}
			}
		});
		about.setOnAction(event -> new About());

		// 强制无延时
		if (multiDL.getValue() != 1) {
			delay.setValue(0);
		}
		multiDL.valueProperty().addListener((obs, o, threadSize) -> {
			if (threadSize != null && threadSize != 1) {
				delay.setValue(0);
			}
		});
		// 强制单线程
		if (delay.getValue() != 0) {
			multiDL.setValue(1);
		}
		delay.valueProperty().addListener((obs, o, delay) -> {
			if (delay != null && delay != 0) {
				multiDL.setValue(1);
			}
		});
		delay.hoverProperty().addListener((obs, o, isHover) -> {
			if (isHover) {
				popupTips.showText(stage, "毫秒");
			} else {
				popupTips.hide();
			}
		});
		delay.setOnMouseMoved(event -> {
			popupTips.setX(event.getScreenX() + 6);
			popupTips.setY(event.getScreenY() + 6);
		});
		// 待解析 PID
		pids.hoverProperty().addListener((obs, o, isHover) -> {
			if (isHover) {
				popupTips.showText(stage, "示例：89031688, 88989071\n支持单品：88989071_p0\n支持直接粘贴链接：https://www.pixiv.net/artworks/89078791");
			} else {
				popupTips.hide();
			}
		});
		pids.setOnMouseMoved(event -> {
			popupTips.setX(event.getScreenX() + 6);
			popupTips.setY(event.getScreenY() + 6);
		});
		// 下载位置
		select.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("选择下载位置");
			File dir = directoryChooser.showDialog(null);
			if (dir != null) {
				path.setText(dir.getAbsolutePath());
			}
		});
		// 过滤
		filter.hoverProperty().addListener((obs, o, isHover) -> {
			if (isHover) {
				popupTips.showText(stage, "过滤失败的合集或单品到 PID 列表重新解析下载");
			} else {
				popupTips.hide();
			}
		});
		filter.setOnMouseMoved(event -> {
			popupTips.setX(event.getScreenX() + 6);
			popupTips.setY(event.getScreenY() + 6);
		});
		filter.setOnAction(event -> {
			if (threads != null) {
				pids.clear();
				for (DownloadThread thread : threads) {
					List<PixivImage> list = thread.getList();
					for (PixivImage pi : list) {
						List<String> ids = pi.getFailList();
						for (String id : ids) {
							pids.appendText(id);
							pids.appendText(", ");
						}
					}
				}
			}
		});

		// 日志
		logClean.setOnMouseClicked(event -> log.clear());
		logExport.setOnMouseClicked(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("导出日志");
			File dir = directoryChooser.showDialog(null);
			if (dir != null) {
				SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss");
				IO.toFile(new File(dir, "pixivDL-log-" + format.format(new Date()) + ".log"), log.getText());
				try {
					Desktop.getDesktop().open(dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// 准备就绪
		byteSpeed.start();
		// 关闭启动页
		if (SplashScreen.getSplashScreen() != null) SplashScreen.getSplashScreen().close();
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		if (threads != null) {
			for (DownloadThread thread : threads) {
				thread.shutdown();
			}
		}
		byteSpeed.shutdown();
		PixivDL.config.bindUpdate();
		new Configer("PixivDL").set(PixivDL.config);
		super.stop();
	}
}