package controller;

import DBAccess.DBCountries;
import DBAccess.DBCustomers;
import DBAccess.DBFirstLevelDivisions;
import Utils.DBConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Contacts;
import model.Countries;
import model.Customers;
import model.FirstLevelDivisions;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Thoai Tran
 * <br>
 *     This is the Modify Customer Controller Class.
 *     This class handles the mechanics to load user input into the SQL DB
 *     To update an existing customer record.
 * */
public class ModifyCustomerController implements Initializable {

    @FXML
    private TextField customerIDTxt;
    @FXML
    private TextField nameTxt;
    @FXML
    private TextField addressTxt;
    @FXML
    private TextField postalCodeTxt;
    @FXML
    private TextField phoneNumberTxt;
    @FXML
    private ComboBox<Countries> countryComboBox;
    @FXML
    private ComboBox<FirstLevelDivisions> firstLevelDivisionComboBox;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Customers selected;
    String userName;


    ObservableList<Countries> countriesList = DBCountries.getAllCountryNames();
    ObservableList<FirstLevelDivisions> firstLevelDivisionsUSList = DBFirstLevelDivisions.getAllFirstLevelDivisionUS();
    ObservableList<FirstLevelDivisions> firstLevelDivisionsUKList = DBFirstLevelDivisions.getAllFirstLevelDivisionUK();
    ObservableList<FirstLevelDivisions> firstLevelDivisionsCanadaList = DBFirstLevelDivisions.getAllFirstLevelDivisionCanada();

    /** This method sets the username to be the same as the current user.
     * @param userName username.
     */
    public ModifyCustomerController(Customers selected, String userName) {
        this.selected = selected;
        this.userName = userName;
    }

    /** This method initializes the implementation of the Modify Customer Controller method.
     * @param url initialize
     * @param resourceBundle initialize.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setCustomer();
    }

    /**This is the Set Customer Method.
     * This method sets the selected customer and loads the information into the text fields.
     * */
    private void setCustomer() {

        String countrytoModify = selected.getCountry();
        Countries selectedCountry = new Countries(countrytoModify);

        String fldtoModify = selected.getDivision();
        FirstLevelDivisions selectedfld = new FirstLevelDivisions(fldtoModify);

        this.nameTxt.setText(selected.getName());
        this.customerIDTxt.setText(Integer.toString(selected.getCustomerId()));
        this.addressTxt.setText(selected.getAddress());
        this.postalCodeTxt.setText(selected.getPostalCode());
        this.phoneNumberTxt.setText(selected.getPhone());
        this.countryComboBox.setValue(selectedCountry);
        this.firstLevelDivisionComboBox.setValue(selectedfld);

    }


    /** This is the Country Combo Box.
     * This loads the combo box with the countries name list.
     * @param event event.
     * */
    @FXML
    void countryComboBox(MouseEvent event) {

        DBConnection.startConnection();
        countryComboBox.setItems(countriesList);

    }

    /** This is the First Level Division Combo Box.
     * This loads the combo box with the states and provinces list.
     * User is prompted to choose country first, and then state/province box
     * is populated with corresponding country.
     * @param event event.
     * */
    @FXML
    void firstLevelDivisionComboBox(MouseEvent event) {

        if(countryComboBox.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please choose Country first.");
            alert.showAndWait();

        } else {

            if (countryComboBox.getSelectionModel().getSelectedItem().getName().equals("U.S")) {

                DBConnection.startConnection();
                firstLevelDivisionComboBox.setItems(firstLevelDivisionsUSList);
            }

            if (countryComboBox.getSelectionModel().getSelectedItem().getName().equals("UK")) {

                DBConnection.startConnection();
                firstLevelDivisionComboBox.setItems(firstLevelDivisionsUKList);
            }

            if (countryComboBox.getSelectionModel().getSelectedItem().getName().equals("Canada")) {

                DBConnection.startConnection();
                firstLevelDivisionComboBox.setItems(firstLevelDivisionsCanadaList);
            }
        }
    }

    /** This is the Save Button.
     * This methods loads the user input into the SQL DB.
     * Validates Input by making sure all fields.
     * Makes sure that countries and first-level divisions match.
     * @param event event.
     * */
    @FXML
    void saveButton(MouseEvent event) throws IOException {

        FirstLevelDivisions divisionSelected = firstLevelDivisionComboBox.getSelectionModel().getSelectedItem();
        Countries countrySelected = countryComboBox.getSelectionModel().getSelectedItem();

        if (divisionSelected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing division field!");
            alert.showAndWait();
            return;
        }

        int id = selected.getCustomerId();
        String name = nameTxt.getText().trim();
        String address = addressTxt.getText().trim();
        String postalCode = postalCodeTxt.getText().trim();
        String phone = phoneNumberTxt.getText().trim();
        String division = divisionSelected.getName();
        String country = countrySelected.getName();

        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing name field!");
            alert.showAndWait();
            return;
        }
        if (address.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing address field!");
            alert.showAndWait();
            return;
        }
        if (postalCode.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing postal code field!");
            alert.showAndWait();
            return;
        }
        if (phone.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing phone field!");
            alert.showAndWait();
            return;
        }
        if (countryComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing country field!");
            alert.showAndWait();
            return;
        }


        try {
            String sqlfld = "SELECT * FROM first_level_divisions \n" +
                    "WHERE Division = ?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlfld);
            ps.setString(1, division);
            ResultSet rs = ps.executeQuery();

            rs.next();
            int divisionId = rs.getInt("Division_ID");

            if (country.equals("U.S") && divisionId > 53) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Country and State/Province combination!");
                alert.setContentText("""
                    Please enter a valid input:
                    Country and State/Province must be valid.
                    """);
                alert.showAndWait();
                return;
            }
            if (country.equals("Canada") && divisionId < 60 || divisionId > 72) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Country and State/Province combination!");
                alert.setContentText("""
                    Please enter a valid input:
                    Country and State/Province must be valid.
                    """);
                alert.showAndWait();
                return;
            }
            if (country.equals("UK") && divisionId < 101  ) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Country and State/Province combination!");
                alert.setContentText("""
                    Please enter a valid input:
                    Country and State/Province must be valid.
                    """);
                alert.showAndWait();
                return;
            }

            String sqlCust = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Division_ID = ? WHERE Customer_ID = ?";
            PreparedStatement psCust = DBConnection.getConnection().prepareStatement(sqlCust);


            psCust.setString(1, name);
            psCust.setString(2, address);
            psCust.setString(3, postalCode);
            psCust.setString(4, phone);
            psCust.setInt(5, divisionId);
            psCust.setInt(6, id);

            psCust.executeUpdate();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customerOverview.fxml"));
            CustomerOverview controller = new CustomerOverview(userName);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

    }

    /** This is the Cancel Button.
     * This brings the user back to the Customer Overview page.
     * @param event event.
     * */
    @FXML
    void cancelButton(MouseEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to Cancel? Changes you made so far will not be saved.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customerOverview.fxml"));
            CustomerOverview controller = new CustomerOverview(userName);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }

    }
}
