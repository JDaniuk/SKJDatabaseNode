
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.UUID;

public class DatabaseNode {
    static int port = 15000;
    static String identifier = null;
    static String command = null;
    static int NetworkSize;
    static int AnswerCounter = 0;
    static int allAnswers;
    static boolean connectedToClient;

    static ArrayList<Integer> returnedValues = new ArrayList<>();

    static int maxValue;
    static int maxKey;
    static int minValue;
    static int minKey;


    static String id = UUID.randomUUID().toString();
    static ServerSocket serverSocket;
    static String sentLastRequest;
    static NodeConnection sentRequest;
    static int key = 0;

    static int value = 0;
    static ArrayList<NodeConnection> connectedNodes = new ArrayList<>();
    static ArrayList<Socket> nodes = new ArrayList<>();
    static NodeConnection respondTo;

    public static void main(String[] args) throws IOException {
        // parameter storage
        //String gateway = null;


        System.out.println(id);
        // Parameter scan loop
        boolean checkingConnections = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-tcpport":
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "-record":

                    String[] parts = args[i + 1].split(":");
                    key = Integer.parseInt(parts[0]);
                    value = Integer.parseInt(parts[1]);
                    break;
                case "-connect":
                    String[] connectionAddres = args[i + 1].split(":");
                    System.out.println("connecting to:" + connectionAddres[0] + ":" + connectionAddres[1]);
                    NodeConnection connection = new NodeConnection(connectionAddres[0], Integer.parseInt(connectionAddres[1]));
                    connection.OrginserverID = id;
                    connectedNodes.add(connection);
                    Thread nodeThread = new Thread(connection);
                    connection.isNode = true;
                    nodeThread.start();
                    break;
            }
            System.out.println("Switch ended");
        }
        System.out.println("NO more arguments");
        maxValue = value;
        minValue = value;
        maxKey = key;
        minKey = key;
        try {
            boolean portAsigned = false;
            while (!portAsigned) {
                try {

                    serverSocket = new ServerSocket(port);
                    portAsigned = true;
                } catch (BindException exception) {
                    port += 1;
                }
            }


            System.out.println("Listening on: " + port);
            while (!(serverSocket.isClosed())) {
                Socket clientSocket = serverSocket.accept();
                NodeConnection nodeConnection = new NodeConnection(clientSocket);
                // nodeConnection.run();
                connectedNodes.add(nodeConnection);
                Thread nodeThread = new Thread(nodeConnection);
                nodeThread.start();
                updateConnections();

            }
            // new Server(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static synchronized void updateConnections() {
        try {
            for (NodeConnection connection : connectedNodes) {
                if (connection.socket.isClosed()) {
                    connectedNodes.remove(connection);
                }
            }
        } catch (ConcurrentModificationException exception) {
            exception.printStackTrace();
        }
    }


    static boolean compareConnections(NodeConnection connection, NodeConnection otherConnection) {
        //returns true if both are a server connected to the same node
        if (connection.isNode && otherConnection.isNode) {
            if (connection.type == 1 && otherConnection.type == 1) {
                System.out.println("Mają typ 1 i zwróci: " + connection.connectedToID.equals(otherConnection.connectedToID));
                return connection.connectedToID.equals(otherConnection.connectedToID);
            } else if (connection.type == 2 && otherConnection.type == 2) {
                System.out.println("Mają typ 2 i zwróci: " + connection.OrginserverID.equals(otherConnection.OrginserverID));

                return connection.OrginserverID.equals(otherConnection.OrginserverID);
            } else if (connection.type != otherConnection.type) {
                return connection.connectedToID.equals(otherConnection.OrginserverID);
            }
        }
        return false;
    }


    static boolean toBeIgnored(NodeConnection connection, String otherID) {
        if (connection.isNode) {
            if (connection.type == 1) {
                if (connection.connectedToID.equals(otherID)) {
                    System.out.println("IGNORED: Reason: typu1 takie same id");
                    return true;
                }
            } else if (connection.type == 2) {
                if (connection.OrginserverID.equals(otherID)) {
                    System.out.println("IGNORED: Reason: typu 2 takie same id");
                    return true;
                }
            }   return false;
        } else return true;

    }

    static void BetterBroadcast(String Message, String OrginServerID, ArrayList<String> serversToIgnore) {
        serversToIgnore.add(OrginServerID);
        for (NodeConnection connectedNode : connectedNodes) {
            ArrayList<String> newIdToIgnore = new ArrayList<>();
            boolean sending = true;
            for (String idOfTheOtherServer : serversToIgnore) {
                if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                    sending = false;
                }
            }
            if (sending) {
                try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                    System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                    for (NodeConnection connection : connectedNodes) {
                        if (connection.type == 1) {
                            newIdToIgnore.add(connection.connectedToID);
                        } else if (connection.type == 2) {
                            newIdToIgnore.add(connection.OrginserverID);
                        }

                    }
                    String MessageToSend = Message;
                    // newIdToIgnore.add(serversToIgnore);
                    for (String old : serversToIgnore) {
                        newIdToIgnore.add(old);
                    }
                    for (String id : newIdToIgnore) {
                        String a = (id + ":");
                        MessageToSend += a;
                    }
                    connectedNode.out.println(MessageToSend);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
        }
    }


    static void answerer(String idWherToSend) {
        for (NodeConnection connection : connectedNodes) {


        }
    }

    static void answer(String Message) {
        sentRequest.out.println(Message);
    }

    static boolean setValue(int key, int value, String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;
        if (DatabaseNode.key == key) {
            DatabaseNode.value = value;
            DatabaseNode.maxValue = value;
            DatabaseNode.minValue = value;
            return true;
        } else {

            serversToIgnore.add(OrginServerID);
            for (NodeConnection connectedNode : connectedNodes) {
                ArrayList<String> newIdToIgnore = new ArrayList<>();
                boolean sending = true;
                for (String idOfTheOtherServer : serversToIgnore) {
                    if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                        sending = false;
                    }
                }
                if (sending) {
                    try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                        System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                        for (NodeConnection connection : connectedNodes) {
                            if (connection.type == 1) {
                                newIdToIgnore.add(connection.connectedToID);
                            } else if (connection.type == 2) {
                                newIdToIgnore.add(connection.OrginserverID);
                            }

                        }
                        String MessageToSend = "-internal set-value " + key + ":" + value + " ";
                        // newIdToIgnore.add(serversToIgnore);
                        for (String old : serversToIgnore) {
                            newIdToIgnore.add(old);
                        }
                        for (String id : newIdToIgnore) {
                            String a = (id + ":");
                            MessageToSend += a;
                        }
                        connectedNode.out.println(MessageToSend);
                        AnswerCounter += 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
            }
        }
        return false;
    }


    static boolean getValue(int key, String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;
        if (DatabaseNode.key == key) {
            return true;
        } else {

            serversToIgnore.add(OrginServerID);
            for (NodeConnection connectedNode : connectedNodes) {
                ArrayList<String> newIdToIgnore = new ArrayList<>();
                boolean sending = true;
                for (String idOfTheOtherServer : serversToIgnore) {
                    if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                        sending = false;
                    }
                }
                if (sending) {
                    try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                        System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                        for (NodeConnection connection : connectedNodes) {
                            if (connection.type == 1) {
                                newIdToIgnore.add(connection.connectedToID);
                            } else if (connection.type == 2) {
                                newIdToIgnore.add(connection.OrginserverID);
                            }

                        }
                        String MessageToSend = "-internal get-value " + key + " ";
                        // newIdToIgnore.add(serversToIgnore);
                        for (String old : serversToIgnore) {
                            newIdToIgnore.add(old);
                        }
                        for (String id : newIdToIgnore) {
                            String a = (id + ":");
                            MessageToSend += a;
                        }
                        connectedNode.out.println(MessageToSend);
                        AnswerCounter += 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
            }
        }
        return false;
    }


    static boolean findKey(int key, String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;
        if (DatabaseNode.key == key) {
            return true;
        } else {

            serversToIgnore.add(OrginServerID);
            for (NodeConnection connectedNode : connectedNodes) {
                ArrayList<String> newIdToIgnore = new ArrayList<>();
                boolean sending = true;
                for (String idOfTheOtherServer : serversToIgnore) {
                    if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                        sending = false;
                    }
                }
                if (sending) {
                    try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                        System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                        for (NodeConnection connection : connectedNodes) {
                            if (connection.type == 1) {
                                newIdToIgnore.add(connection.connectedToID);
                            } else if (connection.type == 2) {
                                newIdToIgnore.add(connection.OrginserverID);
                            }

                        }
                        String MessageToSend = "-internal find-key " + key + " ";
                        // newIdToIgnore.add(serversToIgnore);
                        for (String old : serversToIgnore) {
                            newIdToIgnore.add(old);
                        }
                        for (String id : newIdToIgnore) {
                            String a = (id + ":");
                            MessageToSend += a;
                        }
                        connectedNode.out.println(MessageToSend);
                        AnswerCounter += 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
            }
        }
        return false;
    }


    static boolean terminate(int key, String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;
        if (DatabaseNode.key == key) {
            return true;
        } else {

            serversToIgnore.add(OrginServerID);
            for (NodeConnection connectedNode : connectedNodes) {
                ArrayList<String> newIdToIgnore = new ArrayList<>();
                boolean sending = true;
                for (String idOfTheOtherServer : serversToIgnore) {
                    if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                        sending = false;
                    }
                }
                if (sending) {
                    try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                        System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                        for (NodeConnection connection : connectedNodes) {
                            if (connection.type == 1) {
                                newIdToIgnore.add(connection.connectedToID);
                            } else if (connection.type == 2) {
                                newIdToIgnore.add(connection.OrginserverID);
                            }

                        }
                        String MessageToSend = "-internal find-key " + key + " ";
                        // newIdToIgnore.add(serversToIgnore);
                        for (String old : serversToIgnore) {
                            newIdToIgnore.add(old);
                        }
                        for (String id : newIdToIgnore) {
                            String a = (id + ":");
                            MessageToSend += a;
                        }
                        connectedNode.out.println(MessageToSend);
                        AnswerCounter += 1;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
            }
        }
        return false;
    }


    static void getNetworkSize(String OrginServerID, ArrayList<String> serversToIgnore) {

        serversToIgnore.add(OrginServerID);
        for (NodeConnection connectedNode : connectedNodes) {
            ArrayList<String> newIdToIgnore = new ArrayList<>();
            boolean sending = true;
            for (String idOfTheOtherServer : serversToIgnore) {
                if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                    sending = false;
                }
            }
            if (sending) {
                try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                    System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                    for (NodeConnection connection : connectedNodes) {
                        if (connection.type == 1) {
                            newIdToIgnore.add(connection.connectedToID);
                        } else if (connection.type == 2) {
                            newIdToIgnore.add(connection.OrginserverID);
                        }

                    }
                    String MessageToSend = "-internal getNetworkSize";
                    // newIdToIgnore.add(serversToIgnore);
                    for (String old : serversToIgnore) {
                        newIdToIgnore.add(old);
                    }
                    for (String id : newIdToIgnore) {
                        String a = (id + ":");
                        MessageToSend += a;
                    }
                    connectedNode.out.println(MessageToSend);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
        }
    }


    static void getMax(String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;


        serversToIgnore.add(OrginServerID);
        for (NodeConnection connectedNode : connectedNodes) {
            ArrayList<String> newIdToIgnore = new ArrayList<>();
            boolean sending = true;
            for (String idOfTheOtherServer : serversToIgnore) {
                if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                    sending = false;
                }
            }
            if (sending) {
                try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                    System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                    for (NodeConnection connection : connectedNodes) {
                        if (connection.type == 1) {
                            newIdToIgnore.add(connection.connectedToID);
                        } else if (connection.type == 2) {
                            newIdToIgnore.add(connection.OrginserverID);
                        }

                    }
                    String MessageToSend = "-internal get-max ";
                    // newIdToIgnore.add(serversToIgnore);
                    for (String old : serversToIgnore) {
                        newIdToIgnore.add(old);
                    }
                    for (String id : newIdToIgnore) {
                        String a = (id + ":");
                        MessageToSend += a;
                    }
                    connectedNode.out.println(MessageToSend);
                    AnswerCounter += 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
        }
        allAnswers = AnswerCounter;
    }


    static void getMin(String OrginServerID, ArrayList<String> serversToIgnore) {
        DatabaseNode.AnswerCounter = 0;


        serversToIgnore.add(OrginServerID);
        for (NodeConnection connectedNode : connectedNodes) {
            ArrayList<String> newIdToIgnore = new ArrayList<>();
            boolean sending = true;
            for (String idOfTheOtherServer : serversToIgnore) {
                if (toBeIgnored(connectedNode, idOfTheOtherServer)) {
                    sending = false;
                }
            }
            if (sending) {
                try {//uzupełnij listę którą bedę wysyłał o znane mi serwery
                    System.out.println("Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
                    for (NodeConnection connection : connectedNodes) {
                        if (connection.type == 1) {
                            newIdToIgnore.add(connection.connectedToID);
                        } else if (connection.type == 2) {
                            newIdToIgnore.add(connection.OrginserverID);
                        }

                    }
                    String MessageToSend = "-internal get-min ";
                    // newIdToIgnore.add(serversToIgnore);
                    for (String old : serversToIgnore) {
                        newIdToIgnore.add(old);
                    }
                    for (String id : newIdToIgnore) {
                        String a = (id + ":");
                        MessageToSend += a;
                    }
                    connectedNode.out.println(MessageToSend);
                    AnswerCounter += 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Not Sending to: connectedID:" + connectedNode.connectedToID + " OrginID " + connectedNode.OrginserverID);
        }
    }


}

class NodeConnection implements Runnable {
    //niech nodeConnection przy storzeniu wysyła komunkat np. -server, żeby potem wiadomo było co jest serverem co nie
    String address;
    int type; //1: Orgin id: this server 2: orgin id: inny serwer
    int port;

    String OrginserverID;
    String connectedToID = null;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    boolean isNode = false;
    static NodeConnection clientThatSentSentValueRequest;

    public NodeConnection(String address, int port) {
        this.OrginserverID = DatabaseNode.id;
        this.type = 1;
        this.address = address;
        this.port = port;
    }

    public NodeConnection(Socket socket) {
        //System.out.println(this.connectedToID + ":" + this.OrginserverID);
        // this.OrginserverID = DatabaseNode.id;
        this.connectedToID = DatabaseNode.id;
        this.socket = socket;
        this.type = 2;
        address = String.valueOf(socket.getInetAddress());
        port = socket.getPort();
    }


    synchronized void ReciveAnswer() {
        DatabaseNode.AnswerCounter = DatabaseNode.AnswerCounter - 1;
        System.out.println("______________________________________________________OTRZYMANO ODPOWIEDŹ, CZEKAM NA:" + DatabaseNode.AnswerCounter);
    }

    ;

    @Override
    public void run() {
        try {
            if (socket == null) {
                // Initialize the socket with the provided address and port
                socket = new Socket(address, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("-type:server " + OrginserverID);
            } else {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }

            // Perform any additional operations related to the connection here

            System.out.println("Connection established with " + address + " on port " + port);
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("MY ORGIN: " + OrginserverID + "MY CONNECTION: " + connectedToID);
                System.out.println("KVPAIR: " + DatabaseNode.key + ":" + DatabaseNode.value);
                String spilt[] = response.split(" ");
                System.out.println("RECIVED:" + response);
                System.out.println("Oczekuje na odpowiedzi w ilosci: " + DatabaseNode.AnswerCounter);
                switch (spilt[0]) {
                    case "-returnID":
                        this.connectedToID = spilt[1];
                        break;

                    case "killme":
                        DatabaseNode.connectedNodes.remove(this);
                        in.close();
                        out.println();
                        socket.close();
                        break;

                    case "terminate":
                        for (NodeConnection connection : DatabaseNode.connectedNodes) {
                            if (!connection.equals(this)) {
                                connection.out.println("killme");
                            }

                        }
                        out.println("OK");
                        in.close();
                        out.println();
                        socket.close();
                        DatabaseNode.serverSocket.close();
                        break;

                    case "send":
                        String message = spilt[1];
                        // DatabaseNode.GeneralBroadCast(message, this, new ArrayList<>());
                        DatabaseNode.BetterBroadcast(message, DatabaseNode.id, new ArrayList<>());

                    case "get-max":
                        DatabaseNode.getMax(DatabaseNode.id, new ArrayList<>());
                        DatabaseNode.sentRequest = this;
                        DatabaseNode.connectedToClient = true;
                        System.out.println("CZekam na Odpowiedzi w liczbie:  " + DatabaseNode.AnswerCounter);
                        if (DatabaseNode.AnswerCounter == 0) {
                            DatabaseNode.answer(DatabaseNode.key + ":" + DatabaseNode.value);
                            socket.close();
                        }
                        break;

                    case "get-min":
                        DatabaseNode.getMin(DatabaseNode.id, new ArrayList<>());
                        DatabaseNode.connectedToClient = true;
                        DatabaseNode.sentRequest = this;
                        if (DatabaseNode.AnswerCounter == 0) {
                            DatabaseNode.answer(DatabaseNode.key + ":" + DatabaseNode.value);
                            socket.close();
                        }
                        break;

                    case "new-record":
                        String[] newData = spilt[1].split(":");

                        int newKey = Integer.parseInt(newData[0]);
                        int newValue = Integer.parseInt(newData[1]);
                        DatabaseNode.key = newKey;
                        DatabaseNode.value = newValue;
                        out.println("OK");
                        socket.close();
                        break;

                    case "gc":
                        for (NodeConnection con : DatabaseNode.connectedNodes) {
                            out.println(con.address + con.port + "/" + con.socket.getInetAddress() + con.socket.getPort());


                        }
                        socket.close();
                        break;

                    case "-type:server":
                        isNode = true;
                        System.out.println(spilt[1]);
                        if (type == 1) {
                            this.OrginserverID = DatabaseNode.id;
                            this.connectedToID = spilt[1];
                        } else if (type == 2) {
                            this.connectedToID = DatabaseNode.id;
                            this.OrginserverID = spilt[1];

                        }
                        out.println("-returnID "+ DatabaseNode.id);
                        //connectedToID = spilt[1];
                        break;

                    case "set-value":
                        String[] kvpair = spilt[1].split(":");
                        int key = Integer.parseInt(kvpair[0]);
                        int value = Integer.parseInt(kvpair[1]);
                        boolean operation = DatabaseNode.setValue(key, value, DatabaseNode.id, new ArrayList<>());
                        DatabaseNode.connectedToClient = true;
                        DatabaseNode.sentRequest = this;
                        System.out.println("STAN ANSWER COUNTER:" + DatabaseNode.AnswerCounter);
                        if (operation) {
                            DatabaseNode.answer("OK");
                        } else if (DatabaseNode.AnswerCounter == 0) {
                            DatabaseNode.answer("ERROR");
                        }
                        //   if (type == 1) {
                        //     DatabaseNode.sentLastRequest = this.connectedToID;
                        //}else {
                        //  DatabaseNode.sentLastRequest = this.OrginserverID;
                        //}
                        break;

                    case "get-value":
                        int keyToFind = Integer.parseInt(spilt[1]);
                        boolean found = DatabaseNode.getValue(keyToFind, DatabaseNode.id, new ArrayList<>());
                        DatabaseNode.connectedToClient = true;
                        DatabaseNode.sentRequest = this;
                        if (found) {
                            DatabaseNode.answer(keyToFind + ":" + DatabaseNode.value);
                            DatabaseNode.sentRequest.socket.close();
                        } else if (DatabaseNode.AnswerCounter == 0) {
                            DatabaseNode.answer("ERROR");
                        }
                        break;
                    //}else DatabaseNode.answer("ERROR");

                    case "find-key":
                        int keyToFind2 = Integer.parseInt(spilt[1]);
                        boolean found2 = DatabaseNode.findKey(keyToFind2, DatabaseNode.id, new ArrayList<>());
                        DatabaseNode.connectedToClient = true;
                        DatabaseNode.sentRequest = this;
                        if (found2) {

                            String inetAdres = DatabaseNode.serverSocket.getLocalSocketAddress().toString().split("/")[1];
                            DatabaseNode.answer(inetAdres);
                            DatabaseNode.sentRequest.socket.close();
                            break;
                        } else if (DatabaseNode.AnswerCounter == 0) {
                            DatabaseNode.answer("ERROR");
                        }
                        break;

                    case "-internal":
                        switch (spilt[1]) {
                            case "getNetworkSize":
                                DatabaseNode.getNetworkSize(DatabaseNode.id, new ArrayList<>());
                                System.out.println("ODPOWIADAM");
                                out.println("-internal Me");
                                DatabaseNode.sentRequest = this;
                                break;

                            case "set-value":
                                System.out.println("recived internal set-value");
                                DatabaseNode.sentRequest = this;
                                String[] kvp = spilt[2].split(":");
                                int k = Integer.parseInt(kvp[0]);
                                int v = Integer.parseInt(kvp[1]);
                                String[] ignoredValues = spilt[3].split(":");
                                ArrayList<String> ignoredList = new ArrayList<>();
                                for (String iv : ignoredValues) {
                                    ignoredList.add(iv);
                                }
                                if (DatabaseNode.setValue(k, v, DatabaseNode.id, ignoredList)) {
                                    out.println("-internal OK");

                                } else if (DatabaseNode.AnswerCounter == 0) {
                                    out.println("-internal ERROR");
                                }
                                ;

                                // }
                                //else out.println("-internal ERROR");
                                ;
                                break;
                            case "get-value":
                                System.out.println("recived internal get-value");
                                DatabaseNode.sentRequest = this;
                                int ktf = Integer.parseInt(spilt[2]);
                                String[] ignoredValues2 = spilt[3].split(":");
                                ArrayList<String> ignoredList2 = new ArrayList<>();
                                for (String iv : ignoredValues2) {
                                    ignoredList2.add(iv);
                                }
                                if (DatabaseNode.getValue(ktf, DatabaseNode.id, ignoredList2)) {
                                    out.println("-internal FOUND " + DatabaseNode.key + ":" + DatabaseNode.value);
                                } else if (DatabaseNode.AnswerCounter == 0) {
                                    out.println("-internal ERROR");
                                }

                                break;
                            case "get-max":
                                System.out.println("recived internal get-max");
                                DatabaseNode.sentRequest = this;
                                String[] ignoredValues3 = spilt[2].split(":");
                                ArrayList<String> ignoredList3 = new ArrayList<>();
                                for (String iv : ignoredValues3) {
                                    ignoredList3.add(iv);
                                }
                                DatabaseNode.getMax(DatabaseNode.id, ignoredList3);
                                System.out.println("CZekam na " + DatabaseNode.AnswerCounter + "Odpowiedzi");
                                if (DatabaseNode.AnswerCounter == 0) {
                                    out.println("-internal MaxValue " + DatabaseNode.maxKey + " " + DatabaseNode.maxValue);
                                }
                                break;

                            case "get-min":
                                System.out.println("recived internal get-min");
                                DatabaseNode.sentRequest = this;
                                String[] ignoredValues4 = spilt[2].split(":");
                                ArrayList<String> ignoredList4 = new ArrayList<>();
                                for (String iv : ignoredValues4) {
                                    ignoredList4.add(iv);
                                }
                                DatabaseNode.getMin(DatabaseNode.id, ignoredList4);
                                if (DatabaseNode.AnswerCounter == 0) {

                                    System.out.println("ODPOWIADAM");
                                    out.println("-internal MinValue " + DatabaseNode.minKey + " " + DatabaseNode.minValue);
                                }
                                break;

                            case "find-key":
                                int keyToFind3 = Integer.parseInt(spilt[2]);
                                String[] ignoredValues5 = spilt[3].split(":");
                                ArrayList<String> ignoredList5 = new ArrayList<>();
                                for (String iv : ignoredValues5) {
                                    ignoredList5.add(iv);
                                }
                                boolean found3 = DatabaseNode.findKey(keyToFind3, DatabaseNode.id, ignoredList5);
                                DatabaseNode.sentRequest = this;
                                if (found3) {
                                    String returnAdres = DatabaseNode.serverSocket.getLocalSocketAddress().toString().split("/")[1];
                                    out.println("-internal FOUNDKEY " + returnAdres);
                                    break;
                                } else if (DatabaseNode.AnswerCounter == 0) {
                                    out.println("-internal ERROR");
                                }
                                break;

                            case "Me":
                                DatabaseNode.NetworkSize += 1;
                                System.out.println("==============================Network size: " + DatabaseNode.NetworkSize);
                                if (DatabaseNode.sentRequest != null) {
                                    if (DatabaseNode.AnswerCounter == 0) {
                                        if (!DatabaseNode.connectedToClient) {
                                            DatabaseNode.answer("-internal Me");
                                        }
                                    }
                                }

                            case "ERROR":
                                ReciveAnswer();
                                System.out.println("Oczekuje na odpowiedzi w ilosci: " + DatabaseNode.AnswerCounter);
                                if (DatabaseNode.sentRequest != null) {
                                    if (DatabaseNode.AnswerCounter == 0) {
                                        if (!DatabaseNode.connectedToClient) {
                                            DatabaseNode.answer("-internal ERROR");
                                        } else if (DatabaseNode.connectedToClient) {
                                            DatabaseNode.answer(("ERROR"));
                                            DatabaseNode.sentRequest.socket.close();
                                            DatabaseNode.sentRequest = null;
                                            DatabaseNode.connectedToClient = false;
                                        }
                                    }
                                }
                                break;

                            case "OK":
                                //odeślij i przerwij
                                if (DatabaseNode.sentRequest != null) {
                                    if (!DatabaseNode.connectedToClient) {
                                        DatabaseNode.answer("-internal OK");
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.AnswerCounter = 0;
                                    } else if (DatabaseNode.connectedToClient) {
                                        DatabaseNode.answer("OK");
                                        DatabaseNode.sentRequest.socket.close();
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.connectedToClient = false;
                                    }

                                }
                                break;

                            case "FOUND":
                                ReciveAnswer();
                                if (DatabaseNode.sentRequest != null) {
                                    String[] returnedPair = spilt[2].split(":");
                                    String foundKey = returnedPair[0];
                                    String foundValue = returnedPair[1];
                                    if (!DatabaseNode.connectedToClient) {
                                        System.out.println("Odpowiadam");
                                        DatabaseNode.answer("-internal FOUND " + foundKey + ":" + foundValue);
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.AnswerCounter = 0;
                                    } else if (DatabaseNode.connectedToClient) {
                                        System.out.println("Odpowiadam");
                                        DatabaseNode.answer(foundKey + ":" + foundValue);
                                        DatabaseNode.sentRequest.socket.close();
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.connectedToClient = false;
                                    }
                                }
                                break;

                            case "FOUNDKEY":
                                ReciveAnswer();
                                if (DatabaseNode.sentRequest != null) {
                                    String foundInet = spilt[2];
                                    if (!DatabaseNode.connectedToClient) {
                                        DatabaseNode.answer("-internal FOUNDKEY " + foundInet);
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.AnswerCounter = 0;
                                    } else if (DatabaseNode.connectedToClient) {
                                        DatabaseNode.answer(foundInet);
                                        DatabaseNode.sentRequest.socket.close();
                                        DatabaseNode.sentRequest = null;
                                        DatabaseNode.connectedToClient = false;
                                    }
                                }
                                break;

                            case "MaxValue":

                                ReciveAnswer();
                                int RecivedValue = Integer.parseInt(spilt[3]);
                                int RecivedKey = Integer.parseInt(spilt[2]);
                                if (RecivedValue > DatabaseNode.maxValue) {
                                    DatabaseNode.maxValue = RecivedValue;
                                    DatabaseNode.maxKey = RecivedKey;
                                    System.out.println("NOWE Min VALUE: " + DatabaseNode.maxValue);
                                }
                                System.out.println("Oczekuje na odpowiedzi w ilosci: " + DatabaseNode.AnswerCounter);
                                if (DatabaseNode.sentRequest != null) {
                                    if (DatabaseNode.AnswerCounter == 0) {
                                        if (!DatabaseNode.connectedToClient) {
                                            System.out.println("ODPOWIADAM");
                                            DatabaseNode.answer("-internal MaxValue " + DatabaseNode.maxKey + " " + DatabaseNode.maxValue);
                                            DatabaseNode.sentRequest = null;
                                            DatabaseNode.AnswerCounter = 0;
                                            DatabaseNode.minValue = DatabaseNode.value;
                                            DatabaseNode.maxValue = DatabaseNode.value;
                                        } else if (DatabaseNode.connectedToClient) {
                                            System.out.println("ODPOWIADAM");
                                            DatabaseNode.answer(DatabaseNode.maxKey + ":" + DatabaseNode.maxValue);
                                            DatabaseNode.sentRequest.socket.close();
                                            DatabaseNode.sentRequest = null;
                                            DatabaseNode.AnswerCounter = 0;
                                            DatabaseNode.minValue = DatabaseNode.value;
                                            DatabaseNode.maxValue = DatabaseNode.value;
                                        }
                                    }
                                }
                                break;


                            case "MinValue":
                                ReciveAnswer();
                                int RecivedValue1 = Integer.parseInt(spilt[3]);
                                int RecivedKey2 = Integer.parseInt(spilt[2]);
                                if (RecivedValue1 < DatabaseNode.minValue) {
                                    DatabaseNode.minValue = RecivedValue1;
                                    DatabaseNode.minKey = RecivedKey2;
                                    System.out.println("NOWE Min VALUE: " + DatabaseNode.minValue);
                                }
                                System.out.println("Oczekuje na odpowiedzi w ilosci: " + DatabaseNode.AnswerCounter);
                                if (DatabaseNode.sentRequest != null) {
                                    if (DatabaseNode.AnswerCounter == 0) {
                                        if (!DatabaseNode.connectedToClient) {
                                            System.out.println("ODPOWIADAM z wartością minimalną : " + DatabaseNode.minValue);
                                            DatabaseNode.answer("-internal MinValue " + DatabaseNode.minKey + " " + DatabaseNode.minValue);
                                            DatabaseNode.sentRequest = null;
                                            DatabaseNode.AnswerCounter = 0;
                                            DatabaseNode.minValue = DatabaseNode.value;
                                            DatabaseNode.maxValue = DatabaseNode.value;
                                        } else if (DatabaseNode.connectedToClient) {
                                            System.out.println("ODPOWIADAM z wartością minimalną : " + DatabaseNode.minValue);
                                            DatabaseNode.answer( DatabaseNode.minKey + ":" + DatabaseNode.minValue);
                                            DatabaseNode.sentRequest.socket.close();
                                            DatabaseNode.sentRequest = null;
                                            DatabaseNode.AnswerCounter = 0;
                                            DatabaseNode.minValue = DatabaseNode.value;
                                            DatabaseNode.maxValue = DatabaseNode.value;
                                        }
                                    }
                                }
                                break;

                        }
                        break;
                }
                if (socket.isClosed()) {
                    in.close();
                    out.close();
                    // DatabaseNode.connectedNodes.remove(this);
                }

            }

        } catch (IOException e) {
            System.err.println("Error establishing connection: " + e.getMessage());
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}



