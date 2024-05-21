package Interfaces;

import javax.swing.JPanel;

public interface IConversationGui {
    String getCnvID();

    boolean isIsGroup();

    JPanel getMsgsBox();
}
