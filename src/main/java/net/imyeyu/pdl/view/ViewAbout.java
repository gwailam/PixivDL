package net.imyeyu.pdl.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.betterfx.BetterFX;

/**
 * 关于页面
 *
 * 夜雨 创建于 2021/4/11 16:15
 */
public class ViewAbout extends Stage {

	private static final String VERSION = "1.1.1";

	protected Label name, version;
	protected Hyperlink blog;

	public ViewAbout() {
		VBox center = new VBox();
		name = new Label("PixivDL", new ImageView("logo.png"));
		name.setFont(Font.font(24));
		Label tips = new Label("本程序仅供学习使用，请到原站支持画师");
		tips.setTextFill(BetterFX.RED);

		version = new Label(VERSION);
		version.setWrapText(false);
		center.setAlignment(Pos.TOP_CENTER);
		center.setSpacing(4);
		center.getChildren().addAll(name, version, tips);

		VBox bottom = new VBox();
		Label developer = new Label("开发者：夜雨");
		Label labelBlog = new Label("个人博客：");
		blog = new Hyperlink("https://www.imyeyu.net");
		HBox blogPane = new HBox(labelBlog, blog);
		blogPane.setAlignment(Pos.CENTER);

		Label cr = new Label("Copyright © 夜雨 2021 All Rights Reserved 版权所有");
		bottom.setSpacing(4);
		bottom.setAlignment(Pos.CENTER);
		bottom.getChildren().addAll(developer, blogPane, cr);

		BorderPane root = new BorderPane();
		root.setCenter(center);
		root.setBottom(bottom);
		root.setPadding(new Insets(8));
		root.setBorder(BetterFX.BORDER_TOP);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(BetterFX.CSS);
		setScene(scene);
		getIcons().add(new Image("icon.png"));
		setTitle("关于");
		setMinWidth(380);
		setMinHeight(240);
		setWidth(380);
		setHeight(240);
		initModality(Modality.APPLICATION_MODAL);
	}
}
