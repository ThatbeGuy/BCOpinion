import java.io.IOException;
import java.util.ArrayList;

public class DataHolder {
	//Graph graph;
	int Runs;
	int Counter = 1;
	SimData data;
	Metrics coll = new Metrics();
	boolean init = false;
	ArrayList<SimThread> threads = new ArrayList<SimThread>();
	
	public DataHolder() {
		Runs = Constants._trials - 1;
		if(Constants.debug){
			Runs = 1;
		}
	}
	public synchronized void TaskHand(SimThread t) throws IOException{
		if(Counter < Runs){
			data.processTrial(t.sim.returnGraph());
			coll.gather(t);
			/*
			if(Constants._murand){
				coll.gather(t.Constants._epsilon, t.sim.migrations, t.sim.opinion_changes, t.ticks, Constants.randMuStart, Constants.randMuEnd);
			}
			else if(Constants.muCheck){
				coll.gather(t.Constants.muIncS, t.sim.migrations, t.sim.opinion_changes, t.ticks);
			}
			else{coll.gather(t.Constants._epsilon, t.sim.migrations, t.sim.opinion_changes, t.ticks);}*/
			Counter ++;
		}
		else { 
			data.processTrial(t.sim.returnGraph());
			coll.gather(t.Constants._epsilon, t.sim.migrations, t.sim.opinion_changes, t.ticks);
			threads.remove(t);
			if(threads.isEmpty()) {
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
			Counter = 0;
			coll.ThreadNum = t.getRun();
            coll.init();
			data = new SimData(t.getRun(), t.Constants, t.verbose);
			threads.add(t);
			t.hold = this;
		}
	}
	public synchronized void threadJoin(SimThread t){
		if(Counter < Runs) {
			Counter ++;
		}
		else {
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
