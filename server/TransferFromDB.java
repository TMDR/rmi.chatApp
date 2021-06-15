/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tmdr
 */
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferFromDB {

    private DateTimeFormatter formatter;

    public TransferFromDB(){
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public ArrayList<String> getClients() throws SQLException, ClassNotFoundException {
        ArrayList<String> clients = new ArrayList<>();
        Statement GetTaxStatement = DataSource.INSTANCE.getConnection().createStatement();
        GetTaxStatement.setQueryTimeout(60);// this gives the query 1 min to be executed
        ResultSet rs = GetTaxStatement.executeQuery("select * from Client;");
        while (rs.next()) {
            clients.add(rs.getString(1));
        }
        return clients;
    }
    public HashMap<String, IGroup> getGroups(HashMap<String,IClient> clientsConnected) throws SQLException, ClassNotFoundException, RemoteException {
        HashMap<String, IGroup> groups = new HashMap<>();
        Statement GetGroupIDsStatement = DataSource.INSTANCE.getConnection().createStatement();
        GetGroupIDsStatement.setQueryTimeout(60);// this gives the query 1 min to be executed
        ResultSet rs = GetGroupIDsStatement.executeQuery("select * from `Group`;");
        while (rs.next()) {
            String groupID = rs.getString(1);
            String adminID = rs.getString(2);
            IGroup grp = new GroupImp(adminID, groupID, clientsConnected);
            Statement GetMembersStatement = DataSource.INSTANCE.getConnection().createStatement();
            GetMembersStatement.setQueryTimeout(60);// this gives the query 1 min to be executed
            ResultSet rs2 = GetMembersStatement.executeQuery("select * from GroupClient where GroupID = '"+groupID+"';");
            while(rs2.next()){
                grp.addClient(adminID, rs2.getString(2));
            }
            groups.put(groupID, grp);
        }
        return groups;
    }

    public HashMap<String, ArrayList<Message>> getMessages() throws ClassNotFoundException, SQLException{
        HashMap<String, ArrayList<Message>> messages = new HashMap<>();
        Statement GetMessagesStatement = DataSource.INSTANCE.getConnection().createStatement();
        GetMessagesStatement.setQueryTimeout(60);// this gives the query 1 min to be executed
        ResultSet rs = GetMessagesStatement.executeQuery("select * from Message;");
        while(rs.next()){
            ArrayList<Object> messageContents = new ArrayList<>();
            String messageID = rs.getString(1);
            String senderID = rs.getString(2);
            String groupID = rs.getString(3);
            String receiverID = rs.getString(4);
            String LocalTime = rs.getString(5).replace("T", " ");
            LocalTime = LocalTime.substring(0, LocalTime.indexOf('.'));
            LocalDateTime time = LocalDateTime.parse(LocalTime, formatter);

            Statement GetFilesStatement = DataSource.INSTANCE.getConnection().createStatement();
            GetFilesStatement.setQueryTimeout(60);// this gives the query 1 min to be executed
            ResultSet rs2 = GetFilesStatement.executeQuery("select MyKindOfFile.* from MyKindOfFile join Attachement on MyKindOfFile.FileId = Attachement.FileId where Attachement.MessageId = '"+messageID+"';");
            while(rs2.next()){
                messageContents.add(new MyKindOfFile(rs2.getBytes(2), rs2.getString(3), rs2.getString(4)));
            }
            messageContents.add(rs.getString(6));
            Message msg = new Message(messageContents, senderID, receiverID,groupID ,time);
            if(messages.get(receiverID) == null){
                ArrayList<Message> msgsss = new ArrayList<>();
                msgsss.add(msg);
                messages.put(receiverID,msgsss);
            }else{
                messages.get(receiverID).add(msg);
            }
        }
        return messages;
    }
}
