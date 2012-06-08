import java.util.ArrayList;
import java.util.Random;


public class Graph {
	
	public final ArrayList<Agent> agents;
	public final ArrayList<Group> groups;
	Constants Constants;
	Random rand = new Random();
	
	public Graph(ArrayList<Agent> a, ArrayList<Group> g, Constants con ) {
		agents = a;
		groups = g;
		Constants = con;
		generate();
	}

	public void migrateAgentToGroup(Agent a, Group toGroup) {
		int numExternal = 0; //= a.external.size();
		long[] profile = new long[10];
		
		//if(a.getGroup().equals(toGroup)) return;
		Group fromGroup = a.getGroup();
		/*if(a.hasNeighbor(a)) { System.out.println("Agent " + a.name + " has itself as a neighbor!");
		System.out.println(
				"Agent: " + a.name + "\n" +
				"To group: " + toGroup.name + ", From group: " + fromGroup.name + "\n" +
				""
				);
		}*/
		
		//remove neighbors from a's original set of agents
		fromGroup.removeAgent(a);
		
		profile[0] = System.nanoTime();
		for(Agent a1: a.getNeighbors()) {
			a1.removeNeighbor(a);
		}
		
		profile[1] = System.nanoTime();
		
		
		//Add new neighbors to a1 (directed)
		profile[2] = System.nanoTime();
		//a.setNeighbors(toGroup.getAgents());
		a.setGroup(toGroup);
		toGroup.addAgent(a);
		
		a.resetExternal();
		profile[3] = System.nanoTime();
		//randomly choose a completely new set of external neighbors
		/*for(Agent a2: agents) {
			if(rand.nextDouble() <= Constants._p_ext && !a.equals(a2) && !a.getGroup().equals(a2.getGroup())) {
				a.addNeighbor(a2);
			}
		} //*/
		profile[4] = System.nanoTime();
		
		/*for(int i = 0; i < Constants._numnodes; i++) {
			if(rand.nextDouble() <= Constants._p_ext) numExternal++;
		}//*/
		
		numExternal = (int)Math.round(Constants._numnodes * ((rand.nextDouble()+0.5)*Constants._p_ext));
		if(numExternal > agents.size() - toGroup.getAgents().size()) numExternal = agents.size() - toGroup.getAgents().size();
		profile[5] = System.nanoTime();
		/*//Group g;
		Group rndGroup;
		Agent rndAgent;
		int gSize;
		for(int i = 0; i < numExternal; i++) {
			while(a.getGroup().equals(rndGroup = groups.get(rand.nextInt(Constants._groups))) || rndGroup.getAgents().isEmpty()) {}
			gSize = rndGroup.getAgents().size();
			while(a.hasNeighbor(rndAgent = rndGroup.getAgents().get(rand.nextInt(gSize)))) {}
			a.addNeighbor(rndAgent);
		} //*/
		profile[6] = System.nanoTime();
		//choose a set of new external neighbors of a fixed size
		/*Agent rndAgent;
		
		for(int i = 0; i < numExternal; i++) {
			//tMeasure[1] += System.nanoTime();
			do {
				rndAgent = agents.get(rand.nextInt(agents.size()));
			}
			while(a.hasNeighbor(rndAgent));
			//tMeasure[2] += System.nanoTime();
			a.addNeighbor(rndAgent);
		} //*/
		profile[7] = System.nanoTime();
		/*tMeasure[3] = System.nanoTime();
		if(tMeasure[3] > tMeasure[0]+1)
		System.out.println(
				"1 - 2: " + (tMeasure[2] - tMeasure[1])/numExternal + "\n"
				+ "0 - 3: " + (tMeasure[3] - tMeasure[0]) + "\n"
				);//*/
		
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
					else {
						break;
					}
				}
				rndAgent = tGroup.getAgents().get(rndAgentID);
			}
			while(a.hasNeighbor(rndAgent));
			//tMeasure[2] += System.nanoTime();
			a.addNeighbor(rndAgent);
		} //*/
		//tMeasure[3] = System.nanoTime();
		/*if(tMeasure[3] > tMeasure[0]+1)
		System.out.println(
				"1 - 2: " + (tMeasure[2] - tMeasure[1])/numExternal + "\n"
				+ "0 - 3: " + (tMeasure[3] - tMeasure[0]) + "\n"
				);//*/
		
		profile[8] = System.nanoTime();
		//Set neighbors in opposite direction
		for(Agent a3: a.getNeighbors()) {
			//System.out.println(a.name + a3.name);
			a3.addNeighbor(a);
		}//*/
		profile[9] = System.nanoTime();
		
		/*if(rand.nextDouble() < .0001) {
		System.out.print(
				"Removing neighbors: " + (profile[1] - profile[0]) + "\n"
				+ "Set new neighbors, change groups: " + (profile[3] - profile[2]) + "\n"
				+ "Determine new number of external neighbors: " + (profile[5] - profile[4]) + "\n"
				+ "Add new external neighbors: " + (profile[7] - profile[6]) + "\n"
				+ "Add reverse neighbors: " + (profile[9] - profile[8]) + "\n"
				);
	}//*/
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
				clusters.add(new OpinionCluster(a.getOpinion(), a.getGroup()));
			}
		}
		
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
		return clusters;
	}
	
	/*//the current method may allow for two opinion clusters to form where there may only be one.
	private void correctOpinionClusters(ArrayList<OpinionCluster> ocs) {
		ArrayList<OpinionCluster> temp;
		for(OpinionCluster o1: ocs) {
			temp = new ArrayList<OpinionCluster>(ocs);
			temp.remove(o1);
			for(OpinionCluster o2: temp) {
				if(o2.opVal >= o1.opVal - Constants._epsilon && o2.opVal <= o1.opVal + Constants._epsilon) {
					
				}
			}
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
