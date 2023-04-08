/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.drone_project;

import java.util.Scanner;

/**
 *
 * @author diamo
 */
public class Drone {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        
        int id = 0;
        String name;
        int x_pos = 0;
        int y_pos = 0;
        
        // Asks user to input ID, reads input, if the ID can not be parsed into an integer, displays error and allows re-input
        while (true) {
            System.out.println("Enter Drone ID: ");
            String idInput = scanner.nextLine();
            try {
                id = Integer.parseInt(idInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("ID must be numeric only.");
            }
        }
        
        // Asks user to input name, reads input and sets it to the name variable
        System.out.println("Enter Drone Name: ");
        name = scanner.nextLine();
        
        // Adds drone details to a new DroneDetails object named drone
        DroneDetails drone = new DroneDetails(id, name, x_pos, y_pos);
        
        // Testing toString and object
        System.out.println(drone);
        System.out.println("-----------------");
        
        // Testing every 10 seconds
        while (true) {
            Thread.sleep(10000); // Sleeps for 10 seconds
            System.out.println("10 Seconds has passed.");
        }
        
    }
    
}
