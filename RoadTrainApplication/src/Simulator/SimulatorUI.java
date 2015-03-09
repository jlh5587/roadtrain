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

        // settings for relative positioning
        int initialXPos = FRAME_WIDTH / 3;
        int currentXPos = initialXPos;
        int currentYPos = FRAME_HEIGHT / 2;
        int frontPos = 0;

        // make nodes green
        g.setColor(Color.green);

        // draw all the nodes
        for (VirtualVanetNode vvn : nodeList) {
            // if first node, this takes role of "front" node
            if (vvn.nodeIndex == 1) {
                frontPos = vvn.x;
            }
            // current Y is relative to front node
            currentXPos = initialXPos + vvn.x - frontPos;
            g.fillOval(currentXPos, currentYPos, 10, 10);
        }
    }

    public void update_ui(ArrayList<VirtualVanetNode> nodeListIn) {
        // point internal node list to the new updated node list.
        this.nodeList = nodeListIn;
        // have it paint components based on the node list
        repaint();
    }


}
