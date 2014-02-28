import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class JSONUtility {

	//collections in DB
	private final String 	packageDataSetCollection = "packageDataSets",
							packageFamilyCollection = "packageFamilies",
							packageCollection = "packages",
							sourceCollection = "sources",
							parentFileCollection = "parentFiles",
							fileCollection = "files";
	private final String	packageFamilyPrefix = "packageFamily",
							packagePrefix = "package",
							sourcePrefix = "source",
							parentFilePrefix = "parentFile",
							filePrefix = "files";
	
	//The MongoClient to be used when communicating with MongoDB
	MongoClient client;
	
	//The database to be used
	DB db;
	
	/**
	 * Constructor for JSONUtility
	 * @param client The MongoClient to be used when communicating with MongoDB
	 */
	public JSONUtility(MongoClient client){
		this.client = client;
	}
	
	/**
	 * Set the DB to use
	 * @param dbname The name of the database
	 */
	public void setDB(String dbname){
		this.db = client.getDB(dbname);
	}
	
	/**
	 * 1. Read xml file contents<br/>
	 * 2. Extract parts from the xml file<br/>
	 * 3. Convert parts to JSON<br/>
	 * 4. Insert parts to DB<br/>
	 * 5. Link objects using their ObjectIds<br/>
	 * @param filename The filename(including the complete path) of the xml file to be transferred
	 * @return <b>True</b> if successful, <b>False</b> if failed.
	 */
	
	public boolean transfer(String filename){
		
		//Initialize Objects
		
		DBObject source, processedPackage, pkg, family, rootFile, document = null;
		
		//Container for XML file contents
		ArrayList<String> chunks = new ArrayList<String>();
		
		//populate xml file container
		chunks = getXML(filename);
		
		//GET PACKAGE MEMBERS (FILES)
		ArrayList<DBObject> files = new ArrayList<DBObject>();
		String s = "";
		while(!(s = removeXmlPart(chunks, "<packageMember", "</packageMember>")).equals(""))
			files.add((DBObject) xmlToJSON(s).get("packageMember"));
		
		//GET SOURCE
		String sourceString = removeXmlPart(chunks, "<processSource", "</processSource>");
		source = (DBObject) xmlToJSON(sourceString).get("processSource");
		
		//GET PROCESSED PACKAGE
		String processedPackageString = removeXmlPart(chunks, "<processedPackage", "</processedPackage>");
		processedPackage = (DBObject) xmlToJSON(processedPackageString).get("processedPackage");
		
		//FROM PROCESSED PACKAGE, EXTRACT FAMILY
		family = (DBObject) processedPackage.removeField("packageFamily");
		
		//FROM PROCESSED PACKAGE, EXTRACT PKG
		pkg = (DBObject) processedPackage.removeField("packageInformation");
		
		//CONSTRUCT ROOT FILE
		//FROM PROCESSED PACKAGE, EXTRACT FILEMETADATA (PART OF ROOT FILE), AFTER THIS PROCESSED PACKAGE SHOULD BE EMPTY
		//REMOVE PART OF ROOT FILE FORM PKG
		rootFile = new BasicDBObject();
		rootFile.put("fileMetadata", (DBObject) processedPackage.removeField("fileMetadata"));
		rootFile.put("packageFileInformation", (DBObject) pkg.removeField("packageFileInformation"));
		
		//CREATE DOCUMENT FROM THE REMAINING CONTENTS OF 'CHUNKS'
		String documentString = "";
		for(String string : chunks)
			documentString += string;
		//check root name
		if(documentString.indexOf("ns3:processPackageDataSet") != -1){
			document = (DBObject) xmlToJSON(documentString).get("ns3:processPackageDataSet");
		}
		else
			document = (DBObject) xmlToJSON(documentString).get("processPackageDataSet");
		
		//BEGIN TRANSFER
		
		//set variable
		String docId, famId, pkgId, srcId, rootFileId;
		ArrayList<String> filesId = new ArrayList<String>();
		
		//INSERT 'document'
		
		db.getCollection(packageDataSetCollection).insert(document);
		//get id as docId
		docId = document.get("_id").toString();
		
		//INSERT 'family'
		db.getCollection(packageFamilyCollection).insert(family);
		//get id as famId
		famId = family.get("_id").toString();
		
		//INSERT 'pkg'
		db.getCollection(packageCollection).insert(pkg);
		//get id as pkgId
		pkgId = pkg.get("_id").toString();
		
		//INSERT SOURCE
		db.getCollection(sourceCollection).insert(source);
		//get id as srcId
		srcId = source.get("_id").toString();
		
		//INSERT ROOT FILE
		db.getCollection(parentFileCollection).insert(rootFile);
		//get id as rootFileId
		rootFileId = rootFile.get("_id").toString();
		
		//INSERT SINGLE FILES
		DBCollection filesGroup = db.getCollection(fileCollection);
		if(files.size() > 0)
			for(DBObject obj : files){
				filesGroup.insert(obj);
				filesId.add(obj.get("_id").toString());
			}
		
		//fix links
		
		//insert document id
		db.getCollection(sourceCollection).update(	new BasicDBObject("_id", new ObjectId(srcId)),
							new BasicDBObject("$set", new BasicDBObject("packageDataSetId", docId)));
		source.put("packageDataSetId", docId);
		db.getCollection(packageCollection).update(	new BasicDBObject("_id", new ObjectId(pkgId)),
							new BasicDBObject("$set", new BasicDBObject("packageDataSetId", docId)));
		pkg.put("packageDataSetId", docId);
		db.getCollection(packageFamilyCollection).update(	new BasicDBObject("_id", new ObjectId(famId)),
							new BasicDBObject("$set", new BasicDBObject("packageDataSetId", docId)));
		family.put("packageDataSetId", docId);
		db.getCollection(parentFileCollection).update(	new BasicDBObject("_id", new ObjectId(rootFileId)),
							new BasicDBObject("$set", new BasicDBObject("packageDataSetId", docId)));
		rootFile.put("packageDataSetId", docId);
		for(int a = 0; a < files.size(); a++){
			filesGroup.update(	new BasicDBObject("_id", new ObjectId(filesId.get(a))),
					new BasicDBObject("$set", new BasicDBObject("packageDataSetId", docId)));
			files.get(a).put("packageDataSetId", docId);
		}
		
		//insert package id
		db.getCollection(sourceCollection).update(	new BasicDBObject("_id", new ObjectId(srcId)),
				new BasicDBObject("$set", new BasicDBObject("packageId", pkgId)));
		source.put("packageId", pkgId);
		db.getCollection(packageFamilyCollection).update(	new BasicDBObject("_id", new ObjectId(famId)),
				new BasicDBObject("$set", new BasicDBObject("packageId", pkgId)));
		family.put("packageId", pkgId);
		db.getCollection(parentFileCollection).update(	new BasicDBObject("_id", new ObjectId(rootFileId)),
				new BasicDBObject("$set", new BasicDBObject("packageId", pkgId)));
		rootFile.put("packageId", pkgId);
		
		//insert source id
		db.getCollection(packageCollection).update(	new BasicDBObject("_id", new ObjectId(pkgId)),
				new BasicDBObject("$set", new BasicDBObject("sourceId", srcId)));
		pkg.put("sourceId", srcId);
		
		//insert family id
		db.getCollection(packageCollection).update(	new BasicDBObject("_id", new ObjectId(pkgId)),
				new BasicDBObject("$set", new BasicDBObject("packageFamilyId", famId)));
		pkg.put("packageFamilyId", famId);
		
		//insert rootfile id
		db.getCollection(packageCollection).update(	new BasicDBObject("_id", new ObjectId(pkgId)),
				new BasicDBObject("$set", new BasicDBObject("parentFileId", rootFileId)));
		pkg.put("parentFileId", rootFileId);
		for(int a = 0; a < files.size(); a++){
			filesGroup.update(	new BasicDBObject("_id", new ObjectId(filesId.get(a))),
					new BasicDBObject("$set", new BasicDBObject("parentFileId", rootFileId)));
			files.get(a).put("parentFileId", rootFileId);
		}
		
		//insert ObjectIds of files to rootFiles
		
		if(files.size() > 0){
			BasicDBList fileList = new BasicDBList();
			for(DBObject o : files)
				fileList.add(o.get("_id").toString());
			
			db.getCollection(parentFileCollection).update(	new BasicDBObject("_id", new ObjectId(rootFileId)),
					new BasicDBObject("$set", new BasicDBObject(filePrefix, fileList)));
			
			rootFile.put(filePrefix, fileList);
		}
		
		//insert source to document
		db.getCollection(packageDataSetCollection).update(
				new BasicDBObject("_id", new ObjectId(docId)),
				new BasicDBObject("$set", new BasicDBObject(sourcePrefix, source)));
		
		//insert package to document
		db.getCollection(packageDataSetCollection).update(
				new BasicDBObject("_id", new ObjectId(docId)),
				new BasicDBObject("$set", new BasicDBObject(packagePrefix, pkg)));
		
		//insert family to document
		db.getCollection(packageDataSetCollection).update(
				new BasicDBObject("_id", new ObjectId(docId)), 
				new BasicDBObject("$set", new BasicDBObject(packageFamilyPrefix, family)));
		
		//insert rootFile to document
		db.getCollection(packageDataSetCollection).update(
				new BasicDBObject("_id", new ObjectId(docId)),
				new BasicDBObject("$set", new BasicDBObject(parentFilePrefix, rootFile)));
		
		return true;
	} 
	
	/**
	 * Extracts a part of XML contents using specified startString and endString
	 * @param chunks The XML contents
	 * @param startString Start extracting from the position of this string
	 * @param endString Stop extracting when the position of the last character of this String is found.
	 * @return Extracted XML part
	 */
	public String removeXmlPart(ArrayList<String> chunks, String startString, String endString){
		int startLine = 0, endLine = -1, startPos = -1, endPos = -1;
		String temp = "";
		//get startline and start position
		while(startPos == -1){
			startPos = chunks.get(startLine).indexOf(startString);
			if(startPos != -1)
				break;
			startLine++;
			if(startLine >= chunks.size())
				return "" ;
		}
		//get endLine and endpos + endstring length
		endLine = startLine;
		while(endPos == -1){
			endPos = chunks.get(endLine).indexOf(endString);
			if(endPos != -1)
				break;
			endLine++;
		}
		//join things up
		if(endLine - startLine == 1){
			temp = chunks.get(startLine).substring(startPos) + chunks.get(endLine).substring(0, endPos + endString.length());
		}
		else{
			String a = chunks.get(startLine).substring(startPos), b = chunks.get(endLine).substring(0, endPos + endString.length()), c = "";
			for(int d = startLine + 1; d < endLine; d++){
				c += chunks.get(d);
			}
			temp = a + c + b;
		}
		
		//remove gathered things
		if(endLine == startLine){
				
				String a = "", b = "";
				temp = chunks.get(startLine).substring(startPos, endPos + endString.length());
				a = chunks.get(startLine).substring(0, startPos);
				b = chunks.get(startLine).substring(endPos + endString.length(), chunks.get(startLine).length());
				chunks.set(startLine, a + b);
		}
		else{
			chunks.set(startLine, chunks.get(startLine).substring(0, startPos));
			chunks.set(endLine, chunks.get(endLine).substring(endPos + endString.length(),chunks.get(endLine).length()));
			for(int a = 0; a < endLine - startLine - 1; a++)
				chunks.remove(startLine + 1);
		}
		//remove empty chunks
		int indexer = 0;
		while(indexer < chunks.size()){
			if(chunks.get(indexer).trim().equals(""))
				chunks.remove(indexer);
			else
				indexer++;
			if(indexer >= chunks.size())
				break;
		}
		return temp;
	}
	
	/**
	 * Read XML file contents, save as ArrayList of String
	 * @param filename The filename of xml file to read
	 * @return XML file content as ArrayList of String
	 */
	ArrayList<String> getXML(String filename){
		ArrayList<String> chunks = null;
		try {
			chunks = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String chunk = "", line;
			while((line = reader.readLine()) != null){
				chunk += line.trim();
				if(chunk.length() >= 1024){
					chunks.add(chunk);
					chunk = "";
				}
			}
			if(chunk.length() > 0)
				chunks.add(chunk);
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return chunks;
	}
	
	/**
	 * Convert valid XML into JSON
	 * @param xml The valid xml file as String
	 * @return DBObject (JSONObject)
	 */
	DBObject xmlToJSON(String xml){
		DBObject temp = null;
		try {
			temp = (DBObject) JSON.parse(XML.toJSONObject(xml).toString());
		} catch (JSONException e) {
			System.out.println(e.getLocalizedMessage());
			// TODO Auto-generated catch block
		}
		return temp;
	}
}