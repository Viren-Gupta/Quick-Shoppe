package com.softwarelab.quickshopee;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    Button btnShowLocation;

    TextView shopName ;
    Button btnGetShop;
    Button btnGetRoute;
    Button btnEnterShop;
    boolean isExecuted=false;
    ListView list;
    HashMap<String,ShopInfoWithDistance> finalData=null;
    QueryUtil query=null;
    double latitude=0;
    double longitude=0;
    ShopSelectActivity sh;

    // GPSTracker class
    GPSTracker gps;

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.CAMERA }, 101);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int locationPer = ContextCompat.checkSelfPermission(this,  android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int cameraPer = ContextCompat.checkSelfPermission(this,  android.Manifest.permission.CAMERA);
//        int storagePer = ContextCompat.checkSelfPermission(this,  android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d(DatabaseConstants.LOG_STATEMENT, "Permission Write: "+String.valueOf(cameraPer));

        if (locationPer != PackageManager.PERMISSION_GRANTED || cameraPer != PackageManager.PERMISSION_GRANTED) {
            Log.i(DatabaseConstants.LOG_STATEMENT, "Permission denied");
            makeRequest();
        }



        sh=new ShopSelectActivity(getSharedPreferences(ShopSelectActivity.OrderNumberDetailPref, Context.MODE_PRIVATE), MainActivity.this);
        sh.checkAlreadyCustomerExists();
        shopName = (TextView) findViewById(R.id.shopName);
        btnGetShop = (Button) findViewById(R.id.btnGetShop);
        btnGetRoute = (Button) findViewById(R.id.btnGetRoute);
        btnEnterShop = (Button) findViewById(R.id.btnEnterShop);
        list=(ListView) findViewById(R.id.listview);
        finalData=new HashMap<String,ShopInfoWithDistance>();
        // show location button click event

        btnGetShop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                isExecuted=true;
                list.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
                shopName.setText("");
                shopName.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                shopName.setHeight(0);

                // create class object
                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    query=new QueryUtil(MainActivity.this,latitude,longitude,list);


                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String message= (String) parent.getItemAtPosition(position);
                shopName.getLayoutParams().height = LinearLayout.LayoutParams.FILL_PARENT;
                shopName.setText("ENTERED SHOP:\n"+message);

            }
        });
        btnEnterShop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isExecuted==false)
                    Toast.makeText(getApplicationContext(),"Please Enter a Shop",Toast.LENGTH_SHORT).show();
                else
                    finalData = query.getFinalData();
                if (isExecuted==true && finalData.size()==0){
                    Toast.makeText(getApplicationContext(),"No Shop In Your Area",Toast.LENGTH_SHORT).show();
                }
                else if (isExecuted==true && finalData.size()>0){
                    String selectedShop= (String) shopName.getText();
                    if (selectedShop.length()>0){
                        String shopId=selectedShop.substring(14,selectedShop.length());
                        //Intent shopIntent = new Intent(MainActivity.this, ShopActivity.class);
                        String[] add=new String[2];
                        String id=parseShopId(selectedShop);

                        add[0]=finalData.get(id).info.getName();
                        add[1]=finalData.get(id).info.getId();
//                        ShopSelectActivity shhh=new ShopSelectActivity();
                        sh.addCustomerToShop(add[1],add[0]);

                    }
                    else
                        Toast.makeText(getApplicationContext(),"Please Select a Shop From List",Toast.LENGTH_SHORT).show();

                }
            }
        });
        btnGetRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isExecuted==false)
                    Toast.makeText(getApplicationContext(),"Please Enter a Shop",Toast.LENGTH_SHORT).show();
                else
                    finalData = query.getFinalData();
                if (isExecuted==true && finalData.size()==0){
                    Toast.makeText(getApplicationContext(),"No Shop In Your Area",Toast.LENGTH_SHORT).show();
                }
                else if (isExecuted==true && finalData.size()>0){
                    String selectedShop= (String) shopName.getText();
                    if (selectedShop.length()>0){
                        String shopId=selectedShop.substring(14,selectedShop.length());
                        Intent shopIntent = new Intent(MainActivity.this, MapActivity.class);
                        double[] add=new double[4];
                        add[0]=latitude;
                        add[1]=longitude;
                        String id=parseShopId(selectedShop);
                        /*Log.d("Check1","="+id+"=");
                        //Log.d("Checking",String.valueOf(finalData.get(id).info.getLatitude()));
                        for (Map.Entry<String,ShopInfoWithDistance> entry:finalData.entrySet()){
                            Log.d("Printitng" ,entry.getValue().info.getId()+" "+entry.getValue().info.getLatitude()+" "+
                                    entry.getValue().info.getLongitude()+" "+entry.getValue().info.getName());
                        }*/
                        add[2]=finalData.get(id).info.getLatitude();
                        add[3]=finalData.get(id).info.getLongitude();
                        shopIntent.putExtra("address",add);
                        startActivity(shopIntent);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Please Select a Shop From List",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    public String parseShopId(String shopName){
        String st="";
        int index=0;
        int tabSize=("    ").length();
        Log.d("size of tab",String.valueOf(tabSize));
        for (int i=shopName.length()-1;i>=0;i--){
            if (shopName.charAt(i)=='('){
                index=i;
                break;
            }
        }
        return shopName.substring(14,index-tabSize);
    }
}