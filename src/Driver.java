import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.EmptyStackException;
import java.lang.Math;


public class Driver {
	private double globalAvg = 0;
	private double globalTotal = 0;
	private double lastAvg = 0;
	public int migrations = 0;
	public int opinion_changes = 0;
	private ArrayList<Agent> agents;
	private ArrayList<Group> groups;
	Random gen = new Random();
	Constants Constants;
	boolean verbose;
	//Stack<Agent> Astack = new Stack<Agent>();
	//Stack<Group> Gstack = new Stack<Group>();
	Graph graph;
	double threshold;
	int runcount;
	boolean run;
	double tickavg;
	int ticks;
	
	public void init(){
		run = true;
		runcount = 0;
		globalAvg = 0;
		lastAvg = 0;
		globalTotal = 0;
		migrations = 0;
		opinion_changes = 0;
		threshold = Constants._epsilon * Constants._mu / 10;
		agents = new ArrayList<Agent>();
		groups = new ArrayList<Group>();
                agents.clear();
                groups.clear();
		for(int i = 0; i < Constants._groups; i++){
			Group group = new Group("G-"+i);
			groups.add(group);
		}
		for(int i = 0; i < Constants._numnodes; i++){
			if(Constants._murand){
				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), randMu());
				agents.add(agent);
			}
			else if(Constants.muCheck)
			{
				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), Constants.muIncS);
				agents.add(agent);
			}
			else{
				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), Constants._mu);
				agents.add(agent);
			}
		}
		for(int i = 0; i < agents.size(); i++){
			int g = gen.nextInt(groups.size());
			agents.get(i).setGroup(groups.get(g));
			groups.get(g).getAgents().add(agents.get(i));
		}
		for(Group g : groups){
			g.calcavg();
		}
		graph = new Graph(agents, groups, Constants);
		graph.Constants = this.Constants;
	}
	
	private double randMu() {
		return (gen.nextDouble() * (Constants.randMuEnd - Constants.randMuStart)) * Constants.randMuStart;
	}

	public void reset(){
		agents.clear();
		groups.clear();
		this.init();
	}
	
	public ArrayList<Agent> returnAgents(){
		return agents;
	}
	
	public ArrayList<Group> returnGroups(){
		return groups;
	}
	
	public void run(){
		int tMigrations; //number of migrations made each time interal
		int tOpinionChange; //opinion changes made in one time interval
		double tOpinionDifference; //total amount that opinions have been changed
		ticks = 0;
		
		//for(int i = 0; i < Constants._iterations; i++) {
		 do{
			tMigrations = 0;
			tOpinionChange = 0;
			tOpinionDifference = 0;
			ticks++;
			for(Agent a : agents) {
				//int select = gen.nextInt(agents.size());
				Agent hold = a;  //agents.get(select);
				if(!hold.neighbors.isEmpty()){
					//int select = gen.nextInt(hold.neighbors.size());
					Agent neighbor = hold.nSelection();
					if(Math.abs(hold.getOpinion() - neighbor.getOpinion()) < Constants._epsilon) {
						Double dub = hold.getOpinion();
						hold.setOpinion(neighbor.getOpinion());
						tOpinionDifference += Math.abs(dub - hold.getOpinion());
						neighbor.setOpinion(dub);
						
						tOpinionChange++;
						globalTotal += hold.getOpinion();
					}
					else if(Constants.Repulsive){
						if(Math.abs(hold.getOpinion() - neighbor.getOpinion()) > (Constants._epsilon + (Constants._epsilon * Constants.repuslivePer / 100))){
							Double dub = hold.getOpinion();
							hold.redOpinion(neighbor.getOpinion());
							tOpinionDifference += Math.abs(dub - hold.getOpinion());
							neighbor.redOpinion(dub);
							tOpinionChange++;
							globalTotal+= hold.getOpinion();
						}
					}
				}
			}
			opinion_changes += tOpinionChange;
			
			tOpinionDifference /= tOpinionChange;
			
			tMigrations = migrate();
			
			/*if(verbose && ticks % 100 == 0){
				System.out.print(
									"Tick " + ticks + "\n"
									+ groups.get(1).getAgents().size() + "\n"
								    + "Number of Migrations: " + tMigrations + "\n"
								    + "Average Opinion Change: " + tOpinionDifference + "\n"
								    + "Total Migrations: " + migrations + "\n"
				//				    + "Runcount: " + runcount + "\n"
				);
			}//*/
			
	        lastAvg = globalAvg;
	        globalAvg = globalTotal / agents.size();
	        globalTotal = 0;
	        /**  if(Math.abs(Math.abs(lastAvg) - Math.abs(globalAvg)) * tMigrations < this.threshold){
	        	runcount++;
	        }
	        else{runcount = 0;}
	        if(runcount > 10){
	        	run = false;
	        } **/
	        for(Group g : groups){
	        	g.calcavg();
	        }
	        if(!(tMigrations > 0)){
	        	this.runcount++;
	        }
	        else{this.runcount = 0;}
		} while((tOpinionDifference) > threshold && runcount < 10 && !Constants.debug);
	}
	
    public int migrate() {
    	int numMigrations = 0;
    	for(Agent a : agents) {
    		if(Math.abs(a.getOpinion() - a.getGroup().getavg()) > this.Constants._epsilon) {
    			boolean changed = false;
    			Group holdG = null;
    			double probRoll = gen.nextDouble();
                        a.calcEx();
    			for(Group b : a.exGroups) {
    				if(!changed){
    					double prob1 = 1 - Math.abs(a.getOpinion() - b.getavg()); // holder for calculating probability
    					double prob2 = prob1 / a.prob(); // actual probability
    					if(prob2 >= probRoll){
    						holdG = b;
    						changed = true;
    						migrations++;
    						break;
    					}
    				}
    			}
    			if(changed) {
    				graph.migrateAgentToGroup(a, holdG);
    				numMigrations++;
    			}
    		}
    	}
    	return numMigrations;
    }
    		
    
    public double returnAvg(){
        return globalAvg;
    }
    
    public Graph returnGraph() {
        return graph;
    }
}

