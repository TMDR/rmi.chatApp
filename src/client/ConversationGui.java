package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import Interfaces.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class ConversationGui extends javax.swing.JFrame implements IConversationGui{

    /**
     * Creates new form ConversationGui
     */
    private final String ownID;
    private IClient client;
    private IServer serverRef;
    private IGroup grp;
    private ArrayList<Message> msgs;
    private final String cnvID;
    private boolean isGroup;
    private JButton getAllMembersGroup;
    private JButton addClientGroup;
    private JButton removeClientGroup;
    private ArrayList<Object> currentMessageContent;
    private final JPanel FilesPane;

    public ConversationGui(String ownID, IClient client, IServer serverRef, ArrayList<Message> msgs, String cnvID) {
        this.ownID = ownID;
        this.client = client;
        this.serverRef = serverRef;
        this.msgs = msgs;
        this.cnvID = cnvID;
        try {
            this.grp = serverRef.getGroup(ownID, cnvID);
        } catch (RemoteException ex) {
        }
        setTitle(cnvID);
        this.setLayout(new BorderLayout());
        initComponents();
        helper();
        if (grp != null) {
            GroupSpecifics();
        }
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 500));
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
        jScrollPane1.setPreferredSize(new Dimension(400, 400));
        msgsBox.setLayout(new BoxLayout(msgsBox, BoxLayout.Y_AXIS));
        FilesPane = new JPanel();
        FilesPane.setLayout(new BoxLayout(FilesPane, BoxLayout.Y_AXIS));
        jPanel1.add(FilesPane, BorderLayout.NORTH);
        currentMessageContent = new ArrayList<>();
    }

    private void GroupSpecifics() {
        JPanel groupOptions = new JPanel(new GridLayout(1, 3));
        this.add(groupOptions, BorderLayout.NORTH);
        this.getAllMembersGroup = new JButton("members");
        this.addClientGroup = new JButton("add client");
        this.removeClientGroup = new JButton("remove client");
        groupOptions.add(this.getAllMembersGroup);
        groupOptions.add(this.addClientGroup);
        groupOptions.add(this.removeClientGroup);
        getAllMembersGroup.addActionListener((ae) -> {
            JFrame AllClients = new JFrame(cnvID);
            AllClients.setLayout(new BorderLayout());
            JPanel members = new JPanel();
            members.setLayout(new BoxLayout(members, BoxLayout.Y_AXIS));
            AllClients.add(members, BorderLayout.CENTER);
            JPanel admin = new JPanel(new FlowLayout(FlowLayout.CENTER));
            AllClients.add(admin, BorderLayout.NORTH);
            try {
                boolean isAdmin = true;
                for (String id : grp.getAllClients()) {
                    if (isAdmin) {
                        admin.add(new JLabel("admin : " + id));
                        isAdmin = false;
                    } else {
                        members.add(new JLabel(id));
                    }
                }
            } catch (RemoteException rex) {
                JOptionPane.showMessageDialog(this, rex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            AllClients.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            AllClients.setMinimumSize(new Dimension(500, 500));
            AllClients.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
            AllClients.setVisible(true);
            AllClients.setAlwaysOnTop(true);
        });
        addClientGroup.addActionListener((ae) -> {
            JFrame AllClients = new JFrame(cnvID);
            JList<String> members = new JList<String>();
            DefaultListModel<String> members_model = new DefaultListModel<>();
            members.setModel(members_model);
            AllClients.add(members);
            try {
                ArrayList<String> toShow = new ArrayList<>();
                for (String id : serverRef.getAllClients(ownID)) {
                    if (!grp.contains(id)) {
                        toShow.add(id);
                        members_model.addElement(id);
                    }
                }
                members.addListSelectionListener((lse) -> {
                    if (!members.getValueIsAdjusting()) {
                        try {
                            try {
                                grp.addClient(ownID, toShow.get(members.getSelectedIndex()));
                            } catch (ClassNotFoundException | SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            AllClients.dispose();
                        } catch (RemoteException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println(ex.getMessage());
                        }
                    }
                });
            } catch (RemoteException rex) {
                JOptionPane.showMessageDialog(this, rex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            AllClients.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            AllClients.setMinimumSize(new Dimension(500, 500));
            AllClients.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
            AllClients.setVisible(true);
            AllClients.setAlwaysOnTop(true);
        });
        removeClientGroup.addActionListener((ae) -> {
            JFrame AllClients = new JFrame(cnvID);
            AllClients.setLayout(new BorderLayout());
            JList<String> members = new JList<String>();
            DefaultListModel<String> members_model = new DefaultListModel<String>();
            members.setModel(members_model);
            AllClients.add(members, BorderLayout.CENTER);
            ArrayList<String> toShow = new ArrayList<>();
            try {
                boolean isAdmin = true;
                for (String id : grp.getAllClients()) {
                    if (isAdmin) {
                        isAdmin = false;
                        continue;
                    }
                    toShow.add(id);
                    members_model.addElement(id);
                }
            } catch (RemoteException rex) {
                JOptionPane.showMessageDialog(this, rex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            members.addListSelectionListener((lse) -> {
                if (!members.getValueIsAdjusting()) {
                    try {
                        try {
                            grp.removeClient(ownID, toShow.get(members.getSelectedIndex()));
                        } catch (ClassNotFoundException | SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        AllClients.dispose();
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            AllClients.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            AllClients.setMinimumSize(new Dimension(500, 500));
            AllClients.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
            AllClients.setVisible(true);
            AllClients.setAlwaysOnTop(true);
        });
    }

    private void helper() {
        try {
            isGroup = serverRef.getGroups(ownID).contains(cnvID);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        msgs.forEach(msg -> {
            if (isGroup) {
                if (msg.getGroupID() != null && msg.getGroupID().equals(cnvID)) {
                    JPanel messagePane = new JPanel();
                    if (msg.getSenderID().equals(ownID) && msg.getReceiverID().equals(cnvID)) {
                        messagePane.setLayout(new FlowLayout(FlowLayout.RIGHT));
                        messagePane.add(messageShow(msg, false));
                    }
                    else if (msg.getReceiverID().equals(cnvID)) {
                        messagePane.setLayout(new FlowLayout(FlowLayout.LEFT));
                        messagePane.add(messageShow(msg, true));
                    }
                    msgsBox.add(messagePane);
                }
            } else {
                JPanel messagePane = new JPanel();
                if (msg.getSenderID().equals(ownID) && msg.getReceiverID().equals(cnvID)) {
                    messagePane.setLayout(new FlowLayout(FlowLayout.RIGHT));
                    messagePane.add(messageShow(msg, false));
                    msgsBox.add(messagePane);
                } else if (msg.getSenderID().equals(cnvID) && msg.getReceiverID().equals(ownID)) {
                    messagePane.setLayout(new FlowLayout(FlowLayout.LEFT));
                    messagePane.add(messageShow(msg, false));
                    msgsBox.add(messagePane);
                }
            }
        });
        try {
            client.setCnv(this);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel messageShow(Message msg, boolean showSender) {
        JPanel msgPanel = new JPanel(new BorderLayout());
        ArrayList<Object> contents = msg.getMessageContents();
        int i;
        JPanel files = new JPanel(new GridLayout(2, (contents.size() - 1) / 2 + 1));
        for (i = 0; i < contents.size() - 1; i++) {//for every ShareableFile
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
                    JOptionPane.showMessageDialog(this, "something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (mkof.getFileType().startsWith("audio/")) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/client/music-note.png"));
                JButton imageBtn = new JButton(icon);
                imageBtn.addActionListener((ae) -> {
                    try (FileOutputStream fos = new FileOutputStream(new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle())) {
                        fos.write(mkof.getContent());
                        JOptionPane.showMessageDialog(this, "File saved here : " + new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle(), "SUCCESS", JOptionPane.PLAIN_MESSAGE);
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
                        JOptionPane.showMessageDialog(this, "File saved here : " + new java.io.File(".").getCanonicalPath() + "/" + mkof.getTitle(), "SUCCESS", JOptionPane.PLAIN_MESSAGE);
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

    public String getCnvID() {
        return cnvID;
    }

    public boolean isIsGroup() {
        return isGroup;
    }

    public JPanel getMsgsBox() {
        return msgsBox;
    }

    @Override
    public void dispose() {
        try {
            client.removeCnv();
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
        }
        super.dispose(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        MessageText = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgsBox = new javax.swing.JPanel();

        jButton2.setText("jButton2");

        jToggleButton1.setText("jToggleButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton1.setText("SEND");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, java.awt.BorderLayout.LINE_END);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(MessageText, java.awt.BorderLayout.CENTER);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client/output-onlinepngtools (2).png"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3, java.awt.BorderLayout.LINE_END);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        msgsBox.setLayout(new javax.swing.BoxLayout(msgsBox, javax.swing.BoxLayout.LINE_AXIS));
        jScrollPane1.setViewportView(msgsBox);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String messageText = MessageText.getText();
        currentMessageContent.add(messageText);
        if (isGroup) {
            try {
                try {
                    serverRef.sendToGroup(cnvID, currentMessageContent, ownID);
                } catch (ClassNotFoundException | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                try {
                    serverRef.sendToClient(cnvID, currentMessageContent, ownID);
                } catch (ClassNotFoundException | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        currentMessageContent = new ArrayList<>();
        FilesPane.removeAll();
        revalidate();
        repaint();
        MessageText.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            try {
                byte[] file = Files.readAllBytes(selectedFile.toPath());
                ShareableFile shareableFile = new ShareableFile(file, selectedFile.getName(), Files.probeContentType(selectedFile.toPath()));
                currentMessageContent.add(shareableFile);
                ImageIcon icon = new ImageIcon(getClass().getResource("/client/output-onlinepngtools (2).png"));
                JLabel label = new JLabel(icon);
                JPanel FilePane = new JPanel(new BorderLayout());
                FilePane.add(label, BorderLayout.WEST);
                JPanel FileDesc = new JPanel();
                FileDesc.setLayout(new BoxLayout(FileDesc, BoxLayout.Y_AXIS));
                FilePane.add(FileDesc);
                JPanel titleflow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                titleflow.add(new JLabel("Title : " + shareableFile.getTitle()));
                JPanel typeflow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                typeflow.add(new JLabel("Type : " + shareableFile.getFileType()));
                FileDesc.add(titleflow);
                FileDesc.add(typeflow);
                FilesPane.add(FilePane);
                revalidate();
                repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField MessageText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel msgsBox;
    // End of variables declaration//GEN-END:variables
}
