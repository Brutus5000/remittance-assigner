/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.schmidt_it.remittanceassigner.controller;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;


/**
 * Special table text field cell that commit its content on focus lost.
 */
public class TextFieldTableCellEx<S, T> extends TextFieldTableCell<S, T> {
    public class CellEditEvent<S, T> extends TableColumn.CellEditEvent<S, T>{
        public S getItem() {
            return item;
        }

        private S item;

        public CellEditEvent(TableView<S> table, TablePosition<S, T> pos, EventType<TableColumn.CellEditEvent<S, T>> eventType, T newValue, S item) {
            super(table, pos, eventType, newValue);
            this.item = item;
        }
    }

    private String curTxt = "";
    private boolean cancel = false;
    private S item;

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>>
    cellFactory(final StringConverter<T> converter) {
        return p -> new TextFieldTableCellEx<>(converter);
    }

    /**
     * Text field cell constructor.
     *
     * @param converter Bidirectional String converter
     */
    private TextFieldTableCellEx(StringConverter<T> converter) {
        super(converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEdit() {
        cancel = false;
        item = getTableView().getItems().get(getTableView().getEditingCell().getRow());

        super.startEdit();

        Node g = getGraphic();

        if (g != null) {
            final TextField tf = (TextField) g;

            curTxt = tf.getText();

            tf.textProperty().addListener((val, oldVal, newVal) -> curTxt = newVal);

            tf.setOnKeyPressed(evt -> {
                if (KeyCode.ESCAPE == evt.getCode()) {
                    cancel = true;
                    cancelEdit();
                }
            });

            tf.setOnKeyReleased(evt -> {
                // No-op to overwrite JavaFX implementation.
            });

            // Special hack for editable TextFieldTableCell.
            // Cancel edit when focus lost from text field, but do not cancel if focus lost to VirtualFlow.
            tf.focusedProperty().addListener((val, oldVal, newVal) -> {
                Node fo = getScene().getFocusOwner();

                if (!newVal) {
                    if (fo instanceof VirtualFlow) {
                        if (fo.getParent().getParent() != getTableView())
                            cancelEdit();
                    } else
                        cancelEdit();
                }
            });

            Platform.runLater(() -> tf.requestFocus());
        }
    }

    @Override
    public void commitEdit(T newValue){
        if(! isEditing() && !cancel) // & not canceled
        {
            final TableView<S> table = getTableView();
            if (table != null) {
                // Inform the TableView of the edit being ready to be committed.
                TableColumn.CellEditEvent editEvent = new TextFieldTableCellEx.CellEditEvent(
                        table,
                        table.getEditingCell(),
                        TableColumn.editCommitEvent(),
                        newValue,
                        item
                );

                Event.fireEvent(getTableColumn(), editEvent);
            }

            // inform parent classes of the commit, so that they can switch us
            // out of the editing state.
            // This MUST come before the updateItem call below, otherwise it will
            // call cancelEdit(), resulting in both commit and cancel events being
            // fired (as identified in RT-29650)
            super.commitEdit(newValue);

            // update the item within this cell, so that it represents the new value
            updateItem(newValue, false);

            if (table != null) {
                // reset the editing cell on the TableView
                table.edit(-1, null);
            }
        }
        else if(!cancel){
            super.commitEdit(newValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        boolean editing = isEditing();

        if (!cancel) {
            commitEdit(getConverter().fromString(curTxt));
        }

        super.cancelEdit();
    }
}