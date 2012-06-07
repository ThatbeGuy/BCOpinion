import java.util.ArrayList;


public class Group {
	public String name;
	private double opavg = 0;
	private ArrayList<Agent> agents;
	private double optotal = 0;
	
	public Group(String name){
		this.name = name;
		agents = new ArrayList<Agent>();
		if(!agents.isEmpty()){
			agents.clear();
		}
	}
	public void addAgent(Agent agent){
		if(!agents.contains(agent)){
			agents.add(agent);
		}
	}
	
	public void removeAgent(Agent agent) {
		if(agents.contains(agent)){
			agents.remove(agent);
		}
	}
	
	public boolean hasAgent(Agent agent){
		return agents.contains(agent);
	}
	public void calcavg(){
		optotal = 0;
		for(Agent a : this.agents){
			optotal = optotal + a.getOpinion();
		}
		opavg = optotal/agents.size();
	}
	public double getavg(){
		return opavg;
	}
	public ArrayList<Agent> getAgents(){
		return agents;
	}
}
