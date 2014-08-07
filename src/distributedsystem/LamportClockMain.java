/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedsystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 *
 * @author Yash M Sawant
 *
 * Distributed algorithms such as resource synchronization often depend on some
 * method of ordering events to function. For example, consider a system with
 * two processes and a disk. The processes send messages to each other, and also
 * send messages to the disk requesting access. The disk grants access in the
 * order the messages were sent. Now, imagine process 1 sends a message to the
 * disk asking for access to write, and then sends a message to process 2 asking
 * it to read. Process 2 receives the message, and as a result sends its own
 * message to the disk. Now, due to some timing delay, the disk receives both
 * messages at the same time.
 *
 * Leslie Lamport provided a elegant solution to the above problem. It follows
 * some simple rules : 1) A process increments its counter before each event in
 * that process; 2) When a process sends a message, it includes its counter
 * value with the message; 3) On receiving a message, the receiver process set
 * its counter to be greater than the maximum of its own value and the received
 * value before it
 */
public class LamportClockMain {

    public static void main(String[] args) throws Exception {
        ProcessClass[] processSystem = new ProcessClass[3];
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        for (int i = 0; i < processSystem.length ; i++) {
            processSystem[i] = new ProcessClass(i);
            while (true) {
                /**
                 * Entering event sequence
                 */
                System.out.println("Ënter Event Senquence for Process : [" 
                        + (i + 1) + "]");
                System.out.println("Event Type: ");
                String eventType = br.readLine();
                if (eventType.equalsIgnoreCase("receive")) {
                    System.out.println("Enter Referring Process ID ");
                    int referringProcessID = Integer.parseInt(br.readLine());
                    processSystem[i].setEventSequence(processSystem,
                            referringProcessID - 1, EventType.RECEIVE);
                } else if (eventType.equalsIgnoreCase("send")) {
                    System.out.println("Enter Referring Process ID ");
                    int referringProcessID = Integer.parseInt(br.readLine());
                    processSystem[i].setEventSequence(processSystem,
                            referringProcessID - 1, EventType.SEND);
                } else {
                    processSystem[i].setEventSequence(processSystem.length,
                            EventType.NOTHING);
                }
                System.out.println("Enter 1 to add more");
                if ((Integer.parseInt(br.readLine())) != 1) {
                    break;
                }
            }
        }
        for(int i = 0 ; i < processSystem.length ; i++) {
            Thread t = new Thread(processSystem[i]);
            t.start();
        }
    }
}