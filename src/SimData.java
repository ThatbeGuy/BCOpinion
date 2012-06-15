import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class SimData {
	//The id of the thread SimData is being run in
	public final int ThreadNum;
	//The current trial being processed (SimData runs after every trial)
	public int cTrial = 1;
	
	//Data writers
	public ArrayList<DataWriter> DataWriters;
	
	//The data being collected
	//public ArrayList<Double> opinionSet = new ArrayList<Double>();
	protected double[] opAverageSet = new double[51];
	protected double[] ocPopAverageSet = new double[51];
	protected double[] ocGroupAverageSet = new double[51];
	
	protected double opAverageTotal = 0;
	protected double ocOccurranceByPopulation = 0;
	protected double ocOccurranceByGroup = 0;
	protected double ocNonConsensusRatio = 0;
	protected double[] cumulativeGroupSize;// = new double[Constants._groups];
	private double avgExternalNeighbors = 0;
	
	//other constants
	//public final double epsValue;
	//Constants Constants = new Constants();
	private final boolean silent = false;
	private final boolean isVerbose;
	
	private final Constants con;
	
	public static void initialize() {
		//make sure the files object is cleared beforehand
		Constants.files.clear();
		
		//create the subfolders yo
		String str[] = Constants._OUTPUT_PATH.split("'\\'|'/'");
		File fObj;
		String temp = "";
		for(int i = 0; i < str.length; i++) {
			fObj = new File(temp + str[i]);
			if(!fObj.exists()) {
				fObj.mkdir();
			}
			temp += str[i] + "\\";
		}
		//assign each
		Constants.files.add("OpinionDensity");
		Constants.files.add("NCRR_N" + Constants._numnodes);
		Constants.files.add("OpinionClusters");
		Constants.files.add("OCPopDist");
		Constants.files.add("OCGroupDist");
		Constants.files.add("Metrics");//*/
	}
	
	public SimData(int tNum, Constants constr, boolean verbose) {
		ThreadNum = tNum;
		con = new Constants(constr);
		isVerbose = verbose;
		cumulativeGroupSize = new double[con._groups];
		printToConsole("Initializing SimData object for independent variable value: " + genIndpVarString());
		/*try {
			writerDensity = new OpinionDensityDataWriter(con.files.get(OpinionDensityDataWriter.id));
			writerOpinionCluster = new OpinionClusterDataWriter(con.files.get(OpinionClusterDataWriter.id));
			writerRealizationFraction = new RealizationFractionByOCDataWriter(con.files.get(RealizationFractionByOCDataWriter.id));
			writerGroupSizeDistribute = new GroupSizeDistributionDataWriter("GroupSizeDistribution");
		} catch(IOException e) {
			e.printStackTrace();
		}//*/
	}
	
	public void processEpsilonValue() {
		try {
			DataWriters = new ArrayList<DataWriter>();
			DataWriters.add(new OpinionDensityDataWriter(con.files.get(OpinionDensityDataWriter.id)));
			DataWriters.add(new OpinionClusterDataWriter(con.files.get(OpinionClusterDataWriter.id)));
			DataWriters.add(new RealizationFractionByOCDataWriter(con.files.get(RealizationFractionByOCDataWriter.id)));
			DataWriters.add(new OCDistDataWriter(con.files.get(OCDistDataWriter.id), 0));
			DataWriters.add(new OCDistDataWriter(con.files.get(OCDistDataWriter.id+1), 1));
			DataWriters.add(new GroupSizeDistributionDataWriter("GroupSizeDistribution"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		ocNonConsensusRatio /= con._trials;
		for(int i = 0; i < opAverageSet.length; i++) {
			opAverageSet[i] /= con._trials;
		}
		
		forcePrintToConsole("Finalizing data for independent variable value: " + genIndpVarString()
				+ "\n\t\t" + "Avg number of external neighbors: " + round(avgExternalNeighbors)
				//+ "\n\t\t" + ""
				);
		/*printToConsole("Statistics gathered for epsilon value " + epsilon + ":\n"
				+ " opAverageTotal: " + opAverageTotal + "\n"
				+ " ocOccurranceByPopulation: " + ocOccurranceByPopulation + "\n"
				+ " ocOccurranceByGroup: " + ocOccurranceByGroup + "\n"
				+ " ocNonConsensusRatio: " + ocNonConsensusRatio + "\n"
				);*/
		try {
			for(DataWriter dw : DataWriters) {
				if(dw.active) dw.addNewRow();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processTrial(Graph g) {
		if(cTrial % (con._trials / 10 + 1) == 0) printToConsole("Adding results from trial " + cTrial + ".");
		
		modifyAverage(g.getAgents());
		calculateCumulativeGroupSize(g.getGroups());
		calculateExtNeighborAvg(g.getAgents());
		
		ArrayList<Graph.OpinionCluster> ocGroup = g.calculateOpinionClustersByGroup();
		ArrayList<Graph.OpinionCluster> ocPopulation = g.calculateOpinionClustersByPopulation();
		
		calculateOpinionClusterGroupAverage(g.getGroups(), ocGroup);
		calculateOpinionClusterPopAverage(g.getGroups(), ocPopulation);
		
		calculateOCPopDist(ocPopulation, g.agents.size());
		calculateOCGroupDist(g.getGroups(),ocGroup, g.agents.size());
		
		//move on to the next trial
		cTrial++;
	}
	
	public void finish() {
		for(DataWriter dw : DataWriters) {
			if(dw.active) dw.finish();
		}
	}
	
	//do this every trial
	private void modifyAverage(ArrayList<Agent> agents) {
		int opPos;
		double[] opTrial = new double[opAverageSet.length];
		for(int i = 0; i < opTrial.length; i++) {
			opTrial[i] = 0;
		}
		for(Agent a : agents) {
			opPos = (int) Math.round(a.opinion*(opAverageSet.length-1));
			opTrial[opPos] += 1;
		}
		
		for(int i = 0; i < opTrial.length; i++) {
			opTrial[i] /= agents.size();
			opAverageSet[i] += opTrial[i];
		}
	}
	
	protected void calculateCumulativeGroupSize(ArrayList<Group> gs) {
		int[] gSizes = new int[con._groups];
		for(int i = 0; i < gSizes.length; i++) {
			if(i >= gs.size()) gSizes[i] = 0;
			else gSizes[i] = gs.get(i).getAgents().size();
		}
		Arrays.sort(gSizes);
		
		int[] cGroupSize = new int[con._groups];
		cGroupSize[0] = gSizes[0];
		for(int i = 1; i < cGroupSize.length; i++) {
			cGroupSize[i] = cGroupSize[i-1] + gSizes[i];
		}
		
		for(int i = 0; i < con._groups; i++) {
			cumulativeGroupSize[i] += (double)cGroupSize[i] / con._trials;
		}
	}
	
	protected void calculateOpinionClusterPopAverage(ArrayList<Group> gs, ArrayList<Graph.OpinionCluster> ocSet) {
		int numClusters = 0;
		for(Graph.OpinionCluster o : ocSet) if(o.occurance > 1) numClusters++;
		
		ocOccurranceByPopulation += (double)numClusters / con._trials;
		if(ocSet.size() > 1) ocNonConsensusRatio += 1;
	}
	
	protected void calculateOpinionClusterGroupAverage(ArrayList<Group> gs, ArrayList<Graph.OpinionCluster> ocSet) {
		int numClusters = 0;
		numClusters = ocSet.size();
		//for(Graph.OpinionCluster o : ocSet) if(o.occurance > 1) numClusters++;
		/*int gNumClusters = 0;
		for(Group g: gs) {
			gNumClusters = 0;
			for(Graph.OpinionCluster o : ocSet) {
				if(o.ocGroup.equals(g)) gNumClusters++;
			}
			if(gNumClusters > 1) {
				ocNonConsensusRatio += 1;
				break;
			}
		}//*/
		
		int numGroups = 0;
		for(Group g: gs) {
			if(g.getAgents().size() > 0) numGroups++;
		}
		double average = (double)numClusters / numGroups;
		
		ocOccurranceByGroup += average / con._trials;
	}
	
	protected void calculateOCPopDist(ArrayList<Graph.OpinionCluster> ocSet, int agentpool) {
		int ocPos;
		double[] ocTrial = new double[ocPopAverageSet.length];
		for(Graph.OpinionCluster o : ocSet) {
			ocPos = (int) Math.round(o.opVal*(ocPopAverageSet.length-1));
			ocTrial[ocPos] += 1;
		}
		
		for(int i = 0; i < ocTrial.length; i++) {
			//ocTrial[i];
			ocPopAverageSet[i] += ocTrial[i] / con._trials;
		}
	}
	
	protected void calculateOCGroupDist(ArrayList<Group> gs, ArrayList<Graph.OpinionCluster> ocSet, int agentpool) {
		int numGroups = 0;
		//numClusters = ocSet.size();
		for(Group g: gs) if(g.getAgents().size() > 0) numGroups++;
		
		int ocPos;
		double[] ocTrial = new double[ocGroupAverageSet.length];
		for(Group g: gs) {
			for(Graph.OpinionCluster o : ocSet) {
				ocPos = (int) Math.round(o.opVal*(ocGroupAverageSet.length-1));
				ocTrial[ocPos] += (double)1 / numGroups;
			}
		}
		
		for(int i = 0; i < ocTrial.length; i++) {
			//ocTrial[i];
			ocGroupAverageSet[i] += ocTrial[i] / con._trials;
		}
	}
	
	private void calculateExtNeighborAvg(ArrayList<Agent> agents) {
		int totalExtNeighbors = 0;
		for(Agent a : agents) {
			totalExtNeighbors += a.getNumExternalNeighbors();
		}
		double trAvg = (double)totalExtNeighbors / agents.size();
		avgExternalNeighbors += trAvg / con._trials;
	}
	
	protected void printToConsole(String s) {
		if(!silent && isVerbose) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	protected void forcePrintToConsole(String s) {
		if(!silent) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	private String genIndpVarString() {
		String s = "";
		for(Constants.indpVar indp : con.independent) {
			s += indp.getName() + ": " + round(indp.getValue()) + ",";
		}
		return s;
	}
	
	private String dataIndpVarString() {
		String s = "";
		for(Constants.indpVar indp : con.independent) {
			s += round(indp.getValue()) + "\t";
		}
		return s;
	}
	
	protected static double round(double d) {
		DecimalFormat dFormat = new DecimalFormat("#.####");
		return Double.valueOf(dFormat.format(d));
	}
	
	private abstract class DataWriter {
		protected final String fileName;
		public final boolean active;
		protected File fileObj;
		protected FileWriter fileWriter;
		protected BufferedWriter fOutput;
		
		public DataWriter(String fName, boolean isActive) throws IOException {
			fileName = new String(fName);
			active = isActive;
		}
		
		protected void initialize() throws IOException {
			fileObj = new File(con._OUTPUT_PATH + fileName + ThreadNum);
			fileWriter = new FileWriter(fileObj);
			fOutput = new BufferedWriter(fileWriter);
			if(!fileObj.exists()) {
				fileObj.createNewFile();
			}
		}
		
		protected abstract void addNewRow() throws IOException;
		
		protected void finish() {
			try {
				fOutput.close();
				fileWriter.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class OpinionDensityDataWriter extends DataWriter {
		public static final int id = 0;
		
		public OpinionDensityDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}

		/*public OpinionDensityDataWriter() throws IOException {
			id = 0;
			initialize(fileName);
			//printToConsole("Instantiating opinion density collection...");
		}*/
		
		//do this for every value of epsilon
		protected void addNewRow() throws IOException {
			//printToConsole("Writing data from epsilon value " + round(epsilon) + " to file.");
			String newRow = "";
			for(int i = 0; i < opAverageSet.length; i++) {
				newRow += dataIndpVarString() + " ";
				newRow += round((double)i / opAverageSet.length) + " ";
				newRow += round(opAverageSet[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
	private class RealizationFractionByOCDataWriter extends DataWriter {
		public static final int id = 1;
		
		public RealizationFractionByOCDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			
			newRow += dataIndpVarString() + " ";
			newRow += round(ocNonConsensusRatio) + "\n";
			//newRow += "HERLLO WORLD";
			
			fOutput.write(newRow);
		}
	}
	
	private class OpinionClusterDataWriter extends DataWriter {
		public static final int id = 2;
		
		public OpinionClusterDataWriter(String fName) throws IOException {
			super(fName, true);
			initialize();
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			//column 1: epsilon value being measured
			newRow += dataIndpVarString() + " ";
			
			//column 2: average number of opinion clusters per group
			newRow += round(ocOccurranceByGroup) + " ";
			
			//column 3: average number of opinion clusters by population
			newRow += round(ocOccurranceByPopulation) + "\n";
			
			fOutput.write(newRow);
		}
	}
	
	private class GroupSizeDistributionDataWriter extends DataWriter {
		
		public GroupSizeDistributionDataWriter(String fName) throws IOException {
			super(fName, false);
		}
		
		//needs to be completed
		protected void addNewRow() throws IOException {
			
		}
		
	}

	private class OCDistDataWriter extends DataWriter {
		public static final int id = 3;
		private final int type;
		
		public OCDistDataWriter(String fName, int t) throws IOException {
			super(fName, true);
			type = t;
			initialize(); 
		}

		public void addNewRow() throws IOException {
			if(type == 0) addNewRow(ocPopAverageSet);
			else if (type == 1) addNewRow(ocGroupAverageSet);
		}
		
		protected void addNewRow(double[] ocDist) throws IOException {
			String newRow = "";
			for(int i = 0; i < ocDist.length; i++) {
				newRow += dataIndpVarString() + " ";
				newRow += round((double)i / ocDist.length) + " ";
				newRow += round(ocDist[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
}
