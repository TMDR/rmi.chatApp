/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import Interfaces.*;

public class ClientImp extends UnicastRemoteObject implements IClient {

    ArrayList<Message> msgs;
    DefaultListModel<String> chatModel;
    DefaultListModel<String> clientModel;
    HashMap<String, Boolean> clientsOnlineStatus;
    ArrayList<String> clients;
    PrintStream s;
    ConversationGui cnvGui;

    public ClientImp(ArrayList<Message> msgs, DefaultListModel<String> chatModel, DefaultListModel<String> clientModel, ArrayList<String> clients, ArrayList<String> online) throws RemoteException {
        this.msgs = msgs;
        this.chatModel = chatModel;
        this.clientModel = clientModel;
        this.clientsOnlineStatus = new HashMap<>();
        this.clients = clients;
        int i = 0;
        for (String client : clients) {
            this.clientsOnlineStatus.put(client, online.contains(client));
            String output = client + ((this.clientsOnlineStatus.get(client) != null && this.clientsOnlineStatus.get(client)) ? " online" : " ");
            this.clientModel.add(i, output);
            i++;
        }
        try {
            s = new PrintStream(new BufferedOutputStream(new FileOutputStream("./user_config_log.info", true)));
        } catch (FileNotFoundException ex) {
            try {
                File dir = new File("./");
                dir.mkdirs();
                File f = new File(dir, "user_config_log.info");
                f.createNewFile();
                s = new PrintStream(new BufferedOutputStream(new FileOutputStream("./user_config_log.info", true)));
            } catch (IOException ex1) {
                throw new RemoteException("Oops something went wrong");
            }
        }
    }

    @Override
    public void closeStream() {
        s.close();
    }

    @Override
    public void notifier(Message msg) throws RemoteException {
        msgs.add(msg);
        s.append(msg + "\n");
        s.flush();
        if (msg.getGroupID() == null && !chatModel.contains(msg.getSenderID()) && !chatModel.contains(msg.getReceiverID())) {
            String chatNew;
            if (ClientGui.ownID.equals(msg.getSenderID())) {
                chatNew = msg.getReceiverID();
            } else {
                chatNew = msg.getSenderID();
            }
            chatModel.addElement(chatNew);
        }
        if (this.cnvGui != null) {
            if (this.cnvGui.isIsGroup()) {
                if (msg.getGroupID() != null && msg.getGroupID().equals(this.cnvGui.getCnvID())/*same grp*/) {
                    JPanel messagePane = new JPanel();
                    if (msg.getSenderID().equals(ClientGui.ownID)) {//i could be the sender
                        messagePane.setLayout(new FlowLayout(FlowLayout.RIGHT));
                        messagePane.add(messageShow(msg, false));
                    } else {
                        messagePane.setLayout(new FlowLayout(FlowLayout.LEFT));
                        messagePane.add(messageShow(msg, true));
                    }
                    this.cnvGui.getMsgsBox().add(messagePane);
                }
            } else {
                JPanel messagePane = new JPanel();
                if (msg.getReceiverID().equals(this.cnvGui.getCnvID())) {
                    messagePane.setLayout(new FlowLayout(FlowLayout.RIGHT));
                    messagePane.add(messageShow(msg, false));
                    this.cnvGui.getMsgsBox().add(messagePane);
                } else if (this.cnvGui.getCnvID().equals(msg.getSenderID())) {
                    messagePane.setLayout(new FlowLayout(FlowLayout.LEFT));
                        messagePane.add(messageShow(msg, false));
                    this.cnvGui.getMsgsBox().add(messagePane);
                }
            }
            this.cnvGui.getMsgsBox().revalidate();
            this.cnvGui.getMsgsBox().repaint();
        }
    }

