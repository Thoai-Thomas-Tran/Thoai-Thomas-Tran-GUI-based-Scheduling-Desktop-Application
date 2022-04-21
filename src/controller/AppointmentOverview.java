package controller;

import DBAccess.DBAppointments;
import Utils.DBConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Appointments;
import model.Contacts;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Thoai Tran
 * <br>
 *     This is the Appointment Overview Controller Class.
 *     This class provides the data stored for appointments from the SQL DB.
 *     This is the main menu for users to add, modify, and delete appointment records.
 *     Users are able to view by this month, this week, or show all.
 * */
public class AppointmentOverview implements Initializable {

    @FXML
    private Button addAppointmentButton;
    @FXML
    private Button deleteAppointmentButton;
    @FXML
    private Button backToMainMenuButton;
    @FXML
    private TableView<Appointments> appointmentsTableView;
    @FXML
    private TableColumn<Appointments, Integer> appointmentIDCol;
    @FXML
    private TableColumn<Appointments, String> titleCol;
    @FXML
    private TableColumn<Appointments, String> descriptionCol;
    @FXML
    private TableColumn<Appointments, String> locationCol;
    @FXML
    private TableColumn<Appointments, String> contactCol;
    @FXML
    private TableColumn<Appointments, String> contactNameCol;
    @FXML
    private TableColumn<Appointments, String> typeCol;
    @FXML
    private TableColumn<Appointments, Timestamp> startDateAndTimeCol;
    @FXML
    private TableColumn<Appointments, Timestamp> endDateAndTimeCol;
    @FXML
    private TableColumn<Appointments, Integer> customerIDCol;
    @FXML
    private TableColumn<Appointments, String> customerNameCol;
    @FXML
    private TableColumn<Appointments, Integer> userIdCol;
    @FXML
    private Button modifyAppointmentButton;
    @FXML
    private RadioButton byMonth;
    @FXML
    private RadioButton byWeek;
    @FXML
    private RadioButton showAll;
    @FXML
    private Label currentTimeZoneLabel;
    @FXML
    private Label currentUserLabel;

    String userName;

    ObservableList<Appointments> appointmentsList = DBAppointments.getAllAppointments();
    ObservableList<Appointments> appointmentsByWeekList = DBAppointments.getAllAppointmentsByWeek();
    ObservableList<Appointments> appointmentsByMonthList = DBAppointments.getAllAppointmentsByMonth();

    /** This method sets the username to be the same as the current user.
     * @param userName username.
     * */
    public AppointmentOverview(String userName) {
        this.userName = userName;
    }


    /** This method initializes the implementation of the AppointmentOverview method.
     * Initializes the DB connection.
     * Generates the table view.
     * Lamda is used to display Current TimeZone and Current User for user reference.
     * Property tags are set to match appointment constructor with table view columns.
     * @param url initialize
     * @param resourceBundle initialize.
     * */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        DBConnection.startConnection();

        generateAppointmentsTable();

        //Lambda to put current TimeZone on display for user reference in Appointment View
        UserLabelInterface message = s -> "Current TimeZone is : " + s;
        currentTimeZoneLabel.setText(message.getMessage(Calendar.getInstance().getTimeZone().getDisplayName()));

        //Lambda to put current User on display for user reference in Appointment View
        UserLabelInterface currentUserTag = s -> "Current User: " + s;
        currentUserLabel.setText(currentUserTag.getMessage(userName));


        appointmentIDCol.setCellValueFactory(new PropertyValueFactory<>("appointmentsId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactId"));
        contactNameCol.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        startDateAndTimeCol.setCellValueFactory(new PropertyValueFactory<>("startDateAndTime"));
        endDateAndTimeCol.setCellValueFactory(new PropertyValueFactory<>("endDateAndTime"));
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customersId"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
    }

    /** This method generates the appointments table view.
     * */
    private void generateAppointmentsTable() {
        appointmentsList.setAll(DBAppointments.getAllAppointments());
        appointmentsTableView.setItems(appointmentsList);
        appointmentsTableView.refresh();
    }

    /** This is the This Month Radio Button.
     * This displays the appointment table for the current month.
     * @param event event
     * */
    @FXML
    void byMonth(MouseEvent event) {

        appointmentsByMonthList.setAll(DBAppointments.getAllAppointmentsByMonth());
        appointmentsTableView.setItems(appointmentsByMonthList);
        appointmentsTableView.refresh();

    }

    /** This is the This Week Radio Button.
     * This displays the appointment table for the current week.
     * @param event event
     * */
    @FXML
    void byWeek(MouseEvent event) {

        appointmentsByWeekList.setAll(DBAppointments.getAllAppointmentsByWeek());
        appointmentsTableView.setItems(appointmentsByWeekList);
        appointmentsTableView.refresh();

    }

    /** This is the Show All Radio Button.
     * This displays the appointment table for all the records.
     * @param event event
     * */
    @FXML
    void showAll(MouseEvent event) {

        generateAppointmentsTable();
    }

    /** This is the Back To Main Menu Button.
     * This brings the user back to the main menu.
     * @param event event
     * */
    @FXML
    void backToMainMenuButton(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainScreen.fxml"));
        MainScreenController controller = new MainScreenController(userName);
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    /** This is the Add Appointment Button.
     * This brings the user to the Add Appointment Screen.
     * @param event event.
     * */
    @FXML
    void addAppointmentButton(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/addAppointmentScreen.fxml"));
        AddAppointmentController controller = new AddAppointmentController(userName);
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    /** This is the Modify Appointment Button.
     * This brings the user to the Modify Appointment Screen.
     * @param event event.
     * */
    @FXML
    void modifyAppointmentButton(MouseEvent event) throws IOException {

        try {
            Appointments selected = appointmentsTableView.getSelectionModel().getSelectedItem();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/modifyAppointmentScreen.fxml"));
            ModifyAppointmentController controller = new ModifyAppointmentController(selected, userName);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR, " Please select an appointment to modify.");
            alert.showAndWait();
            e.printStackTrace();
        }


}

    /** This is the Delete Appointment Button
     * This method provides the user with the ability to delete records,
     * upon confirmation.
     * @param event event
     * */
    @FXML
    void deleteAppointmentButton(MouseEvent event) {
        Appointments appointmentToRemove = appointmentsTableView.getSelectionModel().getSelectedItem();

        try {
            if (appointmentsTableView.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " " +
                        "Please select an appointment to delete.");
                alert.showAndWait();
                return;
            }
            boolean confirm = confirmationWindow(appointmentToRemove.getAppointmentsId(),
                    appointmentToRemove.getCustomerName(), appointmentToRemove.getType());
            if (!confirm) {
                return;
            }

            String sqlAppoint = "DELETE FROM appointments WHERE Appointment_ID = ?";
            PreparedStatement psCustAppoint = DBConnection.getConnection().prepareStatement(sqlAppoint);
            psCustAppoint.setInt(1, appointmentToRemove.getAppointmentsId());
            psCustAppoint.executeUpdate();

            generateAppointmentsTable();

        } catch (SQLException e) {

            e.printStackTrace();
        }

    }

    /** This method defines a confirmation window to be used in other methods.
     * @param name
     * */
    private boolean confirmationWindow(int id, String name, String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete part");
        alert.setHeaderText("Are you sure you want to delete appointment ID : " + id + ", " + name + ", type: " + type + " ?");
        alert.setContentText("Click ok to confirm");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }
}