import java.io.IOException;
import java.util.ArrayList;

public class DataHolder {
	Graph graph;
	int Runs;
	int Counter = 1;
	SimData data;
	Metrics coll = new Metrics();
	boolean init = false;
	ArrayList<SimThread> threads = new ArrayList<SimThread>();
	public DataHolder(){
		Runs = Constants._trials - 1;
		if(Constants.debug){
			Runs = 1;
		}
	}
	public synchronized void TaskHand(SimThread t) throws IOException{
		if(Counter < Runs){
			data.processTrial(t.sim.returnGraph());
			coll.gather(t);
			Counter ++;
		}
		else {
			data.processTrial(t.sim.returnGraph());
			coll.gather(t);
			threads.remove(t);
			if(threads.isEmpty()){
				data.processEpsilonValue();
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
			Number indpVar;
			if(Constants.muCheck) indpVar = t.Constants.muIncS;
			else indpVar = t.Constants._epsilon;
			Counter = 0;
			coll.ThreadNum = t.getRun();
            coll.init();
			data = new SimData(t.getRun(), indpVar, t.verbose);
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
