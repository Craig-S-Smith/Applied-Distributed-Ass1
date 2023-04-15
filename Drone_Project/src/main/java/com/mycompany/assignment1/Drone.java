/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * @author diamo
 */
public class Drone extends Thread {

    static DroneDetails drone;
    static ArrayList<FireDetails> fires = new ArrayList<>();
    
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();
        Socket s = null;
        String hostName = "localhost";
        String serverMessage = "";
        String message = "";
        
        // Drone ID
        int id = 0;
        
        // Drone Name
        String name;
        
        // Drone cooordinates
        int x_pos = 0;
        int y_pos = 0;
        
        // If the server has issued a recall
        boolean recallStatus = false;
        
        // Asks user to input ID, reads input, if the ID can not be parsed into an integer, displays error and allows re-input
        while (true) {
            System.out.println("Enter Drone ID: ");
            String idInput = scanner.nextLine();
            try {
                id = Integer.parseInt(idInput);
                if (id < 0) {
                    System.out.println("ID must not be zero or negative.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("ID must be numeric only.");
            }
        }
        
        // Asks user to input name, reads input and sets it to the name variable
        System.out.println("Enter Drone Name: ");
        name = scanner.nextLine();
        
        // Adds drone details to a new DroneDetails object named drone
        drone = new DroneDetails(id, name, x_pos, y_pos, true);
        
        // Make first connection here
        try {
            int serverPort = 8888;
            
            s = new Socket(hostName, serverPort);
            
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
			
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            
            // Sends drone object to server
            out.writeObject(drone);
            
            // Reads server String response, says if recall or confirmed
            serverMessage = (String)in.readObject();
            
            // Checks if the message was a recall, acts accordingly
            if (serverMessage.equals("recall")) {
                System.out.println("Recall Initiated");
                // Confirmation Message to Server
                message = "Recall Confirmed";
                out.writeObject(message);
                // Closes connection
                s.close();
            
            // If the server confirms the input, just confirms it in commandline
            } else if (serverMessage.equals("confirmed")) {
                System.out.println("confirmed");
            }
            
            // Writes that there's 0 fires right now to implement
            out.writeObject(0);
            
        } catch (UnknownHostException e){System.out.println("Socket:"+e.getMessage());
	} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
	} catch (IOException e){System.out.println("readline:"+e.getMessage());
        } catch(ClassNotFoundException ex){ ex.printStackTrace();
	} finally {if(s!=null) try {s.close();}catch (IOException e){System.out.println("close:"+e.getMessage());}}
        
        // Create Thread
        Drone thread = new Drone();
        thread.start();
        
        // Facilitates Drone movements
        while (true) {
            // If drone needs to be recalled, stops movement by breaking loop
            if (recallStatus) {
                break;
            }
            
            // Sleeps thread for 2 seconds
            Thread.sleep(2000);
            
            // Gets a random int then calls a case to move a drone in one of 4 diagonal directions
            switch (rand.nextInt(4)) {
                case 1:
                    x_pos += rand.nextInt(5);
                    y_pos += rand.nextInt(5);
                    break;
                case 2:
                    x_pos -= rand.nextInt(5);
                    y_pos += rand.nextInt(5);
                    break;
                case 3: 
                    x_pos += rand.nextInt(5);
                    y_pos -= rand.nextInt(5);
                    break;
                case 4:
                    x_pos -= rand.nextInt(5);
                    y_pos -= rand.nextInt(5);
                    break;
            }
            
            // Sets drone object's positions to new ones
            drone.setX_pos(x_pos);
            drone.setY_pos(y_pos);
            
            // Makes random number up to 20, if the number is 1 reports that there's a fire at the position
            int fireRand = rand.nextInt(30);
            if (fireRand == 1) {
                int fireSeverity = rand.nextInt(9);
                System.out.println("Fire with Severity " + fireSeverity + " spotted at " + x_pos + ", " + y_pos);
                FireDetails fire = new FireDetails(0, x_pos, y_pos, id, fireSeverity);
                fires.add(fire);
            }
            
            // System.out.println(drone);
        }
            
    }
    
    DroneDetails returnDrone() {
        return drone;
    }
    
    @Override
    public void run() {
        // Connect to server every 10 seconds
        
        Socket s = null;
        String hostName = "localhost";
        String serverMessage = "";
        String message = "";
        DroneDetails drone;
        
        while (true) {
            try {
                // Sleeps thread for 10 seconds before executing further code
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Drone.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Connects to Server Here
            try {
            int serverPort = 8888;
            
            s = new Socket(hostName, serverPort);
            
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
			
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            
            // Gets drone object from returnDrone function then writes it to server
            drone = returnDrone();
            out.writeObject(drone);
            
            // Reads server String response, says if recall or confirmed
            serverMessage = (String)in.readObject();
            
            // Checks if the message was a recall, acts accordingly
            if (serverMessage.equals("recall")) {
                System.out.println("Recall Initiated");
                // Sends recall confirmation to server
                message = "Recall Confirmed";
                out.writeObject(message);
                
                // Closes connection
                s.close();
                
            } else if (serverMessage.equals("confirmed")) {
                // If the server confirms the input, just confirms it in commandline
                System.out.println("confirmed");
            }
            
            out.writeObject(0);
            
            } catch (UnknownHostException e){System.out.println("Socket:"+e.getMessage());
            } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
            } catch (IOException e){System.out.println("readline:"+e.getMessage());
            } catch(ClassNotFoundException ex){ ex.printStackTrace();
            } finally {if(s!=null) try {s.close();}catch (IOException e){System.out.println("close:"+e.getMessage());}}
        }
    }
        
}