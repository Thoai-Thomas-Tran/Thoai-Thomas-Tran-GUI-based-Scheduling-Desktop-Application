package controller;


import DBAccess.DBAppointments;
import DBAccess.DBContacts;
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
import model.*;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Thoai Tran
 * <br>
 *     This is the Add Appointment Controller Class.
 *     This class handles the mechanics to load user input into the SQL DB
 *     To create a new appointment.
 * */
public class AddAppointmentController implements Initializable {


    @FXML
    private TextField appointmentIDTxt;
    @FXML
    private TextField customerIDTxt;
    @FXML
    private TextField titleTxt;
    @FXML
    private TextField locationTxt;
    @FXML
    private TextField userTxt;
    @FXML
    private ComboBox<Contacts> contactListComboBox;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private DatePicker startDateDatePicker;
    @FXML
    private ComboBox<LocalTime> startTimeComboBox;
    @FXML
    private TextArea DescriptionTxt;
    @FXML
    private DatePicker endDateDatePicker;
    @FXML
    private ComboBox<LocalTime> endTimeComboBox;
    @FXML
    private ComboBox<String> typeComboBox;

    String userName;
    private final ZoneId localZoneID = ZoneId.systemDefault();
    private static final ZoneId ET = ZoneId.of("America/New_York");
    ObservableList<Contacts> contactList = DBContacts.getAllContactNames();

    /** This method sets the username to be the same as the current user.
    * @param userName username.
    */
    public AddAppointmentController(String userName) {
        this.userName = userName;
    }

    /** This method initializes the implementation of the addAppointmentController method.
     * @param url initialize
     * @param resourceBundle initialize.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    /** This is the contact list combo box
    * This loads the combo box with the contact names
    * @param event  event.
    */
    @FXML
    void contactListComboBox(MouseEvent event) {

        DBConnection.startConnection();
        contactListComboBox.setItems(contactList);
    }

    /** This is the type combo Box
     * This loads the combo box with the type list
     * @param event  event.
     **/
    @FXML
    void typeComboBox(MouseEvent event) {

        typeComboBox.getItems().addAll(
                "Planning Session", "De-Briefing", "Consultation", "Training");
    }

    /** This is the Start Date Picker
     * @param event event.
     **/
    @FXML
    void startDateDatePicker(MouseEvent event) {

    }

    /** This is the Start Time Combo Box.
     *  This loads the combo box with a list of times to choose from.
     * Times are set from 08:00 to 22:00 Eastern Time.
     * These are the hours of business.
     * It is impossible for user to select and time outside of business Time.
     *  @param event  event.
     **/
    @FXML
    void startTimeComboBox(MouseEvent event) {


        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(23, 0);

        while (start.isBefore((end.plusSeconds(1)))) {
            startTimeComboBox.getItems().add(start);
            start = start.plusMinutes(30);
        }

    }

    /** This is the End Date Picker.
     * @param event  event.
     **/
    @FXML
    void endDateDatePicker(MouseEvent event) {
    }

    /** This is the End Time Combo Box.
     * This loads the combo box with times to choose.
     * Times are set from 08:00 to 22:00 Eastern Time.
     * These are the hours of business.
     * It is impossible for user to select and time outside of business Time.
     * @param event event.
     **/
    @FXML
    void endTimeComboBox(MouseEvent event) {


        LocalTime start = LocalTime.of(8,0);
        LocalTime end = LocalTime.of(23, 0);

        while (start.isBefore((end.plusSeconds(1)))) {
            endTimeComboBox.getItems().add(start);
            start = start.plusMinutes(30);
        }

    }


