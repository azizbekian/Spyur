package com.azizbekian.spyur.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on May 01, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingResponse {

    public final List<String> executives;
    public final List<ContactInfo> contactInfos;
    public final WorkingDays workingDays;
    public final List<String> websites;
    public final String listingInSpyur;
    public final List<String> images;
    public final String videoUrl;
    public final boolean hasMapCoordinates;

    public ListingResponse(List<String> executives, List<ContactInfo> contactInfos,
                           WorkingDays workingDays, List<String> websites, String listingInSpyur,
                           List<String> images, String videoUrl, boolean hasMapCoordinates) {
        this.executives = executives;
        this.contactInfos = contactInfos;
        this.workingDays = workingDays;
        this.websites = websites;
        this.listingInSpyur = listingInSpyur;
        this.images = images;
        this.videoUrl = videoUrl;
        this.hasMapCoordinates = hasMapCoordinates;
    }

    public static class ContactInfo implements Parcelable {
        public final LatLng loc;
        public final String address;
        public final String region;
        public final List<String> phoneNumber;

        public ContactInfo(LatLng loc, String address, String region, List<String> phoneNumber) {
            this.loc = loc;
            this.address = address;
            this.region = region;
            this.phoneNumber = phoneNumber;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(loc, flags);
            dest.writeString(address);
            dest.writeString(region);
            dest.writeList(phoneNumber);
        }

        public static final Creator CREATOR = new Creator() {
            public ContactInfo createFromParcel(Parcel in) {
                return new ContactInfo(in);
            }

            public ContactInfo[] newArray(int size) {
                return new ContactInfo[size];
            }
        };

        public ContactInfo(Parcel in) {
            loc = in.readParcelable(LatLng.class.getClassLoader());
            address = in.readString();
            region = in.readString();
            phoneNumber = new ArrayList<>();
            in.readList(phoneNumber, List.class.getClassLoader());
        }

        public static class Builder {
            private LatLng loc;
            private String address;
            private String region;
            private List<String> phoneNumbers;

            public Builder() {
                phoneNumbers = new ArrayList<>();
            }

            public Builder setLocation(double lat, double lng) {
                this.loc = new LatLng(lat, lng);
                return this;
            }

            public Builder setAddress(String address) {
                this.address = address;
                return this;
            }

            public Builder setRegion(String region) {
                this.region = region;
                return this;
            }

            public Builder addPhoneNumber(String phoneNumber) {
                this.phoneNumbers.add(phoneNumber);
                return this;
            }

            public ContactInfo build() {
                return new ContactInfo(loc, address, region, phoneNumbers);
            }
        }

        @Override
        public String toString() {
            return "ContactInfo{" +
                    "loc=" + loc +
                    ", address='" + address + '\'' +
                    ", region='" + region + '\'' +
                    ", phoneNumber=" + phoneNumber +
                    '}';
        }
    }

    public static class WorkingDays {
        private final Map<Day, Boolean> days;

        public WorkingDays(Map<Day, Boolean> days) {
            this.days = days;
        }

        public static class Builder {
            public Map<Day, Boolean> days;

            public Builder() {
                days = new HashMap<>(7);
                days.put(Day.MON, Boolean.FALSE);
                days.put(Day.TUE, Boolean.FALSE);
                days.put(Day.WED, Boolean.FALSE);
                days.put(Day.THU, Boolean.FALSE);
                days.put(Day.FRI, Boolean.FALSE);
                days.put(Day.SAT, Boolean.FALSE);
                days.put(Day.SUN, Boolean.FALSE);
            }

            public Builder setWorkingDay(Day day) {
                days.put(day, Boolean.TRUE);
                return this;
            }

            public WorkingDays build() {
                return new WorkingDays(days);
            }
        }

        @Override
        public String toString() {
            return "WorkingDays{" +
                    "days=" + days +
                    '}';
        }

        public enum Day {MON, TUE, WED, THU, FRI, SAT, SUN}
    }

    public static class Builder {
        private List<String> executives;
        private List<ContactInfo> contactInfos;
        private WorkingDays workingDays;
        private List<String> websites;
        private String listingInSpyur;
        private List<String> images;
        private String videoUrl;

        public Builder() {
            executives = new ArrayList<>();
            contactInfos = new ArrayList<>();
            websites = new ArrayList<>();
            images = new ArrayList<>();
        }

        public Builder addExecutive(String executive) {
            executives.add(executive);
            return this;
        }

        public Builder addContactInfo(ContactInfo contactInfo) {
            contactInfos.add(contactInfo);
            return this;
        }

        public Builder setWorkingDays(WorkingDays workingDays) {
            this.workingDays = workingDays;
            return this;
        }

        public Builder addWebsite(String websiteUri) {
            this.websites.add(websiteUri);
            return this;
        }

        public Builder setListingInSpyur(String listingInSpyur) {
            this.listingInSpyur = listingInSpyur;
            return this;
        }

        public Builder addImage(String imageUri) {
            this.images.add(imageUri);
            return this;
        }

        public Builder setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public ListingResponse build() {

            boolean hasMapCoordinates = false;
            for (ContactInfo contactInfo : contactInfos) {
                if (contactInfo.loc != null) {
                    hasMapCoordinates = true;
                    break;
                }
            }

            return new ListingResponse(executives, contactInfos, workingDays, websites,
                    listingInSpyur, images, videoUrl, hasMapCoordinates);
        }
    }

    @Override
    public String toString() {
        return "ListingResponse{" +
                "executives=" + executives +
                ", contactInfos=" + contactInfos +
                ", workingDays=" + workingDays +
                ", websites=" + websites +
                ", listingInSpyur='" + listingInSpyur + '\'' +
                ", images=" + images +
                ", videoUrl='" + videoUrl + '\'' +
                ", hasMapCoordinates=" + hasMapCoordinates +
                '}';
    }
}
