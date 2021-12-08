import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
	int value = 1;
	public Queue<Process> list = new LinkedList<Process>();

	/// All of the below should belong in the this class
	public synchronized void wait(Process P) {
		this.value--;
		if (this.value < 0) {
			this.list.add(P);
			block(P);
			// System.out.println(P.getProcessName() + "has been sent to the semaphore
			// waiting queue since S < 0");
		}
	}

	public synchronized void signal() {
		this.value++;
		if (this.value <= 0) {
			Process P = this.list.poll();
			wakeUp(P);
			// System.out.println(P.getProcessName() + "has been put back into the ready
			// queue");
		}
	}

	private void wakeUp(Process P) {
		PCB pcb = CPU.getPCB(P.getPID());
		pcb.setState(STATE.READY);
		CPU.updatePCBList(pcb);
		Scheduler.addToPQ(P);
		notify();

	}

	private void block(Process p) {
		this.list = Scheduler.sortSemaphoreWaitingQueue(this.list);
		PCB pcb = CPU.getPCB(p.getPID());
		pcb.setState(STATE.WAIT);
		CPU.updatePCBList(pcb);
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void print() {
		System.out.println("All processes:");
		for (Process p : this.list) {
			System.out.println(p.getProcessName() + " Priority: " + p.priority);

		}
	}
}
