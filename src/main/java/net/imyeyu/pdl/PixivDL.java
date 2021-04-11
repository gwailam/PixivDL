package net.imyeyu.pdl;

import javafx.application.Application;
import net.imyeyu.betterjava.config.Config;
import net.imyeyu.betterjava.config.Configer;
import net.imyeyu.pdl.ctrl.Main;

/**
 * Pixiv 图片批量下载
 *
 * 夜雨 创建于 2021/4/10 16:47
 */
public class PixivDL {

	public static Config config;

	public static void main(String[] args) {
		// 禁止 DPI 缩放
		System.setProperty("glass.win.minHiDPI", "1");

		// 配置文件
		Configer configer = new Configer("PixivDL.ini");
		try {
			config = configer.get();
		} catch (Exception e) {
			config = configer.reset();
			config.put("cache-isResetConfig", true);
		}
		// 启动
		Application.launch(Main.class, args);
	}
}
