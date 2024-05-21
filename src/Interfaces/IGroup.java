package Interfaces;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.rmi.Remote;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public interface IGroup extends Remote{
    ArrayList<String> sendToAll(Message msg) throws RemoteException;
    void addClient(String senderID,String id) throws RemoteException, ClassNotFoundException, SQLException;
    boolean contains(String id) throws RemoteException;
    void removeClient(String senderID,String id) throws RemoteException, ClassNotFoundException, SQLException;
    ArrayList<String> getAllClients() throws RemoteException;
    ArrayList<Message> getOfflineMessages(String idClient) throws RemoteException;
}
