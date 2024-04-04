
// Importa le classi necessarie per gestire input/output e networking.
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

// Dichiarazione della classe Server.
public class Server {
    private static final int PORT = 8000;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static FileWriter fileWriter; // Aggiunto per la scrittura su file
    private static int usersPresent;

    private static String onlineUsers;

    public static void main(String[] args) {
        try {
            fileWriter = new FileWriter("chat.txt", true); // Apertura del file in modalità append così non vengo
                                                           // cancellati ma aggiunti dopo
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server avviato sulla porta " + PORT);
                while (true) {
                    new ClientHandler(serverSocket.accept()).start();
                }
            } finally {
                fileWriter.close(); // quando si conclude si chiude
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        private static void updateOnlineUsersMessage() {
            onlineUsers = "Utenti online: " + usersPresent;
        }

        @Override
        public void run() {
            String messageLog = null; // Inizializzato fuori dal try per essere accessibile ovunque
            try {
                Scanner in = new Scanner(clientSocket.getInputStream());
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // Attende la ricezione dell'username dal client come primo messaggio
                messageLog = in.nextLine();// contiene l'username(meglio cambiare nome)
                String welcomeMessage = "L'utente " + messageLog + " si è appena connesso dall'IP "
                        + clientSocket.getRemoteSocketAddress();
                // Prima di aggiungere il writer all'insieme, annuncia la connessione del nuovo
                // utente
                clientWriters.add(out);
                usersPresent++;
                updateOnlineUsersMessage(); // Aggiorna il messaggio con il numero di utenti online
                broadcast(welcomeMessage);
                broadcast(onlineUsers); // Notifica il numero aggiornato di utenti online

                while (true) {
                    if (in.hasNextLine()) {
                        String message = in.nextLine();
                        broadcast(message);
                        System.out.println(message);
                        // Scrivi il messaggio nel file di log
                        synchronized (fileWriter) { // uso synchronized per evitare errori avendo threads multipli
                            fileWriter.write(message + "\n");
                            fileWriter.flush(); // questo metodo assicura che il messaggio venga scritto correttamente
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                    usersPresent--;
                    updateOnlineUsersMessage(); // Aggiorna il messaggio con il numero di utenti online
                    String goodbyeMessage = "L'utente " + messageLog + " ha lasciato la chat.";
                    broadcast(goodbyeMessage);
                    // Invia il messaggio aggiornato con il numero di utenti attivi
                    String activeUsersMessage = "Ci sono " + usersPresent + " utenti attivi.";
                    broadcast(activeUsersMessage);
                    // broadcast(onlineUsers); // Notifica il numero aggiornato di utenti online

                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            // Verifica che il messaggio non sia null e non sia una stringa vuota
            if (message != null && !message.trim().isEmpty()) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            } else {
                // Puoi scegliere di loggare o di ignorare i messaggi nulli o vuoti
                System.out.println("Tentativo di inviare un messaggio vuoto o null.");
            }
        }

    }
}