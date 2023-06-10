import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.CRC32;

public class Receiver {
    private String ipAddress; 
    private int port;
    private String outputFilename; 

    public static void main(String[] args) {
        // Inicia o destinatário
        Receiver receiver = new Receiver();
        receiver.startTransfer();

        System.out.println("Transferência concluída!");
    }

    public Receiver() {
        this.ipAddress = "127.0.0.1"; // Endereço IP do destinatário
        this.port = 9876;  // Porta utilizada na comunicação
        this.outputFilename = "received_file.txt"; // Nome do arquivo de saída
    }

    public void startTransfer() {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Iniciou a conexão do receiver...");

            FileOutputStream fileOutputStream = new FileOutputStream(outputFilename);

            int expectedSeqNum = 0; // Número de sequência esperado

            System.out.println("Recebendo os pacotes...");
            while (true) {
                // Recebimento do pacote
                DatagramPacket packet = receivePacket(socket);
                System.out.println("Pacote recebido");

                // Verificação do número de sequência e CRC
                int packetSeqNum = extractSeqNumber(packet);
                System.out.println("Numero de sequência do pacote: " + packetSeqNum);
                if (packetSeqNum == expectedSeqNum && isPacketValid(packet)) {
                    // Pacote correto e na ordem esperada
                    byte[] packetData = extractPacketData(packet);
                    fileOutputStream.write(packetData);
                    System.out.println("Salvou o pacote no arquivo de saída");

                    expectedSeqNum++;
                }

                // Envio do ACK
                sendAck(socket, expectedSeqNum, address, packet.getPort());

                if (isLastPacket(packet)) {
                    System.out.println("Todos os pacotes foram recebidos");
                    // Último pacote recebido, encerramento da transferência
                    break;
                }
            }

            fileOutputStream.close();
            socket.close();
            System.out.println("Conexão do receiver encerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DatagramPacket receivePacket(DatagramSocket socket) throws IOException {
        System.out.println("Tentando receber pacote...");
        byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    private int extractSeqNumber(DatagramPacket packet) {
        byte[] data = packet.getData();
        return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
    }

    private byte[] extractPacketData(DatagramPacket packet) {
        System.out.println("Extraindo dados do pacote...");
        byte[] data = packet.getData();
        byte[] packetData = new byte[packet.getLength() - Packet.HEADER_SIZE];
        System.arraycopy(data, Packet.HEADER_SIZE, packetData, 0, packetData.length);
        return packetData;
    }

    private boolean isPacketValid(DatagramPacket packet) {
        byte[] data = packet.getData();
        CRC32 crc32 = new CRC32();
        crc32.update(data, Packet.HEADER_SIZE, packet.getLength() - Packet.HEADER_SIZE);
    
        // Corrigir o cálculo do checksum
        long calculatedChecksum = crc32.getValue();
        int receivedChecksum = ((data[2] & 0xFF) << 24) | ((data[3] & 0xFF) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
        
        System.out.println("Calculated checksum: " + calculatedChecksum);
        System.out.println("Received checksum: " + receivedChecksum);
        return calculatedChecksum == receivedChecksum;
    }

    private boolean isLastPacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        return (data[6] & 0x01) == 1;
    }

    private void sendAck(DatagramSocket socket, int seqNum, InetAddress address, int port) throws IOException {
        byte[] ackData = new byte[Packet.ACK_SIZE];
        ackData[0] = (byte) ((seqNum >> 8) & 0xFF);
        ackData[1] = (byte) (seqNum & 0xFF);

        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, address, port);
        System.out.println("Enviando ack para o sender...");
        socket.send(ackPacket);
    }
}
