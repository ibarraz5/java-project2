package taskone;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;



public class Client {

    private static BufferedReader stdin;

    /**
     * Function JSONObject add().
     */
    public static JSONObject add() {
        JSONObject request = new JSONObject();
        String strToSend = null;
        request.put("selected", 1);

        try {
            System.out.print("Please input the string: ");
            strToSend = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        request.put("data", strToSend);
        return request;
    }

    /**
     * Function JSONObject remove().
     */
    public static JSONObject remove() {
        JSONObject request = new JSONObject();
        String strToSend = null;
        request.put("selected", 2);

        int inNum;
        boolean validNum = false;
        while (!validNum) {

            // Valid input
            try {
                System.out.print("Please input the index: ");
                strToSend = stdin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  Parse input
            if (strToSend != null) {
                try {
                    inNum = Integer.parseInt(strToSend);
                    request.put("data", inNum);
                    validNum = true;
                } catch (NumberFormatException ne) {
                    System.out.println("Input is not number, continue");
                }
            }
        }

        return request;
    }

    /**
     * Function JSONObject display().
     */
    public static JSONObject display() {
        JSONObject request = new JSONObject();
        request.put("selected", 3);
        request.put("data", "");
        return request;
    }

    /**
     * Function JSONObject count().
     */
    public static JSONObject count() {
        JSONObject request = new JSONObject();
        request.put("selected", 4);
        request.put("data", "");
        return request;
    }

    /**
     * Function JSONObject reverse().
     */
    public static JSONObject reverse() {
        JSONObject request = new JSONObject();
        request.put("selected", 5);

        String strToSend = null;
        int inNum;
        boolean validNum = false;
        while (!validNum) {
            System.out.print("Please input the index: ");

            //  Read input
            try {
                strToSend = stdin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  Parse input
            if (strToSend != null) {
                try {
                    inNum = Integer.parseInt(strToSend);
                    request.put("data", inNum);
                    validNum = true;
                } catch (NumberFormatException ne) {
                    System.out.println("Input is not number, continue");
                }
            }
        }
        return request;
    }

    /**
     * Function JSONObject quit().
     */
    public static JSONObject quit() {
        JSONObject request = new JSONObject();
        request.put("selected", 0);
        request.put("data", ".");
        return request;
    }

    /**
     * Function main().
     */
    public static void main(String[] args) throws IOException {
        String host;
        int port;
        Socket sock;
        stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (args.length != 2) {
                // gradle runClient -Phost=localhost -Pport=9099 -q --console=plain
                System.out.println("Usage: gradle runClient -Phost=localhost -Pport=9099");
                System.exit(0);
            }

            host = args[0];
            port = -1;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.out.println("[Port] must be an integer");
                System.exit(2);
            }

            sock = new Socket(host, port);
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();
            Scanner input = new Scanner(System.in);
            int choice;
            do {
                System.out.println();
                System.out.println("Client Menu");
                System.out.println("Please select a valid option (1-5). 0 to disconnect the client");
                System.out.println("1. add <string> - adds a string to the list and display it");
                System.out.println("2. remove <integer>- remove element at index");
                System.out.println("3. display - display the list");
                System.out.println("4. count - returns the number of elements in the list");
                System.out.println("5. reverse <integer> - reverse the string at index");
                System.out.println("0. quit");
                System.out.println();

                // Input error handling
                if (input.hasNextInt()) {
                    choice = input.nextInt();
                } else {
                    choice = 99;  // fail here
                }
                JSONObject request = null;
                switch (choice) {
                    case (1):
                        request = add();
                        break;
                    case (2):
                        request = remove();
                        break;
                    case (3):
                        request = display();
                        break;
                    case (4):
                        request = count();
                        break;
                    case (5):
                        request = reverse();
                        break;
                    case (0):
                        request = quit();
                        break;
                    default:
                        System.out.println("Please select a valid option (0-6).");
                        break;
                }
                if (request != null) {
                    System.out.println(request);
                    NetworkUtils.send(out, JsonUtils.toByteArray(request));
                    byte[] responseBytes = NetworkUtils.receive(in);
                    JSONObject response = JsonUtils.fromByteArray(responseBytes);

                    if (response.has("error")) {
                        System.out.println(response.getString("error"));
                    } else {
                        System.out.println();
                        System.out.println("The response from the server: ");
                        System.out.println("datatype: " + response.getString("type"));
                        System.out.println("data: " + response.getString("data"));
                        System.out.println();
                        String typeStr = (String) response.getString("type");
                        if (typeStr.equals("quit")) {
                            sock.close();
                            out.close();
                            in.close();
                            System.exit(0);
                        }
                    }
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
