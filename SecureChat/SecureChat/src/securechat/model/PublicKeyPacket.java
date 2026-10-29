package securechat.model;

import java.security.PublicKey;

public class PublicKeyPacket extends BasePacket {
    private static final long serialVersionUID = 1L;
    private PublicKey publicKey;

    public PublicKeyPacket(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}