import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-divine-pond-a5214pzt.us-east-2.aws.neon.tech/neondb?sslmode=require";
        Properties props = new Properties();
        props.setProperty("user", "neondb_owner");
        props.setProperty("password", "Zt4T7sPofQkm");
        try (Connection conn = DriverManager.getConnection(url, props);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT MAX(product_id) FROM products");
            if (rs.next()) {
                System.out.println("Max Product ID: " + rs.getInt(1));
            }
            rs = stmt.executeQuery("SELECT last_value FROM products_product_id_seq");
            if (rs.next()) {
                System.out.println("Sequence Last Value: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
