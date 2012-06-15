import java.io.IOException;


public class SimThread extends Thread {
	int ThreadNum;
	private int runNum;
	boolean verbose;
	Constants Constants;
	boolean active;
	Driver sim;
	DataHolder hold;
	boolean freeze = false;
	Object Ob;
	int ticks;
	
	public SimThread(int threadnum, boolean verbose) {
		this.ThreadNum = threadnum;
		this.verbose = verbose;
		this.active = true;
	}
	
	public void run() {
		while(active){
			if(freeze){
				try {
					this.freeze = false;
					Ob.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sim = new Driver();
			sim.Constants = new Constants(this.Constants);
			sim.verbose = this.verbose;
		    sim.init();
			sim.run();
			if(freeze){
				try {
					this.freeze = false;
					Ob.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ticks = sim.ticks;
			try {
				hold.TaskHand(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	public void setThreadNum(int num){
		this.ThreadNum = num;
	}
	public void setRunNum(int num){
		this.runNum = num;
	}
	public void ping(){
		this.notify();
	}
	public void deactivate(){
		this.active = false;
	}
	/*public void setGraph(){
		
	}*/
	public int getRun(){
		return this.runNum;
	}
	public void setHolder(DataHolder d){
		this.hold = d;
	}
	public void activate(){
		this.active = true;
		hold.threadJoin(this);
	}
	public void freeze(Object o){
		Ob = o;
		this.freeze = true;
	}
}
