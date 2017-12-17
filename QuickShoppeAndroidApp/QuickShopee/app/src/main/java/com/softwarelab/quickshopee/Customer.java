package com.softwarelab.quickshopee;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by GAURANG PC on 06-11-2017.
 */
public class Customer {
    private String status;
    private Item[] purchases;
    private String time;

    public Customer() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        this.status = DatabaseConstants.PASSIVE_STATUS;
        this.purchases = null;
        this.time =formattedDate;
    }



    public Item[] getPurchases() {
        return purchases;
    }

    public void setPurchases(Item[] purchases) {
        this.purchases = purchases;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
