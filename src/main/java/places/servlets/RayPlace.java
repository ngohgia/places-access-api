package places.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class RayPlace {
	private String id = "";
	private String mUserId = "";
	private String mCreatedBy = "";
	private String mGooglePlaceId = "";
	private String mGooglePlaceRef = "";
	private String mFoursquareId = "";
	private double mLat;
	private double mLng;
	private String mGeoHash;
	private String mShortGeoHash;
	private String mPlaceName;
	private String[] mCategories = {};
	private String mPhone;
	private String mAddress = "";
	private boolean mIsPublic;
	private LinkedHashMap<String, String> mAccessInfo = new LinkedHashMap<String, String>();
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setUserId(String uId){
		mUserId = uId;
	}
	
	public void setCreatedBy(String uId){
		mCreatedBy = uId;
	}
	
	public void setGooglePlaceId(String googlePlaceId){
		mGooglePlaceId = googlePlaceId;
	}
	
	public void setGooglePlaceRef(String googlePlaceRef){
		mGooglePlaceRef = googlePlaceRef;
	}
	
	public void setFoursquareId(String foursquareId){
		mFoursquareId = foursquareId;
	}
	
	public void setLat(double lat){
		mLat = lat;
	}
	
	public void setLng(double lng){
		mLng = lng;
	}
	
	public void updateGeoHash(){
		mGeoHash = GeoHash.encodeHash(mLat, mLng);
		mShortGeoHash = mGeoHash.substring(0, GeoHash.SHORT_HASHES);
	}
	
	public void setPlaceName(String name){
		mPlaceName = name;
	}
	
	public void setCategories(String[] cats){
		mCategories = cats;
	}
	
	public void setCategoriesFromJSONArr(JSONArray cats) throws Exception{
		ArrayList<String> catsArr = new ArrayList<String>();
		for (int i= 0; i< cats.length(); i++){
			catsArr.add(cats.getString(i));
		}
		
		mCategories = new String[catsArr.size()];
		mCategories = catsArr.toArray(mCategories);
	}
	
	public JSONArray getJSONArrFromCategories(){
		JSONArray cats = new JSONArray();
		
		for (int i= 0; i< mCategories.length; i++)
			cats.put(mCategories[i]);
		
		return cats;
	}
	
	public void setPhone(String phone){
		mPhone = phone;
	}
	
	public void setAddress(String address){
		mAddress = address;
	}
	
	public void setAccessInfo(LinkedHashMap<String, String> accessInfo){
		mAccessInfo = accessInfo;
	}
	
	public void setAccessInfoFromJSONArr(JSONArray infoArr) throws Exception{
		mAccessInfo = new LinkedHashMap<String, String>();
		
		for (int i= 0; i< infoArr.length(); i++){
			JSONObject info = infoArr.getJSONObject(i);
			
			Iterator<String> keys = info.keys();
			String key = keys.next();
			mAccessInfo.put(key, info.getString(key));
		}
	}
	
	public JSONArray getAccessInfoAsJSONArr() throws Exception{
		JSONArray infoArr = new JSONArray();
		
		for (Entry<String, String> entry : mAccessInfo.entrySet())
		{
		    JSONObject info = new JSONObject();
		    info.put(entry.getKey(), entry.getValue());
		    
		    infoArr.put(info);
		}
		
		return infoArr;
	}
	
	public void setIsPublic(boolean isPublic){
		mIsPublic = isPublic;
	}
	
/*	public void setPlaceInfoTag(String tag){
		mPlaceInfoTag = tag;
	}
	*/
	
	public String getId(){
		return this.id;
	}
	
	public String getUserId(){
		return mUserId;
	}
	
	public String getCreatedBy(){
		return mCreatedBy;
	}
	
	public String getGooglePlaceId(){
		return mGooglePlaceId;
	}
	
	public String getGooglePlaceRef(){
		return mGooglePlaceRef;
	}
	
	public String getFoursquareId(){
		return mFoursquareId;
	}
	
	public double getLat(){
		return mLat;
	}
	
	public double getLng(){
		return mLng;
	}
	
	public String getGeoHash(){
		return mGeoHash;
	}
	
	public String getShortGeoHash(){
		return mShortGeoHash;
	}
	
	public String getPlaceName(){
		return mPlaceName;
	}
	
	public String[] getPlaceType(){
		return mCategories;
	}
	
	public String getPhone(){
		return mPhone;
	}
	
	public String getAddress(){
		return mAddress;
	}
	
	public boolean getIsPublic(){
		return mIsPublic;
	}
	
	public HashMap<String, String> getAccessInfo(){
		return mAccessInfo;
	}
	
/*	public String getPlaceInfoTag(){
		return mPlaceInfoTag;
	}*/
	
	public String toString(){
		String tmp = "";
		tmp = "UserId: " + mUserId + 
				"Name: " + mPlaceName + ", Type: " + mCategories;
		return tmp;
	}
}
