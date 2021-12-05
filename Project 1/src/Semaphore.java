import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
	static int value = 1;
	public static Queue<Process> list = new LinkedList<Process>();

	// Semaphore methods
	// S is the semaphore the process has, P is the process thats calling this
	// method

	/// All of the below should belong in the semaphore class
	public static synchronized void wait(Process P) {
		System.out.println("										"+P.getProcessName() + " called wait()");
		Semaphore.value--;
		if (Semaphore.value < 0) {
			Semaphore.list.add(P);
			block(P);
			System.out.println(P.getProcessName() + "has been sent to the semaphore waiting queue since S < 0");
		}
	}

	public static synchronized void signal() {
		
		Semaphore.value++;
		if (Semaphore.value <= 0) {
			Process P = Semaphore.list.poll();
			wakeUp(P);
			System.out.println(P.getProcessName() + "has been put back into the ready queue");
		}
	}

	private static void wakeUp(Process P) {
		PCB pcb = CPU.getPCB(P.getPID());
		pcb.setState(STATE.READY);
		CPU.updatePCBList(pcb);
		Dispatcher.addToReadyQueue(P, pcb);
	}

	private static void block(Process p) {
		Semaphore.list = Scheduler.sortSemaphoreWaitingQueue(Semaphore.list);
		PCB pcb = CPU.getPCB(p.getPID());
		pcb.setState(STATE.WAIT);
		CPU.updatePCBList(pcb);
	}

	public void print() {
		System.out.println("All processes:");
		for (Process p : Semaphore.list) {
			System.out.println(p.getProcessName() + " Priority: " + p.priority);

		}
	}
}
