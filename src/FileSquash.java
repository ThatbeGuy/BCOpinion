import java.io.*;


public class FileSquash {
	private int iteration = 0;
	public void squash(int runs) throws IOException{
		for(String s : Constants.files){
			iteration = 0;
			for(int i = 0; i < runs; i++){
				File file = new File(Constants._OUTPUT_PATH + s + "_final.txt");
				if(!file.exists()) {
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				for(int j = 0; j < runs; j++){
					File copy = new File(fileIterator(s));
				    InputStream in = new FileInputStream(copy);
				    byte[] buf = new byte[1024];
				    int len;
				    while ((len = in.read(buf)) > 0) {
				       out.write(buf, 0, len);
				    }
				    in.close();
				    copy.deleteOnExit();
				}
				this.iteration=0;
				out.close();
			}
		}
	}
		
		
		/**
		squashFile(Constants._OUTPUT_PATH + "OpinionDensity_thread",".txt",runs);
		squashFile(Constants._OUTPUT_PATH + "NonConsensusRealizationRatio_thread", ".txt", runs);
		squashFile(Constants._OUTPUT_PATH + "OpinionClusters_thread", ".txt", runs);
		squashFile(Constants._OUTPUT_PATH + "Metrics", ".txt", runs); **/
	
	/** private void squashFile(String fStart, String fEnd, int runs) throws IOException {
		File file = new File(fStart + "_final" + fEnd);
		OutputStream out = new FileOutputStream(file);
		if(!file.exists()) {
			file.createNewFile();
		}
		for(int i = 0; i < runs; i++){
			File copy = new File(fStart + i + fEnd);
		    InputStream in = new FileInputStream(copy);
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		       out.write(buf, 0, len);
		    }
		    in.close();
		    copy.deleteOnExit();
		}
		out.close();
	}*/
	private String fileIterator(String str){
		iteration++;
		return Constants._OUTPUT_PATH + str + (iteration - 1);
	}
}
