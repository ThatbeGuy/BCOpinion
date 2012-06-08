import java.util.ArrayList;

public class DataHolder {
	Graph graph;
	int Runs = Constants._trials - 1;
	int Counter = 1;
	SimData data;
	Metrics coll = new Metrics();
	boolean init = false;
	ArrayList<SimThread> threads = new ArrayList<SimThread>();
	
	public synchronized void TaskHand(SimThread t){
		if(data.ThreadNum == 1) System.out.println("Counter: " + Counter + ", Runs: " + Runs);
		if(Counter < Runs){
			data.processTrial(t.sim.returnGraph());
			coll.gather(t.Constants._epsilon, t.sim.migrations, t.sim.opinion_changes, t.ticks);
			Counter ++;
		}
		else {
			if(data.ThreadNum == 1) System.out.println(t.getName() + " is finished.");
			data.processTrial(t.sim.returnGraph());
			coll.gather(t.Constants._epsilon, t.sim.migrations, t.sim.opinion_changes, t.ticks);
			threads.remove(t);
			if(threads.isEmpty()){
				data.processEpsilonValue();
                                //System.out.println("Write");
				data.finish();
				coll.close();
				init = false;
				Main.monitor.threadInc(t, this);
			}
			else{
                threads.remove(t);
				Main.monitor.threadCheck(t);
			}
		}
	}
	public synchronized void init(SimThread t) {
		if(!init){
			Counter = 0;
			coll.ThreadNum = t.getRun();
                        coll.init();
			data = new SimData(t.getRun(), t.Constants._epsilon, t.verbose);
			threads.add(t);
			t.hold = this;
		}
	}
	public synchronized void threadJoin(SimThread t){
		if(Counter < Runs){
			Counter ++;
		}
		else{
			threads.remove(t);
			Main.monitor.threadCheck(t);
		}
	}
	public void addThread(SimThread t){
		if(Counter < Runs){
			threads.add(t);
			t.activate();
			System.out.println("Thread " + t + " added");
		}
	}
	public void FreezeAll(){
		for(SimThread t : this.threads){
			t.freeze(this);
		}
	}
	public void UnfreezeAll(){
		notifyAll();
	}
}
