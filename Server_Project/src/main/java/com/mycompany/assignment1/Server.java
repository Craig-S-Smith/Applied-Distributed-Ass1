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
        
        readDrones();
        
        try {
            int serverPort = 8888;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            
            while(true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
            
        }   catch(IOException e) {System.out.println("Listen Socket : " + e.getMessage());}
    }
    
    static boolean ifRecall() {
        return recallStatus;
    }
    
    static void addDrone(DroneDetails tempDrone) {
        boolean newDrone = true;
        
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
        
        if (newDrone) {
            DroneDetails drone = new DroneDetails(tempDrone.getId(), tempDrone.getName(), tempDrone.getX_pos(), tempDrone.getY_pos(), true);
            drones.add(drone);
        }
    }
    
    static void readDrones() {
        try (
            FileInputStream fileIn = new FileInputStream("drones.bin");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            
            ArrayList<DroneDetails> tempDrones = (ArrayList<DroneDetails>) objectIn.readObject();
            if (tempDrones != null) {
                drones = tempDrones;
            }
            
        } catch(EOFException | FileNotFoundException e) {
        } catch(IOException e) {e.printStackTrace();
	} catch(ClassNotFoundException ex){ex.printStackTrace();
        }
    }
    
    static void saveDrones() {       
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
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;
    
    public Connection (Socket aClientSocket) {
        
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
            
            DroneDetails tempDrone = (DroneDetails)in.readObject();
            
            if (Server.ifRecall()) {
                message = "recall";
                out.writeObject(message);
                clientMessage = (String)in.readObject();
                if (clientMessage.equals("Recall Confirmed")) {
                    tempDrone.setActive(false);
                }
            } else {
                message = "confirmed";
                out.writeObject(message);
            }
            
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
