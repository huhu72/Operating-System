import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class t2  {
	
	static boolean status;
	
public void setStatus(boolean s) {
		this.status = s;
	}
	
	public void print() {
		Timer t = new Timer();
		
		TimerTask tt = new TimerTask() {
			int i = 1;
			@Override
			public void run() {
				
			System.out.println("Time: " + i + " Status: " + status);
				i ++;
			}
			
		};
		t.scheduleAtFixedRate(tt, 0, 1000);
	}
	public static void main(String[] args) {
		/*t ti = new t("t1", 2);
		t tj = new t("t2", 1);
		Queue<t> rq = new PriorityQueue<t>((p1,p2)->{
			return p2.p -p1.p;
		});
		rq.add(tj);
		rq.add(ti);
		for(t tx : rq) {
			tx.print();
		}*/
	}

}
