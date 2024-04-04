import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client <server-ip> <port> <username>");
            System.exit(1);
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];

        try (Socket socket = new Socket(serverIp, port);
                Scanner userInput = new Scanner(System.in);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Invio dell'username al server subito dopo la connessione.
            out.println(username);
            System.out.println("Connected to server. Start typing messages (type 'exit' to quit).");

            // Istanza di EnigmaSimulator, se richiesto dall'utente.
            EnigmaSimulator enigmaSimulator = new EnigmaSimulator();
            boolean enigmaOn = false; // booleana enigmaOn, false indica non attivo.

            // Thread per ascoltare e stampare i messaggi in arrivo dal server.
            Thread serverListener = new Thread(() -> {
                try (Scanner in = new Scanner(socket.getInputStream())) {
                    while (in.hasNextLine()) {
                        System.out.println(in.nextLine());
                    }
                } catch (IOException e) {
                    System.out.println("Errore durante la lettura dal server: " + e.getMessage());
                }
            });
            serverListener.start();
            // Ciclo principale per l'invio di messaggi.
            while (true) {
                String message = userInput.nextLine();

                // Esce dal ciclo se l'utente digita "exit".
                if (message.equalsIgnoreCase("exit")) {
                    out.println(username + " ha lasciato la chat.");
                    break;
                }

                if (message.equalsIgnoreCase("enigma_on")) { // Quando l'utente scrive il comando enigma_on in chat
                    System.out.println("Message encrypting ON");// Verrà stampato un messaggio di avviso
                    enigmaOn = true;// E la variabile diventerà true, attivando l'encrypting a riga 60
                    continue;

                }

                if (message.equalsIgnoreCase("enigma_off")) {// Quando l'utente scrive il comando enigma_on in chat
                    System.out.println("Message encrypting OFF");// Verrà stampato un messaggio di avviso
                    enigmaOn = false;// E la variabile diventerà false, disattivando l'encrypting a riga 60
                    continue;
                }

                // Cripta il messaggio se l'utente ha scritto il comando enigma_on
                if (enigmaOn == true) {
                    message = enigmaSimulator.cifraDecifra(message, true);
                }

                // Invia il messaggio al server.
                out.println(username + ": " + message);
            }


        } catch (IOException e) {
            System.out.println("Si è verificato un errore di rete: " + e.getMessage());
        }
    }
}