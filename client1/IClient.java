/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author tmdr
 */
public interface IClient extends Remote{
    public void notifier(Message msg) throws RemoteException;
    public void onlineNotifier(String ID,boolean isOnline) throws RemoteException;
    public void clientsNotifier(String ID) throws RemoteException;
    public void closeStream() throws RemoteException;
    public void groupAdditionNotifier(String groupID) throws RemoteException;
    public void groupRemovingNotifier(String groupID) throws RemoteException;
    public void setCnv(IConversationGui cvnGui) throws RemoteException;
    public void removeCnv() throws RemoteException;
    public void groupMemberAdditionNotifier(String groupID,String memberID) throws RemoteException;
    public void groupMemberRemovingNotifier(String groupID,String memberID) throws RemoteException;
}
