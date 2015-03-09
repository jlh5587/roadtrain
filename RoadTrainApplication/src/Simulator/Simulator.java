//package Simulator;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Carlos Lemus
 * Created on 2/27/2015
 */

public class Simulator {

    static SimulatorEngine seng;
    static SimulatorUI simUI;

    public static void main(String[] args) {
        String filePath = "";
        int update_interval;

        // user doesn't provide a file path
        try {
            filePath = args[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Please provide a path to the config file as " +
                    "a first argument");
            System.exit(1);
        }

        // if user doesn't provide an update interval, set to 1sec
        try {
            update_interval = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            update_interval = 1000;
        }

        seng = new SimulatorEngine(filePath);
        simUI = new SimulatorUI();
        ArrayList<VirtualVanetNode> vvnList;

        while (true) {
            vvnList = updateNodes();
            simUI.update_ui(vvnList);
            try {
                Thread.sleep(update_interval);
            } catch (InterruptedException ex) {
                // do nothing because YOLO
            }
        }
    }

    private static ArrayList<VirtualVanetNode> updateNodes() {
        try {
            seng.parseFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ArrayList<VirtualVanetNode> vvnList = seng.getVvnList();

        return vvnList;
    }
}
