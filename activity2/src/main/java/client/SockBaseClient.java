package client;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Entry;
import buffers.ResponseProtos.Response;

import java.io.*;
import java.net.Socket;

class SockBaseClient {

    public static void main(String[] args) throws Exception {
        String host = null;
        int port = -1;

        if (argsCheck(args)) {
            host = args[0];
            port = getPort(args);
        }

        boolean displayMenu = true;
        boolean hasDisconnect = false;
        boolean exitGame = false;

        // connect to the server
        try (Socket serverSock = new Socket(host, port); OutputStream out = serverSock.getOutputStream(); InputStream in = serverSock.getInputStream()) {

            // Ask user for username
            System.out.println("Please provide your name for the server.");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String strToSend = stdin.readLine();

            Request request = null;
            Response response = null;

            if (serverSock.isConnected()) {
                request = buildNameReq(strToSend);                   // Client name request
                request.writeDelimitedTo(out);                                                  // write to the server
                response = getResponse(in);    // read from the server
            } else {
                serverSock.close();
            }

            // Server response
            if (response != null) {
                if (response.getResponseType() == Response.ResponseType.GREETING) {
                    System.out.println(response.getMessage());
                }
            }

            // Outer loop to terminate connection
            while (!hasDisconnect) {

                // First inner loop: Menu
                int selection = -99;
                if (displayMenu) {
                    while (selection == -99) {
                        System.out.println("* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to play the game \n 3 - quit the game");
                        String menuSelection = stdin.readLine();
                        try {
                            int parseSelection = Integer.parseInt(menuSelection);
                            if (checkBounds(parseSelection)) {
                                selection = parseSelection;

                                switch (selection) {
                                    case 1:
                                        request = buildLeaderReq();
                                        break;
                                    case 2:
                                        request = buildGameReq();    // New game request
                                        displayMenu = false;
                                        break;
                                    case 3:
                                        request = buildQuitReq();        // Terminate session
                                        hasDisconnect = true;
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + selection);
                                }
                            }
                        } catch (IOException | NumberFormatException e) {
                            System.out.println("Enter a valid number");
                        }
                    }
                }

                // Validate connection, write request, and read response
                if (request != null && response != null && serverSock.isBound()) {
                    request.writeDelimitedTo(out);
                    response = getResponse(in);
                } else {
                    serverSock.close();
                }

                // Response actions
                if (response != null) {
                    switch (response.getResponseType()) {
                        case LEADER:
                            for (Entry lead : response.getLeaderList()) {
                                System.out.println(lead.getName() + ":" + lead.getWins());
                            }
                            break;
                        case TASK:
                            System.out.println(response.getTask());
                            System.out.println(response.getImage());
                            int inRow = -99;
                            boolean validRow = false;
                            while (!validRow) {
                                System.out.println("Enter a row value: ");
                                String rowInput = stdin.readLine();

                                // check input for null
                                if (rowInput != null) {
                                    if (!isExit(rowInput)) {
                                        try {
                                            inRow = Integer.parseInt(rowInput);
                                            validRow = true;
                                        } catch (NumberFormatException e) {
                                            System.out.println("Enter a valid integer for Row.");
                                        }
                                    } else {
                                        exitGame = true;
                                        displayMenu = true;
                                        break;
                                    }
                                }
                            }
                            int inColumn = -99;
                            boolean validColumn = false;
                            while (!validColumn) {
                                System.out.println("Enter a column value");
                                String columnInput = stdin.readLine();

                                if (columnInput != null) {
                                    if (!isExit(columnInput)) {
                                        try {
                                            inColumn = Integer.parseInt(columnInput);
                                            validColumn = true;
                                        } catch (NumberFormatException e) {
                                            System.out.println("Enter a valid integer for Column.");
                                        }
                                    } else {
                                        exitGame = true;
                                        displayMenu = true;
                                        break;
                                    }
                                }
                            }
                            if (exitGame) {
                                request = buildQuitReq();
                            } else {
                                request = buildTaskReq(inRow, inColumn);
                            }
                            break;
                        case WON:
                            System.out.println(response.getMessage());
                            System.out.println(response.getImage());
                            displayMenu = true;
                            break;
                        case ERROR:
                            System.out.println(response.getMessage());
                            break;
                        case BYE:
                            System.out.println(response.getMessage());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + response.getResponseType());
                    }
                }

                if (exitGame) {
                    request.writeDelimitedTo(out);                                      // write to the server
                    response = Response.parseDelimitedFrom(in);       // read from the server
                }

            }
        }
    }

    /**
     * Validates the command line arguments.
     *
     * @param args string array containing the command line arguments
     * @return true = length of array = 2 | false = length of array less than or greater than 2
     */
    private static boolean argsCheck(String[] args) {
        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        return true;
    }

    /**
     * Returns the port value within the command line arguments.
     *
     * @param args string array containing the command line arguments
     * @return integer representing the port
     */
    private static int getPort(String[] args) {
        int port = -1;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }
        return port;
    }

    /**
     * Name request builder.
     *
     * @param name string representing client name
     * @return Request
     */
    private static Request buildNameReq(String name) {
        return Request.newBuilder().setOperationType(Request.OperationType.NAME).setName(name).build();
    }

    /**
     * Getter for server response.
     *
     * @param in inputStream
     * @return Response
     * @throws IOException io exception
     */
    private synchronized static Response getResponse(InputStream in) throws IOException {
        return Response.parseDelimitedFrom(in);
    }

    /**
     * Checks the bounds of the menu selection.
     *
     * @param i integer value representing the client selection
     * @return true = "i" values within the bounds
     * @throws IOException io exception
     */
    private static boolean checkBounds(int i) throws IOException {
        return i > 0 && i < 4;
    }

    /**
     * Leader request builder.
     *
     * @return Request
     */
    private static Request buildLeaderReq() {
        Request op;
        op = Request.newBuilder().setOperationType(Request.OperationType.LEADER).build();
        return op;
    }

    /**
     * New game request builder.
     *
     * @return Request
     */
    private static Request buildGameReq() {
        Request op;
        op = Request.newBuilder().setOperationType(Request.OperationType.NEW).build();
        return op;
    }

    /**
     * Quit request builder.
     *
     * @return Request
     */
    private static Request buildQuitReq() {
        Request op;
        op = Request.newBuilder().setOperationType(Request.OperationType.QUIT).build();
        return op;
    }

    /**
     * Validates the user input is not "exit"
     *
     * @param input string representing the clients input
     * @return true = input is equal to "exit"
     */
    private static boolean isExit(String input) {
        return input.equalsIgnoreCase("exit");
    }

    /**
     * Task request builder.
     *
     * @param row    integer representing the row
     * @param column integer representing the column
     * @return Request
     */
    private static Request buildTaskReq(int row, int column) {
        Request op;
        op = Request.newBuilder().setOperationType(Request.OperationType.ROWCOL).setRow(row).setColumn(column).build();
        return op;
    }
}


