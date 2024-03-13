package Interfaces;

import javax.swing.JPanel;

public interface IConversationGui {
    public String getCnvID();

    public boolean isIsGroup();

    public JPanel getMsgsBox();
}
