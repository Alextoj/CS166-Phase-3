/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
   STARTING UP STUFF


   source sql/scripts/create_db.sh
   
   source java/scripts/compile.sh
   
    */


   /*
    * Creates a new user

      User Vars:
         login
         password
         role - Customer by default
         favoriteItems
         phoneNum
    **/
   // Done?
   public static void CreateUser(PizzaStore esql){
      System.out.println("Creating User Profile...\n");
      try {
         System.out.print("\tEnter Username: ");
         String username = in.readLine();

         System.out.print("\tEnter Password: ");
         String password = in.readLine();

         System.out.print("\tEnter User Phone Number: ");
         String phoneNum = in.readLine();

         String createUserQry = ("INSERT INTO USERS (login, password, role, favoriteItems, phoneNum) VALUES ('"  
                                 + username + "', '" + password + "', 'Customer', '', '" + phoneNum + "');");
         // System.out.println(createUserQry);

         esql.executeUpdate(createUserQry);
         System.out.println("\nProfile has been Created...\n");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }   
   }//end CreateUser

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
    
   // Done?
   public static String LogIn(PizzaStore esql){
      System.out.println("Logging In...\n");
      try {
         // Ask for Log In 
         System.out.print("\tEnter Username: ");
         String username = in.readLine();

         System.out.print("\tEnter Password: ");
         String password = in.readLine();

         // Check for Log In
         String query = "SELECT * FROM Users WHERE login = '" + username + "' AND password = '" + password + "';";
         int userNum = esql.executeQuery(query);

         if (userNum > 0) {
            System.out.println("\nSuccessfully Logged In...\n");
            return username;
         } 
         else {
            System.out.println("\nInvalid Login or Password...\n");
            return null;
         }
      } 
      catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      } 
   }//end

   // entree, sides, drinks
   // Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String username) {
        try {
         String findRole = String.format("SELECT role FROM USERS WHERE login = '%s';", username);
         // System.out.println(findRole);
         List<List<String>> role = esql.executeQueryAndReturnResult(findRole);
         // System.out.println(role);

         if (!role.isEmpty()) {
            String check = role.get(0).get(0).trim();
            // System.out.println(check);

            if(check.equals("Customer")) {
               System.out.println("\nUser Profile: [ " + username + " ]");
               String userProfile = String.format("SELECT * FROM USERS WHERE login = '%s';", username);
               List<List<String>> user = esql.executeQueryAndReturnResult(userProfile);
               if(!user.isEmpty()) {
                  List<String> userInfo = user.get(0);
                  System.out.println("|Login: " + userInfo.get(0));
                  System.out.println("|Password: " + userInfo.get(1));
                  System.out.println("|Role: " + userInfo.get(2));
                  System.out.println("|Favorite Items: " + userInfo.get(3));
                  System.out.println("|Phone Number: " + userInfo.get(4));
                  System.out.println();
               } else {
                  System.out.println("User not found...");
               }
            } else {
               boolean cont = true;
               while(cont) {
                  System.out.println("\nMANAGER VIEW: [ " + username + " ]");
                  System.out.println("1. View Manager Account");
                  System.out.println("2. View Users Account");
                  System.out.println("9. < EXIT");
                  System.out.print("Please make your choice: ");
                  String userChoice = in.readLine();

                  try {
                     int input = Integer.parseInt(userChoice);
                     String accQry = "";
                     List<List<String>> user;
                     
                     switch (input) {
                        case 1:
                           accQry = String.format("SELECT * FROM USERS WHERE login = '%s'", username);
                           user = esql.executeQueryAndReturnResult(accQry);
                           System.out.println("\nManager Profile: [ " + username + " ]");
                           if(!user.isEmpty()) {
                              List<String> userInfo = user.get(0);
                              System.out.println("|Login: " + userInfo.get(0));
                              System.out.println("|Password: " + userInfo.get(1));
                              System.out.println("|Role: " + userInfo.get(2));
                              System.out.println("|Favorite Items: " + userInfo.get(3));
                              System.out.println("|Phone Number: " + userInfo.get(4));
                              System.out.println();
                           } else {
                              System.out.println("User not found...");
                           }
                           break;

                        case 2:
                           System.out.print("Enter User to View: ");
                           String login = in.readLine();
                           accQry = String.format("SELECT * FROM USERS WHERE login = '%s'", login);
                           user = esql.executeQueryAndReturnResult(accQry);
                           System.out.println("\nUser Profile: [ " + login + " ]");
                           if(!user.isEmpty()) {
                              List<String> userInfo = user.get(0);
                              System.out.println("|Login: " + userInfo.get(0));
                              System.out.println("|Password: " + userInfo.get(1));
                              System.out.println("|Role: " + userInfo.get(2));
                              System.out.println("|Favorite Items: " + userInfo.get(3));
                              System.out.println("|Phone Number: " + userInfo.get(4));
                              System.out.println();
                           } else {
                              System.out.println("User not found...\n");
                           }
                           break;

                        case 9: 
                           cont = false;
                           return;
                     
                        default:
                           System.out.println("Invalid Choice...\n");
                           cont = true;
                           break;
                     }
                  } catch (Exception e) {
                  System.err.println(e.getMessage());
                  }
               }
            }
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      } 
    }
    public static void updateProfile(PizzaStore esql, String username) {
      try {

         String findRole = String.format("SELECT role FROM USERS WHERE login = '%s';", username);
         List<List<String>> role = esql.executeQueryAndReturnResult(findRole);
         String userchoice;
         int input;
         List<String> isolate;
         String updateQry = "";
         boolean editing = true;

         if (!role.isEmpty()) {
            String check = role.get(0).get(0).trim();

            if(check.equals("Customer")) {

               while(editing) {
                  System.out.println("\nCUSTOMER PROFILE: [ "  + username + " ]");
                  System.out.println("---------");
                  System.out.println("1. View Profile (Favorite Item & Phone Number)");
                  System.out.println("2. Update Favorite Item");
                  System.out.println("3. Change Password");
                  System.out.println("4. Change Phone Number");
                  System.out.println("9. < EXIT");
                  System.out.print("Please make your choice: ");

                  try {
                     input = Integer.parseInt(in.readLine());

                     switch (input) {
                        case 1:
                           updateQry = String.format("SELECT favoriteItems, phoneNum FROM USERS WHERE login = '%s';", username);
                           role = esql.executeQueryAndReturnResult(updateQry);
                           // System.out.print(role);

                           if(!role.isEmpty()) {
                              isolate = role.get(0);
                              // System.out.print(isolate);

                              String item = isolate.get(0);
                              if((item.length()) == 0) item = "N/A";

                              System.out.println(String.format("\n|Favorite Item(s): %s", item));
                              System.out.println(String.format("|Phone Number: %s\n", isolate.get(1)));

                           } else {
                              System.out.println("User Not Found...");
                           }
                           break;

                        case 2:
                           System.out.println("---------");
                           System.out.print("Enter New Favorite Item: ");
                           userchoice = in.readLine();
                           updateQry = String.format("UPDATE USERS SET favoriteItems = '%s' WHERE login = '%s';", userchoice, username);
                           
                           try {
                              esql.executeUpdate(updateQry);
                              System.out.println("\nFavorite Item(s) has Successfully Updated...");
                           } catch (Exception e) {
                              System.err.println(e.getMessage());
                           }
                           break;
                        
                        case 3:
                           boolean match = false;
                           boolean invalid = false;
                           String security = "";
                           System.out.println("---------");
                           System.out.print("Enter Current Password: ");
                           userchoice = in.readLine();

                           updateQry = String.format("SELECT password FROM USERS WHERE login = '%s';", username);
                           role = esql.executeQueryAndReturnResult(updateQry);
                           isolate = role.get(0);
                           security = isolate.get(0);

                           while(!invalid) {

                              if((userchoice.equals(security))) invalid = true;
                              else {
                                 System.out.println("\nIncorrect Password...");
                                 System.out.println("Please Enter Password Again...\n");

                                 System.out.print("Enter Current Password: ");
                                 userchoice = in.readLine();

                                 invalid = false;
                              }
                           }
                           System.out.println("Success ~ Password Match...");

                           System.out.print("\nEnter New Password: ");
                           userchoice = in.readLine();

                           System.out.print("ReEnter New Password: ");
                           security = in.readLine();

                           while(!match) {
                              if((userchoice.equals(security))) match = true;
                              else {
                                    System.out.println("\nPasswords do not Match...");
                                    System.out.println("Please Enter Password Again...\n");

                                    System.out.print("Enter New Password: ");
                                    userchoice = in.readLine();

                                    System.out.print("ReEnter New Password: ");
                                    security = in.readLine();

                                    match = false;
                              }
                           }

                           updateQry = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s';", security, username);
                           esql.executeUpdate(updateQry);

                           System.out.println("\nPassword has Successfully Updated...");
                           break;
                        
                        case 4:
                           System.out.println("---------");
                           System.out.print("Enter New Phone Number: ");
                           userchoice = in.readLine();
                           updateQry = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s';", userchoice, username);
                           
                           try {
                              esql.executeUpdate(updateQry);
                              System.out.println("\nPhone Number has Successfully Updated...");
                           } catch (Exception e) {
                              System.err.println(e.getMessage());
                           }
                           break;

                        case 9:
                           //Exit
                           editing = false;
                           return;
                     
                        default:
                           System.out.println("\nInvalid Output...");
                           break;
                     }
                  } catch (Exception e) {
                     System.err.println(e.getMessage());
                  }
               }
            } else {

               while(editing) {
                  System.out.println("\nMANAGER PROFILE: [ " + username + " ]");
                  System.out.println("---------");
                  System.out.println("1. View Profile (Favorite Item & Phone Number)");
                  System.out.println("2. Update Favorite Item");
                  System.out.println("3. Change Password");
                  System.out.println("4. Change Phone Number");
                  System.out.println("5. Update User Account");
                  System.out.println("9. < EXIT");
                  System.out.print("Please make your choice: ");

                  try {
                     input = Integer.parseInt(in.readLine());

                     switch (input) {
                        case 1:
                           updateQry = String.format("SELECT favoriteItems, phoneNum FROM USERS WHERE login = '%s';", username);
                           role = esql.executeQueryAndReturnResult(updateQry);
                           // System.out.print(role);

                           if(!role.isEmpty()) {
                              isolate = role.get(0);
                              // System.out.print(isolate);

                              String item = isolate.get(0);
                              if((item.length()) == 0) item = "N/A";

                              System.out.println(String.format("\n|Favorite Item(s): %s", item));
                              System.out.println(String.format("|Phone Number: %s\n", isolate.get(1)));

                           } else {
                              System.out.println("User Not Found...");
                           }
                           break;

                        case 2:
                           System.out.println("---------");
                           System.out.print("Enter New Favorite Item: ");
                           userchoice = in.readLine();
                           updateQry = String.format("UPDATE USERS SET favoriteItems = '%s' WHERE login = '%s';", userchoice, username);
                           
                           try {
                              esql.executeUpdate(updateQry);
                              System.out.println("\nFavorite Item(s) has Successfully Updated...\n");
                           } catch (Exception e) {
                              System.err.println(e.getMessage());
                           }
                           
                           break;
                        
                        case 3:
                           boolean match = false;
                           boolean invalid = false;
                           String security = "";
                           System.out.println("---------");
                           System.out.print("Enter Current Password: ");
                           userchoice = in.readLine();

                           updateQry = String.format("SELECT password FROM USERS WHERE login = '%s';", username);
                           role = esql.executeQueryAndReturnResult(updateQry);
                           isolate = role.get(0);
                           security = isolate.get(0);

                           while(!invalid) {

                              if((userchoice.equals(security))) invalid = true;
                              else {
                                 System.out.println("\nIncorrect Password...");
                                 System.out.println("Please Enter Password Again...\n");

                                 System.out.print("Enter Current Password: ");
                                 userchoice = in.readLine();

                                 invalid = false;
                              }
                           }
                           System.out.println("Success ~ Password Match...");

                           System.out.print("\nEnter New Password: ");
                           userchoice = in.readLine();

                           System.out.print("ReEnter New Password: ");
                           security = in.readLine();

                           while(!match) {
                              if((userchoice.equals(security))) match = true;
                              else {
                                    System.out.println("\nPasswords do not Match...");
                                    System.out.println("Please Enter Password Again...\n");

                                    System.out.print("Enter New Password: ");
                                    userchoice = in.readLine();

                                    System.out.print("ReEnter New Password: ");
                                    security = in.readLine();

                                    match = false;
                              }
                           }
                           updateQry = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s';", security, username);
                           esql.executeUpdate(updateQry);

                           System.out.println("\nPassword has Successfully Updated...");
                           break;
                        
                        case 4:
                           System.out.println("---------");
                           System.out.print("Enter New Phone Number: ");
                           userchoice = in.readLine();
                           updateQry = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s';", userchoice, username);
                           
                           try {
                              esql.executeUpdate(updateQry);
                              System.out.println("\nPhone Number has Successfully Updated...\n");
                           } catch (Exception e) {
                              System.err.println(e.getMessage());
                           }
                           break;

                        case 5:
                           boolean modify = true;
                           String holdName;
                           System.out.println("---------");
                           System.out.print("Enter Username Account to Edit: ");
                           userchoice = in.readLine();
                           holdName = userchoice;


                           updateQry = String.format("SELECT * FROM USERS WHERE login = '%s';", username);
                           role = esql.executeQueryAndReturnResult(updateQry);

                           if(!role.isEmpty()) {
                              while(modify) {
                                 System.out.println("1. Update User Login");
                                 System.out.println("2. Update User Role");
                                 System.out.println("9. < EXIT");
                                 System.out.print("Please make your choice: ");

                                 try {
                                    input = Integer.parseInt(in.readLine());

                                    switch (input) {
                                       case 1:
                                          System.out.println("---------");
                                          System.out.print("Enter New User Login: ");
                                          userchoice = in.readLine();
                                          updateQry = String.format("UPDATE USERS SET login = '%s' WHERE login = '%s';", userchoice, holdName);
                                          
                                          try {
                                             esql.executeUpdate(updateQry);
                                             System.out.println("\nUser Login has Successfully Updated...\n");
                                          } catch (Exception e) {
                                             System.err.println(e.getMessage());
                                          }
                                       break;

                                       case 2:
                                          System.out.println("---------");
                                          System.out.print("Enter New User Role (Manager, Customer, Driver): ");
                                          userchoice = in.readLine();
                                          updateQry = String.format("UPDATE USERS SET role = '%s' WHERE login = '%s';", userchoice, holdName);
                                          
                                          try {
                                             esql.executeUpdate(updateQry);
                                             System.out.println("\nUser Role has Successfully Updated...\n");
                                          } catch (Exception e) {
                                             System.err.println(e.getMessage());
                                          }
                                       break;

                                       case 9:
                                          // Exit
                                          modify = false;
                                       break;
                                    
                                       default:
                                          System.out.println("\nInvalid Choice...");
                                          break;
                                    }

                                 } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                 }
                              }
                           } else {
                              System.out.println("User Not Found...");
                              break;
                           }
                           break;

                        case 9:
                           //Exit
                           editing = false;
                           return;
                     
                        default:
                           System.out.println("\nInvalid Choice...");
                           break;
                     }
                  } catch (Exception e) {
                     System.err.println(e.getMessage());
                    }
                }
            }
         } else {
            System.out.println("\nUser Not Found...");
            return;
            }
      } catch (Exception e) {
         System.err.println(e.getMessage());
        }
    }
    public static void viewMenu(PizzaStore esql) {
        try {

         int input;
         String viewMenuQry = "";
         String userChoice = "";
         boolean exitCase = true;

         while(exitCase) {
            System.out.println("\nPizza Store MENU");
            System.out.println("---------");
            System.out.println("1. View Full Menu");
            System.out.println("2. Search Menu by Type");
            System.out.println("3. Search Menu by Price");
            System.out.println("9. < EXIT");
            userChoice = in.readLine();

            try {
               input = Integer.parseInt(userChoice);
            } catch (Exception e) {
               System.err.println(e.getMessage());
               continue;
            } 
           
            switch (input) {
               case 1:
                  viewMenuQry = "SELECT * FROM ITEMS";
                  exitCase = false;
                  break;   

               case 2:
                  System.out.print("Enter Choice of Type (entree, sides, drinks): ");
                  userChoice = in.readLine();

                  viewMenuQry = String.format("SELECT * FROM ITEMS WHERE typeOfItem = ' %s'", userChoice);
                  // System.out.println(viewMenuQry);

                  exitCase = false; 
                  break;

               case 3:
                  System.out.print("Enter Max Price: ");
                  userChoice = in.readLine();
                  try {
                     float price = Float.parseFloat(userChoice);

                     viewMenuQry = "SELECT * FROM ITEMS WHERE price <= " + price;
                     // System.out.println(viewMenuQry);

                  } catch (Exception e) {
                     System.err.println(e.getMessage());
                     continue;
                  } 

                  exitCase = false;
                  break;

               case 9:
                  exitCase = false;
                  return;
            
               default:
                  System.out.println("Invalid Choice! Please Enter a Valid Choice (1-3)...");
                  exitCase = true;
                  break;
            }
         }

         exitCase = true;
         while(exitCase) {
            System.out.println("---------");
            System.out.println("1. Sort by Price (High to Low)");
            System.out.println("2. Sort by Price (Low to High)");
            System.out.println("3. No Sort");
            System.out.print("Please make your choice: ");
            userChoice = in.readLine();

            try {
               input = Integer.parseInt(userChoice);

               switch (input) {
                  case 1:
                     viewMenuQry += " ORDER BY price DESC;";
                     exitCase = false;
                     break;
            
                  case 2:
                     viewMenuQry += " ORDER BY price ASC;";
                     exitCase = false;
                     break;

                  case 3:
                     viewMenuQry += ";";
                     exitCase = false;
                     break;
               
                  default:
                     System.out.println("Invalid Choice! Please Enter a Valid Choice (1-3)...");
                     exitCase = true;
                     break;
               }

               List<List<String>> MENU = esql.executeQueryAndReturnResult(viewMenuQry);

               System.out.println("\nPizza Store Menu");
               System.out.println("---------");
               for(List<String> item : MENU) {
                  System.out.println("|Item: " + item.get(0) + " | Price: $" + item.get(3) + "|\n\t|Type:" + item.get(2) + "\n\t|Description: " + item.get(4) + "\n\t|Ingredients: " + item.get(1) + "\n");
               }

            } catch (Exception e) {
               System.err.println(e.getMessage());
               continue;
            } 
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } 
    }
    public static void placeOrder(PizzaStore esql, String username) {
        try {
         String userinput = "";
         int storeID;
         String storeLocation = "";
         String locateQry = "";
         
         System.out.println("\nORDER");
         System.out.println("---------");
         System.out.print("Enter Store ID to Place Order (1-1000): ");
         userinput = in.readLine();

         try {
            storeID = Integer.parseInt(userinput);

            locateQry = String.format("SELECT * FROM STORE WHERE storeID = %d", storeID);
            List<List<String>> Sto = esql.executeQueryAndReturnResult(locateQry);
            if(!Sto.isEmpty()) {

               List<String> StoreName = Sto.get(0);
               storeID = Integer.parseInt(StoreName.get(0));
               storeLocation = StoreName.get(1);
               System.out.println("Ordering From: " + storeLocation + " | StoreID: " + storeID);
               boolean continueOrder = true;
               int quantity;
               double totalPrice = 0.0;
               double tempPrice = 0.0;
               List<List<String>> itemFind;
               List<String> itemStuff;

               List<String> items = new ArrayList<>();
               List<Integer> quant = new ArrayList<>();

               while (continueOrder) {
                  System.out.print("Enter Item to Order: ");
                  userinput = in.readLine();
                  // System.out.print(userinput);

                  locateQry = String.format("SELECT * FROM ITEMS WHERE itemName = '%s';", userinput);
                  // System.out.println(locateQry);

                  itemFind = esql.executeQueryAndReturnResult(locateQry);
                  if(!itemFind.isEmpty()) {
                     itemStuff = itemFind.get(0);

                     System.out.print("Enter Quantity to Order: ");
                     quantity = Integer.parseInt(in.readLine());

                     // System.out.println(quantity);

                     items.add(userinput);
                     quant.add(quantity);

                     // System.out.println(itemStuff.get(3));

                     tempPrice = Float.parseFloat(itemStuff.get(3));
                     totalPrice += (tempPrice * quantity);

                  } else {
                     System.out.print("\nInvalid Item Name: " + userinput + " Not Found...\n");
                     continueOrder = true;
                     continue;
                  }

                  boolean invalid = true;

                  while(invalid) {
                     System.out.print("Would you like to Order Another Item? y/n: ");
                     userinput = in.readLine();

                     if(userinput.equals("y") || userinput.equals("Y")) {
                        continueOrder = true; 
                        invalid = false;
                        break;
                     }
                     else if(userinput.equals("n") || userinput.equals("N")) {
                        continueOrder = false; 
                        invalid = false;
                        break;
                     } else System.out.println("\nInvalid Input (y/n)...\n");
                  }

               } // While Loop Bracket

               String orderQry = String.format("INSERT INTO FOODORDER (login, storeID, totalPrice, orderTimestamp, orderStatus) " + 
                        "VALUES('%s', '%d', '%.2f', NOW(), 'incomplete') RETURNING orderID;", username, storeID, totalPrice);

               List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderQry);
               if (orderResult.isEmpty()) {
                  throw new Exception("Order placement failed, could not retrieve Order ID.");
               }

               int orderID = Integer.parseInt(orderResult.get(0).get(0)); // Get orderID
               // System.out.println(orderID);
               // System.out.println(orderID);

               for(int i = 0; i < items.size(); i++) {
                  // System.out.printf("'%d', '%s', '%d'\n", orderID, items.get(i), quant.get(i));
                  String insertQry = String.format(" INSERT INTO ITEMSINORDER (orderID, itemName, quantity) VALUES ('%d', '%s', '%d');", orderID, items.get(i), quant.get(i));
                  esql.executeUpdate(insertQry);
               }

               System.out.println("\nYour Order Has Been Placed!");
               System.out.println("Order ID: " + orderID);
               System.out.printf("Total Order Price: %.2f\n\n", totalPrice);

            } else {
               System.out.println("\nStore Not Found...\n");
            }

         }  catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Invalid Input...");
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }

    }
    public static void viewAllOrders(PizzaStore esql, String username) {
      try {

         String roleQuery = "SELECT role FROM users WHERE login = '" + username + "';";
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String role = roleResult.get(0).get(0).trim();

         String targetUser = username; 

         if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("driver")) {
            System.out.print("Enter the username of the user whose order history you want to view: ");
            targetUser = in.readLine();
         }

         String query = "SELECT * FROM FoodOrder WHERE login ='" + targetUser + "';";
         List<List<String>> allOrders = esql.executeQueryAndReturnResult(query);

         if (!allOrders.isEmpty()) {
            int count = 1;
            System.out.println("\nOrder History of " + targetUser + ": \n");

            for (List<String> order : allOrders) {
                System.out.printf("[%d] ~ [ Order ID: %s ]\n", count,  order.get(0));
                count++;
            }
               System.out.println();
         } else {
            System.out.println("No Order History for " + targetUser + ".");
         }   

      }  catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
      }
   }
   public static void viewRecentOrders(PizzaStore esql, String username) {
      try {

         String roleQuery = "SELECT role FROM users WHERE login = '" + username + "';";
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String role = roleResult.get(0).get(0).trim();

         String targetUser = username; 

         if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("driver")) {
            System.out.print("Enter the username of the user whose recent order history you want to view: ");
            targetUser = in.readLine();
         }

         String query = "SELECT orderID, orderTimestamp, orderStatus FROM FoodOrder WHERE login = '" + targetUser + "' ORDER BY orderTimestamp DESC LIMIT 5;";
         List<List<String>> lastFive = esql.executeQueryAndReturnResult(query);

         if (!lastFive.isEmpty()) {
            int count = 1;
            System.out.println("\nRecent Order History of " + targetUser + ": \n");

            for (List<String> order : lastFive) {
                System.out.printf("[%d] ~ [ Order ID: %s ]\n", count,  order.get(0));
                count++;
            }
               System.out.println();
         } else {
            System.out.println("No Recent Order History for " + targetUser + ".");
         }   

      }  catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
      }
   }
    public static void viewOrderInfo(PizzaStore esql, String username) {
        try {
            String roleQuery = "SELECT role FROM users WHERE login = '" + username + "';";
            List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

            if (roleResult.isEmpty()) {
                System.out.println("User not found.");
                return;
            }

            String role = roleResult.get(0).get(0).trim();
            String targetUser = username;

            if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("driver")) {
                System.out.print("Enter the username of the user whose order details you want to view: ");
                targetUser = in.readLine();
            }

            System.out.print("Enter the Order ID to view details: ");
            String orderID = in.readLine();

            String orderQuery = "SELECT orderTimestamp, totalPrice, orderStatus, login " +
                                "FROM FoodOrder WHERE orderID = '" + orderID + "';";

            List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderQuery);

            if (orderResult.isEmpty()) {
                System.out.println("Order not found.");
                return;
            }

            String orderTimestamp = orderResult.get(0).get(0);
            String totalPrice = orderResult.get(0).get(1);
            String orderStatus = orderResult.get(0).get(2);
            String orderOwner = orderResult.get(0).get(3);

            if (!role.equalsIgnoreCase("manager") && !role.equalsIgnoreCase("driver") && !orderOwner.equals(username)) {
                System.out.println("You are not authorized to view this order.");
                return;
            }

            System.out.printf("\nOrder ID: %s\nDate: %s\nTotal Price: $%s\nStatus: %s\n",
                            orderID, orderTimestamp, totalPrice, orderStatus);

            String itemsQuery = "SELECT itemName, quantity FROM ItemsInOrder WHERE orderID = '" + orderID + "';";
            List<List<String>> items = esql.executeQueryAndReturnResult(itemsQuery);

            if (!items.isEmpty()) {
                System.out.println("Items in this order:");
                for (List<String> item : items) {
                    System.out.printf("   - %s (x%s)\n", item.get(0), item.get(1));
                }
            } else {
                System.out.println("No items found in this order.");
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

   public static void viewStores(PizzaStore esql) {
        try {
         String viewStoresQry = "SELECT * FROM STORE;";
         List<List<String>> Stores = esql.executeQueryAndReturnResult(viewStoresQry);

         for(List<String> Store : Stores) {
            System.out.println("|ID: " + Store.get(0) + " | Rating: " + Store.get(5) + " | IsOpen?: " + Store.get(4) + " |\n|Address: " + Store.get(1) + "\n|" + Store.get(2) + ", " + Store.get(3) + "\n");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } 
   }
   public static void updateOrderStatus(PizzaStore esql, String username) {
      try{
         String roleQuery = "SELECT role FROM users WHERE login = '" + username + "';";
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

         if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
         }

         String role = roleResult.get(0).get(0).trim();

         if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("driver")) {
            System.out.println("Type in the orderID you want to update the status of: ");
            String orderID = in.readLine();
            System.out.println("Enter the new order status: ");
            String orderStatus = in.readLine();
               String query = "UPDATE FoodOrder SET orderStatus ='" + orderStatus + "' WHERE orderID ='" + orderID + "';";
               esql.executeUpdate(query);
            return;
         }
         else {
            System.out.println("You do not have permission to update order status.");   
         }  
      }  catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }  
   public static void updateMenu(PizzaStore esql, String username) {
        try {
         String findRole = String.format("SELECT role FROM USERS WHERE login = '%s';", username);
         List<List<String>> role = esql.executeQueryAndReturnResult(findRole);

         if (!role.isEmpty()) {
            String check = role.get(0).get(0).trim();
            if(check.equals("Customer")) {
               System.out.println("\nYou do not have Permission to Update the Menu...\n");
               return;
            }
         } else {
            System.out.println("User not found...");
            return;
         }
         boolean menuUpdate = true;
         String userChoice;
         while(menuUpdate) {
            System.out.println("UPDATE MENU ~ Manager View");
            System.out.println("---------");
            System.out.println("1. Update Menu Item");
            System.out.println("2. Add Item to Menu");
            System.out.println("9. < Exit");
            System.out.print("Please make your choice: ");
            userChoice = in.readLine();

            try {
               int input = Integer.parseInt(userChoice);
               String upQry = "";
               List<List<String>> item;
               double inp;
                        
               switch (input) {
                  case 1:
                  System.out.print("Enter Update Item: ");
                  userChoice = in.readLine();

                  
                  upQry = String.format("SELECT * FROM ITEMS WHERE itemName = '%s'", userChoice);
                  item = esql.executeQueryAndReturnResult(upQry);

                  if(!item.isEmpty()) {
                     boolean itemEdit = true;
                     String I = item.get(0).get(0).trim();

                     while(itemEdit) {
                        System.out.println("\nSelect Item Data you would like to Update:");
                        System.out.println("---------");
                        System.out.println("1. Item Name");
                        System.out.println("2. Ingredient(s)");
                        System.out.println("3. Type of Item");
                        System.out.println("4. Price");
                        System.out.println("5. Description: ");
                        System.out.println("9. < Exit ");

                        System.out.print("Please make your choice: ");
                        userChoice = in.readLine();

                        try {
                           input = Integer.parseInt(userChoice);

                           switch (input) {
                              case 1:
                                 System.out.print("Enter New Item Name: ");
                                 userChoice = in.readLine();
                                 upQry = String.format("UPDATE ITEMS SET itemName = '%s' WHERE itemName = '%s'", userChoice, I);
                                 break;

                              case 2:
                                 System.out.print("Enter New Ingredient(s): ");
                                 userChoice = in.readLine();
                                 upQry = String.format("UPDATE ITEMS SET ingredients = '%s' WHERE itemName = '%s'", userChoice, I);
                                 break;

                              case 3:
                                 System.out.print("Enter New Item Type: ");
                                 userChoice = in.readLine();
                                 upQry = String.format("UPDATE ITEMS SET typeOfItem = '%s' WHERE itemName = '%s'", userChoice, I);
                                 break;

                              case 4:

                                 System.out.print("Enter New Price: ");
                                 inp = Double.parseDouble(in.readLine());
                                 upQry = String.format("UPDATE ITEMS SET price = '%s' WHERE itemName = '%s'", inp, I);
                                 break;

                              case 5:
                                 System.out.print("Enter New Description: ");
                                 userChoice = in.readLine();
                                 upQry = String.format("UPDATE ITEMS SET description = '%s' WHERE itemName = '%s'", userChoice, I);
                                 break;

                              case 9:
                                 return;
                           
                              default:
                                 System.out.print("Invalid Item Choice...");
                                 break;
                           }

                           if(!upQry.isEmpty()) {
                              esql.executeUpdate(upQry);
                              System.out.println("Item information has been Updated Successfully!");
                           }
                        } catch (Exception e) {
                           System.out.print("Invalid Item Choice...");
                           itemEdit = true;
                           continue;
                        }
                     }
                  } else {
                     System.out.print("Item Not Found...");
                  }
                  break;

               case 2:
                  String iN, In, tI, dE;
                  double pR;
                  boolean itemEdit = true;
                  boolean editPerm = true;

                  while(itemEdit) {
                     if(editPerm) {
                        System.out.print("Enter Item Name: ");
                        iN = in.readLine();
                        System.out.print("Enter Ingredient(s): ");
                        In = in.readLine();
                        System.out.print("Enter Type of Item: ");
                        tI = in.readLine();

                        System.out.print("Enter Price: ");
                        pR = Double.parseDouble(in.readLine());

                        System.out.print("Enter Description: ");
                        dE = in.readLine();
                        upQry = String.format("INSERT INTO ITEMS (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', ' %s', '%f', '%s');", iN, In, tI, pR, dE);
                     }

                     editPerm = false;

                     esql.executeUpdate(upQry);
                     System.out.println("Item has been Added Successfully!\n");


                     System.out.print("Would you like to Continue Adding Items? (y/n): ");
                     String userinput = in.readLine();

                     if(userinput.equals("y") || userinput.equals("Y")) {
                        itemEdit = true; 
                        editPerm = true;
                        break;
                     }
                     else if(userinput.equals("n") || userinput.equals("N")) {
                        itemEdit = false; 
                        break;
                     } else System.out.println("\nInvalid Input (y/n)...\n");

                  }               
                  break;

               case 9: 
                  menuUpdate = false;
                  return;
            
               default:
                  System.out.println("Invalid Choice...\n");
                  menuUpdate = true;
                  break;
               }
            } catch (Exception e) {
               System.err.println(e.getMessage());
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void updateUser(PizzaStore esql, String username) {
    try {
        String roleQuery = "SELECT role FROM users WHERE login = '" + username + "';";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty()) {
            System.out.println("User not found.");
            return;
        }

        String role = roleResult.get(0).get(0).trim();

        if (role.equalsIgnoreCase("manager")) {

            System.out.print("Enter the username of the user you want to update: ");
            String user = in.readLine();

            boolean updated = false; 
            StringBuilder queryBuilder = new StringBuilder("UPDATE Users SET ");

            System.out.print("Do you want to update the role? (yes/no): ");
            String choice = in.readLine();
            if (choice.equalsIgnoreCase("yes")) {
                System.out.print("Enter new role: ");
                String newRole = in.readLine();
                if (!newRole.isEmpty()) {
                    queryBuilder.append("role = '").append(newRole).append("', ");

                    updated = true;
                }
            }

            System.out.print("Do you want to update the favorite item? (yes/no): ");
            choice = in.readLine();
            if (choice.equalsIgnoreCase("yes")) {
                System.out.print("Enter favorite item: ");
                String favoriteItems = in.readLine();
                if (!favoriteItems.isEmpty()) {
                    queryBuilder.append("favoriteItems = '").append(favoriteItems).append("', ");
                    updated = true;
                }
            }

            System.out.print("Do you want to update the phone number? (yes/no): ");
            choice = in.readLine();
            if (choice.equalsIgnoreCase("yes")) {
                System.out.print("Enter new phone number: ");
                String phoneNum = in.readLine();
                if (!phoneNum.isEmpty()) {
                    queryBuilder.append("phoneNum = '").append(phoneNum).append("', ");
                    updated = true;
                }
            }

            if (updated) {
                // Remove the last comma and space from the query
                queryBuilder.setLength(queryBuilder.length() - 2);
                queryBuilder.append(" WHERE login = '").append(user).append("';");

                esql.executeUpdate(queryBuilder.toString());
                System.out.println("User updated successfully.");
            } else {
                System.out.println("No changes made.");
            }
        } else {
            System.out.println("You do not have permission to update users.");
        }
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}

}//end PizzaStore
