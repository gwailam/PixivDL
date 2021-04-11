package net.imyeyu.pdl.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.extend.AnchorPaneX;
import net.imyeyu.betterfx.extend.BorderX;
import net.imyeyu.betterjava.Encode;
import net.imyeyu.betterjava.config.ConfigT;
import net.imyeyu.pdl.PixivDL;
import net.imyeyu.pdl.component.DLTable;
import net.imyeyu.pdl.component.NetSpeed;
import net.imyeyu.pdl.component.PopupTips;
import net.imyeyu.pixelfx.PixelApplication;
import net.imyeyu.pixelfx.Zpix;
import net.imyeyu.pixelfx.component.GroupPane;
import net.imyeyu.pixelfx.component.InputGroup;
import net.imyeyu.pixelfx.component.PixelButton;
import net.imyeyu.pixelfx.dialog.Alert;

/**
 * 主界面
 *
 * 夜雨 创建于 2021/4/11 01:43
 */
public abstract class ViewMain extends PixelApplication {

	protected Label logClean, logExport;
	protected DLTable list;
	protected NetSpeed netSpeed;
	protected TextArea cookie, pids, log;
	protected TextField ip, port, path;
	protected PopupTips popupTips;
	protected PixelButton toggle, stop, about, select, filter;
	protected ComboBox<Integer> multiDL, delay;

