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
 * Various design flaws such as ProcessSystem class!!!
 */


interface ProcessPrototype extends Runnable {
    public void sendMessage(ProcessSystemInterface processSystem, 
            int pID, Event event);
    public void receiveMessage(ProcessSystemInterface processSystem,
            int pID, Event event, int fromEventID);
}
interface ProcessSystemInterface {
    public void includeProcess(ProcessPrototype process);
    public ProcessPrototype getProcessHavingPID(int pID);
    public int getProcessSystemLength();
}

class ProcessSystem implements ProcessSystemInterface{
    
    private ProcessClass[] processSystem;
    private int processSystemLength;
    ProcessSystem(int processSystemLength) {
        processSystem = new ProcessClass[processSystemLength];
        this.processSystemLength = processSystemLength;
    }
    @Override
    public void includeProcess(ProcessPrototype process) {
        ProcessClass p = (ProcessClass)process;
        p.setProcessSystem(this);
        processSystem[p.pID] = p;
    }
    @Override
    public ProcessPrototype getProcessHavingPID(int pID) {
        return processSystem[pID];
    }
    @Override
    public int getProcessSystemLength() {
        return processSystemLength;
    }
}
class ProcessClass implements ProcessPrototype {

    /**
     * Be aware of synchronization problem
     */
    ProcessClass(int pID) {
        this.pID = pID;
        this.eventSequence = new ArrayList<>();
    }
    protected int pID;
    protected int eventIDCounter;
    private List<Event> eventSequence;
    private ProcessSystemInterface processSystem;
    public void setProcessSystem(ProcessSystemInterface processSystem) {
        this.processSystem = processSystem;
    }
    public ProcessSystemInterface getProcessSystem() {
        return this.processSystem;
    }
    public void setEventSequence(
            EventType eventType) {
        /**
         * For an empty event
         */
        eventSequence.add(new distributedsystem.Event(
                processSystem.getProcessSystemLength(), eventType));
    }
    public void setEventSequence(EventType eventType, 
            int referringProcessID, int thisEvent, int referringEventID) {
        /**
         *  For an event having EventType "SEND" OR "RECEIVE"
         */
        assert (processSystem != null);
        assert (referringProcessID > 0);
        eventSequence.add(
                new distributedsystem.Event(processSystem.
                    getProcessSystemLength(), referringProcessID, eventType,
                    thisEvent, referringEventID));
    }


    public List<Event> getEventSequence() {
        return this.eventSequence;
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
            if(event.getEventType() == EventType.SEND) {
                this.sendMessage(
                        processSystem, event.getReferringProcessID(), event);
            }
            
            
            eventIDCounter++;
        }
    }
    @Override
    public void sendMessage(ProcessSystemInterface processSystem, int pID, 
        Event event) {
        processSystem.getProcessHavingPID(pID).
                receiveMessage(processSystem, this.pID, event, eventIDCounter);
    }
    @Override
    public void receiveMessage(
            ProcessSystemInterface processSystem, int pID, Event fromEvent, 
            int fromEventID) {
        
        ProcessClass fromProcess = 
                (ProcessClass)processSystem.getProcessHavingPID(pID);
        int value = fromProcess.
                eventSequence.get(fromEventID).
                logicalClockSequence[fromProcess.pID];
        this.eventSequence.get(fromEvent.getEventMap()).
                    logicalClockSequence[fromProcess.pID] =
                Math.max(this.eventSequence.get(fromEvent.getEventMap()).
                    logicalClockSequence[fromProcess.pID], value);
    }
    
}
class Event {
    /*
     * process corresponds to referring process
     * eventType corresponds receivingFrom or sendingTo
     */
    private int pID;
    private int[] eventMap = 
            new int[2];//key, value pair 
    private EventType eventType;
    int[] logicalClockSequence;
    
    
    public int getEventMap() {
        return this.eventMap[1];
    }
    /**
     * 
     * For an event labeled "SEND" or "RECEIVE"
     * @param processSystemLength
     * @param pID
     * @param eventType
     * @param thisEvent
     * @param referringProcessEventID 
     */
    public Event(int processSystemLength, int pID, EventType eventType
            , int thisEvent, int referringProcessEventID) {
        assert(pID >= 0);
        this.pID = pID;
        this.eventType = eventType;
        this.eventMap[0] = thisEvent;
        this.eventMap[1] = referringProcessEventID;
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
    public EventType getEventType() {
        return eventType;
    }
    public int getReferringProcessID() {
        return this.pID;
    }
}
enum EventType {
    RECEIVE, SEND, INTERNALEVENT;
}