    /** This is the Save Button.
     * This methods loads the user input into the SQL DB.
     * Validates Input and sets the time as Eastern time
     * to be then converted to UTC time.
     * @param event event.
     * */
    @FXML
    void saveButton(MouseEvent event) throws IOException {

        //reading date and time values separately
        LocalTime startTime = startTimeComboBox.getValue();
        LocalTime endTime = endTimeComboBox.getValue();
        LocalDate startDate = startDateDatePicker.getValue();
        LocalDate endDate = endDateDatePicker.getValue();

        if (startTimeComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing start time field!");
            alert.showAndWait();
            return;
        }
        if (startDateDatePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing start date field!");
            alert.showAndWait();
            return;
        }
        if (endTimeComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing end time field!");
            alert.showAndWait();
            return;
        }
        if (endDateDatePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing end date field!");
            alert.showAndWait();
            return;
        }

        //combining both time and date values into on LocalDateTime value
        LocalDateTime combinedStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime combinedEnd = LocalDateTime.of(endDate, endTime);

        //this reads the time in as Eastern time from local time
        //automatically converts to UTC when inserted into SQL
        ZonedDateTime startETtoUTC = combinedStart.atZone(ET).withZoneSameInstant(localZoneID);
        ZonedDateTime endETtoUTC = combinedEnd.atZone(ET).withZoneSameInstant(localZoneID);

        Timestamp sqlStartTS = Timestamp.valueOf(startETtoUTC.toLocalDateTime());
        Timestamp sqlEndTS = Timestamp.valueOf(endETtoUTC.toLocalDateTime());


        Contacts contactSelected = contactListComboBox.getSelectionModel().getSelectedItem();
        String type = typeComboBox.getSelectionModel().getSelectedItem();

        if (combinedStart.isAfter(combinedEnd)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Start Time and Date must be before End Time and Date.");
            alert.showAndWait();
            return;
        }

        if (contactListComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing contact name field!");
            alert.showAndWait();
            return;
        }
        if (typeComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing type field!");
            alert.showAndWait();
            return;
        }


        int id = 0;
        for (Appointments appointments : DBAppointments.getAllAppointments()) {
            if (appointments.getAppointmentsId() > id)
                id = appointments.getAppointmentsId();
        }

        appointmentIDTxt.setText(String.valueOf(++id));
        String customerId = customerIDTxt.getText().trim();
        String title = titleTxt.getText().trim();
        String description = DescriptionTxt.getText().trim();
        String location = locationTxt.getText().trim();
        String contactName = contactSelected.getName();
        String userId = userTxt.getText().trim();

        if (customerId.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing customer ID field!");
            alert.showAndWait();
            return;
        }
        if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing title field!");
            alert.showAndWait();
            return;
        }
        if (description.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing description field!");
            alert.showAndWait();
            return;
        }
        if (location.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing location field!");
            alert.showAndWait();
            return;

        }
        if (userId.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Missing user ID field!");
            alert.showAndWait();
            return;
        }

        try {
            String sqlC = "SELECT * FROM contacts \n" +
                    "WHERE Contact_Name = ?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlC);
            ps.setString(1, contactName);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int contactId = rs.getInt("Contact_ID");

            String sqlAppoint = "INSERT INTO appointments (Appointment_ID, Customer_ID, Title, Type, Location, Description, Contact_ID, User_ID, Start, End) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psCust = DBConnection.getConnection().prepareStatement(sqlAppoint);

            psCust.setInt(1, id);
            psCust.setString(2, customerId);
            psCust.setString(3, title);
            psCust.setString(4, type);
            psCust.setString(5, location);
            psCust.setString(6, description);
            psCust.setInt(7, contactId);
            psCust.setString(8, userId);
            psCust.setTimestamp(9, sqlStartTS);
            psCust.setTimestamp(10, sqlEndTS);

            if (overlapCheck(sqlStartTS, sqlEndTS)){
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Your appointment request for user ID: " + userId +
                                " that conflicts with another appointment they have, please choose a different time.");
                alert.showAndWait();
                return;
            }

            psCust.executeUpdate();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/appointmentOverview.fxml"));
            AppointmentOverview controller = new AppointmentOverview(userName);
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (SQLException ex) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Warning!!");
            alert.setContentText("""
                    Please enter a valid input:
                    
                    Customer ID must be an integer
                    Contact ID must be an integer
                    """);
            alert.showAndWait();
        }
    }

    /** This is the Cancel Button.
     * This brings the user back to the Appointment Overview page.
     * @param event event.
     * */
    @FXML
    void cancelButton(MouseEvent event) throws IOException {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to Cancel? Changes you made so far will not be saved.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/appointmentOverview.fxml"));
                AppointmentOverview controller = new AppointmentOverview(userName);
                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
    }

    /**
     * This method handles the Input Validation for appointments regarding scheduled Overlaps.
     * Grabs the user textfield that the user input has made and checks to see if
     * new appointment requests conflict with that users current schedule.
     * @param newStart start time request
     * @param newEnd  end time request.
     * */
    private boolean overlapCheck(Timestamp newStart, Timestamp newEnd) throws SQLException {

        String userName = userTxt.getText();

        try {
            String sqlfld = "SELECT * FROM users \n" +
                    "WHERE User_ID = ?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlfld);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            rs.next();
            int userID = rs.getInt("User_ID");


            PreparedStatement pst = DBConnection.getConnection().prepareStatement(
                    "SELECT * FROM appointments\n" +
                            "WHERE ? BETWEEN Start AND End OR ? BETWEEN Start AND End OR ? < Start AND ? > End\n" +
                            "AND User_ID = ?");
            pst.setTimestamp(1, Timestamp.valueOf(newStart.toLocalDateTime()));
            pst.setTimestamp(2, Timestamp.valueOf(newEnd.toLocalDateTime()));
            pst.setTimestamp(3, Timestamp.valueOf(newStart.toLocalDateTime()));
            pst.setTimestamp(4, Timestamp.valueOf(newEnd.toLocalDateTime()));
            pst.setInt(5, userID);
            ResultSet rsu = pst.executeQuery();

            if (rsu.next()) {
                return true;
            }

        } catch (Exception sqe) {
            sqe.printStackTrace();
        }
        return false;
    }
}
