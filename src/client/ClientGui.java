package client;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.time.format.DateTimeFormatter;
import Interfaces.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author tmdr
 */
public class ClientGui extends javax.swing.JFrame {

    /**
     * Creates new form ClientGui
     */
    IServer serverRef;
    IClient client;
    public static String ownID;
    DefaultListModel<String> ChatModel;
    DefaultListModel<String> ClientModel;
    ArrayList<Message> msgs;
    boolean firstTime;

    public ClientGui() throws RemoteException, ClassNotFoundException, SQLException {
        ChatModel = new DefaultListModel<>();
        ClientModel = new DefaultListModel<>();
        msgs = new ArrayList<>();
        serverRef = null;
        initComponents();
        helper();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    // called when the app is closing(abt to be closed)
                    if(client != null)
                        client.closeStream();
                    if(serverRef != null)
                        serverRef.disconnect(ownID);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
        serverRef.reconnect(ownID, client, firstTime);
        ChatModel.addAll(serverRef.getGroups(ownID));
        setTitle(ownID);
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
    }

    private void helper() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("./user_config_log.info"));
        } catch (FileNotFoundException ex) {
            try {
                File dir = new File("./");
                dir.mkdirs();
                File f = new File(dir, "user_config_log.info");
                f.createNewFile();
                br = new BufferedReader(new FileReader("./user_config_log.info"));
            } catch (IOException ex1) {
                JOptionPane.showMessageDialog(this, "Oops something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        String line,serverIP = "";
        ownID = null;
        firstTime = false;

        try {
            if (br != null && (serverIP = br.readLine()) == null) {
                serverIP = JOptionPane.showInputDialog("Enter the ip of the server");
            }
            try {
                String test =  "rmi://" + serverIP + ":20000/serv";
                System.out.println(test);
                serverRef = (IServer) Naming.lookup(test
                );
            } catch (NotBoundException | MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, "Oops server not found you cannot use the app", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, "Something went wrong : " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            if ((br != null) && (ownID = br.readLine()) == null) {
                ownID = JOptionPane.showInputDialog("Give me your ID Please");
                PrintStream s = null;
                try {
                    s = new PrintStream(new FileOutputStream("./user_config_log.info"));
                } catch (FileNotFoundException ex) {
                    try {
                        File dir = new File("./");
                        dir.mkdirs();
                        File f = new File(dir, "user_config_log.info");
                        f.createNewFile();
                        s = new PrintStream(new FileOutputStream("./user_config_log.info"));
                    } catch (IOException ex1) {
                        JOptionPane.showMessageDialog(this, "Oops something went wrong", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                if (s != null) {
                    s.println(serverIP);
                    s.println(ownID);
                }
                firstTime = true;
            }
            try {
                client = new ClientImp(msgs, ChatModel, ClientModel, serverRef.getAllClients(ownID),
                        serverRef.getOnlineClients(ownID));
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, "Oops something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (br != null && !firstTime) {
                while ((line = br.readLine()) != null && !line.equals("\n")) {
                    //TODO: limitation where file name has an underscore in it, causes error.
                    String[] Tokens = line.split("_");
                    String Contents = Tokens[0].substring(Tokens[0].indexOf('[') + 1, Tokens[0].lastIndexOf(']'));// ShareableFile
                    // objects
                    ArrayList<Object> messageContents = new ArrayList<>();
                    while (Contents.contains(",")) {
                        String fileDescs = Contents.substring(0, Contents.indexOf(","));// one ShareableFile
                        String content = fileDescs.substring(fileDescs.indexOf('[') + 1, fileDescs.indexOf(']'));
                        String title = fileDescs.substring(fileDescs.indexOf('|') + 1, fileDescs.lastIndexOf('|'));
                        title = title.substring(title.indexOf('=') + 1, title.length());
                        String type = fileDescs.substring(fileDescs.lastIndexOf('|') + 1, fileDescs.length());
                        type = type.substring(type.indexOf('=') + 1, type.length() - 1);
                        String[] bytes = content.split("\\+");
                        byte[] contentBytes = new byte[bytes.length];
                        for (int i = 0; i < bytes.length; i++) {
                            contentBytes[i] = Byte.parseByte(bytes[i]);
                        }
                        messageContents.add(new ShareableFile(contentBytes, title, type));
                        Contents = Contents.substring(Contents.indexOf(",") + 1, Contents.length());
                    }
                    messageContents.add(Contents);// text
                    String senderID = Tokens[1].substring(Tokens[1].indexOf('=') + 1, Tokens[1].length());
                    String GroupID = Tokens[2].substring(Tokens[2].indexOf('=') + 1, Tokens[2].length());
                    String ReceiverID = Tokens[3].substring(Tokens[3].indexOf('=') + 1, Tokens[3].length());
                    String LocalTime = Tokens[4].substring(Tokens[4].indexOf('=') + 1, Tokens[4].length() - 1)
                            .replace("T", " ");
                    LocalTime = LocalTime.substring(0, LocalTime.indexOf('.'));
                    if (GroupID.equals("null")) {
                        GroupID = null;
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    Message msg = new Message(messageContents, senderID, ReceiverID, GroupID,
                            LocalDateTime.parse(LocalTime, formatter));
                    msgs.add(msg);
                    if (msg.getGroupID() == null && !ChatModel.contains(msg.getSenderID())
                            && !ChatModel.contains(msg.getReceiverID())) {
                        String chatNew;
                        if (ClientGui.ownID.equals(msg.getSenderID())) {
                            chatNew = msg.getReceiverID();
                        } else {
                            chatNew = msg.getSenderID();
                        }
                        ChatModel.addElement(chatNew);
                    }
                }
            }
        } catch (IOException ex) {
        } finally {

            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        Status = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Chats = new javax.swing.JList<>();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        AllClients = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CHATMDR");

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton1.setText("Disconnect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, java.awt.BorderLayout.CENTER);

        jButton2.setText("Reconnect");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, java.awt.BorderLayout.PAGE_START);

        Status.setText("Status : Connected");
        jPanel8.add(Status);

        jPanel1.add(jPanel8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jButton3.setText("Create Group");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton3, java.awt.BorderLayout.CENTER);

        jButton4.setText("Broadcast");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel2.setLayout(new java.awt.GridLayout(1, 2, 10, 10));

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("All Chats + All Groups");
        jPanel4.add(jLabel1);

        jPanel5.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        Chats.setModel(ChatModel);
        Chats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EnterExistingChat(evt);
            }
        });
        jScrollPane1.setViewportView(Chats);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5);

        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("All Clients");
        jPanel7.add(jLabel2);

        jPanel6.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        AllClients.setModel(ClientModel);
        AllClients.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NewChat(evt);
            }
        });
        jScrollPane2.setViewportView(AllClients);

        jPanel6.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel6);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        try {
            serverRef.createGroup(JOptionPane.showInputDialog("Enter Group ID"), ownID);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }// GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed

        // TODO add your handling code here:
        try {
            serverRef.reconnect(ownID, client, firstTime);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Status.setText("Status : Connected");
    }// GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            serverRef.disconnect(ownID);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        Status.setText("Status : DisConnected");
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        try {
            ArrayList<Object> messageContents = new ArrayList<>();
            messageContents.add(JOptionPane.showInputDialog("Enter your text message"));
            serverRef.Broadcast(messageContents, ownID);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }// GEN-LAST:event_jButton4ActionPerformed

    private void EnterExistingChat(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_EnterExistingChat
        // TODO add your handling code here:
        if (Chats.getSelectedValue().equals("null"))
            return;
        new ConversationGui(ownID, client, serverRef, msgs, Chats.getSelectedValue()).setVisible(true);
    }// GEN-LAST:event_EnterExistingChat

    private void NewChat(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_NewChat
        // TODO add your handling code here:
        String cnvID = AllClients.getSelectedValue();
        if (cnvID.equals("null"))
            return;
        if (cnvID.contains("online")) {
            cnvID = cnvID.split(" ")[0];
        }
        new ConversationGui(ownID, client, serverRef, msgs, cnvID).setVisible(true);
    }// GEN-LAST:event_NewChat

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                try {
                    new ClientGui().setVisible(true);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (RemoteException ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> AllClients;
    private javax.swing.JList<String> Chats;
    private javax.swing.JLabel Status;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}

