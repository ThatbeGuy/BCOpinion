
public class ThreadDelegate {
	private int runNum = 0;
	Constants constr;
	private boolean initialized = false;
	//double epsilon = constr._epsilon;
	
	public synchronized void threadInc(SimThread t, DataHolder d){
		if(constr.increment()) {
			t.setRunNum(runNum);
			t.Constants = new Constants(constr);
			runNum++;
			d.init(t);
		}
		else threadCheck(t);
	}
	
	public void threadStart(SimThread t){
		if(!initialized) {
			initialized = true;
			t.setRunNum(runNum);
			t.Constants = new Constants(constr);
			runNum++;
			DataHolder holder = new DataHolder();
			holder.init(t);
			t.start();
		}
		else if(constr.increment()) {
			t.setRunNum(runNum);
			t.Constants = new Constants(constr);
			runNum++;
			DataHolder holder = new DataHolder();
			holder.init(t);
			t.start();
		}
	}
	
	public int getRuns(){
		return runNum;
	}
	
	public void threadCheck(SimThread t){
		t.deactivate();
		for(SimThread a : Main.threads){
			if(a.active){
				if(a.hold.Counter < a.hold.Runs && a.hold != t.hold){
					threadHop(t,a);
					break;
				}
			}
		}
	}
	
	public void threadHop(SimThread hopper, SimThread to){
		//to.hold.FreezeAll();
		/*hopper.Constants._epsilon = to.Constants._epsilon;
		hopper.Constants._SIM_epsilon_start = to.Constants._SIM_epsilon_start;
		hopper.Constants._SIM_epsilon_final = to.Constants._SIM_epsilon_start;//*/
		hopper.Constants = new Constants(to.Constants);
		//hopper.sim = to.sim;
		hopper.sim.Constants = new Constants(to.Constants);
		hopper.sim.init();
		hopper.hold = to.hold;
		to.hold.addThread(hopper);
		//hopper.hold.UnfreezeAll();
	}
}