    private JPanel messageShow(Message msg, boolean showSender) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        ArrayList<Object> contents = msg.getMessageContents();
        int i;
        JPanel files = new JPanel(new GridLayout(2, (contents.size() - 1) / 2 + 1));
        for (i = 0; i < contents.size() - 1; i++) {
            ShareableFile mkof = (ShareableFile) contents.get(i);
            JPanel filePanel = new JPanel(new BorderLayout());
            filePanel.add(new JLabel(mkof.getFileType()), BorderLayout.NORTH);
            filePanel.add(new JLabel(mkof.getTitle()), BorderLayout.SOUTH);
            if (mkof.getFileType().startsWith("image/")) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(mkof.getContent());
                    BufferedImage master = ImageIO.read(bais);
                    Image scaled = master.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH);
                    JButton imageBtn = new JButton(new ImageIcon(scaled));
                    imageBtn.setSize(200, 200);
                    imageBtn.addActionListener((ae) -> {
                        ImageIcon icon = new ImageIcon(mkof.getContent());
                        JFrame imageFrame = new JFrame();
                        JButton btn = new JButton(icon);
                        btn.addActionListener((aes) -> {
                            imageFrame.dispose();
                        });
                        imageFrame.add(btn);
                        imageFrame.pack();
                        imageFrame.setVisible(true);
                        imageFrame.setLocationRelativeTo(null);
                    });
                    filePanel.add(imageBtn);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this.cnvGui, "something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (mkof.getFileType().startsWith("audio/")) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/client/music-note.png"));
                JButton imageBtn = new JButton(icon);
                imageBtn.addActionListener((ae) -> {
                    try (FileOutputStream fos = new FileOutputStream(new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle())) {
                        fos.write(mkof.getContent());
                        JOptionPane.showMessageDialog(this.cnvGui, "File saved here : " + new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle(), "SUCCESS", JOptionPane.PLAIN_MESSAGE);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ConversationGui.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ConversationGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                imageBtn.setSize(200, 200);
                filePanel.add(imageBtn);
            } else {
                ImageIcon icon = new ImageIcon(getClass().getResource("/client/output-onlinepngtools (2).png"));
                JButton imageBtn = new JButton(icon);
                imageBtn.addActionListener((ae) -> {
                    try (FileOutputStream fos = new FileOutputStream(new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle())) {
                        fos.write(mkof.getContent());
                        JOptionPane.showMessageDialog(this.cnvGui, "File saved here : " + new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle(), "SUCCESS", JOptionPane.PLAIN_MESSAGE);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ConversationGui.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ConversationGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                imageBtn.setSize(200, 200);
                filePanel.add(imageBtn);
            }
            files.add(filePanel);
        }
        if(contents.size() != 1){
            msgPanel.add(files, BorderLayout.CENTER);
        }
        //message text
        JPanel headers = new JPanel(new BorderLayout());
        JLabel messageTextLabel = new JLabel((String) contents.get(i));
        headers.add(messageTextLabel, BorderLayout.CENTER);
        if (showSender) {
            JLabel senderLabel = new JLabel(msg.getSenderID());
            Random random = new Random(ZonedDateTime.now().toInstant().toEpochMilli());
            senderLabel.setForeground(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            headers.add(senderLabel, BorderLayout.NORTH);
        }
        String msgInfoPosition = BorderLayout.NORTH;
        if(contents.size() == 1){
            msgInfoPosition = BorderLayout.CENTER;
        }
        headers.add(new JLabel(msg.getTime().toString()),BorderLayout.SOUTH);
        msgPanel.add(headers, msgInfoPosition);
        return msgPanel;
    }

    @Override
    public void setCnv(IConversationGui cnvGUI) throws RemoteException {
        this.cnvGui = (ConversationGui)cnvGUI;
    }

    @Override
    public void removeCnv() throws RemoteException {
        this.cnvGui = null;
    }

    @Override
    public void onlineNotifier(String ID, boolean isOnline) throws RemoteException {
        if (clients.indexOf(ID) != -1) {
            clientModel.set(clients.indexOf(ID), ID + (isOnline ? " online" : " "));
        } else {
            clientModel.addElement(ID);
        }
    }

    @Override
    public void clientsNotifier(String ID) throws RemoteException {
        clientModel.addElement(ID + " online");
        clients.add(ID);
    }

    @Override
    public String toString() {
        return "ClientImp{" + "msgs=" + msgs + ", chatModel=" + chatModel + ", clientModel=" + clientModel + ", clientsOnlineStatus=" + clientsOnlineStatus + ", clients=" + clients + ", s=" + s + '}';
    }

    @Override
    public void groupAdditionNotifier(String groupID) throws RemoteException {
        chatModel.addElement(groupID);
    }

    @Override
    public void groupRemovingNotifier(String groupID) throws RemoteException {
        chatModel.removeElement(groupID);
    }

    @Override
    public void groupMemberAdditionNotifier(String groupID, String memberID) throws RemoteException {
        if (this.cnvGui != null && this.cnvGui.getCnvID() != null && this.cnvGui.getCnvID().equals(groupID)) {
            JPanel messagePane = new JPanel();
            messagePane.setLayout(new FlowLayout(FlowLayout.CENTER));
            if (ClientGui.ownID.equals(memberID)) {
                messagePane.add(new JLabel("you were added"));
            } else {
                messagePane.add(new JLabel(memberID + " was added"));
            }
            this.cnvGui.getMsgsBox().add(messagePane);
            this.cnvGui.getMsgsBox().revalidate();
            this.cnvGui.getMsgsBox().repaint();
        }
    }

    @Override
    public void groupMemberRemovingNotifier(String groupID, String memberID) throws RemoteException {
        if (this.cnvGui != null && this.cnvGui.getMsgsBox() != null && this.cnvGui.getCnvID().equals(groupID)) {
            JPanel messagePane = new JPanel();
            messagePane.setLayout(new FlowLayout(FlowLayout.CENTER));

            if (!ClientGui.ownID.equals(memberID)) {
                messagePane.add(new JLabel(memberID + " was removed"));
            } else {
                messagePane.add(new JLabel("you were removed"));
            }
            this.cnvGui.getMsgsBox().add(messagePane);
            this.cnvGui.getMsgsBox().revalidate();
            this.cnvGui.getMsgsBox().repaint();
        }
    }

}
