/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *
 * @author tmdr this class actually works
 */
public class DataSource {

    private Connection con = null;

    private PreparedStatement CreateMessage, CreateMyKindOfFile, CreateAttachement,CreateClient,CreateGroup,CreateGroupClient;

    private PreparedStatement DropMessage, DropMyKindOfFile, DropAttachement,DropClient,DropGroup,DropGroupClient;

    // dropTables is never called but i call when i need to empty the db
    public void dropTables() throws SQLException, ClassNotFoundException {// needed if reset button
                                                                                              // hitted (not required in
                                                                                              // the given)
        DropAttachement = getConnection().prepareStatement("drop table if exists Attachement");
        try {
            DropAttachement.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropAttachement.close();
        }
        DropMessage = getConnection().prepareStatement("drop table if exists Message");
        try {
            DropMessage.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropMessage.close();
        }
        DropMyKindOfFile = getConnection().prepareStatement("drop table if exists MyKindOfFile");
        try {
            DropMyKindOfFile.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropMyKindOfFile.close();
        }
        DropClient = getConnection().prepareStatement("drop table if exists Client");
        try {
            DropClient.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropClient.close();
        }
        DropGroup = getConnection().prepareStatement("drop table if exists Group");
        try {
            DropGroup.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropGroup.close();
        }
        DropGroupClient = getConnection().prepareStatement("drop table if exists GroupClient");
        try {
            DropGroupClient.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropGroupClient.close();
        }
    }

    // this is extra normal
    protected Connection getConnection() throws ClassNotFoundException {
        if (con == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:ChatAPP.db");// in SQLite it doesn't throw an exception
                                                                            // it just creates the db file
            } catch (SQLException ex) {
            }
        }
        return con;
    }

    // this creates tables i need (i do not think db is our context here so i won't
    // comment db schema)
    private void createTables() throws SQLException, ClassNotFoundException {// i could have created
                                                                                                // the tables aside but
                                                                                                // this is way better

        CreateMyKindOfFile = getConnection().prepareStatement(
                "create table if not exists MyKindOfFile (FileId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "content BLOB,Title TEXT,FileType TEXT)");
        try {
            CreateMyKindOfFile.executeUpdate();
        } finally {
            CreateMyKindOfFile.close();
        }

        CreateMessage = getConnection().prepareStatement(
                "create table if not exists Message (MessageId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "senderID TEXT,groupID TEXT,receiverID TEXT,time TEXT,content TEXT)");
                        //we do not include foreign key on receiverID bcz it may be a grp or a client and this is a simple program it doesn't need to cover everything
                        //just the necessary to keep the data persistent
        try {
            CreateMessage.executeUpdate();
        }  finally {
            CreateMessage.close();
        }
        CreateAttachement = getConnection().prepareStatement(
                "create table if not exists Attachement (AttachementId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "MessageId integer,FileId integer,FOREIGN KEY(MessageId) REFERENCES Message(MessageId),FOREIGN KEY(FileId) REFERENCES MyKindOfFile(FileId))");
        try {
            CreateAttachement.executeUpdate();
        }  finally {
            CreateAttachement.close();
        }
        CreateClient = getConnection().prepareStatement(
                "create table if not exists Client (ClientID TEXT NOT NULL PRIMARY KEY)");
        try {
            CreateClient.executeUpdate();
        } finally {
            CreateClient.close();
        }
        CreateGroup = getConnection().prepareStatement(
                "create table if not exists `Group` (GroupID TEXT NOT NULL PRIMARY KEY,AdminID TEXT,FOREIGN KEY(AdminID) REFERENCES Client(ClientID))");
        try {
            CreateGroup.executeUpdate();
        } finally {
            CreateGroup.close();
        }
        CreateGroupClient = getConnection().prepareStatement(
                "create table if not exists GroupClient (GroupClientId integer NOT NULL PRIMARY KEY AUTOINCREMENT,ClientID TEXT,GroupID TEXT,FOREIGN KEY(GroupID) REFERENCES `Group`(GroupID),FOREIGN KEY(ClientID) REFERENCES Client(ClientID))");
        try {
            CreateGroupClient.executeUpdate();
        } finally {
            CreateGroupClient.close();
        }
    }

    // this is Data Seeding which actually creates data i need when the db is
    // resetted
    protected final void DataSeeding() {
        //no data seeding needed here
    }

    private DataSource() throws ClassNotFoundException {
        // this is kinda migration that happens everytime you run the app always
        // maintaining a good and updated db
        File dbFile = new File("ChatAPP.db");
        boolean firstRun = !dbFile.exists();
        try {
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:ChatAPP.db");// in SQLite it doesn't throw an exception
                                                                            // it just creates the db file
            } catch (SQLException ex) {
            }
            createTables();
            if (firstRun) {
                DataSeeding();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // this is obvious

    public void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
                // DriverManager.getConnection("jdbc:sqlite:ChatAPP.db;shutdown=true");
            } catch (SQLException ex) {

            }
        }
    }

    public static DataSource INSTANCE;// this is initialize in the static block
    static {// this initializes INSTANCE and it gets executed when the class is loaded
        try {
            INSTANCE = new DataSource();
        } catch (ClassNotFoundException ex) {
            System.out.println(ex+"\nINSTANCE = "+INSTANCE);
        }
    }
}
