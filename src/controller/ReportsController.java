package controller;


import DBAccess.DBContacts;
import DBAccess.DBUsers;
import Utils.DBConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Contacts;
import model.Users;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


/**
 * @author Thoai Tran
 * <br>
 *     This is the Reports Controller Class.
 *     This Class allows users to run reports for User schedule,
 *     Contact Schedule, and get the count of types of appointments for the current month.
 * */
public class ReportsController implements Initializable {

    @FXML
    private Button appointmentTypeByMonthButton;
    @FXML
    private Button additionalReportButton;
    @FXML
    private Button backToMainMenuButton;
    @FXML
    private Button consultantScheduleButton;
    @FXML
    private Button contactScheduleSearchButton;
    @FXML
    private TextArea reportsTextArea;
    @FXML
    private Button clearButton;
    @FXML
    private ComboBox<Users> consultationScheduleComboBox;
    @FXML
    private ComboBox<Contacts> contactScheduleComboBox;
    String userName;

    ObservableList<Users> usersList = DBUsers.getAllUserNames();
    ObservableList<Contacts> contactList = DBContacts.getAllContactNames();
    private static final ZoneId localZoneID = ZoneId.systemDefault();
    private static final ZoneId utcZoneID = ZoneId.of("UTC");
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter dtfToTable = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    /** This method sets the username to be the same as the current user.
     * @param userName username.
     * */
    public ReportsController(String userName) {
        this.userName = userName;
    }

    /** This method initializes the implementation of the Reports Controller method.
     * @param url initialize
     * @param resourceBundle initialize.
     * */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /** This is the Appointment Type By Month Button.
     * This methods runs a report to display how many of each type of appointment
     * there are for the current month.
     * @param event  event
     * */
    @FXML
    void appointmentTypeByMonthButton(MouseEvent event) {


        String typeOne = "Planning Session";
        String typeTwo = "De-Briefing";
        String typeThree = "Consultation";
        String typeFour = "Training";

        try {

            String sqlOne = "SELECT COUNT(Type) \n" +
                    "FROM appointments \n" +
                    "WHERE Type = ? \n" +
                    "AND MONTH(Start) = MONTH(current_date)";
            PreparedStatement psOne = DBConnection.getConnection().prepareStatement(sqlOne);
            psOne.setString(1, typeOne);
            ResultSet rsOne = psOne.executeQuery();
            rsOne.next();
            int planningSessionsCount = rsOne.getInt("COUNT(Type)");


            String sqlTwo = "SELECT COUNT(Type) \n" +
                    "FROM appointments \n" +
                    "WHERE Type = ? \n" +
                    "AND MONTH(Start) = MONTH(current_date)";
            PreparedStatement psTwo = DBConnection.getConnection().prepareStatement(sqlTwo);
            psTwo.setString(1, typeTwo);
            ResultSet rsTwo = psTwo.executeQuery();
            rsTwo.next();
            int DeBriefingCount = rsTwo.getInt("COUNT(Type)");


            String sqlThree = "SELECT COUNT(Type) \n" +
                    "FROM appointments \n" +
                    "WHERE Type = ? \n" +
                    "AND MONTH(Start) = MONTH(current_date)";
            PreparedStatement psThree = DBConnection.getConnection().prepareStatement(sqlThree);
            psThree.setString(1, typeThree);
            ResultSet rsThree = psThree.executeQuery();
            rsThree.next();
            int consultation = rsThree.getInt("COUNT(Type)");


            String sqlFour = "SELECT COUNT(Type) \n" +
                    "FROM appointments \n" +
                    "WHERE Type = ? \n" +
                    "AND MONTH(Start) = MONTH(current_date)";
            PreparedStatement psFour = DBConnection.getConnection().prepareStatement(sqlFour);
            psFour.setString(1, typeFour);
            ResultSet rsFour = psFour.executeQuery();
            rsFour.next();
            int training = rsFour.getInt("COUNT(Type)");


            reportsTextArea.setText("Number of Types This Month: \n\n" +
                    "Planning Sessions: " + planningSessionsCount + "\n" +
                    "De-Briefing: " + DeBriefingCount + "\n" +
                    "Consultation: " + consultation + "\n" +
                    "Training: " + training + "\n");

        } catch (SQLException ex) {

            ex.printStackTrace();
        }
    }


    /** This is the Consultant Schedule Combo Box.
     * This methods loads the user name list.
     * @param event event.
     * */
    @FXML
    void consultationScheduleComboBox(MouseEvent event) {

        DBConnection.startConnection();
        consultationScheduleComboBox.setItems(usersList);
    }

