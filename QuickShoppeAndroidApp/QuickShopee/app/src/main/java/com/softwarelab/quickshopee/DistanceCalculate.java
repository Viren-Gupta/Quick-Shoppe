package com.softwarelab.quickshopee;

/**
 * Created by rehan on 3/11/17.
 */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DistanceCalculate extends Activity {
    HashMap<String,ShopInfoWithDistance> shopAddressWithLocation;
    ArrayList<String> list_items;
    Context mContext=null;
    ListView list;
    ListAdapter adapter;
    FetchUtil fetch;
    DistanceCalculate(Context mContext,ListView list){
        this.mContext=mContext;
        this.list=list;
        list_items=new ArrayList<String>();
        fetch=new FetchUtil();
        shopAddressWithLocation=new HashMap<String,ShopInfoWithDistance>();
    }
    protected HashMap<String,ShopInfoWithDistance>  getData(){
        return this.shopAddressWithLocation;
    }
    public HashMap<String,ShopInfoWithDistance> calcDistance(LatLng origin,ArrayList<shopInfo> shopAddress){
        Iterator<shopInfo> it=shopAddress.iterator();

        adapter = new ArrayAdapter(mContext,android.R.layout.simple_expandable_list_item_1,list_items);
        list.setAdapter(adapter);       
        while (it.hasNext()){
            shopInfo shop=it.next();
            LatLng dest=new LatLng(shop.getLatitude(),shop.getLongitude());
            String url = fetch.getDistanceUrl(origin, dest);
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(new shopInfoWithUrl(shop,url));
        }
        return shopAddressWithLocation;
    }
    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<shopInfoWithUrl, Void, shopInfoWithUrl> {

        @Override
        protected shopInfoWithUrl doInBackground(shopInfoWithUrl... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = fetch.downloadUrl(url[0].url);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return (new shopInfoWithUrl(url[0].info,data));
        }

        @Override
        protected void onPostExecute(shopInfoWithUrl result) {
            super.onPostExecute(result);

            JSONObject jsonRespRouteDistance = null;
            try {
                if ((new JSONObject(result.url)
                        .get("status")
                        .toString()).equals("OK")){
                    jsonRespRouteDistance = new JSONObject(result.url)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray ("elements")
                            .getJSONObject(0)
                            .getJSONObject("distance");
                    String distance = jsonRespRouteDistance.get("text").toString();
                    shopAddressWithLocation.put(result.info.getId(),new ShopInfoWithDistance(result.info,distance));
                    list_items.add(result.info.getId()+"    "+"("+distance+")");

                    list.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, list_items));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    }
    private class shopInfoWithUrl{
        shopInfo info;
        String url;
        shopInfoWithUrl(shopInfo info,String url){
            this.info=info;
            this.url=url;
        }
    }
}
