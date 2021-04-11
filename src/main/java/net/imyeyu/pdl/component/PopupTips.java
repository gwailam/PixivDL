package net.imyeyu.pdl.component;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import net.imyeyu.betterfx.extend.BgFill;
import net.imyeyu.betterfx.extend.BorderX;

/**
 * 弹窗提示
 *
 * 夜雨 创建于 2021/4/11 13:23
 */
public class PopupTips extends Popup {

	private final Label label;
	private final ImageView imageView;
	private final StackPane root;

	public PopupTips() {
		label = new Label();
		label.setWrapText(true);
		label.setPadding(new Insets(4, 8, 4, 8));

		imageView = new ImageView();

		root = new StackPane();
		DropShadow shadow = new DropShadow();
		shadow.setRadius(0);
		shadow.setOffsetX(2);
		shadow.setOffsetY(2);
		shadow.setSpread(1);
		shadow.setColor(Color.valueOf("#000A"));

		root.setEffect(shadow);
		root.setBackground(new BgFill("#DFECFA").build());
		root.setBorder(new BorderX("#CDDEF0").width(2).build());

		StackPane shadowPane = new StackPane();
		shadowPane.setPadding(new Insets(3));
		shadowPane.setBackground(Background.EMPTY);
		shadowPane.getChildren().add(root);

		setOpacity(.9);
		getContent().add(shadowPane);
	}

	public void showText(Window window, String text) {
		root.getChildren().setAll(label);
		label.setText(text);
		show(window);
	}

	public void showImage(Window window, Image img) {
		root.getChildren().setAll(imageView);
		imageView.setImage(img);
		setWidth(img.getWidth());
		setHeight(img.getHeight());
		show(window);
	}
}
