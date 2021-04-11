package net.imyeyu.pdl.bean;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 下载列表对象
 *
 * 夜雨 创建于 2021/4/11 01:38
 */
public class PixivImage {

	private final String id; // 作品 ID
	private final SimpleIntegerProperty imgI = new SimpleIntegerProperty(0); // 当前下载单品
	private final SimpleIntegerProperty imgCount = new SimpleIntegerProperty(0); // 作品总数
	private final SimpleDoubleProperty percent = new SimpleDoubleProperty(0); // 单品下载进度
	private final SimpleListProperty<String> failList = new SimpleListProperty<>(); // 下载失败单品

	public PixivImage(String id) {
		this.id = id;
		failList.set(FXCollections.observableArrayList());
	}

	public String getId() {
		return id;
	}

	public SimpleIntegerProperty imgIProperty() {
		return imgI;
	}

	public void setImgI(int imgI) {
		this.imgI.set(imgI);
	}

	public SimpleIntegerProperty imgCountProperty() {
		return imgCount;
	}

	public void setImgCount(int imgCount) {
		this.imgCount.set(imgCount);
	}

	public SimpleDoubleProperty percentProperty() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent.set(percent);
	}

	public ReadOnlyBooleanProperty notFailProperty() {
		return failList.emptyProperty();
	}

	public ObservableList<String> getFailList() {
		return failList.get();
	}

	public void addFail(String id) {
		failList.add(id);
	}
}