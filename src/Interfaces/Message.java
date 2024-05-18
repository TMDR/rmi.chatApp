package Interfaces;
import java.io.Serializable;
import java.time.LocalDateTime;
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
public class Message implements Serializable{
    private ArrayList<Object> messageContents;
    private String senderID;
    private String groupID;
    private String receiverID;
    private LocalDateTime time;

    public Message(ArrayList<Object> messageContents, String senderID,String receiverID,LocalDateTime time) {
        this.messageContents = messageContents;
        this.senderID = senderID;
        this.groupID = null;
        this.receiverID = receiverID;
        this.time = time;
    }

    public Message(ArrayList<Object> messageContents, String senderID,String receiverID, String group,LocalDateTime time) {
        this.messageContents = messageContents;
        this.senderID = senderID;
        this.groupID = group;
        this.receiverID = receiverID;
        this.time = time;
    }

    public LocalDateTime getTime() {
        return time;
    }

    
    
    public String getReceiverID() {
        return receiverID;
    }
    
    public ArrayList<Object> getMessageContents() {
        return messageContents;
    }

    public String getSenderID() {
        return senderID;
    }
    
    public String getGroupID(){
        return groupID;
    }

    @Override
    public String toString() {
        return "Message{" + "messageContents=" + messageContents + "_ senderID=" + senderID + "_ groupID=" + groupID + "_ receiverID=" + receiverID + "_ time=" + time + '}';
    }

    
}
