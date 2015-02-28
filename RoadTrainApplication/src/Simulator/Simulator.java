package Simulator;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Carlos Lemus
 * Created on 2/27/2015
 */
public class Simulator {
    public static void main(String[] args) {
        SimulatorEngine seng = new SimulatorEngine("./Simulator/test.txt");
        SimulatorUI sui = new SimulatorUI();

        try {
            seng.parseFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ArrayList<VirtualVanetNode> vvnList = seng.getVvnList();

        sui.update_ui(vvnList);
    }
}
