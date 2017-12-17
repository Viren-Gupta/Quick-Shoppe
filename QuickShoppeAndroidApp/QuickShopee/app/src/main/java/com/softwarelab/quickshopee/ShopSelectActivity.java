package com.softwarelab.quickshopee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopSelectActivity {


    private ListView shopsListView;

    private SharedPreferences sharedpref;
    public static final String OrderNumberDetailPref = "OrderNumberDetailPref";


    public static final String ORDER_NUMBER_STRING = "ORDER_NUMBER_STRING";
    public static final String SHOP_ID_STRING = "SHOP_ID_STRING";
    public static final String SHOP_NAME = "SHOP_NAME";

    private FirebaseDatabase database;
    Activity context;
    ShopSelectActivity(SharedPreferences shar,Activity con){
        context=con;
        sharedpref = shar;
        database = FirebaseDatabase.getInstance();
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_select);

        database = FirebaseDatabase.getInstance();



        final ArrayList<String> shopsArr = new ArrayList<>();
        shopsArr.add("easy_day_id");
        shopsArr.add("reliance_fresh_id");
        shopsArr.add("Shailendra_Trees_Shop");
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, shopsArr);

        shopsListView =  (ListView)findViewById(R.id.shopsListView);
        shopsListView.setAdapter(adapter);
        shopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int shopIndex = position;
                String shopId = shopsArr.get(shopIndex);
//                DatabaseConstants.createLoadingDialog(ShopSelectActivity.this);
                addCustomerToShop(shopId);
//                String orderNumber = String.valueOf("10");
//                saveOrderNumberLocally(orderNumber, shopId);
//                openHomeActivity(orderNumber, shopId);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DatabaseConstants.hideLoadingDialog();
        super.onBackPressed();
    }
*/
    public void checkAlreadyCustomerExists(){
//        sharedpref = getSharedPreferences(OrderNumberDetailPref, Context.MODE_PRIVATE);
        verifyOrderAlreadyPresent();
    }

    public void addCustomerToShop(String shopId,final String name) {


        Log.d(DatabaseConstants.LOG_STATEMENT, "Add Cutsomer to shop");
        DatabaseConstants.createLoadingDialog(context);

        final DatabaseReference databaseReference = database.getReference(shopId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int maxOrderNumber = -1;
                String orderNumber = String.valueOf(maxOrderNumber+1);
                if (dataSnapshot.hasChild(DatabaseConstants.customers) == true){
                    if (dataSnapshot.child(DatabaseConstants.customers).getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.child(DatabaseConstants.customers).getChildren()) {
//                            Log.d(DatabaseConstants.LOG_STATEMENT, String.valueOf(snapshot.getValue()));
//                            Log.d(DatabaseConstants.LOG_STATEMENT, String.valueOf(snapshot.getKey()));
                            Log.d(DatabaseConstants.LOG_STATEMENT, String.valueOf(databaseReference.getKey()));
                            int orderValue = Integer.valueOf(snapshot.getKey());
                            if (orderValue> maxOrderNumber){
                                maxOrderNumber = orderValue;
                            }
                        }
                    }
                    orderNumber  = String.valueOf(maxOrderNumber+1);
                    databaseReference.child(DatabaseConstants.customers).child(orderNumber).setValue(new Customer());
                } else {
                    databaseReference.child(DatabaseConstants.customers).setValue(orderNumber);
                    databaseReference.child(DatabaseConstants.customers).child(orderNumber).setValue(new Customer());
                }
                DatabaseConstants.hideLoadingDialog();


                String shopId = databaseReference.getKey();
                saveOrderNumberLocally(orderNumber, shopId,name);

//                DatabaseConstants.hideLoadingDialog();

                openHomeActivity(orderNumber, shopId,name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                DatabaseConstants.hideLoadingDialog();
            }
        });
    }

   /* @Override
    protected void onStart() {
        super.onStart();
//        DatabaseConstants.createLoadingDialog(ShopSelectActivity.this);

    }*/

    private void verifyOrderAlreadyPresent(){
        if (sharedpref.contains(ORDER_NUMBER_STRING) && sharedpref.contains(SHOP_ID_STRING)){
            String orderNumber = sharedpref.getString(ORDER_NUMBER_STRING, null);
            String shopId = sharedpref.getString(SHOP_ID_STRING, null);
            String name = sharedpref.getString(SHOP_NAME, null);
            checkOnlineDataForOrder(orderNumber, shopId,name);
        }
//        DatabaseConstants.hideLoadingDialog();
    }

    private void checkOnlineDataForOrder(final String orderNumber, final String shopId,final String name) {
        DatabaseConstants.createLoadingDialog(context);

        Log.d(DatabaseConstants.LOG_STATEMENT, "In Check Online for shop: "+shopId+ " and Order Number : "+orderNumber);
        DatabaseReference databaseReference =database.getReference(shopId);

        databaseReference.child(DatabaseConstants.customers).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DatabaseConstants.createLoadingDialog(context);
                        Log.d(DatabaseConstants.LOG_STATEMENT, "For : "+orderNumber+"  Found : "+dataSnapshot.hasChild(orderNumber));
                        if (dataSnapshot.hasChild(orderNumber) == true){

                            openHomeActivity(orderNumber, shopId,name);
                            Log.d(DatabaseConstants.LOG_STATEMENT, "In Data Snapshot with Values");
                        } else {
                            Log.d(DatabaseConstants.LOG_STATEMENT, "In Data Snapshot Is NULL");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        DatabaseConstants.createLoadingDialog(context);
                        Log.d(DatabaseConstants.LOG_STATEMENT, "Cancelled");
                    }
                });
    }



    private void saveOrderNumberLocally(String orderNumber, String shopId,String name){
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString(ORDER_NUMBER_STRING, orderNumber);
        editor.putString(SHOP_ID_STRING, shopId);
        editor.putString(SHOP_NAME, name);
        editor.commit();
    }


    private void openHomeActivity(String orderNumber, String shopId,String name){
        DatabaseConstants.hideLoadingDialog();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(SHOP_ID_STRING, shopId);
        intent.putExtra(SHOP_NAME, name);
        intent.putExtra(ORDER_NUMBER_STRING, orderNumber);
        Log.d("Hello","Done");
        context.startActivity(intent);
    }
}
