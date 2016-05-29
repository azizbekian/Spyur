package com.azizbekian.spyur.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on May 01, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchResponse {

    private List<SearchItem> mSearchItems;
    private boolean mHasNext;

    public SearchResponse(List<SearchItem> searchItems, boolean hasNext) {
        this.mSearchItems = searchItems;
        this.mHasNext = hasNext;
    }

    public List<SearchItem> getSearchItems() {
        return mSearchItems;
    }

    public boolean hasNext() {
        return mHasNext;
    }

    public static class SearchItem implements Parcelable {
        private final String mTitle;
        private final String mHref;
        private final String mLogo;
        private final boolean mHasAdditionalPlacePhoto;
        private final boolean mHasAdditionalPlaceVideo;
        private final List<String> mFound;

        private SearchItem(String title, String href, String logo, List<String> found,
                           boolean hasAdditionalPlacePhoto, boolean hasAdditionalPlaceVideo) {
            this.mTitle = title;
            this.mHref = href;
            this.mLogo = logo;
            this.mFound = found;
            this.mHasAdditionalPlacePhoto = hasAdditionalPlacePhoto;
            this.mHasAdditionalPlaceVideo = hasAdditionalPlaceVideo;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mTitle);
            parcel.writeString(mHref);
            parcel.writeString(mLogo);
            parcel.writeByte((byte) (mHasAdditionalPlacePhoto ? 1 : 0));
            parcel.writeByte((byte) (mHasAdditionalPlaceVideo ? 1 : 0));
            parcel.writeList(mFound);
        }

        public static final Creator CREATOR = new Creator() {
            public SearchItem createFromParcel(Parcel in) {
                return new SearchItem(in);
            }

            public SearchItem[] newArray(int size) {
                return new SearchItem[size];
            }
        };

        public SearchItem(Parcel in) {
            mTitle = in.readString();
            mHref = in.readString();
            mLogo = in.readString();
            mHasAdditionalPlacePhoto = in.readByte() != 0;
            mHasAdditionalPlaceVideo = in.readByte() != 0;
            mFound = new ArrayList<>();
            in.readList(mFound, null);
        }

        public String getTitle() {
            return mTitle;
        }

        public String getHref() {
            return mHref;
        }

        public String getLogo() {
            return mLogo;
        }

        public boolean hasAdditionalPlacePhoto() {
            return mHasAdditionalPlacePhoto;
        }

        public boolean hasAdditionalPlaceVideo() {
            return mHasAdditionalPlaceVideo;
        }

        public List<String> getFound() {
            return mFound;
        }

        public static class Builder {
            private String title = "";
            private String href = "";
            private String logo;
            private boolean hasAdditionalPlacePhoto;
            private boolean hasAdditionalPlaceVideo;
            private List<String> foundList = new ArrayList<>();

            public Builder setTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder setHref(String mHref) {
                this.href = mHref;
                return this;
            }


            public Builder setLogo(String mLogo) {
                this.logo = mLogo;
                return this;
            }

            public Builder addFound(String found) {
                foundList.add(found);
                return this;
            }

            public void setHasAdditionalPlacePhoto() {
                this.hasAdditionalPlacePhoto = true;
            }

            public void setHasAdditionalPlaceVideo() {
                this.hasAdditionalPlaceVideo = true;
            }

            public SearchItem build() {
                return new SearchItem(title, href, logo, foundList, hasAdditionalPlacePhoto,
                        hasAdditionalPlaceVideo);
            }

        }

        @Override
        public String toString() {
            return "SearchItem{" +
                    "title='" + mTitle + '\'' +
                    ", href='" + mHref + '\'' +
                    ", logo='" + mLogo + '\'' +
                    ", hasAdditionalPlacePhoto=" + mHasAdditionalPlacePhoto +
                    ", hasAdditionalPlaceVideo=" + mHasAdditionalPlaceVideo +
                    ", mFound=" + mFound +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "mSearchItems=" + mSearchItems +
                ", mHasNext=" + mHasNext +
                '}';
    }
}
