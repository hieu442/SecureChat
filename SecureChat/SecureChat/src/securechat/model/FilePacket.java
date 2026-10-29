package securechat.model;

public class FilePacket extends BasePacket {
    private static final long serialVersionUID = 1L;
    
    private String senderName;
    private String fileName;
    private byte[] encryptedFileData;
    private byte[] md5Hash;

    public FilePacket(String senderName, String fileName, byte[] encryptedFileData, byte[] md5Hash) {
        this.senderName = senderName;
        this.fileName = fileName;
        this.encryptedFileData = encryptedFileData;
        this.md5Hash = md5Hash;
    }
    
    public String getSenderName() { return senderName; }
    public String getFileName() { return fileName; }
    public byte[] getEncryptedFileData() { return encryptedFileData; }
    public byte[] getMd5Hash() { return md5Hash; }
}