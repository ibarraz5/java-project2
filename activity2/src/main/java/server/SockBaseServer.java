package server;

import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Entry;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Response.ResponseType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;

class SockBaseServer implements Runnable {
    static String logFilename = "logs.txt";
    static String leaderBoardFilename = "leader_log.txt";
    static Response.Builder leaderBoard = Response.newBuilder().setResponseType(Response.ResponseType.LEADER);
    Game game;
    Socket clientSocket;
    InputStream in = null;
    OutputStream out = null;

    public SockBaseServer(Socket sock, Game game) {
        this.clientSocket = sock;
        this.game = game;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e) {
            System.out.println("Error in constructor: " + e);
        }
    }

    public static void main(String[] args) {
        Game game = new Game();

        // initialize leader board file
        initLeaderFile();

        // initialize leaderboard
        initLeaderboard();

        int port = 9099; // default port

        if (argsCheck(args)) {
            port = getPort(args);
        }

        System.out.println("Battleship ThreadServer Started...");
        ServerSocket serv = connectServerSocket(port);

        while (true) {
            try {
                Socket clientSocket = serv.accept();
                System.out.println("Battleship Thread Created...");
                Runnable serverRunnable = new SockBaseServer(clientSocket, game);
                Thread serverThread = new Thread(serverRunnable);
                serverThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates log file for the leaderboard.
     */
    private static void initLeaderFile() {
        try {
            File leaderFile = new File(leaderBoardFilename);
            if (leaderFile.createNewFile()) {
                System.out.println("File created: " + leaderFile.getName());
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Populates the leaderboard builder with log contents.
     */
    private static void initLeaderboard() {
        String line;
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(leaderBoardFilename)); BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    String[] lineArray = line.split(",");
                    String element1 = lineArray[0];
                    String element2 = lineArray[1];

                    int wins = 0;
                    try {
                        wins = Integer.parseInt(element2);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    addEntry(element1, wins);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates the input arguments from the command line.
     *
     * @param args string array representing the command line arguments
     * @return true = port command line arguments are present
     */
    private static boolean argsCheck(String[] args) {
        if (args.length != 1) {
            System.out.println("Expected arguments: <port(int)>");
            System.exit(1);
        }
        return true;
    }

    /**
     * Validates the port argument is a valid integer.
     *
     * @param args string array representing the command line arguments
     * @return integer that's been parsed
     */
    private static int getPort(String[] args) {
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }
        return port;
    }

    /**
     * Creates and initializes a server socket
     *
     * @param port integer representing the port number given in the command line
     * @return server socket
     */
    private static ServerSocket connectServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        return serverSocket;
    }

    /**
     * Adds an entry to the leaderboard.
     *
     * @param name string representing the client name
     * @param wins integer representing the client wins
     */
    private synchronized static void addEntry(String name, int wins) {
        Entry leader = Entry.newBuilder().setName(name).setWins(wins).build();
        leaderBoard.addLeader(leader);
    }

    /**
     * Run method for individual threads.
     */
    @Override
    public void run() {

        String name = "";

        try {
            boolean quit = false;
            while (!quit) {

                Request request;
                Response response;

                if (clientSocket.isConnected()) {
                    request = Request.parseDelimitedFrom(in);
                } else {
                    break;
                }

                switch (request.getOperationType()) {
                    case NAME:
                        name = request.getName();
                        writeToLog(name, Message.CONNECT);
                        response = buildGreetingRes(name);
                        break;
                    case LEADER:
                        leaderBoard.getLeaderList();
                        response = buildLeaderRes();
                        break;
                    case NEW:
                        game.newGame(); // starting a new game
                        response = buildNewRes();
                        writeToLog(name, Message.START);    // Log entry
                        break;
                    case ROWCOL:
                        int row = -99;
                        int column = -99;
                        if (request.hasRow() && request.hasColumn()) {
                            row = request.getRow();
                            column = request.getColumn();
                        }
                        if (game.hasValidLocation(row, column)) {
                            int foundLocations = game.getIdx();
                            game.replaceOneCharacter(row, column);

                            // Win condition
                            if (game.getIdx() == 12) {   // won condition
                                boolean nameExists = false;
                                List<Entry> leaderList = leaderBoard.getLeaderList();

                                // New entry
                                if (!leaderList.isEmpty()) {
                                    // Finds existing leader entries and increments
                                    for (Entry entry : leaderList) {
                                        if (entry.getName().equalsIgnoreCase(name)) {
                                            String entryName = entry.getName();
                                            int entryWins = entry.getWins();
                                            entryWins += 1;
                                            addEntry(entryName, entryWins);
                                            leaderBoard.removeLeader(leaderList.indexOf(entry)).build();    // remove existing entry
                                            writeToLeaderLog(name);
                                            nameExists = true;
                                            break;
                                        }
                                    }

                                    // Accounts for new entries in the leaderboard
                                    if (!nameExists || leaderList.isEmpty()) {
                                        addEntry(name, 1);
                                        writeToLeaderLog(name);
                                    }
                                }

                                showLeaderboard(); 
                                response = buildWonRes();
                                writeToLog(name, Message.WIN);
                            } else if (game.getIdx() > foundLocations) {
                                response = buildTaskRes(true);  // hit response
                            } else {
                                response = buildTaskRes(false); // miss response
                            }
                        } else {
                            response = buildErrorRes("Error: Row | Column must integers.");
                        }
                        System.out.println("Image: \n" + game.getImage());
                        break;
                    case QUIT:
                        response = buildByeRes(name);
                        quit = true;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + request);
                }

                if (response.getResponseType().equals(ResponseType.WON)) {
                    game.setWon();
                }
                response.writeDelimitedTo(out);
            }

        } catch (Exception ex) {
            System.out.println("Client " + name + " connection has been terminated.");
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.out.println("Client connection has been closed");
            }
        }
    }

    /**
     * Writing a new entry to our log
     *
     * @param name    - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect)
     */
    private synchronized static void writeToLog(String name, Message message) {
        try {
            // read old log file
            Logs.Builder logs = readLogFile(logFilename);

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " + name + " - " + message);
            System.out.println(date.toString() + ": " + name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // write to log file
            logsObj.writeTo(output);
        } catch (Exception e) {
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Greeting response builder.
     *
     * @param name string representing the client name
     * @return Response
     */
    private synchronized Response buildGreetingRes(String name) {
        return Response.newBuilder().setResponseType(ResponseType.GREETING).setMessage("Hello " + name + ". Welcome to a the game of Battleship. ").build();
    }

    /**
     * Leaderboard response builder.
     *
     * @return Response
     */
    private synchronized Response buildLeaderRes() {
        return leaderBoard.build();
    }

    /**
     * New game response builder.
     *
     * @return Response
     */
    private synchronized Response buildNewRes() {
        return Response.newBuilder().setResponseType(ResponseType.TASK).setImage(game.getImage()).setTask("Select a row and column.").build();
    }

    /**
     * Writes to the leaderboard log file.
     *
     * @param name string representing the client name
     */
    public synchronized static void writeToLeaderLog(String name) {

        try {
            // input the (modified) file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(leaderBoardFilename));
            StringBuilder inputBuffer = new StringBuilder();
            String line;

            boolean found = false;
            while ((line = file.readLine()) != null) {
                if (line.contains(name)) {
                    // parse for wins
                    String[] contents = line.split(",");
                    int currWins = Integer.parseInt(contents[1]);
                    currWins += 1;
                    line = contents[0] + "," + currWins;
                    found = true;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }

            if (!found) {
                while ((line = file.readLine()) != null) {
                    inputBuffer.append(line);
                    inputBuffer.append('\n');
                }
                inputBuffer.append(name).append(",1");
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(leaderBoardFilename);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }

    }

    /**
     * Displays the leaderboard.
     */
    private synchronized void showLeaderboard() {
        for (Entry lead : leaderBoard.getLeaderList()) {
            System.out.println(lead.getName() + ": " + lead.getWins());
        }
    }

    /**
     * Win response builder.
     *
     * @return Response
     */
    private synchronized Response buildWonRes() {
        return Response.newBuilder().setResponseType(ResponseType.WON).setImage(game.getImage()).setMessage("You win! All Battleships have been destroyed!!").build();
    }

    /**
     * Task response builder.
     *
     * @param hasHit boolean representing if client coordinates were a hit.
     * @return Response
     */
    private synchronized Response buildTaskRes(boolean hasHit) {
        if (hasHit) {
            return Response.newBuilder().setResponseType(ResponseType.TASK).setImage(game.getImage()).setHit(true).setTask("\nTarget Hit!!\nSelect a row and column.").build();
        }
        return Response.newBuilder().setResponseType(ResponseType.TASK).setImage(game.getImage()).setHit(false).setTask("\nTarget Missed!!\nSelect a row and column.").build();
    }

    /**
     * Error response builder.
     *
     * @param errorMessage string representing the error
     * @return Response
     */
    private synchronized Response buildErrorRes(String errorMessage) {
        return Response.newBuilder().setResponseType(ResponseType.ERROR).setMessage(errorMessage).build();
    }

    /**
     * Bye response builder.
     *
     * @param name string representing the client name
     * @return Response
     */
    private synchronized Response buildByeRes(String name) {
        return Response.newBuilder().setResponseType(ResponseType.BYE).setMessage("Goodbye " + name).build();
    }

    /**
     * Reading the current log file
     *
     * @return Logs.Builder a builder of a logs, entry from protobuf
     */
    public static Logs.Builder readLogFile(String fileName) throws Exception {
        Logs.Builder logs = Logs.newBuilder();

        try {
            return logs.mergeFrom(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            System.out.println(fileName + ": File not found.  Creating a new file.");
            return logs;
        }
    }

}
