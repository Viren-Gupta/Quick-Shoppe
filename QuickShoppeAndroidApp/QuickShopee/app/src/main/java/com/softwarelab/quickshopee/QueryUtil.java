package com.softwarelab.quickshopee;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rehan on 31/10/17.
 */

public  class QueryUtil{
    DistanceCalculate calc;
    private static ListView list;
    private static Context mContext=null;
    private static double latitude=0;
    private static double longitude=0;
    public QueryUtil(Context mContext,double latitutde, double longitude,ListView list){
        this.calc=new DistanceCalculate(mContext,list);
        this.mContext = mContext;
        this.latitude = latitutde;
        this.longitude = longitude;
        this.list=list;

        try {
            getShopList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public  Comparator<ShopInfoWithDistance> comp=new Comparator<ShopInfoWithDistance>(){
        @Override
        public int compare(ShopInfoWithDistance ob1,ShopInfoWithDistance ob2){
            double diff=Double.valueOf(ob1.distance)-Double.valueOf(ob2.distance);
            if (diff<0)
                return -1;
            else if (diff>0)
                return 1;
            else
                return 0;
        }
    };
    protected HashMap<String,ShopInfoWithDistance>  getFinalData(){
        return calc.getData();
    }
    public void getShopList() throws JSONException {
        final String TAG = "PostDetailActivity";
        final HashMap<String,String> shopMapInJson=new HashMap<String,String>();
        final ArrayList<shopInfo> shopAddress=new ArrayList<shopInfo>();
        final ProgressDialog[] mProgressDialog = {null};
        new DatabaseRetrieval().mReadDataOnce(new OnGetDataListener() {
            @Override
            public void onStart() {
                if (mProgressDialog[0] == null) {
                    mProgressDialog[0] = new ProgressDialog(mContext);
                    mProgressDialog[0].setMessage("Loading");
                    mProgressDialog[0].setIndeterminate(true);
                }

                mProgressDialog[0].show();
            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Gson gson = new Gson();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    shopMapInJson.put(data.getKey(),gson.toJson(data.getValue()));
                }
                for (Map.Entry<String,String> entry : shopMapInJson.entrySet()){
                    JSONObject jsonObject= null;
                    try {
                        jsonObject = new JSONObject(entry.getValue());
                        shopInfo address=new shopInfo(jsonObject.getDouble("latitude"),
                                jsonObject.getDouble("longitude"),
                                jsonObject.getString("name"),
                                entry.getKey(),
                                jsonObject.getString("city"));
                        shopAddress.add(address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getNearShop(shopAddress);
                if (mProgressDialog[0] != null && mProgressDialog[0].isShowing()) {
                    mProgressDialog[0].dismiss();
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getNearShop(ArrayList<shopInfo> shopAddress){
        ArrayList<shopInfo> nearShop=new ArrayList<shopInfo>();
        ArrayList<ShopInfoWithDistance> nearShopWithTheDistance=new ArrayList<ShopInfoWithDistance>();


        Location current1 = new  Location("");
        current1.setLatitude(latitude);
        current1.setLongitude(longitude);

        Log.d("       Calling function","Hello");

        Iterator<shopInfo> it=shopAddress.iterator();
        while (it.hasNext()){
            Location dest=new Location("");
            shopInfo shop=it.next();
            dest.setLatitude(shop.getLatitude());
            dest.setLongitude(shop.getLongitude());
            double distance = current1.distanceTo(dest);

            //adding the shop which are within 10kms of distance
            if ((distance/1000)<10)
                nearShopWithTheDistance.add(new ShopInfoWithDistance(shop,String.valueOf(distance)));
            Log.d("latitude = "+latitude," longitude "+longitude);
            Log.d("latitude = ",String.valueOf(shop.getLatitude())+" Longitude = "+String.valueOf(shop.getLongitude()));
            Log.d("dist = ",String.valueOf(distance/1000));
        }

        LatLng current =new LatLng(latitude,longitude);
        Collections.sort(nearShopWithTheDistance,comp);
        Iterator<ShopInfoWithDistance> iter=nearShopWithTheDistance.iterator();
        while (iter.hasNext()){
            nearShop.add(iter.next().info);
        }

        Log.d("size = ",String.valueOf(nearShop.size()));
        HashMap<String,ShopInfoWithDistance> shopDistance=calc.calcDistance(current,nearShop);

    }

}