import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {
    private static final int PACKET_SIZE = 300;
    private static final int WINDOW_SIZE = 10;
    private static final int TIMEOUT = 2000;
    private static final int DUPLICATE_ACK_THRESHOLD = 3;

    private String ipAddress;
    private int port;
    private String filename;

    public static void main(String[] args) {
        // Inicia o remetente
        Sender sender = new Sender();
        sender.startTransfer();
    
        System.out.println("Transferência concluída!");
    }

    public Sender() {
        this.ipAddress = "127.0.0.1"; // Endereço IP do destinatário
        this.port = 9876; // Porta utilizada na comunicação
        this.filename = "file.txt"; // Nome do arquivo a ser transferido
    }

    public void startTransfer() {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Iniciou a conexão do sender...");

            byte[] fileData = readFileData(filename);
            List<Packet> packets = createPackets(fileData);

            int base = 0; // Base do window
            int nextSeqNum = 0; // Próximo número de sequência a ser enviado
            int lastAckReceived = -1; // Último ACK recebido
            int duplicateAckCount = 0; // Contador de ACKs duplicados

            while (base < packets.size()) {
                // Envio dos pacotes dentro do window
                for (int i = base; i < Math.min(base + WINDOW_SIZE, packets.size()); i++) {
                    Packet packet = packets.get(i);
                    sendPacket(socket, packet, address, port);
                }

                // Recebimento de ACKs
                DatagramPacket ackPacket = receiveAck(socket);
                int ackNumber = extractAckNumber(ackPacket);
                System.out.println("Ack number: " + ackNumber);
                
                if (ackNumber == lastAckReceived) {
                    System.out.println("Ack duplicado");
                    // Recebido um ACK duplicado
                    duplicateAckCount++;

                    if (duplicateAckCount >= DUPLICATE_ACK_THRESHOLD) {
                        System.out.println("Ack duplicado 3 vezes");
                        // Retransmissão imediata do pacote
                        handleDuplicateAcks(socket, packets.get(lastAckReceived), address, port);
                        duplicateAckCount = 0;
                    }
                    continue;
                }

                lastAckReceived = ackNumber;
                duplicateAckCount = 0;

                if (ackNumber == packets.size() - 1) {
                    System.out.println("Todos os pacotes foram enviados");
                    // Todos os pacotes foram confirmados, transferência concluída
                    break;
                }

                if (ackNumber > base) {
                    // Movimento da janela deslizante
                    base = ackNumber;
                    nextSeqNum = base + 1;

                    // Reinicia o timer
                    socket.setSoTimeout(TIMEOUT);
                }

                // Verifica se há pacotes atrasados
                if (nextSeqNum < packets.size()) {
                    System.out.println("Achou um pacote atrasado");
                    // Envio do próximo pacote
                    Packet packet = packets.get(nextSeqNum);
                    sendPacket(socket, packet, address, port);
                    nextSeqNum++;
                }
            }

            socket.close();
            System.out.println("Conexão do sender encerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private byte[] readFileData(String filename) throws IOException {
        File file = new File(filename);
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileData);
        }
        return fileData;
    }

    private List<Packet> createPackets(byte[] fileData) {
        List<Packet> packets = new ArrayList<>();
        int sequenceNumber = 0;
        int offset = 0;
        System.out.println("Criando pacotes...");
        while (offset < fileData.length) {
            int length = Math.min(PACKET_SIZE, fileData.length - offset);
            byte[] packetData = Arrays.copyOfRange(fileData, offset, offset + length);
            Packet packet = new Packet(sequenceNumber, packetData);
            packets.add(packet);
            sequenceNumber++;
            offset += length;
        }
        System.out.println("Pacotes criados");
        return packets;
    }

    private void sendPacket(DatagramSocket socket, Packet packet, InetAddress address, int port) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(packet.getSequenceNumber());
        dataOutputStream.writeInt(packet.getChecksum());
        dataOutputStream.writeInt(packet.getData().length);
        dataOutputStream.write(packet.getData());
        dataOutputStream.flush();
        byte[] sendData = outputStream.toByteArray();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
        System.out.println("Enviou o pacote - seq: " + packet.getSequenceNumber() + " checksum: " + packet.getChecksum() + " tamanho: " + packet.getData().length);
    }

    private DatagramPacket receiveAck(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[4];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        return receivePacket;
    }

    private int extractAckNumber(DatagramPacket ackPacket) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ackPacket.getData());
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        return dataInputStream.readInt();
    }

    private void handleDuplicateAcks(DatagramSocket socket, Packet packet, InetAddress address, int port) throws IOException {
        // Realiza a retransmissão imediata do pacote
        sendPacket(socket, packet, address, port);
    }
}