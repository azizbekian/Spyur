package com.incentive.yellowpages.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class SearchResponse(val searchItems: MutableList<SearchResponse.SearchItem>,
                          val hasNext: Boolean) {

    data class SearchItem constructor(val title: String,
                                      val href: String,
                                      val logo: String,
                                      val hasAdditionalPlacePhoto: Boolean,
                                      val hasAdditionalPlaceVideo: Boolean,
                                      val found: List<String>) : Parcelable {

        constructor(source: Parcel) : this(source.readString(), source.readString(),
                source.readString(), source.readByte().toInt() != 0, source.readByte().toInt() != 0,
                source.createStringArrayList())

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            parcel.writeString(title)
            parcel.writeString(href)
            parcel.writeString(logo)
            parcel.writeByte((if (hasAdditionalPlacePhoto) 1 else 0).toByte())
            parcel.writeByte((if (hasAdditionalPlaceVideo) 1 else 0).toByte())
            parcel.writeStringList(found)
        }

        companion object {

            @JvmField @Suppress("unused") val CREATOR: Parcelable.Creator<SearchItem> =
                    object : Parcelable.Creator<SearchItem> {
                        override fun createFromParcel(`in`: Parcel): SearchItem {
                            return SearchItem(`in`)
                        }

                        override fun newArray(size: Int): Array<SearchItem?> {
                            return arrayOfNulls(size)
                        }
                    }
        }

        class Builder {
            private var title = ""
            private var href = ""
            private lateinit var logo: String
            private var hasAdditionalPlacePhoto: Boolean = false
            private var hasAdditionalPlaceVideo: Boolean = false
            private val foundList = ArrayList<String>()

            fun setTitle(title: String): Builder {
                this.title = title
                return this
            }

            fun setHref(mHref: String): Builder {
                this.href = mHref
                return this
            }


            fun setLogo(mLogo: String): Builder {
                this.logo = mLogo
                return this
            }

            fun addFound(found: String): Builder {
                foundList.add(found)
                return this
            }

            fun setHasAdditionalPlacePhoto() {
                this.hasAdditionalPlacePhoto = true
            }

            fun setHasAdditionalPlaceVideo() {
                this.hasAdditionalPlaceVideo = true
            }

            fun build(): SearchItem {
                return SearchItem(title, href, logo, hasAdditionalPlacePhoto,
                        hasAdditionalPlaceVideo, foundList)
            }

        }

    }

}
