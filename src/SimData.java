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
	public OpinionDensityDataWriter writerDensity;
	public OpinionClusterDataWriter writerOpinionCluster;
	public RealizationFractionByOCDataWriter writerRealizationFraction;
	public GroupSizeDistributionDataWriter writerGroupSizeDistribute;
	public OCPopLocDataWriter writerOCPopDist;
	
	//The data being collected
	//public ArrayList<Double> opinionSet = new ArrayList<Double>();
	protected double[] opAverageSet = new double[51];
	protected double[] ocAverageSet = new double[51];
	
	protected double opAverageTotal = 0;
	//protected double ocAverageByPopulation = 0;
	protected double ocOccurranceByPopulation = 0;
	//protected double ocAverageByGroup = 0;
	protected double ocOccurranceByGroup = 0;
	protected double ocNonConsensusRatio = 0;
	protected double[] cumulativeGroupSize = new double[Constants._groups];
	
	//other constants
	//public final double epsValue;
	//Constants Constants = new Constants();
	private final boolean silent = false;
	private final boolean isVerbose;
	private final double epsilon;
	
	
	
	public static void initialize() {
		//make sure the files object is cleared beforehand
		Constants.files.clear();
		
		//assign each
		Constants.files.add("OpinionDensity");
		Constants.files.add("NCRR_N" + Constants._numnodes);
		Constants.files.add("OpinionClusters");
		Constants.files.add("OCPopDist");
		Constants.files.add("Metrics");//*/
	}
	
	public SimData(int tNum, double ep, boolean verbose) {
		ThreadNum = tNum;
		epsilon = ep;
		isVerbose = verbose;
		printToConsole("Initializing object...");
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
			writerDensity = new OpinionDensityDataWriter(Constants.files.get(OpinionDensityDataWriter.id));
			writerOpinionCluster = new OpinionClusterDataWriter(Constants.files.get(OpinionClusterDataWriter.id));
			writerRealizationFraction = new RealizationFractionByOCDataWriter(Constants.files.get(RealizationFractionByOCDataWriter.id));
			writerOCPopDist = new OCPopLocDataWriter(Constants.files.get(OCPopLocDataWriter.id));
			writerGroupSizeDistribute = new GroupSizeDistributionDataWriter("GroupSizeDistribution");
		} catch(IOException e) {
			e.printStackTrace();
		}
		ocNonConsensusRatio /= Constants._trials;
		for(int i = 0; i < opAverageSet.length; i++) {
			opAverageSet[i] /= Constants._trials;
		}
		
		printToConsole("Finalizing data for epsilon value: " + epsilon);
		//System.out.println(simDensity);
		/*printToConsole("Statistics gathered for epsilon value " + epsilon + ":\n"
				+ " opAverageTotal: " + opAverageTotal + "\n"
				+ " ocOccurranceByPopulation: " + ocOccurranceByPopulation + "\n"
				+ " ocOccurranceByGroup: " + ocOccurranceByGroup + "\n"
				+ " ocNonConsensusRatio: " + ocNonConsensusRatio + "\n"
				);*/
		try {
			writerDensity.addNewRow();
			writerOpinionCluster.addNewRow();
			writerRealizationFraction.addNewRow();
			writerOCPopDist.addNewRow();
		} catch(IOException e) {
			e.printStackTrace();
		}
		cTrial = 1;
	}
	
	public void processTrial(Graph g) {
		printToConsole("Adding results from trial " + cTrial + ".");
		
		modifyAverage(g.getAgents());
		calculateCumulativeGroupSize(g.getGroups());
		
		ArrayList<Graph.OpinionCluster> ocGroup = g.calculateOpinionClustersByGroup();
		ArrayList<Graph.OpinionCluster> ocPopulation = g.calculateOpinionClustersByPopulation();
		
		calculateOpinionClusterGroupAverage(g.getGroups(), ocGroup);
		calculateOpinionClusterPopAverage(g.getGroups(), ocPopulation);
		
		calculateOCPopDist(ocPopulation, g.agents.size());
		
		//for(Agent a: g.getAgents()) opinionSet.add(a.opinion);
		
		//move on to the next trial
		cTrial++;
	}
	
	public void finish() {
		printToConsole("Closing streams...");
		writerDensity.finish();
		writerOpinionCluster.finish();
		writerRealizationFraction.finish();
		writerOCPopDist.finish();
		
		
		//printToConsole("Four score and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal. Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this. But, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us -- that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion -- that we here highly resolve that these dead shall not have died in vain -- that this nation, under God, shall have a new birth of freedom -- and that government of the people, by the people, for the people, shall not perish from the earth.");
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
		for(Graph.OpinionCluster o : ocSet) if(o.occurance > 1) numClusters++;
		
		int numGroups = 0;
		for(Group g: gs) {
			if(g.getAgents().size() > 0) numGroups++; 
		}
		double average = (double)numClusters / numGroups;
		
		ocOccurranceByGroup += average / Constants._trials;
	}
	
	protected void calculateOCPopDist(ArrayList<Graph.OpinionCluster> ocSet, int agentpool) {
		int ocPos;
		double[] ocTrial = new double[ocAverageSet.length];
		for(Graph.OpinionCluster o : ocSet) {
			ocPos = (int) Math.round(o.opVal*50);
			ocTrial[ocPos] += o.occurance / agentpool;
		}
		
		for(int i = 0; i < ocTrial.length; i++) {
			//ocTrial[i];
			ocAverageSet[i] += ocTrial[i] / Constants._trials;
		}
	}
	
	protected void printToConsole(String s) {
		if(!silent && isVerbose) System.out.println("SimData(" + ThreadNum + ") : " + s);
	}
	
	protected static double round(double d) {
		DecimalFormat dFormat = new DecimalFormat("#.###");
		return Double.valueOf(dFormat.format(d));
	}
	
	private abstract class DataWriter {
		protected final String fileName;
		protected File fileObj;
		protected FileWriter fileWriter;
		protected BufferedWriter fOutput;
		
		public DataWriter(String fName) throws IOException {
			fileName = new String(fName);
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
			super(fName);
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
				newRow += round(epsilon) + " ";
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
			super(fName);
			initialize();
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			
			newRow += round(epsilon) + " ";
			newRow += round(ocNonConsensusRatio) + "\n";
			//newRow += "HERLLO WORLD";
			
			fOutput.write(newRow);
		}
	}
	
	private class OpinionClusterDataWriter extends DataWriter {
		public static final int id = 2;
		
		public OpinionClusterDataWriter(String fName) throws IOException {
			super(fName);
			initialize();
		}
		
		protected void addNewRow() throws IOException {
			String newRow = "";
			//column 1: epsilon value being measured
			newRow += round(epsilon) + " ";
			
			//column 2: average number of opinion clusters per group
			newRow += round(ocOccurranceByGroup) + " ";
			
			//column 3: average number of opinion clusters by population
			newRow += round(ocOccurranceByPopulation) + "\n";
			
			fOutput.write(newRow);
		}
	}
	
	private class GroupSizeDistributionDataWriter extends DataWriter {
		
		public GroupSizeDistributionDataWriter(String fName) throws IOException {
			super(fName + epsilon);
		}
		
		//needs to be completed
		protected void addNewRow() throws IOException {
			
		}
		
	}

	private class OCPopLocDataWriter extends DataWriter {
		public static final int id = 3;
		
		public OCPopLocDataWriter(String fName) throws IOException {
			super(fName);
			initialize();
		}

		protected void addNewRow() throws IOException {
			String newRow = "";
			for(int i = 0; i < ocAverageSet.length; i++) {
				newRow += round(epsilon) + " ";
				newRow += round((double)i / 50) + " ";
				newRow += round(ocAverageSet[i]) + " ";
				newRow += "\n";
			}
			fOutput.write(newRow);
		}
	}
	
}
