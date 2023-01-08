package taskone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;


class Performer {

    private final StringList stringListState;
    private final Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.stringListState = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "add");
        stringListState.add(str);
        json.put("data", stringListState.toString());
        return json;
    }

    /**
     * Remove the string from the list given the index.
     *
     * @param index int representing the index
     * @return json object
     */
    public JSONObject remove(int index) {
        JSONObject json = new JSONObject();

        //  Validates index has a value
        if (stringListState.isWithinBounds(index)) {
            json.put("datatype", 2);
            json.put("type", "remove");
            json.put("data", stringListState.remove(index));
        } else {
            json = error("Error: No string value is located at that index " + index);
        }

        return json;
    }

    /**
     * Displays the lists
     *
     * @return json object
     */
    public JSONObject display() {
        JSONObject json = new JSONObject();
        json.put("datatype", 3);
        json.put("type", "display");
        json.put("data", stringListState.displayList());
        return json;
    }

    /**
     * Counts the number of elements in list
     *
     * @return json object
     */
    public JSONObject count() {
        JSONObject json = new JSONObject();
        json.put("datatype", 4);
        json.put("type", "count");
        String size = String.valueOf(stringListState.size());
        json.put("data", size);
        json.put("option", stringListState.displayList());
        return json;
    }

    /**
     * Reverses the string element given the index
     *
     * @param index int representing the index
     * @return json object
     */
    public JSONObject reverse(int index) {
        JSONObject json = new JSONObject();

        //  Validates index has a value
        if (stringListState.isWithinBounds(index)) {
            json.put("datatype", 5);
            json.put("type", "reverse");
            json.put("data", stringListState.reverse(index));
        } else {
            json = error("Error: No string value is located at that index " + index);
        }

        return json;
    }

    /**
     * Error print statement
     *
     * @param err string containing error
     * @return json object
     */
    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }

    /**
     * Communication interface between the client and server
     */
    public void doPerform() {
        boolean quit = false;
        OutputStream out;
        InputStream in;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("ThreadedServer connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();

                int choice = message.getInt("selected");
                switch (choice) {
                    case (1) -> {
                        String inStr1 = (String) message.get("data");
                        returnMessage = add(inStr1);
                    }
                    case (2) -> {
                        int removeIndex = (int) message.get("data");
                        if (stringListState.isWithinBounds(removeIndex)) returnMessage = remove(removeIndex);
                        else returnMessage = error("Index " + removeIndex + " is out of range");
                    }
                    case (3) -> returnMessage = display();
                    case (4) -> returnMessage = count();
                    case (5) -> {
                        int reverseIndex = (int) message.get("data");
                        returnMessage = reverse(reverseIndex);
                    }
                    case (0) -> quit = true;
                    default -> returnMessage = error("Invalid selection: " + choice + " is not an option");
                }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
