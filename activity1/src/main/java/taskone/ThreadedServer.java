package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



class ThreadedServer {

    private static int clientPool = 0;

    private static void addClient(){
        clientPool+=1;
    }

    public static void main(String[] args) throws Exception {
        int port;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runServer -Pport=9099 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }

        ServerSocket server = new ServerSocket(port);
        server.setReuseAddress(true);

        System.out.println("ThreadedServer Started...");
        while (true) {
            try {
                System.out.println("Accepting a Request...");
                Socket sock = server.accept();
                addClient();
                ClientHandler clientHandler = new ClientHandler(clientPool,sock, strings);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static class ClientHandler extends Thread {
        private final Performer performer;

        public ClientHandler(int id, Socket socket, StringList stringList) {
            this.performer = new Performer(socket, stringList);
            System.out.println("Client " + id + " is connected.");
        }

        @Override
        public void run() {
            performer.doPerform();
        }
    }


}