    /** This is the Consultant Schedule Button.
     * This method generates the reports for each user schedule.
     * Times are converted from UTC to system Local Time.
     * @param event event.
     * */
    @FXML
    void consultantScheduleButton(MouseEvent event) {

        //  Schedule for each User

        Users userSelected = consultationScheduleComboBox.getSelectionModel().getSelectedItem();

        String userName = userSelected.getName();

        try {

            String sqlC = "SELECT * FROM users \n" +
                    "WHERE User_Name = ?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlC);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            rs.next();

            int userID = rs.getInt("User_ID");


            String sqlOne = "SELECT * \n" +
                    "FROM appointments \n" +
                    "WHERE User_ID = ? ";
            PreparedStatement psOne = DBConnection.getConnection().prepareStatement(sqlOne);
            psOne.setInt(1, userID);

            ResultSet rsOne = psOne.executeQuery();

            StringBuilder userReport = new StringBuilder();
            while (rsOne.next()) {
                int appointmentId = rsOne.getInt("Appointment_ID");
                String title = rsOne.getString("Title");
                String type = rsOne.getString("Type");
                String description = rsOne.getString("Description");
                String startDateAndTimeUTC = rsOne.getString("Start");
                String endDateAndTimeUTC = rsOne.getString("End");
                int customerId = rsOne.getInt("Customer_ID");

                LocalDateTime startUTC = LocalDateTime.parse(startDateAndTimeUTC, dtf);
                LocalDateTime endUTC = LocalDateTime.parse(endDateAndTimeUTC, dtf);

                ZonedDateTime localZoneStart = startUTC.atZone(utcZoneID).withZoneSameInstant(localZoneID);
                ZonedDateTime localZoneEnd = endUTC.atZone(utcZoneID).withZoneSameInstant(localZoneID);

                String localStartDT = localZoneStart.format(dtfToTable);
                String localEndDT = localZoneEnd.format(dtfToTable);


                userReport.append("======== " + userName + " Schedule ======== \n\n" +
                        "Appointment ID: " + appointmentId + "\n" +
                        "Title: " + title + "\n" +
                        "Type: " + type + "\n" +
                        "Description: " + description + "\n" +
                        "Start: " + localStartDT + "\n" +
                        "End: " + localEndDT + "\n" +
                        "Customer_ID: " + customerId + "\n\n");

            }
            reportsTextArea.setText(userReport.toString());

        } catch (SQLException ex) {

            ex.printStackTrace();

        }
    }

    /** This is the Contact Schedule Combo Box.
     * This method loads the contact name list
     * @param event event.
     * */
    @FXML
    void contactScheduleComboBox(MouseEvent event) {

        DBConnection.startConnection();
        contactScheduleComboBox.setItems(contactList);
    }


    /** This is the Contact Schedule Button.
     * This method generates the reports for the selected contact schedule.
     * Times are converted from UTC to user system Local Time.
     * @param event event.
     * */
    @FXML
    void contactScheduleSearchButton(MouseEvent event) {


        Contacts contactSelected = contactScheduleComboBox.getSelectionModel().getSelectedItem();

        String contactName = contactSelected.getName();

        try {

            String sqlC = "SELECT * FROM contacts \n" +
                    "WHERE Contact_Name = ?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlC);
            ps.setString(1, contactName);
            ResultSet rs = ps.executeQuery();

            rs.next();

            int contactID = rs.getInt("Contact_ID");


            String sqlOne = "SELECT * \n" +
                    "FROM appointments \n" +
                    "WHERE Contact_ID = ? ";
            PreparedStatement psOne = DBConnection.getConnection().prepareStatement(sqlOne);
            psOne.setInt(1, contactID);

            ResultSet rsOne = psOne.executeQuery();

            StringBuilder userReport = new StringBuilder();
            while (rsOne.next()) {
                int appointmentId = rsOne.getInt("Appointment_ID");
                String title = rsOne.getString("Title");
                String type = rsOne.getString("Type");
                String description = rsOne.getString("Description");
                String startDateAndTimeUTC = rsOne.getString("Start");
                String endDateAndTimeUTC = rsOne.getString("End");
                int customerId = rsOne.getInt("Customer_ID");

                LocalDateTime startUTC = LocalDateTime.parse(startDateAndTimeUTC, dtf);
                LocalDateTime endUTC = LocalDateTime.parse(endDateAndTimeUTC, dtf);

                ZonedDateTime localZoneStart = startUTC.atZone(utcZoneID).withZoneSameInstant(localZoneID);
                ZonedDateTime localZoneEnd = endUTC.atZone(utcZoneID).withZoneSameInstant(localZoneID);

                String localStartDT = localZoneStart.format(dtfToTable);
                String localEndDT = localZoneEnd.format(dtfToTable);


                userReport.append("======== " + contactName + " Schedule ======== \n\n" +
                        "Appointment ID: " + appointmentId + "\n" +
                        "Title: " + title + "\n" +
                        "Type: " + type + "\n" +
                        "Description: " + description + "\n" +
                        "Start: " + localStartDT + "\n" +
                        "End: " + localEndDT + "\n" +
                        "Customer_ID: " + customerId + "\n\n");

            }
            reportsTextArea.setText(userReport.toString());

        } catch (SQLException ex) {

            ex.printStackTrace();

        }

    }

    /** This is the Clear Button.
     * This button clears the reports screen.
     * @param event event.
     * */
    @FXML
    void clearButton(MouseEvent event) {

        reportsTextArea.clear();

    }

    /** This is the Back To Main Menu Button
     * This method brings the user back to the Main Menu.
     * @param event event.
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
}
