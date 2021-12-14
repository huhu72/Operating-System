import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
	private static HashMap<Long, PCB> pcbInfo = new HashMap<>();
	// Optimized by the scheduler. Priorities are determined whether if its a sys
	// operation or I/O. This is a subset of the processes queue
	private static Queue<Process> readyQueue = new PriorityQueue<>((p1, p2) -> {
		return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
	});;
	private static Queue<Process> waitingQueue = new PriorityQueue<>((p1, p2) -> {
		return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
	});;
	private Boolean quantumStatus = true;
	Timer timer;

	Scheduler() {

	}

	public static void addToPQ(Process p) {
		Scheduler.readyQueue.add(p);
	}

	public static void addPCBInfo(PCB pcb) {
		Scheduler.pcbInfo.put(pcb.getProcessPID(), pcb);
	}

	public static Process getProcess() {
		return Scheduler.readyQueue.poll();
	}

	public static void setReadyQueue(Queue<Process> rq, HashMap<Long, PCB> pcbList) {
		Scheduler.readyQueue = rq;
		Scheduler.pcbInfo = pcbList;
	}

	public static Queue<Process> getReadyQueue() {
		return readyQueue;
	}

	public static void setWaitingQueue(Queue<Process> waitingQueue, HashMap<Long, PCB> pcbList) {
		Scheduler.waitingQueue = waitingQueue;
		Scheduler.pcbInfo = pcbList;

	}

	public static Queue<Process> getWaitingQueue() {
		return Scheduler.waitingQueue;
	}

	public Boolean getQuantumStatus() {
		return this.quantumStatus;
	}

	public void run(Process process) {

		OS.info.setText(OS.info.getText() + "\n" + "Starting a new timer for " + process.getProcessName());
		quantumStatus = true;
		timer = new Timer();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				quantumStatus = false;
				killQuantumTimer(process);
			}

		};
		timer.schedule(tt, 4);

	}

	public void killQuantumTimer(Process process) {
		timer.cancel();
		OS.info.setText(OS.info.getText() + "\n" + process.getProcessName() + " timer has been terminated");
	}

	public static Queue<Process> sortSemaphoreWaitingQueue(Queue<Process> list) {
		Queue<Process> sortedList = new PriorityQueue<>((p1, p2) -> {
			return Scheduler.pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
		});
		sortedList.addAll(list);
		return sortedList;
	}

}
//TODO: Re-formated semaphore logic but need to implement the sorting of the list
/*
 * new Comparator<Process>() {
 * 
 * @Override public int compare(Process p1, Process p2) {
 * if(pcbInfo.get(p1.getPID()).getPriority() <
 * pcbInfo.get(p2.getPID()).getPriority() ) { return -1; }
 * if(pcbInfo.get(p1.getPID()).getPriority() >
 * pcbInfo.get(p2.getPID()).getPriority()) { return 1; } return 0; }
 * 
 * }
 */