import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Metrics {
	public int ThreadNum = 0;
	File fileObj;
	FileWriter fileWriter = null;
	BufferedWriter fOutput = null;
	
	public void init(){
		fileObj = new File(Constants._OUTPUT_PATH + "Metrics" + ThreadNum);
		if(!fileObj.exists()) {
			try {
				fileObj.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fileWriter = new FileWriter(fileObj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fOutput = new BufferedWriter(fileWriter);
	}
	public void gather(double epsilon, int migrations, int opinion_changes, int ticks){
		try {
			fOutput.write("For epsilon " + epsilon + " there were " + migrations + " migrations and " +
					opinion_changes + " opinion changes over " + ticks + " ticks");
			fOutput.write("\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Metrics gathered.");
	}
	public void gather(double epsilon, int migrations, int opinion_changes, int ticks, boolean trigger){
		try {
			fOutput.write("For mu " + epsilon + " there were " + migrations + " migrations and " +
					opinion_changes + " opinion changes over " + ticks + " ticks");
			fOutput.write("\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void gather(double epsilon, int migrations, int opinion_changes, int ticks, double muStart, double muEnd){
		try {
			fOutput.write("For epsilon " + epsilon + " there were " + migrations + " migrations and " +
					opinion_changes + " opinion changes over " + ticks + " ticks \n with a randomized " +
							"mu value between " + muStart + " and " + muEnd);
			fOutput.write("\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void gather(SimThread t) throws IOException{
		fOutput.write("For epsilon " + t.Constants._epsilon);
		if(Constants.muCheck && !Constants._murand){
			fOutput.write(" and mu " + t.Constants.muIncS);
		}
		fOutput.write(" there were " + t.sim.migrations + " migrations and " +
					t.sim.opinion_changes + " opinion changes over " + t.ticks + " ticks");
		if(Constants._murand){
			fOutput.write("\n with a randomized mu value between " + + Constants.randMuStart
					+ " and " + Constants.randMuEnd);
		}
		if(Constants.DynamicGroups){
			fOutput.write(". At the end there were " + t.sim.getnumGroups() + " groups.");
		}
		fOutput.write("\r\n");
	}
	public void close() {
		try {
			fOutput.close();
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
