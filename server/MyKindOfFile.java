
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tmdr
 */
public class MyKindOfFile implements Serializable{
    private byte[] content;
    private String Title;
    private String FileType;    

    public MyKindOfFile(byte[] content, String Title, String FileType) {
        this.content = content;
        this.Title = Title;
        this.FileType = FileType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getTitle() {
        return Title;
    }

    public String getFileType() {
        return FileType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MyKindOfFile{" + "content=[");
        for(int i = 0;i< content.length;i++){
            sb.append(content[i]);
            if(i != content.length-1)
                sb.append('+');
        }
        sb.append("]| Title=");
        sb.append(Title);
        sb.append("| FileType=");
        sb.append(FileType);
        sb.append('}');
        return sb.toString();
    }
    
    
}
