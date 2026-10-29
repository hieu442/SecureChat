package securechat.model;

public class ChatPacket extends BasePacket {
    private static final long serialVersionUID = 1L;
    
    private String senderName;
    private byte[] encryptedData; 
    private byte[] md5Hash;       

    public ChatPacket(String senderName, byte[] encryptedData, byte[] md5Hash) {
        this.senderName = senderName;
        this.encryptedData = encryptedData;
        this.md5Hash = md5Hash;
    }

    public String getSenderName() { return senderName; }
    public byte[] getEncryptedData() { return encryptedData; }
    public byte[] getMd5Hash() { return md5Hash; }
}