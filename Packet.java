import java.util.zip.CRC32;

public class Packet {
    public static final int MAX_PACKET_SIZE = 300;
    public static final int HEADER_SIZE = 6;
    public static final int ACK_SIZE = 2;

    private int sequenceNumber;
    private byte[] data;
    private int checksum;

    public Packet(int sequenceNumber, byte[] data) {
        this.sequenceNumber = sequenceNumber;
        this.data = data;
        calculateChecksum();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

    public int getChecksum() {
        return checksum;
    }

    private void calculateChecksum() {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        this.checksum = (int) crc32.getValue();
    }
}
