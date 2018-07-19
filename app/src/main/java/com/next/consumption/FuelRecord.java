package com.next.consumption;

/**
 * Created by Nextaty on 25.03.2018.
 */

public class FuelRecord {
    Long id;
    Long date;
    String month;
    Long mileage;
    Float amount;
    String location;
    Float consumption;
    Float cost;
    public boolean blank;

    public FuelRecord(long date, long mileage, float amount, float consumption, String location, float cost) {
        this(null, date, mileage, amount, consumption, location, cost);
    }

    public FuelRecord(Long id, Long date, Long mileage, Float amount, Float consumption, String location, Float cost) {
        this.id = id;
        this.date = date;
        this.mileage = mileage;
        this.amount = amount;
        this.location = location;
        this.consumption = consumption;
        this.cost = cost;
    }

    public FuelRecord(String month, long mileage, float amount, float cost) {
        this.month = month;
        this.mileage = mileage;
        this.amount = amount;
        this.cost = cost;
    }

    public FuelRecord() {
        blank=true;
    }

    @Override
    public String toString() {
        return "FuelRecord{" +
                "id=" + id +
                ", date=" + date +
                ", mileage=" + mileage +
                ", amount=" + amount +
                ", location='" + location + '\'' +
                ", consumption=" + consumption +
                ", cost=" + cost +
                '}';
    }
}
