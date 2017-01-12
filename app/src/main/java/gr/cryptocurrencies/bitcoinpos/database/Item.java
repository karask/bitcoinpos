package gr.cryptocurrencies.bitcoinpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

import gr.cryptocurrencies.bitcoinpos.utilities.DateUtilities;

/**
 * Created by kostas on 22/8/2016.
 * DAO object of Item -- for simplicity since other patterns would be an overkill
 */
public class Item implements Parcelable {

    private Integer itemId;
    private String name;
    private String description;
    private String color;
    private Date createdAt;
    private int displayOrder;
    private double amount;
    private boolean isAvailable;


    public Item(Integer itemId, String name, String description, double amount, int order, String color, boolean isAvailable, Date createdAt) {

        if(name == null)
            throw new IllegalArgumentException("Name or amount are invalid!");

        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.color = color;
        this.displayOrder = order;
        this.amount = amount;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


    protected Item(Parcel in) {
        itemId = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        description = in.readString();
        color = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt != -1 ? new Date(tmpCreatedAt) : null;
        displayOrder = in.readInt();
        amount = in.readDouble();
        isAvailable = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (itemId == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(itemId);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(color);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1L);
        dest.writeInt(displayOrder);
        dest.writeDouble(amount);
        dest.writeByte((byte) (isAvailable ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    // currently only keeps item's name and amount
    // TODO: put separator in a GenericUtils class ?
    public String toString() {
        return name + "[-|-]" + amount;
    }
}