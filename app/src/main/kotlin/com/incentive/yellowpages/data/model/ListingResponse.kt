package com.incentive.yellowpages.data.model

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.misc.createParcel
import java.util.*

data class ListingResponse(val executives: List<String>?, val contactInfos: List<ListingResponse.ContactInfo>?,
                           val workingDays: ListingResponse.WorkingDays?, val websites: List<String>?,
                           val listingInSpyur: String?, val images: List<String>?, val videoUrl: String?,
                           val hasMapCoordinates: Boolean = false) : Parcelable {
    data class ContactInfo constructor(val loc: LatLng?, val address: String?, val region: String?,
                                       var phoneNumbers: List<String>) : Parcelable {

        constructor(source: Parcel) : this(source.readParcelable<LatLng>(LatLng::class.java.classLoader),
                source.readString(),
                source.readString(),
                source.createStringArrayList())

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(loc, flags)
            dest.writeString(address)
            dest.writeString(region)
            dest.writeStringList(phoneNumbers)
        }

        class Builder {
            private var loc: LatLng? = null
            private var address: String? = null
            private var region: String? = null
            private val phoneNumbers: MutableList<String> = ArrayList()

            fun setLocation(lat: Double, lng: Double): Builder {
                this.loc = LatLng(lat, lng)
                return this
            }

            fun setAddress(address: String): Builder {
                this.address = address
                return this
            }

            fun setRegion(region: String): Builder {
                this.region = region
                return this
            }

            fun addPhoneNumber(phoneNumber: String): Builder {
                this.phoneNumbers.add(phoneNumber)
                return this
            }

            fun build(): ContactInfo {
                return ContactInfo(loc, address, region, phoneNumbers)
            }
        }

        companion object {
            @JvmField @Suppress("unused") val CREATOR = createParcel(::ContactInfo)
        }

    }

    data class WorkingDays(private val days: HashMap<WorkingDays.Day, Boolean>) : Parcelable {

        enum class Day {
            MON, TUE, WED, THU, FRI, SAT, SUN
        }

        companion object {
            @JvmField val CREATOR: Parcelable.Creator<WorkingDays> = object : Parcelable.Creator<WorkingDays> {
                override fun createFromParcel(source: Parcel): WorkingDays = WorkingDays(source)
                override fun newArray(size: Int): Array<WorkingDays?> = arrayOfNulls(size)
            }
        }

        @Suppress("UNCHECKED_CAST")
        constructor(source: Parcel) : this(source.readBundle().getSerializable("map") as HashMap<Day, Boolean>)

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            val bundle = Bundle()
            bundle.putSerializable("map", days)
            dest?.writeBundle(bundle)
        }

        class Builder {
            var days: MutableMap<Day, Boolean> = HashMap(7)

            init {
                days.put(Day.MON, java.lang.Boolean.FALSE)
                days.put(Day.TUE, java.lang.Boolean.FALSE)
                days.put(Day.WED, java.lang.Boolean.FALSE)
                days.put(Day.THU, java.lang.Boolean.FALSE)
                days.put(Day.FRI, java.lang.Boolean.FALSE)
                days.put(Day.SAT, java.lang.Boolean.FALSE)
                days.put(Day.SUN, java.lang.Boolean.FALSE)
            }

            fun setWorkingDay(day: Day): Builder {
                days.put(day, java.lang.Boolean.TRUE)
                return this
            }

            fun build(): WorkingDays {
                return WorkingDays(days as HashMap<Day, Boolean>)
            }
        }
    }

    class Builder {
        private val executives: MutableList<String> = ArrayList()
        private val contactInfos: MutableList<ContactInfo> = ArrayList()
        private var workingDays: WorkingDays? = null
        private val websites: MutableList<String> = ArrayList()
        private var listingInSpyur: String? = null
        private val images: MutableList<String> = ArrayList()
        private var videoUrl: String? = null

        fun addExecutive(executive: String): Builder {
            executives.add(executive)
            return this
        }

        fun addContactInfo(contactInfo: ContactInfo): Builder {
            contactInfos.add(contactInfo)
            return this
        }

        fun setWorkingDays(workingDays: WorkingDays): Builder {
            this.workingDays = workingDays
            return this
        }

        fun addWebsite(websiteUri: String): Builder {
            this.websites.add(websiteUri)
            return this
        }

        fun setListingInSpyur(listingInSpyur: String): Builder {
            this.listingInSpyur = listingInSpyur
            return this
        }

        fun addImage(imageUri: String): Builder {
            this.images.add(imageUri)
            return this
        }

        fun setVideoUrl(videoUrl: String): Builder {
            this.videoUrl = videoUrl
            return this
        }

        fun build(): ListingResponse {

            val hasMapCoordinates = contactInfos.any { it.loc != null }

            return ListingResponse(executives, contactInfos, workingDays, websites,
                    listingInSpyur, images, videoUrl, hasMapCoordinates)
        }
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ListingResponse> = object : Parcelable.Creator<ListingResponse> {
            override fun createFromParcel(source: Parcel): ListingResponse = ListingResponse(source)
            override fun newArray(size: Int): Array<ListingResponse?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(ArrayList<String>().apply {
        source.readList(this, String::class.java.classLoader)
    },
            source.createTypedArrayList(ContactInfo.CREATOR),
            source.readParcelable<WorkingDays?>(WorkingDays::class.java.classLoader),
            ArrayList<String>().apply { source.readList(this, String::class.java.classLoader) },
            source.readString(), ArrayList<String>().apply { source.readList(this, String::class.java.classLoader) },
            source.readString(), source.readByte().toInt() != 0)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeList(executives)
        dest?.writeTypedList(contactInfos)
        dest?.writeParcelable(workingDays, 0)
        dest?.writeList(websites)
        dest?.writeString(listingInSpyur)
        dest?.writeList(images)
        dest?.writeString(videoUrl)
        dest?.writeByte(if (hasMapCoordinates) 1 else 0)
    }
}
