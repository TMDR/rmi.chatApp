/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IServer extends Remote {

    void reconnect(String id, IClient client,boolean dataLost)throws RemoteException, ClassNotFoundException, SQLException;

    void disconnect(String id) throws RemoteException;
    
    void createGroup(String idGroup, String idAdmin) throws RemoteException, ClassNotFoundException, SQLException;
    
    void sendToClient(String toID,ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    void sendToGroup(String idGroup,ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    void Broadcast(ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    ArrayList<String> getAllClients(String ID) throws RemoteException;
    
    IGroup getGroup(String senderID,String groupID) throws RemoteException;
    
    ArrayList<String> getGroups(String senderID) throws RemoteException;
    
    ArrayList<String> getOnlineClients(String ID) throws RemoteException;
    
}
