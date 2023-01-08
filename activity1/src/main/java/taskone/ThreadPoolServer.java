package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


class ThreadPoolServer {

    public static void main(String[] args) throws Exception {
        int nThreads = 0;
        Executor pool;
        int port;
        StringList strings = new StringList();

        if (args.length != 2) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runServer -Pport=9099 -Pthreadsize=20 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
            nThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }

        pool = Executors.newFixedThreadPool(nThreads);
        ServerSocket server = new ServerSocket(port);
        server.setReuseAddress(true);

        System.out.println("ThreadedServer Started...");
        while (true) {
            try {
                System.out.println("Accepting a Request...");
                Socket sock = server.accept();
                pool.execute(new ClientHandler(sock, strings));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static class ClientHandler extends Thread {
        private final Performer performer;

        public ClientHandler(Socket socket, StringList stringList) {
            this.performer = new Performer(socket, stringList);
        }

        @Override
        public void run() {
            performer.doPerform();
        }
    }


}
