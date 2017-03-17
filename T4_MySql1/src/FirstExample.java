
//STEP 1. Import required packages
import java.sql.*;

public class FirstExample {
 // JDBC driver name and database URL
 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 static final String DB_URL = "jdbc:mysql://localhost:3306/Test1?autoReconnect=true&useSSL=false";
 // static final String DB_URL = "jdbc:mysql://localhost/Test1";
 // jdbc:mysql://localhost:3306/Peoples?autoReconnect=true&useSSL=false

 //  Database credentials
 static final String USER = "username";
 static final String PASS = "password";
 
 public static void main(String[] args) {
 Connection conn = null;
 Statement stmt = null;

 // Properties properties = new Properties();
 // properties.setProperty("user", USER);
 // properties.setProperty("password", PASS);
 // properties.setProperty("useSSL", "false");
 // properties.setProperty("autoReconnect", "true");
  
 try{
    //STEP 2: Register JDBC driver
    Class.forName("com.mysql.jdbc.Driver");

    //STEP 3: Open a connection
    System.out.println("Connecting to database...");
    conn = DriverManager.getConnection(DB_URL,USER,PASS);

    //STEP 4: Execute a query
    System.out.println("Creating statement...");
    stmt = conn.createStatement();
    String sql, tablename;
    
    // delete all rows before the load
    tablename = "Products4";
    
    // sql = "SELECT 'Dept Code', Status, 'Product Title' FROM Products";
    //sql = "SELECT `Category IDs` as CategoryIds, Status, sku FROM `4872-edit-products-29`";
    sql = "select sum(price) as TotalPrice FROM " + tablename;
    ResultSet rs = stmt.executeQuery(sql);
    //System.out.println("Executed Query...");

    //STEP 5: Extract data from result set
    while(rs.next()){
       //Retrieve by column name
    	String TotalPrice = rs.getString("TotalPrice");

    	// String categoryids = rs.getString("CategoryIds");
       // String status = rs.getString("status");
       // String sku = rs.getString("sku");
       // String ProductTitle = rs.getString("'Product Title'");
       //System.out.print("Extract data from result set...");

       //Display values
       System.out.println("TotalPrice: " + TotalPrice);
       //        System.out.print("`Category IDs`: " + categoryids);
       // System.out.print(", Status: " + status);
       //System.out.println(", sku: " + sku);
    }
    //STEP 6: Clean-up environment
    //System.out.println("Clean up environment...");
    rs.close();
    stmt.close();
    conn.close();
 }catch(SQLException se){
    //Handle errors for JDBC
    se.printStackTrace();
 }catch(Exception e){
    //Handle errors for Class.forName
    e.printStackTrace();
 }finally{
    //finally block used to close resources
    try{
       if(stmt!=null)
          stmt.close();
    }catch(SQLException se2){
    }// nothing we can do
    try{
       if(conn!=null)
          conn.close();
    }catch(SQLException se){
       se.printStackTrace();
    }//end finally try
 }//end try
 System.out.println("Goodbye!");
}//end main
}//end FirstExample