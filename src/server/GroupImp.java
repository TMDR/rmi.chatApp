package server;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Interfaces.Message;
import Interfaces.IGroup;
import Interfaces.IClient;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author tmdr
 */
public class GroupImp extends UnicastRemoteObject implements IGroup {

    private HashMap<String, IClient> members = new HashMap<>();
    private String adminID;
    private HashMap<String/*client reciever*/, ArrayList<Message>/*message plus client sender*/> offLineMsg = new HashMap<>();
    private String ID;
    private HashMap<String,IClient> clientsConnected;
    private final PersistentStorage PST;
    
    public GroupImp(String adminID,String ID,HashMap<String,IClient> clients) throws RemoteException {
        this.adminID = adminID;
        this.ID = ID;
        this.clientsConnected = clients;
        PST = new PersistentStorage();
    }
    
    @Override
    public boolean contains(String id) throws RemoteException {
        return members.containsKey(id) || adminID.equals(id);
    }

    @Override
    public ArrayList<String> sendToAll(Message msg/*needed if client is offline i should schedule it*/) throws RemoteException {
        if(!adminID.equals(msg.getSenderID()) && !members.containsKey(msg.getSenderID()))
            return new ArrayList<>();
        ArrayList<String> membersplusadmin = new ArrayList<>(((HashMap<String,IClient>)members.clone()).keySet());
        membersplusadmin.add(0, adminID);
        for (String id : membersplusadmin) {
            if (clientsConnected.containsKey(id)) {
                clientsConnected.get(id).notifier(msg);
            } else {
                //client offline
                ArrayList<Message> scheduledMessages = offLineMsg.get(id);
                if (scheduledMessages == null) {
                    scheduledMessages = new ArrayList<>();
                }
                scheduledMessages.add(msg);
                offLineMsg.put(id, scheduledMessages);
            }
        }
        return membersplusadmin;
    }

    @Override
    public void addClient(String senderID, String id) throws RemoteException, ClassNotFoundException, SQLException {
        if (!senderID.equals(adminID)) {
            throw new RemoteException("You are not admin talk to the admin so he can add a client");
        }
        if (members.containsKey(id)) {
            throw new RemoteException("client already in !");
        }
        if(!ServerImp.initializing)
            PST.addMemberToGroup(id, ID);
        members.put(id, clientsConnected.get(id));
        if(clientsConnected.get(id) != null)
            clientsConnected.get(id).groupAdditionNotifier(ID);
        if(clientsConnected.get(adminID) != null)            
            clientsConnected.get(adminID).groupMemberAdditionNotifier(ID,id);
        for(String memberID : members.keySet())
            if(clientsConnected.get(memberID) != null)
                clientsConnected.get(memberID).groupMemberAdditionNotifier(ID,id);
    }

    @Override
    public void removeClient(String senderID, String id) throws RemoteException, ClassNotFoundException, SQLException {
        if (!senderID.equals(adminID)) {
            throw new RemoteException("You are not admin talk to the admin so he can remove a client");
        }
        if (!members.containsKey(id)) {
            throw new RemoteException("client not in group !");
        }
        PST.removeMemberToGroup(id,ID);
        members.get(id).groupRemovingNotifier(ID);
        members.remove(id);
        clientsConnected.get(adminID).groupMemberRemovingNotifier(ID,id);
        for(String memberID : members.keySet())
            clientsConnected.get(memberID).groupMemberRemovingNotifier(ID,id);
    }

    @Override
    public ArrayList<String> getAllClients() throws RemoteException {
        ArrayList<String> clientsR = new ArrayList<>(members.keySet());
        clientsR.add(0,adminID);
        return clientsR;
    }

    @Override
    public ArrayList<Message> getOfflineMessages(String idClient) throws RemoteException {
        ArrayList<Message> msgssss = offLineMsg.get(idClient);
        if(msgssss == null)
            msgssss = new ArrayList<>();
        return msgssss;
    }

}
