// backup

package com.softwarelab.quickshopee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class HomeActivity extends AppCompatActivity {

    // TextView barcodeResult;
    String shopName;
    String customerId;
    private int no_of_product=0;

    private SharedPreferences sharedPref;
    private FirebaseDatabase database;

    private DatabaseReference databaseReference;

    private ListView mListView;
    private ArrayList<Item> shoppingList;
    private ArrayList<String> itemKeys;
//    private ArrayAdapter<String> listAdapter ;
    private MyLVAdapter mylv;
    private DatabaseReference custRef;
    public static final String CHECKEDOUT_STRING = "CHECKEDOUT_STRING";
    public static final String ITEMS = "ITEMS";
    private static final String INTIAL_CHECKOUT = "0";
    private static final String CONFIRM_CHECKOUT = "1";
    private static final String CHECKEDOUT = "2";

    boolean listhasitem=false;
    private ImageView imageView;
    private TextView tview;


    private Button btn;
    private Button cancelbtn;
    FloatingActionButton fb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //barcodeResult=(TextView)findViewById(R.id.barcode_result);
        btn=(Button)findViewById(R.id.checkOut);
        cancelbtn=(Button)findViewById(R.id.cancelOrderBtn);
        fb=(FloatingActionButton)findViewById(R.id.scan_barcode);



        database = FirebaseDatabase.getInstance();

        sharedPref = getSharedPreferences(ShopSelectActivity.OrderNumberDetailPref, Context.MODE_PRIVATE);
        if (sharedPref.contains(CHECKEDOUT_STRING) == false) {
            setCheckedStatus(INTIAL_CHECKOUT);
        }
        if (sharedPref.contains(ITEMS) == false) {
            setQuant("false");
        }

        Intent intent = getIntent();
        shopName = intent.getStringExtra(ShopSelectActivity.SHOP_ID_STRING);
        customerId = intent.getStringExtra(ShopSelectActivity.ORDER_NUMBER_STRING);

        String shop = intent.getStringExtra(ShopSelectActivity.SHOP_NAME);
        setTitle(shop);

        Log.d(DatabaseConstants.LOG_STATEMENT,"     "+shopName+" "+customerId);
        if (shopName == null || customerId == null) {
            openShopSelectActivity();
        }
        databaseReference = database.getReference(shopName);
        custRef = databaseReference.child(DatabaseConstants.customers).child(customerId).child("purchases");
        goToLastState();
        mListView = (ListView) findViewById( R.id.mainListView );
        shoppingList = new ArrayList<Item>();
        itemKeys = new ArrayList<String>();
//        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, shoppingList);

        mylv = new MyLVAdapter(this, shoppingList);
        mListView.setAdapter(mylv);

        if(listhasitem==false){
            imageView=(ImageView)findViewById(R.id.iviewID);
            imageView.setAlpha(0.35f);
            tview=(TextView)findViewById(R.id.tviewID);

        }

        // working
        custRef.addChildEventListener(new ChildEventListener() {
            Item it1 = new Item();
            String name, price, qty;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                listhasitem=true;
                tview.setText("");
                imageView.setVisibility(View.GONE);
                it1 = dataSnapshot.getValue(Item.class);
                name = it1.getName();
                price = it1.getPrice();
                qty = it1.getQuantity();

                String itkey = dataSnapshot.getKey();
                itemKeys.add(itkey);

                shoppingList.add(it1);
                mylv.notifyDataSetChanged();

                Log.d(DatabaseConstants.LOG_STATEMENT,"Count of list "+mylv.getCount());
                if(mylv.getCount()>0)
                {
                    setQuant("true");

                    Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());
                }else
                {
                    setQuant("false");
                }

                Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                it1 = dataSnapshot.getValue(Item.class);
                name = it1.getName();
                price = it1.getPrice();
                qty = it1.getQuantity();

                String changedKey = dataSnapshot.getKey();

                int index = itemKeys.indexOf(changedKey);

                shoppingList.set(index, it1);
                mylv.notifyDataSetChanged();

                Log.d(DatabaseConstants.LOG_STATEMENT,"Count of list "+mylv.getCount());
                if(mylv.getCount()>0)
                {
                    setQuant("true");

                    Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());
                }else
                {
                    setQuant("false");
                }

                Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                it1 = dataSnapshot.getValue(Item.class);
                name = it1.getName();
                price = it1.getPrice();
                qty = it1.getQuantity();

                String removedentry = dataSnapshot.getKey();

                int index = itemKeys.indexOf(removedentry);

                shoppingList.remove(index);

                itemKeys.remove(index);

                mylv.notifyDataSetChanged();

                Log.d(DatabaseConstants.LOG_STATEMENT,"Count of list "+mylv.getCount());
                if(mylv.getCount()>0)
                {
                    setQuant("true");

                    Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());
                }else
                {
                    setQuant("false");
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setAlpha(0.35f);
                    tview.setText("Your cart seems to be empty !");
                }

                Log.d(DatabaseConstants.LOG_STATEMENT,"item "+getQuant());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ListAdapter", "ERROR from the list view");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeShopMenuBtn:
                changeShopClicked();
                //Toast.makeText(HomeActivity.this, "Cart Removed", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    /*add  click event to the scan barcode button*/
    public void scanBarcode(View v){
        Intent intent=new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(intent,0);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0)
        {

            if(resultCode==CommonStatusCodes.SUCCESS)
            {
                if(data!=null)
                {
                    Barcode barcode=data.getParcelableExtra("barcode");

                    //barcodeResult.setText("Barcode value : "+barcode.displayValue);

                    Intent intent=new Intent(this,ShowProduct.class);

                    intent.putExtra("Barcode",barcode.displayValue);

                    intent.putExtra("shopName",shopName);

                    intent.putExtra("customerId",customerId);

                    startActivityForResult(intent,1);


                }else{
                    // barcodeResult.setText("No barcode found");
                }
            }
        }
        else{

            super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You cannot go back!!" , Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
    }

    private String getCheckedStatus(){
        String status = sharedPref.getString(CHECKEDOUT_STRING, null);
        if (status == null){
            return INTIAL_CHECKOUT;
        }
        return status;
    }

    private void setCheckedStatus(String status){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CHECKEDOUT_STRING, status);
        editor.commit();
    }
    private String getQuant()
    {
        String status = sharedPref.getString(ITEMS, null);
        if (status == null){
            return "false";
        }
        return status;
    }
    private void setQuant(String value)
    {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ITEMS, value);
        editor.commit();
    }

    public void changeShopClicked() {
        if(getCheckedStatus().equals(INTIAL_CHECKOUT))
        {

            Log.d(DatabaseConstants.LOG_STATEMENT, "Change Shop Clicked!");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Changing Shop will delete your Current Cart items!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"CHANGE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            removeLocalData();
                            removeOnlineDataAndOpenNewActivity();

                            Toast.makeText(HomeActivity.this, "Cart Removed", Toast.LENGTH_SHORT).show();
                        }
                    });
            alertDialog.show();
        }else
        {

            Toast.makeText(HomeActivity.this, "Can't shop Change", Toast.LENGTH_SHORT ).show();

        }
        /*else if(getCheckedStatus().equals(CONFIRM_CHECKOUT))
        {
            Log.d(DatabaseConstants.LOG_STATEMENT, "Cancel Order!");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Cancel Order will delete your Current Cart items!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Log.d(DatabaseConstants.LOG_STATEMENT,shopName+" "+customerId);
                            DatabaseReference dbr=database.getReference(shopName).child(DatabaseConstants.customers).child(customerId).child(DatabaseConstants.purchases);

                            final ArrayList<Purchases> finallist=new ArrayList<Purchases>();
                            final ArrayList<Purchases> list=new ArrayList<Purchases>();
                            dbr.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                                    int i=0;
                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                        Log.d(DatabaseConstants.LOG_STATEMENT,postSnapshot.toString());
                                        Purchases purchases=postSnapshot.getValue(Purchases.class);
                                        list.add(purchases);

                                    }
                                    Iterator<Purchases> itr=list.iterator();
                                    while(itr.hasNext()){
                                        final Purchases purchases=itr.next();
                                        // Log.d(DatabaseConstants.LOG_STATEMENT,"purchases list "+purchases.getBarCode());

                                        String code=purchases.getBarCode();
                                        int q=Integer.parseInt(purchases.getQuantity());

                                        Log.d(DatabaseConstants.LOG_STATEMENT,q+" "+purchases.getBarCode());

                                        DatabaseReference databaseRef=database.getReference(shopName).child(DatabaseConstants.items);
                                        databaseRef.child(purchases.getBarCode()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Item item=dataSnapshot.getValue(Item.class);
                                                purchases.setTotalquantity(item.getQuantity());
                                                finallist.add(purchases);
                                                int quant = Integer.parseInt(item.getQuantity());
                                                String str=purchases.getQuantity();

                                                quant=quant+Integer.parseInt(str);

                                                database.getReference(shopName).child(DatabaseConstants.items).child(purchases.getBarCode()).child(DatabaseConstants.quantity).setValue(Integer.toString(quant));


                                                //Log.d(DatabaseConstants.LOG_STATEMENT," hjhjhjhjz "+finallist.size());
                                            }
                                            @Override

                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(DatabaseConstants.LOG_STATEMENT, "Cancel!!");
                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            setCheckedStatus(INTIAL_CHECKOUT);
                            removeLocalData();
                            removeOnlineDataAndOpenNewActivity();

                        }
                    });
            alertDialog.show();


        }else
        {
            removeLocalData();
            openShopSelectActivity();
        }*/
    }

    public void cancelOrder(View v)
    {
        if(getCheckedStatus().equals(CONFIRM_CHECKOUT))
        {
            Log.d(DatabaseConstants.LOG_STATEMENT, "Cancel Order!");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Cancel Order will delete your Current Cart items!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Log.d(DatabaseConstants.LOG_STATEMENT,shopName+" "+customerId);
                            DatabaseReference dbr=database.getReference(shopName).child(DatabaseConstants.customers).child(customerId).child(DatabaseConstants.purchases);

                            final ArrayList<Purchases> finallist=new ArrayList<Purchases>();
                            final ArrayList<Purchases> list=new ArrayList<Purchases>();
                            dbr.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                                    int i=0;
                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                        Log.d(DatabaseConstants.LOG_STATEMENT,postSnapshot.toString());
                                        Purchases purchases=postSnapshot.getValue(Purchases.class);
                                        list.add(purchases);

                                    }
                                    Iterator<Purchases> itr=list.iterator();
                                    while(itr.hasNext()){
                                        final Purchases purchases=itr.next();
                                        // Log.d(DatabaseConstants.LOG_STATEMENT,"purchases list "+purchases.getBarCode());

                                        String code=purchases.getBarCode();
                                        int q=Integer.parseInt(purchases.getQuantity());

                                        Log.d(DatabaseConstants.LOG_STATEMENT,q+" "+purchases.getBarCode());

                                        DatabaseReference databaseRef=database.getReference(shopName).child(DatabaseConstants.items);
                                        databaseRef.child(purchases.getBarCode()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Item item=dataSnapshot.getValue(Item.class);
                                                purchases.setTotalquantity(item.getQuantity());
                                                finallist.add(purchases);
                                                int quant = Integer.parseInt(item.getQuantity());
                                                String str=purchases.getQuantity();

                                                quant=quant+Integer.parseInt(str);

                                                database.getReference(shopName).child(DatabaseConstants.items).child(purchases.getBarCode()).child(DatabaseConstants.quantity).setValue(Integer.toString(quant));


                                                //Log.d(DatabaseConstants.LOG_STATEMENT," hjhjhjhjz "+finallist.size());
                                            }
                                            @Override

                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(DatabaseConstants.LOG_STATEMENT, "Cancel!!");
                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            setCheckedStatus(INTIAL_CHECKOUT);
                            removeLocalData();
                            removeOnlineDataAndOpenNewActivity();

                        }
                    });
            alertDialog.show();


        }else
        {
            removeLocalData();
            openShopSelectActivity();
        }
    }
    private void removeOnlineDataAndOpenNewActivity() {

        DatabaseConstants.createLoadingDialog(HomeActivity.this);

        final DatabaseReference databaseReference = database.getReference(shopName).child(DatabaseConstants.customers);
        Log.d(DatabaseConstants.LOG_STATEMENT, "Selected SHop id: "+shopName);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DatabaseConstants.LOG_STATEMENT, "To remove Order id : "+customerId);
                databaseReference.child(customerId).removeValue();
                Log.d(DatabaseConstants.LOG_STATEMENT, "Order Removed!!");
                DatabaseConstants.hideLoadingDialog();
                openShopSelectActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                DatabaseConstants.hideLoadingDialog();
            }


        });
    }

    private void openShopSelectActivity(){
        DatabaseConstants.hideLoadingDialog();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
//        finish();
    }

    private void removeLocalData(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(ShopSelectActivity.ORDER_NUMBER_STRING);
        editor.remove(ShopSelectActivity.SHOP_ID_STRING);
        editor.remove(ITEMS);
        editor.remove(CHECKEDOUT_STRING);
        editor.commit();
        Log.d(DatabaseConstants.LOG_STATEMENT, "Local Data removed");
    }

    /*
    it is for data update after check out
    */
    public void checkOut(View view)
    {
        String status=getCheckedStatus();
        Log.d(DatabaseConstants.LOG_STATEMENT,"Checkout "+status);

        if(status.equals(INTIAL_CHECKOUT))
        {
            if(getQuant().equals("true"))
                {
                Log.d(DatabaseConstants.LOG_STATEMENT,"items status is "+getQuant());
                Log.d(DatabaseConstants.LOG_STATEMENT,shopName+" "+customerId);
                DatabaseReference dbr=database.getReference(shopName).child(DatabaseConstants.customers).child(customerId).child(DatabaseConstants.purchases);

                // final ArrayList<Purchases> finallist=new ArrayList<Purchases>();
                final ArrayList<Purchases> list=new ArrayList<Purchases>();
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                        int i=0;
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Log.d(DatabaseConstants.LOG_STATEMENT,postSnapshot.toString());
                            Purchases purchases=postSnapshot.getValue(Purchases.class);
                            list.add(purchases);

                        }
                        Iterator<Purchases> itr=list.iterator();
                        while(itr.hasNext()){
                            final Purchases purchases=itr.next();
                            String code=purchases.getBarCode();
                            int q=Integer.parseInt(purchases.getQuantity());

                            Log.d(DatabaseConstants.LOG_STATEMENT,q+" "+purchases.getBarCode());

                            DatabaseReference databaseRef=database.getReference(shopName).child(DatabaseConstants.items);
                            databaseRef.child(purchases.getBarCode()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Item item=dataSnapshot.getValue(Item.class);
                                    purchases.setTotalquantity(item.getQuantity());
                                    //finallist.add(purchases);
                                    int quant = Integer.parseInt(item.getQuantity());
                                    String str=purchases.getQuantity();
                                    if((quant-Integer.parseInt(str))>=0)
                                    {
                                        quant=quant-Integer.parseInt(str);
                                        database.getReference(shopName).child(DatabaseConstants.items).child(purchases.getBarCode()).child(DatabaseConstants.quantity).setValue(Integer.toString(quant));

                                    }else{
                                        database.getReference(shopName).child(DatabaseConstants.customers).child(customerId).child(DatabaseConstants.purchases).child(purchases.getBarCode()).removeValue();
                                    }

                                }
                                @Override

                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(DatabaseConstants.LOG_STATEMENT, "Cancel!!");
                                }
                            });
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                    setCheckedStatus(CONFIRM_CHECKOUT);
                    btn.setText("confirm Check out");
                    cancelbtn.setVisibility(View.VISIBLE);
                    cancelbtn.setText("Cancel");
                    fb.hide();
            }else
            {
                Log.d(DatabaseConstants.LOG_STATEMENT, "Order is empty!");
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                //alertDialog.setTitle("Alert");
                alertDialog.setMessage("You can't order Zero item!");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();
            }
            Log.d(DatabaseConstants.LOG_STATEMENT,"last state is "+getCheckedStatus());
        }else if(status.equals(CONFIRM_CHECKOUT)){
            btn.setText("confirmed");
            cancelbtn.setEnabled(false);
            //mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
            Log.d(DatabaseConstants.LOG_STATEMENT, "confirmed Order!");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            //alertDialog.setTitle("Alert");
            alertDialog.setMessage("Your Customer Id is "+customerId+" Kindly go to the counter for verifying your order");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            removeLocalData();
                            openShopSelectActivity();
                        }
                    });
            alertDialog.show();
            btn.setEnabled(false);
            setCheckedStatus(CHECKEDOUT);
            Log.d(DatabaseConstants.LOG_STATEMENT,"last state is "+getCheckedStatus());
        }
        else if(status.equals(CHECKEDOUT))
        {

            setQuant("false");
            fb.hide();
            setCheckedStatus(INTIAL_CHECKOUT);
            removeLocalData();
        }

    }

    private void goToLastState()
    {
        String str=getCheckedStatus();
        Log.d(DatabaseConstants.LOG_STATEMENT,"old status "+str);

        if(str.equals(CONFIRM_CHECKOUT))
        {

            setCheckedStatus(CONFIRM_CHECKOUT);
            btn.setText("confirm Check out");
            cancelbtn.setVisibility(View.VISIBLE);
            cancelbtn.setText("Cancel");
            fb.hide();


        }else if(str.equals(CHECKEDOUT))
        {
            btn.setText("confirmed");
            cancelbtn.setEnabled(false);
            //mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
            Log.d(DatabaseConstants.LOG_STATEMENT, "confirmed Order!");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            //alertDialog.setTitle("Alert");
            alertDialog.setMessage("Your Customer Id is "+customerId+" Kindly go to the counter for verifying your order");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            removeLocalData();
                            openShopSelectActivity();
                        }
                    });
            alertDialog.show();
            cancelbtn.setVisibility(View.VISIBLE);
            cancelbtn.setText("Cancel");
            cancelbtn.setEnabled(false);
            btn.setEnabled(false);
            fb.hide();
            setCheckedStatus(CHECKEDOUT);
            Log.d(DatabaseConstants.LOG_STATEMENT,"last state is "+getCheckedStatus());
        }
    }

    public void productRemoveBtnClicked(View view){
        final int position = (int) view.getTag();
        //Toast.makeText(HomeActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        if(position>=0)
        {
            if(getCheckedStatus().equals(INTIAL_CHECKOUT))
            {
                String toDeleteBarCode=itemKeys.get(position);
                database.getReference(shopName).child(DatabaseConstants.customers).child(customerId).child(DatabaseConstants.purchases).child(toDeleteBarCode).removeValue();

                mylv.notifyDataSetChanged();

                if(mylv.getCount()>0)
                {
                    setQuant("true");

                }else {
                    setQuant("false");
                }
            }
            else {
                Toast.makeText(HomeActivity.this, "Now you Can't Delete item", Toast.LENGTH_SHORT ).show();
            }
        }


    }
}
