/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author tmdr
 */
public interface IServer extends Remote {

    public void reconnect(String id, IClient client,boolean dataLost/*if data was lost we can recover them just like telegram and DIE whatsapp*/)throws RemoteException, ClassNotFoundException, SQLException;

    public void disconnect(String id) throws RemoteException;
    
    public void createGroup(String idGroup, String idAdmin) throws RemoteException, ClassNotFoundException, SQLException;
    
    public void sendToClient(String toID,ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    public void sendToGroup(String idGroup,ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    public void Broadcast(ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException;
    
    public ArrayList<String> getAllClients(String ID) throws RemoteException;
    
    public IGroup getGroup(String senderID,String groupID) throws RemoteException;
    
    public ArrayList<String> getGroups(String senderID) throws RemoteException;
    
    public ArrayList<String> getOnlineClients(String ID) throws RemoteException ;
    
}
