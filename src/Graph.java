import java.util.ArrayList;
import java.util.Random;


public class Graph {
	
	public final ArrayList<Agent> agents;
	public final ArrayList<Group> groups;
	Constants Constants;
	Random rand = new Random();
	private int numEdges;
	
	public Graph(ArrayList<Agent> a, ArrayList<Group> g, Constants con ) {
		agents = a;
		groups = g;
		Constants = con;
		generate();
	}

	public void migrateAgentToGroup(Agent a, Group toGroup) {
		//int numExternal = a.getNumExternalNeighbors();
		//if(a.getNumExternalNeighbors() > 10) System.out.println("whaaaa");
		//long[] profile = new long[10];
		
		Group fromGroup = a.getGroup();
		
		//remove neighbors from a's original set of agents
		fromGroup.removeAgent(a);
		a.group = null;
		
		//profile[0] = System.nanoTime();
		for(Agent a1: a.getExternalNeighbors()) {
			a1.removeNeighbor(a);
		}
		//profile[1] = System.nanoTime();
		
		
		//Add new neighbors to a1 (directed)
		//profile[2] = System.nanoTime();
		a.setGroup(toGroup);
		toGroup.addAgent(a);
		//numEdges -= a.getNumExternalNeighbors();
		a.resetExternal();
		//profile[3] = System.nanoTime();
		
		//int numExternal = 0;
		/*for(int i = 0; i < Constants._numnodes; i++) {
			if(rand.nextDouble() < Constants._p_ext) numExternal++;
		}//*/
		
		//profile[4] = System.nanoTime();
		double rndDub = rand.nextDouble();
		int numExternal = (int)Math.round((agents.size()-toGroup.getAgents().size()) * ((rndDub*(rndDub-0.5)+1)*Constants._p_ext)); //*/
		if(numExternal > agents.size() - toGroup.getAgents().size()) numExternal = agents.size() - toGroup.getAgents().size(); //*/
		//profile[5] = System.nanoTime();
		//numEdges += numExternal;
		//profile[6] = System.nanoTime();
		ArrayList<Group> exGroupSet = new ArrayList<Group>(groups);
		exGroupSet.remove(toGroup);
		Group tGroup = exGroupSet.get(0);
		int rndAgentID;
		Agent rndAgent;
		for(int i = 0; i < numExternal; i++) {
			//tMeasure[1] += System.nanoTime();
			do {
				rndAgentID = rand.nextInt(agents.size()-toGroup.getAgents().size());
				for(Group g: exGroupSet) {
					if(rndAgentID >= (tGroup = g).getAgents().size()) rndAgentID -= g.getAgents().size();
					else break;
				}
				rndAgent = tGroup.getAgents().get(rndAgentID);
			}
			while(a.hasNeighbor(rndAgent));
			//tMeasure[2] += System.nanoTime();
			a.addNeighbor(rndAgent);
		}
		//profile[7] = System.nanoTime();
		
		//profile[8] = System.nanoTime();
		//Set neighbors in opposite direction
		for(Agent a3: a.getExternalNeighbors()) {
			a3.addNeighbor(a);
		}
		//profile[9] = System.nanoTime();
	}
	
	private void generate() {
		//create a complete graph of each group of agents
		//for(Group g : groups) generateGroupGraph(g);

		//randomly select edges within graph
		
		Random rand = new Random();
		Agent a1;
		Agent a2;
		
		if(Constants._groups <= 1) {
			return;
		}
		
		for(int i = 0; i < Constants._edges; i++) {
			do {
				a1 = agents.get(rand.nextInt(agents.size()));
				a2 = agents.get(rand.nextInt(agents.size()));
			} while(a1.hasNeighbor(a2) || a1.equals(a2));
			a1.addNeighbor(a2);
			a2.addNeighbor(a1);
		}
		numEdges = Constants._edges;
		
	}
	
