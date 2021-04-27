package net.imyeyu.pdl.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import net.imyeyu.betterfx.BetterFX;

/**
 * 下载进度
 *
 * 夜雨 创建于 2021/4/10 22:19
 */
public class PixivPB extends StackPane {

	private final ProgressBar pb;
	private final Label text;
	private final SimpleIntegerProperty now = new SimpleIntegerProperty(0);
	private final SimpleIntegerProperty count = new SimpleIntegerProperty(0);

	public PixivPB() {
		pb = new ProgressBar();

		text = new Label("0 / 0");
		text.setTranslateY(-2);
		pb.progressProperty().addListener((obs, o, pb) -> {
			if (pb != null) {
				text.setTextFill(pb.doubleValue() < .5 ? BetterFX.BLACK : BetterFX.WHITE);
			}
		});

		now.addListener((obs, o, now) -> text.setText(now + " / " + count.get()));
		count.addListener((obs, o, count) -> text.setText(now.get() + " / " + count));

		getChildren().addAll(pb, text);
	}

	public ProgressBar getPb() {
		return pb;
	}

	public IntegerProperty nowProperty() {
		return now;
	}

	public IntegerProperty countProperty() {
		return count;
	}

	public DoubleProperty progressProperty() {
		return pb.progressProperty();
	}
}