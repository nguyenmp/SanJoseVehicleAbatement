package ninja.mpnguyen.sanjosevehicleabatement;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Report implements Serializable, Parcelable {
    public final long date;
    public final String address;
    public final String comment;
    public final String license;
    public final String make;
    public final String color;
    public final String state;
    public final String model;

    public Report(long date, String address, String comment, String license, String make, String color, String state, String model) {
        this.date = date;
        this.address = address;
        this.comment = comment;
        this.license = license;
        this.make = make;
        this.color = color;
        this.state = state;
        this.model = model;
    }

    public Report(Parcel source) {
        date = source.readLong();
        address = source.readString();
        comment = source.readString();
        license = source.readString();
        make = source.readString();
        color = source.readString();
        state = source.readString();
        model = source.readString();
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date);
        dest.writeString(address);
        dest.writeString(comment);
        dest.writeString(license);
        dest.writeString(make);
        dest.writeString(color);
        dest.writeString(state);
        dest.writeString(model);
    }
}