	/**private void generateGroupGraph(Group g) {
		//generate a K-complete graph within each group in the program.
		for(Agent i : g.getAgents()) {
			for(Agent j : g.getAgents() ) {
				if(!i.equals(j)) i.addNeighbor(j);
			}
		}
	}**/
	
	public ArrayList<OpinionCluster> calculateOpinionClustersByPopulation() {
		ArrayList<OpinionCluster> clusters = new ArrayList<OpinionCluster>();
		
		boolean isNewOpinionCluster;
		for(Agent a: agents) {
			isNewOpinionCluster = true;
			for(OpinionCluster o: clusters) {
				if(a.getOpinion() > o.opVal - Constants._epsilon && a.getOpinion() < o.opVal + Constants._epsilon) {
					o.increment(a.opinion);
					isNewOpinionCluster = false;
					break;
				}
			}
			if(isNewOpinionCluster) {
				clusters.add(new OpinionCluster(a.getOpinion(), null));
			}
		}
		correctOpinionClusters(clusters);
		return clusters;
	}
	
	//call once the population converges, and only upon convergence
	public ArrayList<OpinionCluster> calculateOpinionClustersByGroup() {
		ArrayList<OpinionCluster> clusters = new ArrayList<OpinionCluster>();
		boolean isNewOpinionCluster;
		for(Group g: groups) {
		for(Agent a: g.getAgents()) {
			isNewOpinionCluster = true;
			for(OpinionCluster o: clusters) {
				if(a.getGroup().equals(o.ocGroup) &&
						a.getOpinion() >= o.opVal - Constants._epsilon && a.getOpinion() <= o.opVal + Constants._epsilon) {
					o.increment(a.opinion);
					isNewOpinionCluster = false;
					break;
				}
			}
			if(isNewOpinionCluster) {
				clusters.add(new OpinionCluster(a.getOpinion(), a.getGroup()));
			}
		} }
		correctOpinionClusters(clusters);
		return clusters;
	}
	
	//the current method may allow for two opinion clusters to form where there may only be one.
	private void correctOpinionClusters(ArrayList<OpinionCluster> ocs) {
		OpinionCluster o1,o2;
		ArrayList<OpinionCluster> toRemove = new ArrayList<OpinionCluster>();
		ArrayList<OpinionCluster> toAdd = new ArrayList<OpinionCluster>();
		for(int i = 0; i < ocs.size(); i++) {
			o1 = ocs.get(i);
			for(int j = i+1; j < ocs.size(); j++) {
				o2 = ocs.get(j);
				if(o2.opVal >= o1.opVal - Constants._epsilon && o2.opVal <= o1.opVal + Constants._epsilon
						&& o1.ocGroup == o2.ocGroup) {
					toAdd.add(new OpinionCluster((o1.opVal*o1.occurance + o2.opVal*o2.occurance)/(o1.occurance+o2.occurance),o1.ocGroup));
					toRemove.add(o1);
					toRemove.add(o2);
					//System.out.println("WHAT THE HECK IT ACTUALLY HAPPENED (OC correction???)");
				}
			}
		}
		
		for(OpinionCluster o : toRemove) {
			ocs.remove(o);
		}
		
		for(OpinionCluster o : toAdd) {
			ocs.add(o);
		}
	}//*/
	
	public class OpinionCluster {
		public double opVal;
		public int occurance = 1;
		public Group ocGroup;
		
		public OpinionCluster(double oVal, Group g) {
			opVal = oVal;
			ocGroup = g;
		}
		
		public void increment(double op) {
			//calculate the new opinion average in the cluster.
			//the basic assumption here is that if the cluster is isolated from its neighbors,
			//and the if the majority of future influences within the cluster will come
			//from its members, then the average opinion of each member represents the value that
			//the cluster will approach.
			opVal = (opVal * occurance + op)/(occurance+1);

			occurance++;
		}
	}
	
	public ArrayList<Group> getGroups() {
		return groups;
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}
	
}
