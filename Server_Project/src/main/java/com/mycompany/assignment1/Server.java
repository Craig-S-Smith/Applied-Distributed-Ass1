/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

/**
 *
 * @author diamo
 */
public class Server extends JFrame implements ActionListener {

    static boolean recallStatus = false;
    static ArrayList<DroneDetails> drones = new ArrayList<>();
    static ArrayList<FireDetails> fires = new ArrayList<>();
    
    // GUI Setup
    private JButton deleteButton = new JButton("Delete Fire");
    private JButton recallButton = new JButton("Recall Drones");
    private JButton moveButton = new JButton("Move Drone");
    private JButton shutDownButton = new JButton("Shut Down");
    
    Server() {
        super("Server GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
        this.setLayout(new FlowLayout());
        this.setResizable(false);
        
        // Add components to GUI
        add(deleteButton);
        add(recallButton);
        add(moveButton);
        add(shutDownButton);
        
        // Action Listeners for Buttons
        deleteButton.addActionListener(this);
        recallButton.addActionListener(this);
        moveButton.addActionListener(this);
        shutDownButton.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
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
        
        Server GUI = new Server();
        
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
        
        if (fires.size() == 0) {
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
        
        int intId = -1;
        
        while (true) {
            String enteredId = JOptionPane.showInputDialog(null, "Enter a Fire ID");
            try {
                intId = Integer.parseInt(enteredId);
                break;
            } catch (NumberFormatException e) {
                System.out.println("ID must be numeric only.");
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }
        
        Iterator<FireDetails> iterator = fires.iterator();
            while (iterator.hasNext()) {
                FireDetails p = iterator.next();
                if (p.getId() == intId) {
                    iterator.remove();
            }
        }
    }
    
    public void recallDrones() {
        recallStatus = true;
    }
    
    public void moveDrone() {
        
    }
    
    public void shutDown() {
        
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
            
            // Receives fires based on integer
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
