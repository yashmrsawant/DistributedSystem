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
 */
interface ProcessPrototype extends Runnable {
    public void sendMessage(ProcessClass process, Event event, int eventId);
    public void receiveMessage(ProcessClass from, Event event, int eventId);
}
class ProcessClass implements ProcessPrototype {

    /**
     * Be aware synchronization problem
     */
    private int pID;
    private int eventIDCounter;
    private List<Event> eventSequence;
    public void setEventSequence(int processSystemLength, 
            EventType eventType) {
        eventSequence.add(new distributedsystem.Event(
                processSystemLength, eventType));
    }
    public void setEventSequence(ProcessClass[] processSystem,
            int referringProcessID, EventType eventType) {
        assert (processSystem != null);
        assert (referringProcessID > 0);
        eventSequence.add(new distributedsystem.Event(
                processSystem, referringProcessID, eventType));
    }

    ProcessClass(int pID) {
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
                    eventIDCounter += 1;
            if (event.getEventType() == EventType.RECEIVE) {
                this.receiveMessage(
                        event.getReferringProcess(), event, eventIDCounter);
            } else {
                this.sendMessage(
                        event.getReferringProcess(), event, eventIDCounter);
            }
            eventIDCounter++;
        }
    }

    @Override
    public void sendMessage(ProcessClass toProcess, Event event, int eventId) {
        toProcess.receiveMessage(this, event, eventId);
    }

    @Override
    public void receiveMessage(
            ProcessClass fromProcess, Event event, int eventId) {
        if (this.eventSequence.get(eventIDCounter).logicalClockSequence[this.pID] 
                < fromProcess.eventSequence.get(eventId).
                logicalClockSequence[fromProcess.pID]) {
            this.eventSequence.get(eventIDCounter).logicalClockSequence[this.pID] 
                    = fromProcess.eventSequence.get(eventId).
                    logicalClockSequence[fromProcess.pID];
        }
    }
}
class Event {
    /*
     * process corresponds to referring process
     * eventType corresponds receivingFrom or sendingTo
     */
    public Event(
            ProcessClass[] processSystem, int processID, EventType eventType) {
        assert(processID >= 0);
        this.process = processSystem[processID];
        this.eventType = eventType;
        this.logicalClockSequence = new int[processSystem.length];
    }
    public Event(
            int processSystemLength, EventType eventType) {
        this.eventType = eventType;
        this.logicalClockSequence = new int[processSystemLength];
    }
    public int[] getLogicalClockSequence() {
        return logicalClockSequence;
    }
    private ProcessClass process;
    
    private EventType eventType;
    int[] logicalClockSequence;
    public EventType getEventType() {
        return eventType;
    }
    public void setEventType(EventType event) {
        this.eventType = event; 
    }
    public ProcessClass getReferringProcess() {
        return process;
    }
    public void setReferringProcess(ProcessClass process) {
        this.process = process;
    }
}
enum EventType {
    RECEIVE, SEND, NOTHING;
}