package net.imyeyu.pdl.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.pixelfx.PixelStage;
import net.imyeyu.pixelfx.Zpix;
import net.imyeyu.pixelfx.component.PixelButton;

/**
 * 关于页面
 *
 * 夜雨 创建于 2021/4/11 16:15
 */
public class ViewAbout extends PixelStage {

	private static final String VERSION = "1.0.0";

	protected Label name, version;
	protected Hyperlink blog;

	public ViewAbout() {
		super(380, 240, true);

		VBox center = new VBox();
		name = new Label("PixivDL", new ImageView("logo.png"));
		Zpix.css(name, Zpix.M);
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

		PixelButton ok = new PixelButton("好");
		ok.setOnAction(event -> close());

		BorderPane.setAlignment(ok, Pos.CENTER);
		BorderPane.setMargin(ok, new Insets(6, 0, 6, 0));
		super.root.setCenter(root);
		super.root.setBottom(ok);
		setSyncTitle("关于 PixivDL");
		getIcons().add(new Image("icon.png"));
		initModality(Modality.APPLICATION_MODAL);
	}
}