	@Override
	public void start(Stage stage) {
		super.start(stage);
		popupTips = new PopupTips();

		// 代理设置
		ip = new TextField();
		ip.setPrefWidth(120);
		PixivDL.config.bindTextProperty(ip, "ip");
		InputGroup<TextField> inputIP = new InputGroup<>("IP：", ip);
		inputIP.setAlignment(Pos.CENTER_RIGHT);

		port = new TextField();
		port.setPrefWidth(120);
		PixivDL.config.bindTextProperty(port, "port");
		InputGroup<TextField> inputPort = new InputGroup<>("端口：", port);

		VBox proxyPane = new VBox(inputIP, inputPort);
		proxyPane.setSpacing(6);
		GroupPane proxyGP = new GroupPane("代理", proxyPane);

		// Cookie
		cookie = new TextArea();
		cookie.setWrapText(true);
		cookie.setPrefSize(280, 51);
		PixivDL.config.bindTextProperty(cookie, "cookie");
		GroupPane cookieGP = new GroupPane("Cookie", cookie);

		// 网速
		netSpeed = new NetSpeed();
		GroupPane netSpeedGP = new GroupPane("下载速度", netSpeed);

		BorderPane cookieNetSpeed = new BorderPane();
		BorderPane.setMargin(cookieGP, new Insets(0, 8, 0, 4));
		BorderPane.setMargin(netSpeedGP, new Insets(0, 8, 0, 4));
		cookieNetSpeed.setLeft(cookieGP);
		cookieNetSpeed.setCenter(netSpeedGP);

		// 设置
		multiDL = new ComboBox<>();
		multiDL.setConverter(new StringConverter<>() {

			public String toString(Integer object) {
				return String.valueOf(object);
			}

			public Integer fromString(String string) {
				boolean isInval = !Encode.isNumber(string);
				if (!isInval) {
					int value = Integer.parseInt(string);
					isInval = value < 1 || 64 < value;
				}
				if (isInval) {
					new Alert("数据不合法，取值范围 [1 - 64] 整数");
				}
				return isInval ? 4 : Integer.parseInt(string);
			}
		});
		multiDL.setValue(1);
		multiDL.getItems().addAll(1, 2, 4, 8, 16, 32, 64);
		multiDL.setEditable(true);
		multiDL.setPrefWidth(64);
		PixivDL.config.bindValueProperty(multiDL, new ConfigT<Integer>("multiDL"));
		InputGroup<ComboBox<Integer>> inputMultiDL = new InputGroup<>("多线程：", multiDL);
		inputMultiDL.setAlignment(Pos.CENTER_RIGHT);

		delay = new ComboBox<>();
		delay.setConverter(new StringConverter<>() {

			public String toString(Integer object) {
				return String.valueOf(object);
			}

			public Integer fromString(String string) {
				boolean isInval = !Encode.isNumber(string);
				if (!isInval) {
					int value = Integer.parseInt(string);
					isInval = value < 0 || 10000 < value;
				}
				if (isInval) {
					new Alert("数据不合法，取值范围 [0 - 10000] 整数（单位：毫秒）");
				}
				return isInval ? 0 : Integer.parseInt(string);
			}
		});
		delay.getItems().addAll(0, 200, 500, 1000, 3000, 5000, 10000);
		delay.setValue(0);
		delay.setEditable(true);
		delay.setPrefWidth(64);
		PixivDL.config.bindValueProperty(delay, new ConfigT<Integer>("delay"));
		InputGroup<ComboBox<Integer>> inputDelay = new InputGroup<>("请求延时：", delay);

		VBox options = new VBox(inputMultiDL, inputDelay);
		options.setSpacing(6);

		// 操作
		toggle = new PixelButton("开始");
		Zpix.css(toggle, Zpix.M);
		stop = new PixelButton("停止");
		about = new PixelButton("关于");

		GridPane ctrls = new GridPane();
		GridPane.setValignment(stop, VPos.BOTTOM);
		ctrls.setHgap(6);
		ctrls.add(toggle, 0, 0, 1, 2);
		ctrls.add(about, 1, 0, 1, 1);
		ctrls.add(stop, 1, 1, 1, 1);
		toggle.prefHeightProperty().bind(ctrls.heightProperty());

		GroupPane ctrlGP = new GroupPane("操作", new HBox(8, options, ctrls));

		// 顶部
		BorderPane header = new BorderPane();
		header.setLeft(proxyGP);
		header.setCenter(cookieNetSpeed);
		header.setRight(ctrlGP);

		// 图片 ID
		Label labelPids = new Label("作品 ID，以半角 \",\" 分隔");
		labelPids.setPadding(new Insets(4, 0, 4, 8));
		labelPids.setBorder(new BorderX(BetterFX.LIGHT_GRAY).width(1, 1, 0, 1).build());
		pids = new TextArea() {
			@Override
			public void paste() {
				// 重写粘贴
				final Clipboard clipboard = Clipboard.getSystemClipboard();
				if (clipboard.hasString()) {
					final String text = clipboard.getString();
					if (text != null) {
						if (text.startsWith("http")) {
							replaceSelection(text.substring(text.lastIndexOf('/') + 1) + ", ");
						} else {
							replaceSelection(text);
						}
					}
				}
			}
		};
		pids.setPromptText("示例：89031688, 88989071");

		BorderPane pidsBox = new BorderPane();
		labelPids.prefWidthProperty().bind(pidsBox.widthProperty());
		pidsBox.setTop(labelPids);
		pidsBox.setCenter(pids);

		// 下载位置
		Label labelPath = new Label("下载到：");
		labelPath.setPadding(new Insets(0, 0, 0, 8));
		HBox labelPathBox = new HBox(labelPath);
		labelPathBox.setAlignment(Pos.CENTER_RIGHT);
		labelPathBox.setBorder(new BorderX(BetterFX.LIGHT_GRAY).width(1, 1, 1, 0).build());
		path = new TextField();
		path.setBorder(new BorderX(BetterFX.LIGHT_GRAY).width(1, 0).build());
		PixivDL.config.bindTextProperty(path, "path");
		select = new PixelButton("选择");
		select.setOnMousePressed(null);
		select.setOnMouseReleased(null);
		filter = new PixelButton("过滤失败单品");
		filter.setOnMousePressed(null);
		filter.setOnMouseReleased(null);

		BorderPane pathBox = new BorderPane();
		BorderPane.setAlignment(labelPath, Pos.CENTER_RIGHT);
		pathBox.setLeft(labelPathBox);
		pathBox.setCenter(path);
		pathBox.setRight(new HBox(select, filter));

		// 下载列表
		list = new DLTable();

		BorderPane dlBox = new BorderPane();
		dlBox.setTop(pathBox);
		dlBox.setCenter(list);

		// 中间
		SplitPane center = new SplitPane();
		SplitPane.setResizableWithParent(pids, false);
		center.setDividerPositions(.4, .6);
		center.getItems().addAll(pidsBox, dlBox);

		// 日志
		log = new TextArea();
		log.setPrefHeight(220);
		log.setEditable(false);
		log.setStyle("-fx-font-family: 'Consolas'");

		// 日志操作
		logClean = new Label("清空");
		logClean.setCursor(Cursor.HAND);
		logClean.setTextFill(Paint.valueOf("#0096C9"));
		logClean.underlineProperty().bind(logClean.hoverProperty());
		logExport = new Label("导出");
		logExport.setCursor(Cursor.HAND);
		logExport.setTextFill(Paint.valueOf("#0096C9"));
		logExport.underlineProperty().bind(logExport.hoverProperty());
		HBox logCtrl = new HBox(6, logClean, logExport);

		AnchorPane logPane = new AnchorPane(log, logCtrl);
		AnchorPaneX.def(log);
		AnchorPaneX.def(logCtrl, 2, 4, null, null);

		BorderPane root = new BorderPane();
		BorderPane.setMargin(center, new Insets(8, 0, 8, 0));
		root.setTop(header);
		root.setCenter(center);
		root.setBottom(logPane);
		root.setPadding(new Insets(8));

		super.root.setCenter(root);
		scene.getStylesheets().add("style.css");
		setSyncTitle("批量爬取 Pixiv 图片 - 夜雨");
		setMinSize(1024, 650);
		stage.getIcons().add(new Image("icon.png"));
	}
}