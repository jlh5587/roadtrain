package Simulator;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Carlos Lemus on 2/27/15.
 */
public class SimulatorEngine {

    private ArrayList vvnList;
    private String fileName;

    public SimulatorEngine(String fileName) {
        this.fileName = fileName;
        this.vvnList = new ArrayList();
    }

    public void parseFile() throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader fileReader = new BufferedReader(fr);

        String line;

        while ((line = fileReader.readLine()) != null) {
            parseLine(line);
        }
    }

    private void parseLine(String line) {
        VirtualVanetNode vvn = new VirtualVanetNode();
        String[] tokens = line.split("\\s+");


        //tokens[0] is discarded because it's just "Node" according to specification
        vvn.nodeIndex = (short)Integer.parseInt(tokens[1]);
        vvn.networkName = tokens[2];
        vvn.port = (short)Integer.parseInt(tokens[3]);
        vvn.x = (short)Integer.parseInt(tokens[4]);
        vvn.y = (short)Integer.parseInt(tokens[5]);
        vvn.links = new ArrayList();

        try {
            if (tokens[6].compareTo("links") == 0) {
                for (int i = 7; i < tokens.length; i++) {
                    vvn.links.add((short)Integer.parseInt(tokens[i]));
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            //do nothing since it means that there simply aren't any links attached.
        }


        vvnList.add(vvn);
    }
}