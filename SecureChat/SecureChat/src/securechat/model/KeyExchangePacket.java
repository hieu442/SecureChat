package securechat.model;

public class KeyExchangePacket extends BasePacket {
    private static final long serialVersionUID = 1L;
    private byte[] encryptedDesKey;

    public KeyExchangePacket(byte[] encryptedDesKey) {
        this.encryptedDesKey = encryptedDesKey;
    }

    public byte[] getEncryptedDesKey() {
        return encryptedDesKey;
    }
}