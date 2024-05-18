package server;/*
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
    public static DataSource INSTANCE;

    static {
        try {
            INSTANCE = new DataSource();
        } catch (ClassNotFoundException ex) {
            System.out.println(ex+"\nINSTANCE = "+INSTANCE);
        }
    }

    private Connection con = null;

    private PreparedStatement CreateMessage, CreateShareableFile, CreateAttachement,CreateClient,CreateGroup,CreateGroupClient;

    private PreparedStatement DropMessage, DropShareableFile, DropAttachement,DropClient,DropGroup,DropGroupClient;

    // for Debug purposes
    public void dropTables() throws SQLException, ClassNotFoundException {
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
        DropShareableFile = getConnection().prepareStatement("drop table if exists ShareableFile");
        try {
            DropShareableFile.executeUpdate();
        } catch (SQLException e) {
        } finally {
            DropShareableFile.close();
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
    protected Connection getConnection() {
        if (con == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:ChatAPP.db");
            } catch (ClassNotFoundException | SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return con;
    }

    private void createTables() throws SQLException {

        CreateShareableFile = getConnection().prepareStatement(
                "create table if not exists ShareableFile (FileId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "content BLOB,Title TEXT,FileType TEXT)");
        try {
            CreateShareableFile.executeUpdate();
        } finally {
            CreateShareableFile.close();
        }

        CreateMessage = getConnection().prepareStatement(
                "create table if not exists Message (MessageId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "senderID TEXT,groupID TEXT,receiverID TEXT,time TEXT,content TEXT)");
        try {
            CreateMessage.executeUpdate();
        }  finally {
            CreateMessage.close();
        }
        CreateAttachement = getConnection().prepareStatement(
                "create table if not exists Attachement (AttachementId integer NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "MessageId integer,FileId integer,FOREIGN KEY(MessageId) REFERENCES Message(MessageId),FOREIGN KEY(FileId) REFERENCES ShareableFile(FileId))");
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

    private DataSource() throws ClassNotFoundException {
        File dbFile = new File("ChatAPP.db");
        boolean firstRun = !dbFile.exists();
        try {
            try {
                con = DriverManager.getConnection("jdbc:sqlite:ChatAPP.db");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            createTables();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
