public class Main {
    public static void main(String[] args) {
        String ipAddress = "127.0.0.1"; // Endereço IP do destinatário
        int port = 9876; // Porta utilizada na comunicação
        String filename = "file.txt"; // Nome do arquivo a ser transferido
        String outputFilename = "received_file.txt"; // Nome do arquivo de saída

        // Inicia o remetente
        Sender sender = new Sender(ipAddress, port, filename);
        sender.startTransfer();

        // Inicia o destinatário
        Receiver receiver = new Receiver(ipAddress, port, outputFilename);
        receiver.startTransfer();

        System.out.println("Transferência concluída!");
    }
}
