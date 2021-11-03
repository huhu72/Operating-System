import java.util.Timer;
import java.util.TimerTask;

public class t {

	static boolean status = false;
	public int p;
	public String name;
	t(String name, int p){
		this.name = name;
		this.p = p;
		
	}
	public void print() {
		System.out.println("Process name: " + this.name + "Priority: " + this.p);
	}
	public static void main(String[] args) {

		Timer t = new Timer();
		t2 t2 = new t2();
		t2.setStatus(status);
		Timer ti2 = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if (status) {
					status = false;
				} else {
					status = true;
				}
				t2.setStatus(status);

			}

		};
		t.scheduleAtFixedRate(tt, 5000, 5000);
		t2.print();
		
		
	}

}
