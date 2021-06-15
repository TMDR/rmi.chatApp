        /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author tmdr
 */
//all private functions are private becuase of possibility of changing data storing from live state of program to db as data becomes persistant
public class ServerImp extends UnicastRemoteObject implements IServer {

    private final HashMap<String, IClient> listConnected = new HashMap<>();
    private final ArrayList<String> listAllClients;
    private final HashMap<String/*client reciever*/, ArrayList<Message>/*message plus client sender*/> offLineMsg = new HashMap<>();
    //not saved in db if server is off then offline messages will be lost XD
    private final HashMap<String/*client reciever*/, ArrayList<Message>/*message plus client sender*/> receiverMsgs;
    private final HashMap<String, IGroup> groups;
    private final TransferFromDB TDB;
    private final PersistentStorage PST;
    public static boolean initializing = true;

    public ServerImp() throws RemoteException, ClassNotFoundException, SQLException {
        TDB = new TransferFromDB();
        PST = new PersistentStorage();
        listAllClients = TDB.getClients();
        groups = TDB.getGroups(listConnected);
        receiverMsgs = TDB.getMessages();
        initializing = false;
    }

    private void saveMsg(String idClient, Message msg) throws ClassNotFoundException, SQLException /*this is a separate function because it can be expanded as i take more time developing the project*/ {
        PST.addMessage(msg);
        if (receiverMsgs.get(idClient) == null) {
            ArrayList<Message> msgs = new ArrayList<>();
            msgs.add(msg);
            receiverMsgs.put(idClient, msgs);
            return;
        }
        receiverMsgs.get(idClient).add(msg);
    }

    private void authenticate(String id) throws RemoteException /*this is a separate function because it can be expanded as i take more time developing the project*/ {
        if (!listAllClients.contains(id)) {
            throw new RemoteException("you are not part of our community !! press connect on our app");
        }
        if (!listConnected.containsKey(id)) {
            throw new RemoteException("you are not connected !! press connect on our app");
        }
        //this part will never be real as well as disconnect and connect but let's just do what it takes XD
    }

    @Override
    public void reconnect(String id, IClient client, boolean dataLost/*if data was lost we can recover them just like telegram and DIE whatsapp*/) throws RemoteException, ClassNotFoundException, SQLException {
        boolean newOne = false;
        if (!listAllClients.contains(id)) {
            listAllClients.add(id);
            PST.addClient(id);
            newOne = true;
        }
        if (listConnected.containsKey(id)) {
            throw new RemoteException("already connected");
        }
        for (IClient cl : listConnected.values()) {
            if (cl != listConnected.get(id)) {
                if (newOne) {
                    cl.clientsNotifier(id);
                } else {
                    cl.onlineNotifier(id, true);
                }
            }
        }
        listConnected.put(id, client);
        if (dataLost) {
            ArrayList<Message> msgsss = new ArrayList<>();
            if (receiverMsgs.get(id) != null) {
                receiverMsgs.get(id).forEach(msg -> {
                    msgsss.add(msg);
                });
            }
            receiverMsgs.keySet().forEach(receiverID -> {
                receiverMsgs.get(receiverID).stream().filter(msg -> (msg.getSenderID().equals(id))).forEachOrdered(msg -> {
                    msgsss.add(msg);
                });
            });
            msgsss.sort((Message t, Message t1) -> {
                if(t.getTime().isAfter(t1.getTime())){
                    return 1;
                }else if (t.getTime().isBefore(t1.getTime())){
                    return -1;
                }else{
                    return 0;
                }
            });
            for (Message m : msgsss) {
                client.notifier(m);
            }
            offLineMsg.put(id, null);
        }
        ArrayList<Message> msgs = offLineMsg.get(id);
        if (msgs != null) {
            for (Message msg : msgs) {
                client.notifier(msg);
                saveMsg(id, msg);//after notifying the client in real time we log the message just so we can resend it to him when he reconnects
                //the main topic is for the message to reach the receiver so first he gets notfied then we may save or do anything on another thread
                //that way we provide speed over consistency which is more related to instant messaging
            }
            offLineMsg.put(id, null);
        }
        for (IGroup grp : groups.values()) {
            if (grp.contains(id)) {
                for (Message msg : grp.getOfflineMessages(id)) {
                    client.notifier(msg);
                    saveMsg(id, msg);
                }
            }
        }
    }

    @Override
    public void disconnect(String id) throws RemoteException {
        authenticate(id);
        listConnected.remove(id);
        for (String clID : listConnected.keySet()) {
            if (clID.equals(id)) {
                continue;
            }
            listConnected.get(clID).onlineNotifier(id, false);
        }
    }

    @Override
    public void createGroup(String idGroup, String idAdmin) throws RemoteException, ClassNotFoundException, SQLException {
        authenticate(idAdmin);
        PST.addGroup(idGroup, idAdmin);
        groups.put(idGroup, new GroupImp(idAdmin, idGroup,listConnected));
        listConnected.get(idAdmin).groupAdditionNotifier(idGroup);
    }

    @Override
    public void sendToClient(String toID, ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException {
        Message message = new Message(messageContents, senderID, toID, LocalDateTime.now());
        authenticate(message.getSenderID());
        IClient cl = listConnected.get(toID);
        if (cl != null) {
            // client online
            cl.notifier(message);
        } else {
            //client offline
            ArrayList<Message> scheduledMessages = offLineMsg.get(toID);
            if (scheduledMessages == null) {
                scheduledMessages = new ArrayList<>();
            }
            scheduledMessages.add(message);
            offLineMsg.put(toID, scheduledMessages);
        }
        IClient clSender = listConnected.get(senderID);
        clSender.notifier(message);
        saveMsg(toID, message);
    }

    @Override
    public void sendToGroup(String idGroup, ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException {
        Message msg = new Message(messageContents, senderID, idGroup, idGroup, LocalDateTime.now());
        authenticate(msg.getSenderID());
        if (groups.get(idGroup) == null) {
            throw new RemoteException("this group is not real is it!!");
        }
        if (!groups.get(idGroup).contains(msg.getSenderID())) {
            throw new RemoteException("you are not in  this group !!");
        }
        for (String receiverID : groups.get(idGroup).sendToAll(msg)) {
            saveMsg(receiverID, msg);
        }
    }

    @Override
    public void Broadcast(ArrayList<Object> messageContents, String senderID) throws RemoteException, ClassNotFoundException, SQLException {
        for (String id : listAllClients) {
            if (!id.equals(senderID)) {
                sendToClient(id, messageContents, senderID);
            }
        }
    }

    @Override
    public ArrayList<String> getAllClients(String ID) throws RemoteException {
        ArrayList<String> result = (ArrayList<String>)listAllClients.clone();
        result.remove(ID);
        return result;
    }

    @Override
    public ArrayList<String> getOnlineClients(String ID) throws RemoteException {
        ArrayList<String> result = (ArrayList<String>) new ArrayList<>(listConnected.keySet()).clone();
        result.remove(ID);
        return result;
    }

    @Override
    public IGroup getGroup(String senderID, String groupID) throws RemoteException {
        if (groups.get(groupID) == null) {
            throw new RemoteException("this group is not real is it!!");
        }
        if (!groups.get(groupID).contains(senderID)) {
            return null;
        }
        return groups.get(groupID);
    }

    @Override
    public ArrayList<String> getGroups(String senderID) throws RemoteException {
        ArrayList<String> grps = new ArrayList<>();
        grps.addAll(groups.keySet());
        grps.removeIf(grpID -> {
            try {
                return !groups.get(grpID).contains(senderID);
            } catch (RemoteException ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        });
        return grps;
    }

}
