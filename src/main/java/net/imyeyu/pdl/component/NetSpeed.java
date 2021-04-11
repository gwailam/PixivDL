package net.imyeyu.pdl.component;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.extend.AnchorPaneX;

/**
 * 网速曲线
 *
 * 夜雨 创建于 2021/4/11 14:58
 */
public class NetSpeed extends AnchorPane {

	private long i = 0;
	private final Label label;
	private final XYChart.Series<String, Number> series;

	public NetSpeed() {
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis;
		yAxis = new NumberAxis();
		xAxis.setOpacity(0);
		xAxis.setTickLabelsVisible(false);
		yAxis.setOpacity(0);
		yAxis.setTickLabelsVisible(false);
		final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

		series = new XYChart.Series<>();

		lineChart.setAnimated(false);
		lineChart.setCreateSymbols(false);
		lineChart.setLegendVisible(false);
		lineChart.setVerticalGridLinesVisible(false);
		lineChart.setHorizontalZeroLineVisible(false);
		lineChart.setAlternativeRowFillVisible(false);
		lineChart.setHorizontalGridLinesVisible(false);
		lineChart.setAlternativeColumnFillVisible(false);
		lineChart.getData().add(series);
		lineChart.setMinSize(0, 0);

		lineChart.prefWidthProperty().bind(widthProperty());
		lineChart.prefHeightProperty().bind(heightProperty());

		AnchorPaneX.def(lineChart, -12, -20, -22, -32);

		label = new Label("0 B/s");
		label.setTextFill(BetterFX.GRAY);
		AnchorPaneX.def(label, -4, 0, null, null);

		getChildren().addAll(lineChart, label);

		for (i = 0; i < 60; i++) {
			series.getData().add(new Data<>(String.valueOf(i), 0));
		}
	}

	public void addValue(double value) {
		series.getData().add(new Data<>(String.valueOf(i), value));
		if (60 < i) series.getData().remove(0);
		i++;
	}

	public void setText(String text) {
		label.setText(text);
	}
}