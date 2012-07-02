import java.io.IOException;


public class SimThread extends Thread {
	int ThreadNum;
	private int runNum;
	boolean verbose;
	Constants Constants = new Constants();
	boolean active;
	Driver sim;
	DataHolder hold;
	boolean freeze = false;
	Object Ob;
	int ticks;
	public SimThread(double eps_start, double eps_end, int threadnum, boolean verbose){
		Constants._SIM_epsilon_start = eps_start;
		Constants._SIM_epsilon_final = eps_end;
		Constants._epsilon = Constants._SIM_epsilon_start;
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
				sim.Constants = this.Constants;
				sim.verbose = this.verbose;
		                sim.init();
						sim.run(runNum);
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
