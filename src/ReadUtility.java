import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class ReadUtility {
	
	private MongoClient client;
	private DB db;
	private String packageDataSetCollection = "packageDataSets", packageFamilyCollection = "packageFamilies", fileCollection = "files",
			packageCollection = "packages", parentFileCollection = "parentFiles", sourceCollection = "sources";
	private HashMap<String, String> dataLocationMap;
	private String
			packageFamilyPrefix = "packageFamily.",
			packagePrefix = "package.",
			parentFilePrefix = "parentFile.",
			sourcePrefix = "source.";
	public final static byte INPUT_OBJECTID = 0, INPUT_PACKAGEDATASETID = 1, INPUT_PACKAGEFAMILYID = 2, INPUT_FILEID = 3,
							INPUT_PACKAGEID = 4, INPUT_PARENTFILEID = 5, INPUT_SOURCEID = 6, INPUT_SHA1 = 7, INPUT_MD5 = 8,
							INPUT_FIRSTSEEN = 9, INPUT_LASTPROCESSED = 10, INPUT_LASTRETRIEVED = 11, INPUT_PROCESSINGSTARTED = 12,
							INPUT_VENDORNAME = 13, INPUT_TAG = 14, INPUT_LASTMODIFIED = 15, INPUT_FAMILYNAME = 16, INPUT_NAMERELATION = 17;
	public final static byte OUTPUT_PACKAGEDATASET = -1, OUTPUT_PACKAGEFAMILY = -2, OUTPUT_FILE = -3, OUTPUT_PACKAGE = -4, OUTPUT_SOURCE = -5,
							OUTPUT_PARENTFILE = -6;
	public final static byte EXACT_STRING_SEARCH_TYPE = 0, SUBSTRING_SEARCH_TYPE = 1;
	
	public ReadUtility(MongoClient client){
		this.client = client;
		setupMap();
	}
	
	public void setDB(String dbname){
		this.db = client.getDB(dbname);
	}
	
	public JSONObject getJSON(String id, String collectionName){
		JSONObject tempJSON = null;
		DBObject temp = null;
		DBCollection col = db.getCollection(collectionName);
		temp = col.findOne(new BasicDBObject("_id", new ObjectId(id)));
		try {
			tempJSON =  new JSONObject(temp.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempJSON;
	}
	
	private void setupMap(){
		dataLocationMap = new HashMap<>();
		dataLocationMap.put("objectId", "_id");
		dataLocationMap.put("packageDataSetId", "packageDataSetId");
		dataLocationMap.put("packageDataSet.processingStarted", "processingStarted");
		dataLocationMap.put("packageId", "packageId");
		dataLocationMap.put("packageFamilyId", "packageFamilyId");
		dataLocationMap.put("sourceId", "sourceId");
		dataLocationMap.put("parentFileId", "parentFileId");
		dataLocationMap.put("fileId", "fileId");
		dataLocationMap.put("packageFamily.vendorName", "vendor.name");
		dataLocationMap.put("file.tag", "detailedInformation.information.tags");
		dataLocationMap.put("file.sha1", "identifier.sha1");
		dataLocationMap.put("file.md5", "identifier.md5");
		dataLocationMap.put("file.firstSeen", "detailedInformation.information.firstSeen");
		dataLocationMap.put("file.lastProcessed", "detailedInformation.information.lastProcessed");
		dataLocationMap.put("file.lastRetrieved", "detailedInformation.information.lastRetrieved");
		dataLocationMap.put("parentFile.tag", "packageFileInformation.tags");
		dataLocationMap.put("parentFile.md5", "fileMetadata.identifier.md5");
		dataLocationMap.put("parentFile.sha1", "fileMetadata.identifier.sha1");
		dataLocationMap.put("parentFile.firstSeen", "packageFileInformation.firstSeen");
		dataLocationMap.put("parentFile.lastProcessed", "packageFileInformation.lastProcessed");
		dataLocationMap.put("parentFile.lastRetrieved", "packageFileInformation.lastRetrieved");
		dataLocationMap.put("package.tag", "tags");
		dataLocationMap.put("package.vendorName", "vendorName");
		dataLocationMap.put("package.familyName", "familyName");
		dataLocationMap.put("package.displayName", "displayName");
		dataLocationMap.put("package.name", "name");
		dataLocationMap.put("source.lastModified", "sourceInformation.lastModified");
		dataLocationMap.put("source.sha1", "sourceInformation.identifier.sha1");
	}
	
	private ArrayList<String> searchDB(Object searchValue, String collectionName, String dataLocation, byte queryType){
		ArrayList<String> ids = new ArrayList<String>();
		DBCollection group = db.getCollection(collectionName);
		DBCursor cursor = null;
		if(queryType == EXACT_STRING_SEARCH_TYPE){
			cursor = group.find(
					new BasicDBObject(dataLocation, searchValue),
					new BasicDBObject("_id", 1)
					);
		}
		else if(queryType == SUBSTRING_SEARCH_TYPE){
			Pattern pattern = Pattern.compile(searchValue.toString(), Pattern.CASE_INSENSITIVE);
			cursor = group.find(
					new BasicDBObject(dataLocation, pattern),
					new BasicDBObject("_id", 1)
					);
		}
		while(cursor.hasNext()){
			String s = cursor.next().get("_id").toString();
			ids.add(s);
		}
		return ids;
	}
	
	public ArrayList<String> search(int outputType, int inputType, String searchValue){
		String dataLocation = "", collectionName = "";
		if(outputType == OUTPUT_PACKAGEDATASET){
			collectionName = packageDataSetCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEFAMILYID){
				dataLocation = packageFamilyPrefix + dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEID){
				dataLocation = packagePrefix + dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PARENTFILEID){
				dataLocation = parentFilePrefix + dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_SOURCEID){
				dataLocation = sourcePrefix + dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_FILEID){
				DBCollection col = db.getCollection(fileCollection);
				DBObject obj = col.findOne(new BasicDBObject("_id", new ObjectId(searchValue)));
				searchValue = obj.get("packageDataSetId").toString();
				dataLocation = dataLocationMap.get("objectId");
				collectionName = packageDataSetCollection;
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PROCESSINGSTARTED){
				dataLocation = dataLocationMap.get("packageDataSet.processingStarted");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
		}
		else if(outputType == OUTPUT_PACKAGEFAMILY){
			collectionName = packageFamilyCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEDATASETID){
				dataLocation = dataLocationMap.get("packageDataSetId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEID){
				dataLocation = dataLocationMap.get("packageId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_VENDORNAME){
				dataLocation = dataLocationMap.get("packageFamily.vendorName");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
		}
		else if(outputType == OUTPUT_FILE){
			collectionName = fileCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEDATASETID){
				dataLocation = dataLocationMap.get("packageDataSetId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PARENTFILEID){
				dataLocation = dataLocationMap.get("parentFileId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_TAG){
				dataLocation = dataLocationMap.get("file.tag");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_MD5){
				dataLocation = dataLocationMap.get("file.md5");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_SHA1){
				dataLocation = dataLocationMap.get("file.sha1");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_FIRSTSEEN){
				dataLocation = dataLocationMap.get("file.firstSeen");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_LASTPROCESSED){
				dataLocation = dataLocationMap.get("file.lastProcessed");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_LASTRETRIEVED){
				dataLocation = dataLocationMap.get("file.lastRetrieved");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
		}
		else if(outputType == OUTPUT_PACKAGE){
			collectionName = packageCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEDATASETID){
				dataLocation = dataLocationMap.get("packageDataSetId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEFAMILYID){
				dataLocation = dataLocationMap.get("packageFamilyId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PARENTFILEID){
				dataLocation = dataLocationMap.get("parentFileId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_SOURCEID){
				dataLocation = dataLocationMap.get("sourceId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_TAG){
				dataLocation = dataLocationMap.get("package.tag");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_VENDORNAME){
				dataLocation = dataLocationMap.get("package.vendorName");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_FAMILYNAME){
				dataLocation = dataLocationMap.get("package.familyName");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_NAMERELATION){
				Pattern pat = Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE);
				BasicDBList list = new BasicDBList();
				list.add(new BasicDBObject("displayName", pat));
				list.add(new BasicDBObject("familyName", pat));
				list.add(new BasicDBObject("vendorName", pat));
				list.add(new BasicDBObject("name", pat));
				DBCursor cursor = db.getCollection(collectionName).find(new BasicDBObject("$or", list));
				ArrayList<String> temp = new ArrayList<String>();
				while(cursor.hasNext())
					temp.add(cursor.next().get("_id").toString());
				return temp;
			}
		}
		else if(outputType == OUTPUT_PARENTFILE){
			collectionName = parentFileCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEDATASETID){
				dataLocation = dataLocationMap.get("packageDataSetId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEID){
				dataLocation = dataLocationMap.get("packageId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_TAG){
				dataLocation = dataLocationMap.get("parentFile.tag");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_VENDORNAME){
				dataLocation = dataLocationMap.get("parentFile.vendorName");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
		}
		else if(outputType == OUTPUT_SOURCE){
			collectionName = sourceCollection;
			if(inputType == INPUT_OBJECTID){
				dataLocation = dataLocationMap.get("objectId");
				return searchDB(new ObjectId(searchValue), collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEDATASETID){
				dataLocation = dataLocationMap.get("packageDataSetId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_PACKAGEID){
				dataLocation = dataLocationMap.get("packageId");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_SHA1){
				dataLocation = dataLocationMap.get("source.sha1");
				return searchDB(searchValue, collectionName, dataLocation, EXACT_STRING_SEARCH_TYPE);
			}
			else if(inputType == INPUT_LASTMODIFIED){
				dataLocation = dataLocationMap.get("source.lastModified");
				return searchDB(searchValue, collectionName, dataLocation, SUBSTRING_SEARCH_TYPE);
			}
		}
		return null;
	}
}