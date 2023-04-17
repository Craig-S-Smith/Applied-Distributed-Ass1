/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author diamo
 */
public class Server extends JFrame implements ActionListener, Runnable {
    
    // If recall has been called
    static boolean recallStatus = false;
    
    // ArrayLists for Drone and Fire Objects
    static ArrayList<DroneDetails> drones = new ArrayList<>();
    static ArrayList<FireDetails> fires = new ArrayList<>();
    
    // GUI Setup, all elements of GUI declared
    private JLabel titleText = new JLabel("Drone Server");
    private JTextArea outputText = new JTextArea(30, 35);
    private JLabel headingText = new JLabel("               Server Output              ");
    private JLabel mapText = new JLabel("               Drone and Fire Map              ");
    private JButton deleteButton = new JButton("Delete Fire");
    private JButton recallButton = new JButton("Recall Drones");
    private JButton moveButton = new JButton("Move Drone");
    private JButton shutDownButton = new JButton("Shut Down");
    private JScrollPane scrollPane; // Scroll pane for the text area
    private MapPanel mapPanel;
    
    public class MapPanel extends JPanel {

        private ArrayList<DroneDetails> drones;
        private ArrayList<FireDetails> fires;

        public MapPanel(ArrayList<DroneDetails> drones, ArrayList<FireDetails> fires) {
            this.drones = drones;
            this.fires = fires;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set background color of map panel
            setBackground(Color.WHITE);
            
            // Draw drones as blue circles with drone id
            for (DroneDetails p : drones) {
                if (p.getActive()) {
                    int x = (p.getX_pos() + 100) * 2;
                    int y = (p.getY_pos() + 100) * 2;
                    int size = 10;
                    g.setColor(Color.BLUE);
                    g.fillOval(x - size/2, y - size/2, size, size);
                    g.setColor(Color.BLACK);
                    g.drawString("Drone " + p.getId(), x - 15, y);
                }
            }
            
            // Draw fires as red circles with fire id and severity
            for (FireDetails p : fires) {
                int x = (p.getX_pos() + 100) * 2;
                int y = (p.getY_pos() + 100) * 2;
                int severity = p.getSeverity();
                int size = 10;
                g.setColor(Color.RED);
                g.fillOval(x - size/2, y - size/2, size, size);
                g.setColor(Color.BLACK);
                g.drawString("Fire " + p.getId() + " (" + severity + ")", x - 30, y - 5);
            }
        }
    }
    
    Server() {
        // Sets settings for java swing GUI Frame
        super("Server GUI");
        
        // Sets font for title
        titleText.setFont(new Font("Arial", Font.PLAIN, 30));
        
        // Sets X button to do nothing, shut down should be used to exit
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Other GUI settings
        setSize(1000, 850);
        this.setLayout(new FlowLayout());
        this.setResizable(false);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(1000, 60));
        buttonPanel.add(deleteButton);
        buttonPanel.add(recallButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(shutDownButton);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel();
        
        // Output Panel
        JPanel outputPanel = new JPanel();
        outputPanel.add(outputText);
        
        scrollPane = new JScrollPane(outputText);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputPanel.add(scrollPane);
        
         // Map Panel
        mapPanel = new MapPanel(drones, fires);
        mapPanel.setPreferredSize(new Dimension(400, 400));
        
        // Add panels and text to GUI
        add(titleText);
        add(buttonPanel);
        
        add(mapText);
        add(headingText);
        bottomPanel.add(mapPanel);
        bottomPanel.add(outputPanel);
        
        add(bottomPanel);
        
        // Makes the GUI visible
        this.setVisible(true);
        
        // Action Listeners for Buttons
        deleteButton.addActionListener(this);
        recallButton.addActionListener(this);
        moveButton.addActionListener(this);
        shutDownButton.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        // This runs when an object action is clicked
        // Gets the name of the object clicked and finds the case
        // Runs the corresponding method and breaks the switch
        String actionString=e.getActionCommand();
        switch(actionString) {
            case "Delete Fire":
                deleteFire();
                break;
                
            case "Recall Drones":
                recallDrones();
                break;
                
            case "Move Drone":
                moveDrone();
                break;
                
            case "Shut Down":
                shutDown();
                break;
        }
    }
    
