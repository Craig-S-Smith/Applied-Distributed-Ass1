/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author diamo
 */
public class Server {

    static boolean recallStatus = false;
    static ArrayList<DroneDetails> drones = new ArrayList<>();
    
    public static void main(String[] args) {
        // Calls function to read data from files
        readDrones();
        
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
    
    static void readDrones() {
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
    }
    
    static void saveDrones() {
        // Saves drones arraylist to drones.bin
        try (
            FileOutputStream fileOut = new FileOutputStream("drones.bin");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)
            ) {
            objectOut.writeObject(drones);
        } catch (IOException e) {
            e.printStackTrace();
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
            
            System.out.println(tempDrone);
            
            Integer fires = (Integer)in.readObject();
            
            System.out.println("There are " + fires + " new fires.");
            
        }catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {System.out.println("readline:"+e.getMessage());
	} catch(ClassNotFoundException ex){ ex.printStackTrace();
	} finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}
    }
}
