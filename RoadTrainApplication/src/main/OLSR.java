import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class OLSR {

    ArrayList<Integer> twoHopNodes;

    Hashtable<Integer, TableEntry> cacheTable;
    Hashtable<Integer, NeighborTableEntry> neighborTable;
    Hashtable<Integer, ComputerInfo> computerInfoTable;
    File configFile;
    int user, currentSeqNum, numMessageCreated;
    int currentSender, currentLastHop, currentTimesForwarded, currentPacketType;
    String currentMessage;
    boolean listen;
    DatagramSocket socket;
    int messageRec, droppedPackets;

    public OLSR(int user, int port, File configFile) throws SocketException {

        cacheTable = new Hashtable<Integer, TableEntry>();
        neighborTable = new Hashtable<Integer, NeighborTableEntry>();
        this.configFile = configFile;
        this.user = user;
        currentSeqNum = 0;
        numMessageCreated = 1;
        listen = true;
        socket = new DatagramSocket(port);
    }

    //This is what the app layer will call to listen for a message
    public String listenForMessage() {
        this.listenForMessage(2);
    }

    //This is what the MPR will use to listen for a message
    public String listenForMessage(int i) {
        byte[] recieved = new byte[4096];

        DatagramPacket receivePacket = new DatagramPacket(recieved, recieved.length);
        try {
            socket.setSoTimeout(1000);
            socket.receive(receivePacket);

            String packetInfo = new String(receivePacket.getData());
            parsePacket(packetInfo);
            if (currentPacketType == 1 && i == 1) {
                //Here is where you will call the method that handles the hello message
                return currentMessage;
            } else if (currentPacketType == 2) {
                //check if you are an MPR and then forward if necessary
                /*IF MPR -> sendMessageAsMPR(packetInfo)*/
                if (cacheTable.get(currentSender).getSeqNum() <= currentSeqNum) {
                    cacheMessage();
                }
                return currentMessage;
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }


        return "";
    }

    //This is the broadcast method that is called from the app layer
    public void broadcast(String message) {
        String packetInfo = "2," + user + "," + numMessageCreated + "," + user + "," + 0 + "," + message;
        numMessageCreated++;

        //We will need to add a check to see if this user is an MPR. If they are, then we will
        //use a different method to send the packet.
        sendNewMessage(packetInfo);
    }

    //This is called to send the hello message beacon
    public void sendHello(String helloMessage) {
        ArrayList<Integer> connects = findForwardConnections();

        for (int i = 0; i < connects.size(); i++) {
            if (computerInfoTable.containsKey(connects.get(i))) {
                sendHelloMessage(computerInfoTable.get(connects.get(i)), helloMessage);
            } else {
                computerInfoTable.put(connects.get(i), compInfo(connects.get(i)));
                sendHelloMessage(computerInfoTable.get(connects.get(i)), helloMessage);
            }

        }
    }

    public void setListen(boolean listen) {
        this.listen = listen;
    }

    public void closeSocket() {
        socket.close();
    }


    //Private methods used to cache, parse and send packets

    private void cacheMessage() {
        cacheTable.get(currentSender).setLastHop(user);
        cacheTable.get(currentSender).setMessage(currentMessage);
        cacheTable.get(currentSender).setNumOfForwards(currentTimesForwarded + 1);
        messageRec++;
        if (cacheTable.get(currentSender).getSeqNum() < currentSeqNum - 1) {
            droppedPackets++;
            System.out.println("Num of packets recieved: " + messageRec);
            System.out.println("Dropped Packets: " + droppedPackets);
        }
        cacheTable.get(currentSender).setSeqNum(currentSeqNum);
    }

    private void parsePacket(String packetInfo) {
        Scanner packetScanner = new Scanner(packetInfo);
        packetScanner.useDelimiter(",");

        //The packet contains PacketType sender, lastHop, times forwarded, message
        // If packet type = 1 then hello message, if 2 then other message
        currentPacketType = packetScanner.nextInt();
        currentSender = packetScanner.nextInt();
        currentSeqNum = packetScanner.nextInt();
        currentLastHop = packetScanner.nextInt();
        currentTimesForwarded = packetScanner.nextInt();
        currentMessage = packetScanner.next();

        packetScanner.close();
    }


    private void sendHelloMessage(ComputerInfo c, String helloMessage) {
        try {

            //The Hello String should be added to to send the appropriate information to the other nodes.
            String packetInfo = "1," + user + "," + currentSeqNum + "," + user + "," + 0 + ", HELLO, "
            user, "," + helloMessage;

            byte[] sendData = new byte[4096];
            sendData = packetInfo.getBytes();
            String compName = c.getName();
            int port = c.getPort();

            InetAddress IPAddress;
            try {
                IPAddress = InetAddress.getByName(compName);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                socket.send(sendPacket);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } catch (Exception e) {
            //	System.out.println(packetInfo);
            //	System.out.println(e.toString());
        }

    }

    //Sends newMessages to MPR's based on status in neighbor table.

    private void sendMessageAsMPR(String packetInfo) {

        try {
            byte[] sendData = new byte[4096];
            sendData = packetInfo.getBytes();


            String compName;
            int port;

            Set<Integer> keys = neighborTable.keySet();
            for (Integer key : keys) {
                ComputerInfo c;
                if (computerInfoTable.containsKey(key)) {
                    c = computerInfoTable.get(key);
                } else {
                    computerInfoTable.put(key, compInfo(key));
                    c = computerInfoTable.get(key);
                }

                compName = c.getName();
                port = c.getPort();

                //For testing purposes. These IP addresses will need to come from the config file.
                InetAddress IPAddress;
                try {
                    IPAddress = InetAddress.getByName(compName);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    socket.send(sendPacket);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            System.out.println(packetInfo);
            System.out.println(e.toString());
        }
    }

    private void sendNewMessage(String packetInfo) {

        try {
            byte[] sendData = new byte[4096];
            sendData = packetInfo.getBytes();

            ArrayList<Integer> mprs = new ArrayList<Integer>();

            Set<Integer> keys = neighborTable.keySet();
            for (Integer key : keys) {
                if (neighborTable.get(key).status.equals("MPR")) {
                    mprs.add(key);
                }
            }


            String compName;
            int port;

            for (int i = 0; i < mprs.size(); i++) {

                ComputerInfo c;
                if (computerInfoTable.containsKey(mprs.get(i))) {
                    c = computerInfoTable.get(mprs.get(i));
                } else {
                    computerInfoTable.put(mprs.get(i), compInfo(mprs.get(i)));
                    c = computerInfoTable.get(mprs.get(i));
                }

                compName = c.getName();
                port = c.getPort();

                //For testing purposes. These IP addresses will need to come from the config file.
                InetAddress IPAddress;
                try {
                    IPAddress = InetAddress.getByName(compName);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    socket.send(sendPacket);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            System.out.println(packetInfo);
            System.out.println(e.toString());
        }
    }


    //Finds the connections based on the config file
    private ArrayList<Integer> findForwardConnections() {
        ArrayList<Integer> toForward = new ArrayList<Integer>();

        Scanner scanFile;
        try {
            scanFile = new Scanner(configFile);

            while (scanFile.hasNext()) {
                String line = scanFile.nextLine();
                Scanner scanLine = new Scanner(line);


                int car = scanLine.nextInt();

                if (car == user) {
                    while (scanLine.hasNext()) {
                        if (scanLine.next().equals("links")) {
                            while (scanLine.hasNext()) {
                                toForward.add(scanLine.nextInt());

                            }

                        }
                    }
                    break;
                }


                scanLine.close();
            }

            scanFile.close();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //	e.printStackTrace();
        }

        return toForward;

    }


    private ComputerInfo compInfo(int id) {
        String compNm = "";
        Scanner scanFile;
        try {
            scanFile = new Scanner(configFile);

            while (scanFile.hasNext()) {
                String line = scanFile.nextLine();

                Scanner scanLine = new Scanner(line);


                int car = scanLine.nextInt();

                if (car == id) {
                    compNm = scanLine.next();
                    int port = scanLine.nextInt();
                    scanLine.close();
                    return (new ComputerInfo(compNm, port));

                }

                scanLine.close();
            }

            scanFile.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Hello Message Handling *
     */
    public void parseHelloMessage(byte[] hm) {

        //Parse hello message info
        HelloMessage currentHello = bytesToHello(hm);
        Integer helloSender = currentHello.getSenderName();
        ArrayList<Integer, NeighborTableEntry> helloNeighbors = currentHello.getNodeNeighbors();

        // current HELLO neighbor is unidirectional until self found in HELLO neighbor's list.
        NeighborTableEntry tableEntry = new NeighborTableEntry(NeighborStatus.UNIDIRECTIONAL);

        Set<Integer> keys = helloNeighbors.keySet();
        ArrayList<Integer> contributedTwoHopNodes = new ArrayList<Integer>();

        //first, determine if node is bidirectional
        if (helloNeighbors.containsKey(user)) {
            tableEntry.setStatus(NeighborStatus.BIDIRECTIONAL);
            if (helloNeighbors.get(user).getStatus() == NeighborStatus.MPR) {
                //update MPR selector table
            }
        } else {
            tableEntry.setStatus(NeighborStatus.UNIDIRECTIONAL);
        }

        if (tableEntry.getStatus(NeighborStatus(BIDIRECTIONAL))) {
            for (Integer key : keys) {
                //if it's a new two-hop node, add to contributed list.
                if (helloNeighbors.get(key).getStatus() == NeighborStatus.BIDIRECTIONAL
                        && !neighborTable.containsKey(key)
                        && !twoHopNodes.contains(key)) {
                    twoHopNodes.add(key);
                    contributedTwoHopNodes.add(key);
                }
            }
        }

        //if it contributed any new 2-hop nodes, it's an MPR. Add two-hop nodes that it gives access to.
        if (contributedTwoHopNodes.size() > 0) {
            tableEntry.setStatus(NeighborStatus.MPR);
            tableEntry.setTwoHopNeighbors(contributedTwoHopNodes.toArray(new Integer[contributedTwoHopNodes.size()]));
        }

        //update table entry
        neighborTable.put(helloSender, neighborEntry);
    }

    public HelloMessage bytesToHello(byte[] helloData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(helloData);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            HelloMessage hm_out = in.readObject();
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
                return o;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //return empty hello object in case data can't be parsed.
        return new HelloMessage(0, null);
    }

    private byte[] helloToBytes(HelloMessage hello) {
        try {
            byte[] objectBytes;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(hello);
            objectBytes = bos.toByteArray();

            out.close();
            bos.close();

            return objectBytes;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // in case converting object fails, return empty byte array.
        return new byte[0];
    }

}
