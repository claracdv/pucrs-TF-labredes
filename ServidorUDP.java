import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServidorUDP {
    public static void main(String args[]) throws Exception {
        // Cria socket do servidor com a porta 9876
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        // Loop infinito para receber e responder mensagens
        while (true) {
            // Declara o pacote a ser recebido
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // Recebe o pacote do cliente
            serverSocket.receive(receivePacket);
            // Pega os dados, o endereço IP e a porta do cliente
            String sentence = new String(receivePacket.getData());
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            // Converte a string em maiúsculo
            String capitalizedSentence = sentence.toUpperCase();
            // Cria o pacote com o dado modificado
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            // Envia o pacote modificado para o cliente
            serverSocket.send(sendPacket);
        }
    }
}
