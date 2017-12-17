package com.softwarelab.quickshopee;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by shailendra on 26/10/17.
 */

public class ShowProduct extends Activity {
    //bar code value
    String barCode;
    //shop name value
    String shopName;
    //product name
    String name=null;
    //to show product name
    TextView productName;
    //to show product price
    TextView price;
    //to show available quantity of product
    TextView quantity;
    //to show buying quantity of product
    TextView quantityToBuy;
    //maximum available quantity of product
    int maxQuantity;
    //buying quantity of product
    int quant;
    //value of customer Id
    String customerId;
    //cost of product
    String cost;


    public String logCatString = "Quick_Shoppe";

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();



        Bundle extras = getIntent().getExtras();

        if(extras==null)
        {

        }
        else if((extras.getString("Barcode")!=null)&&(extras.getString("shopName")!=null)){

            setContentView(R.layout.activity_get_data_from_db);

            DisplayMetrics displayMetrics = new DisplayMetrics();

            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            getWindow().setLayout((int) (width * (0.75)), (int) (height * (0.25)));

            barCode=extras.getString("Barcode");
            shopName =extras.getString("shopName");
            customerId=extras.getString("customerId");


            Log.d(logCatString, "Shop name: "+shopName+" bar code: "+barCode);

            productName = (TextView)findViewById(R.id.productName);

            price =(TextView)findViewById(R.id.price);

            quantity=(TextView)findViewById(R.id.quantity);

            quantityToBuy=(TextView)findViewById(R.id.quantityToBuy);

            //get query from database

            databaseReference = database.getReference(shopName).child(DatabaseConstants.items);


            databaseReference.child(barCode).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(logCatString, "received!!");
                    if (dataSnapshot.getValue()!=null){
                        Item item = dataSnapshot.getValue(Item.class);
                        name=item.getName();
                        cost=item.getPrice();
                        productName.setText(item.getName());
                        Log.d(logCatString, "Name "+item.getName());
                        Log.d(logCatString, "Price "+item.getPrice());
                        price.setText("Rs. "+item.getPrice());
                        quantity.setText("Avaliable: "+item.getQuantity()+" units");
                       // price.setText(item.getPrice());
                       // quantity.setText(item.getQuantity());
                        maxQuantity = Integer.parseInt(item.getQuantity());
                        quant=Integer.parseInt(quantityToBuy.getText().toString());
                        quant=Math.min(quant,maxQuantity);
                        quantityToBuy.setText(String.valueOf(quant));
                    } else {
                        Log.d(logCatString, "Null!!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(logCatString, "Cancel!!");
                }
            });

          /*  productName.setText(barCode);

            price.setText("Rs. "+"10");

            quantity.setText("In Stock "+"50");*/


        }else{

        }

    }
    public void add(View v){

        quant=Integer.parseInt(quantityToBuy.getText().toString());

        if(quant<maxQuantity)
        {
            quant++;
        }
        quantityToBuy.setText(String.valueOf(quant));
    }
    public void sub(View v){

        quant=Integer.parseInt(quantityToBuy.getText().toString());

        if(quant>0)
        {
            quant--;
        }
        quantityToBuy.setText(String.valueOf(quant));

    }
    public void Cancel(View v){

        finish();
    }
    public void addToCart(View v){
        //send to all data to data base database

        quant=Integer.parseInt(quantityToBuy.getText().toString());


        //customerId(int),shopName(string),quant(int),barCode(string),productName(string),
        // price(float),totalPrice(float)

        if(name!=null&&quant>0)
        {

            CustomerProduct customerProduct=new CustomerProduct();
            customerProduct.setBarCode(barCode);
            customerProduct.setName(name);
            customerProduct.setPrice(cost);
            customerProduct.setQuantity(String.valueOf(quant));
            DatabaseReference newref=database.getReference(shopName).child(DatabaseConstants.customers).child(customerId);
            newref.child(DatabaseConstants.purchases).child(barCode).setValue(customerProduct);

        }

        finish();
    }
}
