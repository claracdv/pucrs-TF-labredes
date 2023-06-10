import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteUDP {
    public static void main(String args[]) throws Exception {
        while (true) {
            Cliente();
            System.out.println("Transferência enviada pelo cliente!");
        }
    };

    public static void Cliente() throws Exception {
        // Cria o stream do teclado
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        // Declara socket cliente
        DatagramSocket clientSocket = new DatagramSocket();
        // Endereço IP do servidor
        InetAddress IPAddress = InetAddress.getByName("localhost");

        final int TIMEOUT = 2000;
        final int DUPLICATE_ACK_THRESHOLD = 3;

        byte[] sendData = new byte[300];
        byte[] receiveData = new byte[300];

        System.out.println("Digite uma mensagem: ");
        // Lê uma linha do teclado
        String sentence = inFromUser.readLine();
        sendData = sentence.getBytes();
        // Cria pacote com o dado, o endereço do server e porta do servidor
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        // Envia o pacote
        clientSocket.send(sendPacket);
        // Declara o pacote a ser recebido
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        // Recebe pacote do servidor
        clientSocket.receive(receivePacket);
        // Transforma o pacote em string
        String modifiedSentence = new String(receivePacket.getData());
        // Mostra no console
        System.out.println("FROM SERVER:" + modifiedSentence);
        // Fecha o cliente
        clientSocket.close();
    }
}