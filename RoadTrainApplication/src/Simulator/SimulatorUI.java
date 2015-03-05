package Simulator;

import java.util.ArrayList;

/**
 * Author: Carlos Lemus
 * Created on 2/28/2015
 */
public class SimulatorUI {

    public void update_ui(ArrayList<VirtualVanetNode> nodeList) {
        for (VirtualVanetNode vvn : nodeList) {
            System.out.print("Node ");
            System.out.print(vvn.nodeIndex + " ");
            System.out.print(vvn.networkName);
            System.out.print(":");
            System.out.print(vvn.port);
            System.out.print(" links ");
            for (Integer i : vvn.links) {
                System.out.print(i);
            }
            System.out.print("\n");
        }
        System.out.println("--------------------------------------");
    }


}
