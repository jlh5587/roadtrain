package Simulator;

import java.io.IOException;

/**
 * Created by carloslemus on 2/27/15.
 */
public class Simulator {
    public static void main(String[] args) {
        SimulatorEngine seng = new SimulatorEngine("./Simulator/test.txt");

        try {
            seng.parseFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
