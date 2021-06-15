
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.rmi.Remote;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tmdr
 */
public interface IGroup extends Remote{
    public ArrayList<String> sendToAll(Message msg) throws RemoteException;
    public void addClient(String senderID,String id) throws RemoteException, ClassNotFoundException, SQLException;
    public boolean contains(String id) throws RemoteException;
    public void removeClient(String senderID,String id) throws RemoteException, ClassNotFoundException, SQLException;
    public ArrayList<String> getAllClients() throws RemoteException;
    public ArrayList<Message> getOfflineMessages(String idClient) throws RemoteException;
    //group schedules offline messages for server to get then when reconnect is called
}
