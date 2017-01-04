package biz.schmidt_it.remittanceassigner.controller;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.NumberFormat;

public class PercentageFormatCell<T> extends TableCell<T, Double> {
    public static <T> Callback<TableColumn<T, Double>, TableCell<T, Double>> getFactory() {
        return p -> new PercentageFormatCell();
    }

    public PercentageFormatCell() {
    }

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && null == item) {
            item = new Double(0.0d);
        }

        setText(item == null ? "" : NumberFormat.getPercentInstance().format(item));
    }
}
