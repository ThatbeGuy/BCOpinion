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
	protected double[] cumulativeGroupSize = new double[Constants._groups];
	private double avgExternalNeighbors = 0;
	
	//other constants
	//public final double epsValue;
	//Constants Constants = new Constants();
	private final boolean silent = false;
	private final boolean isVerbose;
	private final Number indVar;
	
	public static void initialize() {
		//make sure the files object is cleared beforehand
		Constants.files.clear();
		
		//create the subfolders yo
		String str[] = Constants._OUTPUT_PATH.split("'\\'|'/'");
		File fObj;
		String temp = "";
		try {
			for(int i = 0; i < str.length; i++) {
				fObj = new File(temp + str[i]);
				if(i < str.length-1 && !fObj.exists()) {
					if(!fObj.isDirectory()) fObj.mkdir();
				}
				else if(fObj.isFile()) fObj.createNewFile();
				temp += str[i] + "\\";
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		//assign each
		Constants.files.add("OpinionDensity");
		Constants.files.add("NCRR_N" + Constants._numnodes);
		Constants.files.add("OpinionClusters");
		Constants.files.add("OCPopDist");
		Constants.files.add("OCGroupDist");
		Constants.files.add("Metrics");//*/
	}
	
	public SimData(int tNum, Number indp, boolean verbose) {
		ThreadNum = tNum;
		indVar = indp;
		isVerbose = verbose;
		printToConsole("Initializing SimData object for independent variable value: " + round(indVar.doubleValue()));
		/*try {
			writerDensity = new OpinionDensityDataWriter(Constants.files.get(OpinionDensityDataWriter.id));
			writerOpinionCluster = new OpinionClusterDataWriter(Constants.files.get(OpinionClusterDataWriter.id));
			writerRealizationFraction = new RealizationFractionByOCDataWriter(Constants.files.get(RealizationFractionByOCDataWriter.id));
			writerGroupSizeDistribute = new GroupSizeDistributionDataWriter("GroupSizeDistribution");
		} catch(IOException e) {
			e.printStackTrace();
		}//*/
	}
	
	public void processEpsilonValue() {
		try {
			DataWriters = new ArrayList<DataWriter>();
			DataWriters.add(new OpinionDensityDataWriter(Constants.files.get(OpinionDensityDataWriter.id)));
			DataWriters.add(new OpinionClusterDataWriter(Constants.files.get(OpinionClusterDataWriter.id)));
			DataWriters.add(new RealizationFractionByOCDataWriter(Constants.files.get(RealizationFractionByOCDataWriter.id)));
			DataWriters.add(new OCDistDataWriter(Constants.files.get(OCDistDataWriter.id), 0));
			DataWriters.add(new OCDistDataWriter(Constants.files.get(OCDistDataWriter.id+1), 1));
			DataWriters.add(new GroupSizeDistributionDataWriter("GroupSizeDistribution"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		ocNonConsensusRatio /= Constants._trials;
		for(int i = 0; i < opAverageSet.length; i++) {
			opAverageSet[i] /= Constants._trials;
		}
		
		forcePrintToConsole("Finalizing data for independent variable value: " + round(indVar.doubleValue())
				+ "\n" + "Avg number of external neighbors: " + avgExternalNeighbors); 
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
		if(cTrial % (Constants._trials / 10 + 1) == 0) printToConsole("Adding results from trial " + cTrial + ".");
		
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
			opPos = (int) Math.round(a.opinion*50);
			opTrial[opPos] += 1;
		}
		
		for(int i = 0; i < opTrial.length; i++) {
			opTrial[i] /= agents.size();
			opAverageSet[i] += opTrial[i];
		}
	}
	
	protected void calculateCumulativeGroupSize(ArrayList<Group> gs) {
		int[] gSizes = new int[Constants._groups];
		for(int i = 0; i < gSizes.length; i++) {
			if(i >= gs.size()) gSizes[i] = 0;
			else gSizes[i] = gs.get(i).getAgents().size();
		}
		Arrays.sort(gSizes);
		
		int[] cGroupSize = new int[Constants._groups];
		cGroupSize[0] = gSizes[0];
		for(int i = 1; i < cGroupSize.length; i++) {
			cGroupSize[i] = cGroupSize[i-1] + gSizes[i];
		}
		
		for(int i = 0; i < Constants._groups; i++) {
			cumulativeGroupSize[i] += (double)cGroupSize[i] / Constants._trials;
		}
	}
	
	protected void calculateOpinionClusterPopAverage(ArrayList<Group> gs, ArrayList<Graph.OpinionCluster> ocSet) {
		int numClusters = 0;
		for(Graph.OpinionCluster o : ocSet) if(o.occurance > 1) numClusters++;
		
		ocOccurranceByPopulation += (double)numClusters / Constants._trials;
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
		
		ocOccurranceByGroup += average / Constants._trials;
	}
	
	protected void calculateOCPopDist(ArrayList<Graph.OpinionCluster> ocSet, int agentpool) {
		int ocPos;
		double[] ocTrial = new double[ocPopAverageSet.length];
		for(Graph.OpinionCluster o : ocSet) {
			ocPos = (int) Math.round(o.opVal*50);
			ocTrial[ocPos] += 1;
		}
		
		for(int i = 0; i < ocTrial.length; i++) {
			//ocTrial[i];
			ocPopAverageSet[i] += ocTrial[i] / Constants._trials;
		}
	}
	
	protected void calculateOCGroupDist(ArrayList<Group> gs, ArrayList<Graph.OpinionCluster> ocSet, int agentpool) {
		int numGroups = 0;
		//numClusters = ocSet.size();
		for(Group g: gs) if(g.getAgents().size() > 0) numGroups++;
		
		int ocPos;
		double[] ocTrial = new double[ocPopAverageSet.length];
		for(Group g: gs) {
			for(Graph.OpinionCluster o : ocSet) {
				if(o.ocGroup.equals(g)) {
					ocPos = (int) Math.round(o.opVal*50);
					ocTrial[ocPos] += (double)1 / numGroups;
				}
			}
		}
		
		for(int i = 0; i < ocTrial.length; i++) {
			//ocTrial[i];
			ocGroupAverageSet[i] += ocTrial[i] / Constants._trials;
		}
	}
	
	private void calculateExtNeighborAvg(ArrayList<Agent> agents) {
		int totalExtNeighbors = 0;
		for(Agent a : agents) {
			totalExtNeighbors += a.getNumExternalNeighbors();
		}
		double trAvg = (double)totalExtNeighbors / agents.size();
		avgExternalNeighbors += trAvg / Constants._trials;
	}
	
	protected void printToConsole(String s) {
		if(!silent && isVerbose) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	protected void forcePrintToConsole(String s) {
		if(!silent) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	protected static double round(double d) {
		DecimalFormat dFormat = new DecimalFormat("#.###");
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
			fileObj = new File(Constants._OUTPUT_PATH + fileName + ThreadNum);
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
				newRow += round(indVar.doubleValue()) + " ";
				newRow += round((double)i / 50) + " ";
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
			
			newRow += round(indVar.doubleValue()) + " ";
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
			newRow += round(indVar.doubleValue()) + " ";
			
			//column 2: average number of opinion clusters per group
			newRow += round(ocOccurranceByGroup) + " ";
			
			//column 3: average number of opinion clusters by population
			newRow += round(ocOccurranceByPopulation) + "\n";
			
			fOutput.write(newRow);
		}
	}
	
	private class GroupSizeDistributionDataWriter extends DataWriter {
		
		public GroupSizeDistributionDataWriter(String fName) throws IOException {
			super(fName + indVar, false);
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
				newRow += round(indVar.doubleValue()) + " ";
				newRow += round((double)i / 50) + " ";
				newRow += round(ocDist[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
}
