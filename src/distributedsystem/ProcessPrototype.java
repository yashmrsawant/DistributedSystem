/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedsystem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yash M Sawant
 * 
 * 
 * ProcessPrototype and extended Classes make extensive use of java concept of 
 * aliasing. 
 */


class ProcessSystem {
    
    private ProcessClass[] processSystem;
    private int processSystemLength;
    ProcessSystem(int processSystemLength) {
        processSystem = new ProcessClass[processSystemLength];
        this.processSystemLength = processSystemLength;
    }
    public void buildProcessHavingPID(int pID) {
        processSystem[pID] = new ProcessClass(pID, this);
    }
    public ProcessClass getProcessHavingPID(int pID) {
        return processSystem[pID];
    }
    public int getProcessSystemLength() {
        return processSystemLength;
    }
}
interface ProcessPrototype extends Runnable {
    public void sendMessage(ProcessSystem processSystem, 
            int pID, Event event, int eventId);
    public void receiveMessage(ProcessSystem processSystem,
            int pID, Event event, int eventId);
}
class ProcessClass implements ProcessPrototype {

    /**
     * Be aware of synchronization problem
     */
    private int pID;
    private int eventIDCounter;
    private List<Event> eventSequence;
    private ProcessSystem processSystem;
    public void setEventSequence(
            EventType eventType) {
        /**
         * For an empty event
         */
        eventSequence.add(new distributedsystem.Event(
                processSystem.getProcessSystemLength(), eventType));
    }
    public void setEventSequence(EventType eventType, int referringProcessID) {
        /**
         *  For an event having EventType "SEND" OR "RECEIVE"
         */
        assert (processSystem != null);
        assert (referringProcessID > 0);
        eventSequence.add(
                new distributedsystem.Event(processSystem.
                    getProcessSystemLength(), referringProcessID, eventType));
    }

    ProcessClass(int pID, ProcessSystem processSystem) {
        this.processSystem = processSystem;
        this.pID = pID;
        this.eventSequence = new ArrayList<>();
    }

    @Override
    public void run() {
        /**
         * No use of shared data
         */
        this.eventIDCounter = 0;
        for (Event event : eventSequence) {
            event.logicalClockSequence[this.pID] =
                    eventIDCounter + 1;
            if (event.getEventType() == EventType.RECEIVE) {
                this.receiveMessage(
                        processSystem,
                        event.getReferringProcessID(), event, eventIDCounter);
            } else {
                this.sendMessage(
                        processSystem, event.getReferringProcessID(), event, eventIDCounter);
            }
            eventIDCounter++;
        }
    }

    @Override
    public void sendMessage(ProcessSystem processSystem, int pID, Event event, 
        int eventId) {
        processSystem.getProcessHavingPID(pID).
                receiveMessage(processSystem, this.pID, event, eventId);
    }

    @Override
    public void receiveMessage(
            ProcessSystem processSystem, int pID, Event event, int eventID) {
        assert(eventIDCounter <= this.eventSequence.size());
        assert(eventID <= processSystem.getProcessHavingPID(pID).
                eventSequence.size());
        ProcessClass fromProcess = processSystem.getProcessHavingPID(pID);

        if (this.eventSequence.get(eventIDCounter).
                logicalClockSequence[this.pID] < fromProcess.
                eventSequence.get(eventID).
                logicalClockSequence[fromProcess.pID]) {
            this.eventSequence.get(eventIDCounter).
                    logicalClockSequence[this.pID] 
                    = fromProcess.eventSequence.get(eventID).
                    logicalClockSequence[fromProcess.pID];
        }
    }
}
class Event {
    /*
     * process corresponds to referring process
     * eventType corresponds receivingFrom or sendingTo
     */
    public Event(int processSystemLength, int pID, EventType eventType) {
        assert(pID >= 0);
        this.pID = pID;
        this.eventType = eventType;
        this.logicalClockSequence = new int[processSystemLength];
    }
    /**
     * For an event labeled "NOTHING"
     * @param processSystemLength
     * @param eventType 
     */
    public Event(
        int processSystemLength, EventType eventType) {
        this.eventType = eventType;
        this.logicalClockSequence = new int[processSystemLength];
    }
    public int[] getLogicalClockSequence() {
        return logicalClockSequence;
    }
    private int pID;
    
    private EventType eventType;
    int[] logicalClockSequence;
    public EventType getEventType() {
        return eventType;
    }
    public int getReferringProcessID() {
        return this.pID;
    }
}
enum EventType {
    RECEIVE, SEND, NOTHING;
}