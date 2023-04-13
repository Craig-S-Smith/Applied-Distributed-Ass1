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
            
            DroneDetails tempDrone = (DroneDetails)in.readObject();
            Server.addDrone(tempDrone);
            
            if (Server.ifRecall()) {
                message = "recall";
            } else {
                message = "confirmed";
            }
            
            out.writeObject(message);
            
            Integer fires = (Integer)in.readObject();
            
            System.out.println("There are " + fires + " new fires.");
            
        }catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {System.out.println("readline:"+e.getMessage());
	} catch(ClassNotFoundException ex){ ex.printStackTrace();
	} finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}
    }
}
