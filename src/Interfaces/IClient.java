package Interfaces;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote{
    void notifier(Message msg) throws RemoteException;
    void onlineNotifier(String ID,boolean isOnline) throws RemoteException;
    void clientsNotifier(String ID) throws RemoteException;
    void closeStream() throws RemoteException;
    void groupAdditionNotifier(String groupID) throws RemoteException;
    void groupRemovingNotifier(String groupID) throws RemoteException;
    void setCnv(IConversationGui cvnGui) throws RemoteException;
    void removeCnv() throws RemoteException;
    void groupMemberAdditionNotifier(String groupID,String memberID) throws RemoteException;
    void groupMemberRemovingNotifier(String groupID,String memberID) throws RemoteException;
}
