package com.thangtruong19.mapdemo2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 15/11/2018.
 */

public class DataParser {
    private HashMap<String,String> getPlace(JSONObject googlePlaceJson){
        HashMap<String,String> googlePlacesMap=new HashMap<>();

        String placeName="-NA-";
        String vinicity="-NA-";
        String latitude="";
        String longtitude="";
        String reference="";

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vinicity")) {
                placeName = googlePlaceJson.getString("vinicity");
            }
            latitude=googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longtitude=googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference=googlePlaceJson.getString("reference");

            googlePlacesMap.put("place_name",placeName);
            googlePlacesMap.put("vicinity",vinicity);
            googlePlacesMap.put("lat",latitude);
            googlePlacesMap.put("lng",longtitude);
            googlePlacesMap.put("reference",reference);

        }catch (JSONException e){
            e.printStackTrace();
        }

        return googlePlacesMap;

    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        int count=jsonArray.length();
        List<HashMap<String,String>> placesList=new ArrayList<>();
        HashMap<String,String> placeMap=null;

        for (int i=0;i<count;i++){
            try {
                placeMap=getPlace((JSONObject)jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray=null;
        JSONObject jsonObject;
        try {
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }
}
