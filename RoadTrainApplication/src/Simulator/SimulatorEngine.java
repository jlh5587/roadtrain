package Simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Carlos Lemus
 * Created on 2/27/2015
 */
public class SimulatorEngine {

    private ArrayList<VirtualVanetNode> vvnList;
    private String fileName;

    public SimulatorEngine(String fileName) {
        this.fileName = fileName;
    }

    public void parseFile() throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader fileReader = new BufferedReader(fr);
        vvnList = new ArrayList<VirtualVanetNode>();

        String line;

        while ((line = fileReader.readLine()) != null) {
            parseLine(line);
        }
        fr.close();
    }

    public ArrayList<VirtualVanetNode> getVvnList() {
        return vvnList;
    }

    private void parseLine(String line) {
        VirtualVanetNode vvn = new VirtualVanetNode();
        //either space characters or comma followed by space
        //TODO: include any space spaces after comma
        String[] tokens = line.split("(\\s+)|(, )");


        //tokens[0] is discarded because it's just "Node" according to specification
        vvn.nodeIndex = (short) Integer.parseInt(tokens[1]);
        vvn.networkName = tokens[2];
        vvn.port = (short) Integer.parseInt(tokens[3]);
        vvn.x = (short) Integer.parseInt(tokens[4]);
        vvn.y = (short) Integer.parseInt(tokens[5]);
        vvn.links = new ArrayList<Integer>();

        try {
            if (tokens[6].compareTo("links") == 0) {
                for (int i = 7; i < tokens.length; i++) {
                    vvn.links.add((Integer) Integer.parseInt(tokens[i]));
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            //do nothing since it means that there simply aren't any links attached.
        }

        vvnList.add(vvn);
    }
}
