//package Simulator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Author: Carlos Lemus
 * Created on 2/28/2015
 */
public class SimulatorUI extends JPanel {


    private ArrayList<VirtualVanetNode> nodeList;

    private JFrame frame;
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;

    public SimulatorUI() {
        this.setBackground(Color.BLACK);

        frame = new JFrame();
        frame.setTitle("VANET Simulator");
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

	int initialYPos = FRAME_HEIGHT/3;

	System.out.println("Initial Y: " + initialYPos);

        int currentYPos = initialYPos;
        int currentXPos = FRAME_WIDTH/2;
        int frontPos = 0;

        g.setColor(Color.green);

        for (VirtualVanetNode vvn : nodeList) {
            if (vvn.nodeIndex == 1) {
                frontPos = vvn.y;
                System.out.println("Front node position: " + frontPos);
            }
	//Current position is initial Y position plus difference with front node
		System.out.println("vvn.y - frontPos = " + (vvn.y - frontPos));
            currentYPos = initialYPos + vvn.y - frontPos;
		System.out.println("currentYPos = " + (currentYPos));
            g.fillOval(currentXPos, currentYPos, 10, 10);

            System.out.println("\nDrawing " + vvn.nodeIndex + " @ " + currentYPos);
        }
    }

    public void update_ui(ArrayList<VirtualVanetNode> nodeListIn) {

        // point internal node list to the new updated node list.
        this.nodeList = nodeListIn;
        // have it paint components based on the node list
        repaint();

//
//        for (VirtualVanetNode vvn : nodeList) {
//            System.out.print("Node ");
//            System.out.print(vvn.nodeIndex + " ");
//            System.out.print(vvn.networkName);
//            System.out.print(":");
//            System.out.print(vvn.port);
//            System.out.print(" links ");
//            for (Integer i : vvn.links) {
//                System.out.print(i);
//            }
//            System.out.print("\n");
//        }
//        System.out.println("--------------------------------------");
    }


}
