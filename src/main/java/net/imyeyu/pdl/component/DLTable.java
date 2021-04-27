package net.imyeyu.pdl.component;

import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.util.Callback;
import net.imyeyu.betterfx.extend.BgFill;
import net.imyeyu.pdl.bean.PixivImage;

/**
 * 下载列表
 *
 * 夜雨 创建于 2021/4/11 01:37
 */
public class DLTable extends TableView<PixivImage> {

	private static final Background BG_FAIL = new BgFill("#F9DBE9").build();

	public DLTable() {
		TableColumn<PixivImage, String> colID = new TableColumn<>("PID");
		TableColumn<PixivImage, String> colPercent = new TableColumn<>("进度");

		colID.setPrefWidth(110);
		colID.setStyle("-fx-alignment: center");
		colPercent.setMinWidth(110);

		colID.setCellValueFactory(new PropertyValueFactory<>("id"));
		colPercent.setCellFactory(new Callback<>() {
			public TableCell<PixivImage, String> call(TableColumn<PixivImage, String> param) {
				return new TableCell<>(){
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty && getTableRow().getItem() != null) {
							PixivImage pi = getTableRow().getItem();
							PixivPB ppb = new PixivPB();

							ppb.nowProperty().bind(pi.imgIProperty());
							ppb.countProperty().bind(pi.imgCountProperty());
							ppb.progressProperty().bind(pi.percentProperty());
							pi.notFailProperty().addListener((obs, o, notFail) -> getTableRow().setBackground(notFail ? Background.EMPTY : BG_FAIL));

							ppb.getPb().prefWidthProperty().bind(colPercent.widthProperty());
							this.setGraphic(ppb);
						} else {
							getTableRow().setBackground(Background.EMPTY);
							this.setGraphic(null);
						}
					}
				};
			}
		});
		// 宽度绑定
		colPercent.prefWidthProperty().bind(widthProperty().subtract(colID.widthProperty()).subtract(16));
		// 禁止调整宽度
		colID.setResizable(false);
		colPercent.setResizable(false);
		// 禁止选择
		addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

		getColumns().add(colID);
		getColumns().add(colPercent);
	}
}