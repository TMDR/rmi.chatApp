/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import Interfaces.ShareableFile;
import Interfaces.Message;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PersistentStorage {

    private Statement InsertClient;
    private Statement InsertGroup;
    private Statement InsertMessage;
    private Statement InsertGroupClient;

//    public void resetDB() throws ClassNotFoundException, SQLException {
//        DataSource.INSTANCE.dropTables();
//    }

    public void addClient(String ID) throws SQLException {
        InsertClient = DataSource.INSTANCE.getConnection().createStatement();
        InsertClient.setQueryTimeout(60);
        try {
            InsertClient.executeUpdate("insert into Client values('" + ID + "')");
        } finally {
            InsertClient.close();
        }
    }

    public void addGroup(String ID, String AdminID) throws SQLException {
        InsertGroup = DataSource.INSTANCE.getConnection().createStatement();
        InsertGroup.setQueryTimeout(60);
        try {
            InsertGroup.executeUpdate("insert into `Group` values('" + ID + "','" + AdminID + "')");
        } finally {
            InsertGroup.close();
        }
    }

    public void addMessage(Message msg) throws SQLException {
        InsertMessage = DataSource.INSTANCE.getConnection().createStatement();
        InsertMessage.setQueryTimeout(60);
        String MessageID = "";
        try {
            InsertMessage
                    .executeUpdate("insert into Message(senderID,groupID,receiverID,time,content) values('"
                            + msg.getSenderID() + "','" + msg.getGroupID() + "','" + msg.getReceiverID() + "','"
                            + msg.getTime().toString() + "','"+msg.getMessageContents().get(msg.getMessageContents().size()-1)+"')");
            ResultSet generatedKeys = InsertMessage.getGeneratedKeys();
            generatedKeys.next();
            MessageID = generatedKeys.getString(1);
        } finally {
            InsertMessage.close();
        }
        ArrayList<Object> contents = msg.getMessageContents();
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        try {
            for (int i = 0; i < contents.size() - 1; i++) {
                ShareableFile file = (ShareableFile) contents.get(i);
                String stmt = "insert into ShareableFile(content,Title,FileType) values(?,?,?)";
                pstmt = DataSource.INSTANCE.getConnection().prepareStatement(stmt);
                pstmt.setQueryTimeout(60 / (contents.size() - 1));
                pstmt.setBytes(1, file.getContent());
                pstmt.setString(2, file.getTitle());
                pstmt.setString(3, file.getFileType());
                pstmt.executeUpdate();
                ResultSet generatedKeys = InsertMessage.getGeneratedKeys();
                generatedKeys.next();
                String insertedID = generatedKeys.getString(1);
                String stmt2 = "insert into Attachement(MessageId,FileId) values(?,?)";
                pstmt2 = DataSource.INSTANCE.getConnection().prepareStatement(stmt2);
                pstmt2.setQueryTimeout(60 / (contents.size() - 1));
                pstmt2.setString(1, MessageID);
                pstmt2.setString(2, insertedID);
                pstmt2.executeUpdate();
            }
        } finally {
            if (pstmt != null)
                pstmt.close();
            if (pstmt2 != null)
                pstmt2.close();
        }
    }

    public void addMemberToGroup(String memberID,String groupID) throws SQLException{
        InsertGroupClient = DataSource.INSTANCE.getConnection().createStatement();
        InsertGroupClient.setQueryTimeout(60);
        try {
            InsertGroupClient.executeUpdate("insert into GroupClient(ClientID,GroupID) values('" + memberID + "','" + groupID + "');");
        }catch(SQLException e){
            e.printStackTrace();
        } finally {
            InsertGroupClient.close();
        }
    }
    public void removeMemberToGroup(String memberID,String groupID) throws SQLException{
        InsertGroupClient = DataSource.INSTANCE.getConnection().createStatement();
        InsertGroupClient.setQueryTimeout(60);
        try {
            InsertGroupClient.executeUpdate("delete FROM GroupClient where ClientID = '" + memberID + "' and GroupID = '" + groupID + "';");
        } finally {
            InsertGroupClient.close();
        }
    }
}
