import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import com.mongodb.MongoClient;

public class ConnectionManager implements Runnable{
	
	//MongoClient created from the UI
	private MongoClient client;
	
	//String input from UI's 'DB' textfield
	private String dbname;
	
	//Files fetched from the input folder selected in the UI
	private ArrayList<String> files;
	
	//Files successfully transferred to MongoDB
	private ArrayList<String> filesDone;
	
	//Files failed to transfer
	private ArrayList<String> failedTransfers;
	
	//Threads responsible for xml file conversion to json, extracting parts from the json, and inserting these parts to MongoDB
	//HardCoded to 5 threads
	private ArrayList<Thread> transmissionThreads;
	
	//The total size(in bytes) of all the files fetched(contents of ArrayList files)
	//Used to compute the percentage of completion
	private long totalSize;
	
	//The total size(in bytes) of all the files successfully transferred to MongoDB
	private volatile long processedSize;
	
	//Reference to UI
	//Used to update some components in UI and to get reference from the objects initialized from the UI
	private GridMongo parent;
	
	//Flags to be returned by the TransmissionThreads when they're done processing their files
	//Used by ConnectionManager to determine if all the TransmissionThreads are done doing their job
	private byte processFlags;
	
	//thread count created
	private byte createdThreads;
	
	/**
	 * Constructor fot ConnectionManager
	 * @param parent Reference to UI
	 */
	public ConnectionManager(GridMongo parent){
		this.parent = parent;
		this.client = parent.getClient();
		this.dbname = parent.getDBName();
		this.files = parent.getFiles();
		this.filesDone = new ArrayList<String>();
		failedTransfers = new ArrayList<String>();
		new Thread(this).start();
	}

	/**
	 * Used by TransmissionThread. Called when a file fails to be transferred.
	 * @param filename The filename of the file failed to be transferred.
	 */
	
	public void returnFailedTransfer(String filename){
		this.failedTransfers.add(filename);
	}
	
	/**
	 * Used by TransmissionThread. Called right after the TransmissionThread finishes its work.
	 */
	
	public void returnFlag(){
		processFlags += 1;
	}
	
	/**
	 * Main code
	 */
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//initialize processFlags as 0
		processFlags = 0;
		
		//initialize createdThreads to 0
		createdThreads = 0;
		
		//lock some components of the UI(just to be safe... and formal)
		parent.lockThings();
		
		//clear output txtarea
		parent.clearOutputArea();
		
		//Staring time of process
		long startTime = System.currentTimeMillis();
		
		//compute total size of files
		for(int a = 0; a < files.size(); a++)
			this.totalSize += new File(files.get(a)).length();
		
		{
			//Split input files into five chunks
			ArrayList<String> chunk1, chunk2, chunk3, chunk4, chunk5;
			chunk1 = new ArrayList<String>();
			chunk2 = new ArrayList<String>();
			chunk3 = new ArrayList<String>();
			chunk4 = new ArrayList<String>();
			chunk5 = new ArrayList<String>();
			for(int a = 0; a < files.size(); a += 5){
				if(a < files.size())
					chunk1.add(files.get(a));
				if(a + 1 < files.size())
					chunk2.add(files.get(a + 1));
				if(a + 2 < files.size())
					chunk3.add(files.get(a + 2));
				if(a + 3 < files.size())
					chunk4.add(files.get(a + 3));
				if(a + 4 < files.size())
					chunk5.add(files.get(a + 4));
			}
		
			//Distribute objects to new Transmission Threads
			transmissionThreads = new ArrayList<Thread>();
			if(!chunk1.isEmpty()){
				transmissionThreads.add(new TransmissionThread(this, chunk1));
				createdThreads++;
			}
			if(!chunk2.isEmpty()){
				transmissionThreads.add(new TransmissionThread(this, chunk2));
				createdThreads++;
			}
			if(!chunk3.isEmpty()){
				transmissionThreads.add(new TransmissionThread(this, chunk3));
				createdThreads++;
			}
			if(!chunk4.isEmpty()){
				transmissionThreads.add(new TransmissionThread(this, chunk4));
				createdThreads++;
			}
			if(!chunk5.isEmpty()){
				transmissionThreads.add(new TransmissionThread(this, chunk5));
				createdThreads++;
			}
		}
		
		while(processFlags != createdThreads){
			try {
				parent.displayTransferredFiles(filesDone.size());
				parent.displayFilesOnQueue((files.size() - filesDone.size()));
				parent.updateProgressBar((int)(((double)processedSize / totalSize) * 100));
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		parent.displayFilesOnQueue((files.size() - filesDone.size()));
		parent.displayTransferredFiles(filesDone.size());
		if(filesDone.size() == files.size())
			parent.updateProgressBar(100);
		parent.unlockThings();
		parent.print("Successfully transferred " + filesDone.size() + " out of " + files.size() + " files.");
		parent.print("Time: " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " seconds");
		files.clear();
		client.close();
	}
	
	/**
	 * Used by TransmissionThread
	 * @return Input at 'DB' textfield of UI
	 */
	
	public String getDBName(){
		return this.dbname;
	}
	
	/**
	 * Used by TransmissionThread
	 * @return MongoClient object from ConnectionManager
	 */
	
	public MongoClient getMongoClient(){
		return this.client;
	}
	
	/**
	 * Used by TransmissionThread
	 * @param file Filename of the file successfully transferred
	 */
	
	public void returnFile(String file){
		filesDone.add(file);
		processedSize += new File(file).length();
		parent.print("Transfer success for " + file);
	}
}