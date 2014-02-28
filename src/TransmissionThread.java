import java.util.ArrayList;

class TransmissionThread extends Thread{

	//The chunk of files given to this Object
	private ArrayList<String> files;
	
	//Used for conversion and transmission of xml files
	private JSONUtility json;
	
	//Used to communicate with the DB
	private ConnectionManager parent;
	
	/**
	 * Constructor for TransmissionThread
	 * @param parent Reference to parentThread (ConnectionManager)
	 * @param files Chunk of files given by ConnectionManager
	 */
	public TransmissionThread(ConnectionManager parent, ArrayList<String> files){
		this.parent = parent;
		this.files = files;
		this.start();
	}
	
	/**
	 * Main work code
	 */
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//Initialize JSONUtility
		json = new JSONUtility(parent.getMongoClient());
		
		//Set DB
		json.setDB(parent.getDBName());
		
		//Temporary string container
		String string = "";
		
		//loop until files are empty
		while(!files.isEmpty()){
			string = files.remove(0);
			json.transfer(string);
			parent.returnFile(string);
		}
		
		//return flag after
		parent.returnFlag();
	}
	
}