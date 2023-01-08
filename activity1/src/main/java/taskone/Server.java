package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * File: Server.java
 * Description: Server tasks.
 */
class Server {

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
        System.out.println("Server Started...");
        while (true) {
            System.out.println("Accepting a Request...");
            Socket sock = server.accept();

            Performer performer = new Performer(sock, strings);
            performer.doPerform();
            try {
                System.out.println("close socket of client ");
                sock.close();
            }  catch (IOException IOex) {
			System.out.println("IOException: CONNECTION FAILED. RESTARTING SERVER.");
			// System.exit(1);
	        } catch (Exception e) {
			System.out.println("SERVER EXCEPTION. RESTARTING SERVER.");
			// System.exit(1);
            }
        }
    }
}