    public static void main(String[] args) {
        // Calls function to read data from files
        readData();
        
        // Starts thread to update map and GUI because that's how it works apparently
        Server obj = new Server();
        Thread thread = new Thread(obj);
        thread.start();
        
        // Sets up connection listener with port 8888
        try {
            int serverPort = 8888;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            
            // Constantly on loop, checks for connections and sends connections to new thread
            while(true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
            
        }   catch(IOException e) {System.out.println("Listen Socket : " + e.getMessage());}
    }
    
    static boolean ifRecall() {
        // Returns if the recall status is true
        return recallStatus;
    }
    
    static void addDrone(DroneDetails tempDrone) {
        // Assumes drone is new until found otherwise
        boolean newDrone = true;
        
        /* Checks each drone object in the drones ArrayList to see
        if the ID is already present, if it is just updates that drone's
        Name, Position and Active Status. If this happens says the drone
        is not new.
        */
        for (DroneDetails p : drones) {
                if (p.getId() == tempDrone.getId()) {
                    p.setName(tempDrone.getName());
                    p.setX_pos(tempDrone.getX_pos());
                    p.setY_pos(tempDrone.getY_pos());
                    p.setActive(tempDrone.getActive());

                    newDrone = false;
                    break;
                }
        }
        
        // If the drone is new, creates the drone object and adds it to the arraylist
        if (newDrone) {
            DroneDetails drone = new DroneDetails(tempDrone.getId(), tempDrone.getName(), tempDrone.getX_pos(), tempDrone.getY_pos(), true);
            drones.add(drone);
        }
        
        // System.out.println(drones.size() + " Drone Objects");
    }
    
    static void addFire(FireDetails tempFire) {
        
        /*
        Assigns ID to the new fire object then adds it to the ArrayList
        If the fire ArrayList is empty it will just give the Fire an ID of 0
        If it's not it'll find the highest Fire ID and set it to one above that
        Then makes a fire object and adds it to the arraylist and prints fire details
        */
        if (fires.isEmpty()) {
            FireDetails fire = new FireDetails(0, tempFire.getX_pos(), tempFire.getY_pos(), tempFire.getDroneId(), tempFire.getSeverity());
            fires.add(fire);
            System.out.println(fire.toString());
        } else {
            int max = 0;
            
            for (FireDetails p : fires) {
                if (p.getId() > max) {
                    max = p.getId();
                }
            }
            
            int fireId = max + 1;
            
            FireDetails fire = new FireDetails(fireId, tempFire.getX_pos(), tempFire.getY_pos(), tempFire.getDroneId(), tempFire.getSeverity());
            fires.add(fire);
            System.out.println(fire.toString());
        }
    }
    
    static void readData() {
        // Reads ArrayList from binary file drones.bin
        try (
            FileInputStream fileIn = new FileInputStream("drones.bin");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            
            ArrayList<DroneDetails> tempDrones = (ArrayList<DroneDetails>) objectIn.readObject();
            /* If the file is empty the tempDrones arraylist will be null
            If this is the case it will not set this temp arraylist to be
            the main arraylist. */
            if (tempDrones != null) {
                drones = tempDrones;
            }
            
        } catch(EOFException | FileNotFoundException e) {
        } catch(IOException e) {e.printStackTrace();
	} catch(ClassNotFoundException ex){ex.printStackTrace();
        }
        
        // Reads file, each variable it checks is seperated by the delimiter, the comma
        // Gets variables from each line and adds it to a fire object then the ArrayList
        String line = "";
        String csvDelimiter = ",";
        
        try (BufferedReader br = new BufferedReader(new FileReader("fires.csv"))) {
         
         // Read remaining lines
         while ((line = br.readLine()) != null) {
            String[] data = line.split(csvDelimiter);
            int id = Integer.parseInt(data[0]);
            int x_pos = Integer.parseInt(data[1]);
            int y_pos = Integer.parseInt(data[2]);
            int droneId = Integer.parseInt(data[3]);
            int severity = Integer.parseInt(data[4]);

            FireDetails fire = new FireDetails(id, x_pos, y_pos, droneId, severity);
            fires.add(fire);
         }
        } catch (IOException e) {
           e.printStackTrace();
        } catch (NumberFormatException e) { e.printStackTrace();
        }
    }
    
    static void saveData() {
        // Saves drones arraylist to drones.bin
        try (
            FileOutputStream fileOut = new FileOutputStream("drones.bin");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)
            ) {
            objectOut.writeObject(drones);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Saves each object in fires ArrayList to fires.csv
        // Uses object .toCSV() to format the string with variables having commas between
        try {
            FileWriter writer = new FileWriter("fires.csv", false);
            for (FireDetails p : fires) {
                writer.write(p.toCSV() + "\n");
            }
            writer.close();
        } catch(IOException e) {e.printStackTrace();
        }
    }
    
    public void deleteFire() {
        // Triggered by Delete Fire Button
        // intId is the id that'll be entered
        int intId = -1;
        
        /*
        Opens Option Pane prompting for a Fire ID
        If cancel is pressed, null will be returned causing the loop to break
        otherwise it'll attempt to parse the ID to int, if this fails the user will be reprompted after an error message
        */
        while (true) {
            String enteredId = JOptionPane.showInputDialog(null, "Enter a Fire ID");
            if (enteredId == null) {
                break;
            }
            try {
                intId = Integer.parseInt(enteredId);
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }
        
        // Iterator goes through ArrayList until it finds the ID, removes the object from ArrayList
        // Originally used a for loop, didn't work for some reason
        Iterator<FireDetails> iterator = fires.iterator();
            while (iterator.hasNext()) {
                FireDetails p = iterator.next();
                if (p.getId() == intId) {
                    iterator.remove();
            }
        }
    }
    
    public void recallDrones() {
        // Sets recall status to true, triggered by recall button
        recallStatus = true;
    }
    
    public void moveDrone() {
        
    }
    
    public void shutDown() {
        /*
        Sets recall status to true
        drones active is set to false before each loop
        Checks each object of the ArrayList to see if a drone is still active
        If one is, dronesActive is set to true
        
        If dronesActive is false that means there's no drones active
        The program saves that data (saveData()) and exits
        
        If there is a drone still active it will loop until no drones are active
        */
        recallStatus = true;
        boolean dronesActive;
        while (true) {
            dronesActive = false;
            for (DroneDetails p : drones) {
                if (p.getActive()) {
                    dronesActive = true;
                }
            }
            
            if (!dronesActive) {
                saveData();
                System.exit(0);
            }
        }
    }

    @Override
    public void run() {
        // Runs constantly
        while (true) {
            
            // Repaints mapPanel
            mapPanel.repaint();
            
            try {
                // Outputs current data
                outputText.setText("Current Data");
                
                // If there's any active drones it'll add drone data heading
                for (DroneDetails p : drones) {
                    if (p.getActive()) {
                        outputText.append("\nDrone Data");
                        break;
                    }
                }
                
                // Goes through drones ArrayList, appends any active drones to GUI text area
                for (DroneDetails p : drones) {
                    if (p.getActive()) {
                        outputText.append("\n");
                        outputText.append(p.toString());
                    }
                }
                
                // Checks if fire is empty, if not adds fire data heading
                if (!fires.isEmpty()) {
                    outputText.append("\nFire Data");
                }
                
                // Goes through fires ArrayList, appends any active drones to GUI text area
                for (FireDetails p : fires) {
                    outputText.append("\n");
                    outputText.append(p.toString());
                }
                
                // Sleeps for 10 seconds before looping
                Thread.sleep(10000);
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class Connection extends Thread {
    // Sets up input and output streams for socket
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;
    
    public Connection (Socket aClientSocket) {
        
        // Assigns streams to the socket and starts the thread run()
        try {
            clientSocket = aClientSocket;
            in = new ObjectInputStream( clientSocket.getInputStream());
            out =new ObjectOutputStream( clientSocket.getOutputStream());
            this.start();
	} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
    
    public void run() {
        try {
            
            String message = "";
            String clientMessage = "";
            
            // Gets drone object from client and adds it to tempDrone object
            DroneDetails tempDrone = (DroneDetails)in.readObject();
            
            // Confirm drone object
            message = "confirmed";
            out.writeObject(message);
            
            // Receives how many fires there are and confirms receival
            Integer numFires = (Integer)in.readObject();
            out.writeObject(message);
            
            // Loops for how many fires there are and receives the fire objects
            // Sends fire object to addFire(); for it to be added, sends confirmation message
            if (numFires > 0) {
                for (int i = 0; i < numFires; i++) {
                    FireDetails tempFire = (FireDetails)in.readObject();
                    Server.addFire(tempFire);
                    message = "confirmed";
                    out.writeObject(message);
                }
            }
            
            // If a Recall is active it will respond to the client saying so
            if (Server.ifRecall()) {
                message = "recall";
                out.writeObject(message);
                clientMessage = (String)in.readObject();
                if (clientMessage.equals("Recall Confirmed")) {
                    // If drone confirms recall, set the drone active to false
                    tempDrone.setActive(false);
                }
            } else {
                // Otherwise just confirms to the client it received the object
                message = "confirmed";
                out.writeObject(message);
            }
            
            // Sends tempDrone to the addDrone function to get it in the ArrayList
            Server.addDrone(tempDrone);
            
            Server.saveData();
            
            System.out.println(tempDrone);
            
            System.out.println("There are " + numFires + " new fires.");
            System.out.println("There are " + Server.fires.size() + " fires.");
            
        }catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {System.out.println("readline:"+e.getMessage());
	} catch(ClassNotFoundException ex){ ex.printStackTrace();
	} finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}
    }
}
