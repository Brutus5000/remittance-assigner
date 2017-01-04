package biz.schmidt_it.remittanceassigner.controller;

import biz.schmidt_it.remittanceassigner.Main;
import biz.schmidt_it.remittanceassigner.Util;
import biz.schmidt_it.remittanceassigner.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;


public class MainWindowController {
    @FXML
    TableView<Invoice> inputTable;
    @FXML
    TableColumn<Invoice, String> invoiceIdColumn;
    @FXML
    TableColumn<Invoice, MonetaryAmount> invoiceAmountColumn;
    @FXML
    TextField targetAmountText;
    @FXML
    TabPane solutionsTabPane;

    private MonetaryAmountFormat formatter;

    private final ObservableList<Invoice> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        formatter = MonetaryFormats.getAmountFormat(Locale.getDefault());

        inputTable.setItems(data);

        invoiceIdColumn.setCellFactory(TextFieldTableCellEx.cellFactory(new DefaultStringConverter()));
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        invoiceAmountColumn.setCellFactory(TextFieldTableCellEx.cellFactory(Util.getMonetaryAmountToStringConverter(formatter)));
        invoiceAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    @FXML
    void onEditCommitId(TableColumn.CellEditEvent<Invoice, String> event) {
        if(event instanceof TextFieldTableCellEx.CellEditEvent)
        {
            TextFieldTableCellEx<Invoice, String>.CellEditEvent<Invoice, String> cellEditEvent = (TextFieldTableCellEx<Invoice, String>.CellEditEvent<Invoice, String>) event;
            cellEditEvent.getItem().setId(event.getNewValue());
        }
    }

    @FXML
    void onEditCommitAmount(TableColumn.CellEditEvent<Invoice, MonetaryAmount> event) {
        if(event instanceof TextFieldTableCellEx.CellEditEvent)
        {
            TextFieldTableCellEx<Invoice, MonetaryAmount>.CellEditEvent<Invoice, MonetaryAmount> cellEditEvent = (TextFieldTableCellEx<Invoice, MonetaryAmount>.CellEditEvent<Invoice, MonetaryAmount>) event;
            cellEditEvent.getItem().setAmount(event.getNewValue());
        }
    }

    @FXML
    void reconcile() {
        if (targetAmountText.getText().length() == 0) {
            targetAmountText.requestFocus();
            return;
        }

        solutionsTabPane.getTabs().clear();
        data.removeIf(invoice -> invoice.getAmount() == null);

        Double[] cashbacks = {0.00d, 0.02d, 0.03d};
        AssignmentAlgorithm algorithm = new AssignmentAlgorithm(Arrays.asList(cashbacks));

        List<AssignmentAlgorithm.Candidate> candidateList = algorithm.solve(data, Util.tryParse(targetAmountText.getText()));

        if (candidateList.size() > 0) {
            int index = 1;
            for (AssignmentAlgorithm.Candidate candidate : candidateList) {
                addSolutionTab(index, candidate.getAssignments());
                index++;
            }
        } else {
            solutionsTabPane.getTabs().add(new Tab(Main.getProperty("result.missing", "No results found")));
        }
    }

    @FXML
    void infoCopyright() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Main.getProperty("copyright.head"));
        alert.setHeaderText(Main.getProperty("copyright.intro"));
        alert.setContentText(Main.getProperty("copyright.licenseTeaser"));

        Label label = new Label(Main.getProperty("copyright.licenseText"));

        String licenseText = new Scanner(getClass().getResourceAsStream("/license"), "UTF-8").useDelimiter("\\A").next();

        TextArea textArea = new TextArea(licenseText);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    void addSolutionTab(Integer index, AssignmentAlgorithm.Result result) {
        ObservableList<Assignment> assignments = FXCollections.observableArrayList(result);

        Tab tab = new Tab(MessageFormat.format(Main.getProperty("result.tabtext"), index));
        TableView tableView = new TableView();
        tab.setContent(tableView);

        TableColumn<Assignment, String> invoiceIdColumn = new TableColumn<>(Main.getProperty("invoice.id", "Invoice ID"));
        invoiceIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));

        TableColumn<Assignment, MonetaryAmount> invoiceTotalAmountColumn = new TableColumn<>(Main.getProperty("invoice.amount", "Total Amount"));
        invoiceTotalAmountColumn.setCellFactory(param -> new TextFieldTableCell(Util.getMonetaryAmountToStringConverter(formatter)));
        invoiceTotalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        invoiceTotalAmountColumn.getStyleClass().add("money");

        TableColumn<Assignment, MonetaryAmount> invoiceAppliedAmountColumn = new TableColumn<>(Main.getProperty("invoice.appliedAmount", "Applied Amount"));
        invoiceAppliedAmountColumn.setCellFactory(param -> new TextFieldTableCell(Util.getMonetaryAmountToStringConverter(formatter)));
        invoiceAppliedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("appliedAmount"));
        invoiceAppliedAmountColumn.getStyleClass().add("money");

        TableColumn<Assignment, Double> invoiceAppliedCashbackColumn = new TableColumn<>(Main.getProperty("invoice.appliedCasback", "Applied Cashback"));
        invoiceAppliedCashbackColumn.setCellFactory(PercentageFormatCell.getFactory());
        invoiceAppliedCashbackColumn.setCellValueFactory(new PropertyValueFactory<>("cashback"));

        tableView.getColumns().addAll(invoiceIdColumn, invoiceTotalAmountColumn, invoiceAppliedAmountColumn, invoiceAppliedCashbackColumn);


        solutionsTabPane.getTabs().add(tab);
        tableView.setItems(assignments);
    }

    @FXML
    void pasteFromClipboard() {
        try {
            String clipboardText = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);

            String[] lines = clipboardText.split("\r?\n");
            for (String line : lines) {
                String[] cellValue = line.split("\t");

                try {
                    if (cellValue.length == 1)
                        data.add(new Invoice("", Util.tryParse(cellValue[0])));
                    else if (cellValue.length >= 2) {
                        data.add(new Invoice(cellValue[0], Util.tryParse(cellValue[1])));
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {

        }
    }

    @FXML
    void addNewLine() {
        Invoice invoice = new Invoice("", Money.of(0, Currency.getInstance(Locale.getDefault()).toString()));
        data.add(invoice);
        inputTable.layout();
        inputTable.edit(inputTable.getItems().size() - 1, invoiceIdColumn);
    }

    @FXML
    void removeSelectedLine() {
        data.removeAll(inputTable.getSelectionModel().getSelectedItems());
    }

    @FXML
    void clearTable() {
        data.clear();
    }
}
