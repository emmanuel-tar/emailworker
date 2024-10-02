package org.example;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailWorker {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:1207/pzcompany";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // Method to send email
    public static void sendEmail(String to, String subject, String body) {
        final String from = "etor@yanot.ng"; // Sender's email
        final String password = "Benuekid4christ"; // Sender's email password

        // Set mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // For Gmail
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Get the Session object
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Compose the message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Send the message
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch purchase details and send email to the customer
    public static void sendSalesDetails(int salesId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Establish database connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // SQL query to fetch customer, purchase, and branch details
            String query = "SELECT c.name, c.email, s.items_sold, s.total_amount, " +
                    "b.branch_name, b.address, b.city, b.state, b.country " +
                    "FROM sales s " +
                    "JOIN customers c ON s.customer_id = c.customer_id " +
                    "JOIN branches b ON s.branch_id = b.branch_id " +
                    "WHERE s.sales_id = ?";

            // Prepare the statement
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, salesId);

            // Execute query
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Fetching data from the result set
                String customerName = rs.getString("name");
                String customerEmail = rs.getString("email");
                String itemsSold = rs.getString("items_sold");
                double totalAmount = rs.getDouble("total_amount");
                String branchName = rs.getString("branch_name");
                String branchAddress = rs.getString("address") + ", " +
                        rs.getString("city") + ", " +
                        rs.getString("state") + ", " +
                        rs.getString("country");

                // Compose the email body
                String subject = "Your Purchase Details from " + branchName;
                String body = "Hello " + customerName + ",\n\n" +
                        "Thank you for shopping with us. Below are your purchase details:\n\n" +
                        "Items Purchased: " + itemsSold + "\n" +
                        "Total Amount: $" + totalAmount + "\n" +
                        "Branch Location: " + branchAddress + "\n\n" +
                        "We hope to see you again soon!\n" +
                        "Best regards,\n" +
                        branchName;

                // Send email
                sendEmail(customerEmail, subject, body);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Example usage: Send purchase details for purchase ID 1
        sendSalesDetails(1);
    }
}

