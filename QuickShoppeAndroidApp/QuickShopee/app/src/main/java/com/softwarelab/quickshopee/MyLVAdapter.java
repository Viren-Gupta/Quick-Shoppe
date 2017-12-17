package com.softwarelab.quickshopee;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by JASDEEP on 26-11-2017.
 */

public class MyLVAdapter extends BaseAdapter {
    Activity context;
    ArrayList<Item> products;

    public MyLVAdapter(Activity context, ArrayList<Item> products) {
        super();
        this.context = context;
        this.products = products;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override       // maybe to do ! check it
    public long getItemId(int position) {
        return 0;
    }


    /*public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }*/

    private class ViewHolder {
        TextView pname;
        TextView pprice;
        TextView pqty;
        TextView ptotal;
        ImageView pRemove;
//        TextView txtViewDescription;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        ViewHolder holder;
        LayoutInflater inflater =  context.getLayoutInflater();

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.row, null);
            holder = new ViewHolder();
            holder.pname = (TextView) convertView.findViewById(R.id.productName);
            holder.pprice = (TextView) convertView.findViewById(R.id.productPrice);
            holder.pqty = (TextView) convertView.findViewById(R.id.productQuantity);
            holder.ptotal = (TextView) convertView.findViewById(R.id.totalTextView);
            holder.pRemove = (ImageView) convertView.findViewById(R.id.removeProdImageView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        int quantity = Integer.valueOf(products.get(position).getQuantity());
        int price = Integer.valueOf(products.get(position).getPrice());


        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        holder.pname.setTypeface(boldTypeface);

        holder.pRemove.setTag(position);

        holder.pname.setText(products.get(position).getName());
        holder.pprice.setText("Price: ₹"+products.get(position).getPrice());
        holder.pqty.setText("Quantity: "+products.get(position).getQuantity());
        holder.ptotal.setText("₹"+String.valueOf(quantity*price));
        return convertView;
    }
}