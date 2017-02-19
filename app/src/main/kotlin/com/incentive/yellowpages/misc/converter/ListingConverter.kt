package com.incentive.yellowpages.misc.converter

import android.content.Context
import com.incentive.yellowpages.data.model.LegendEnum
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.MhdEnum
import com.incentive.yellowpages.data.model.WeekDayEnum
import com.incentive.yellowpages.data.remote.ApiContract
import com.incentive.yellowpages.injection.ApplicationContext
import com.incentive.yellowpages.ui.base.BaseApplication
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.Converter
import retrofit2.Retrofit
import timber.log.Timber
import java.io.IOException
import java.lang.reflect.Type

class ListingConverter : Converter<ResponseBody, ListingResponse> {

    companion object {

        private val PREFIX_PHONE_NUMBER = "•"
        private val PREFIX_DISTRICT = "("

        internal val INSTANCE = ListingConverter()
    }

    @ApplicationContext val context: Context = BaseApplication.appComponent.context()

    class Factory : Converter.Factory() {

        override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?,
                                           retrofit: Retrofit?): Converter<ResponseBody, *> {
            return INSTANCE
        }

    }

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): ListingResponse? {

        val builder = ListingResponse.Builder()

        val document = Jsoup.parse(value.string(), ApiContract.BASE)
        val fieldSets = document.select("fieldset") ?: return null

        for (fieldSet in fieldSets) {
            val legendEnum = LegendEnum.fromString(context, fieldSet.select("legend").text()) ?: continue

            when (legendEnum) {
                LegendEnum.CONTACT_INFORMATION -> parseContactInformation(fieldSet, builder)
            }
        }

        return builder.build()
    }

    private fun parseContactInformation(fieldSet: Element, builder: ListingResponse.Builder) {
        val ulClassContactInfo = fieldSet.select("ul[class=contactInfo]").first()
        if (null != ulClassContactInfo) {
            val mhds = ulClassContactInfo.select("li[class=mhd]") ?: return
            for (e in mhds) {
                val mhdEnum = MhdEnum.fromString(context, e.text()) ?: continue
                when (mhdEnum) {
                    MhdEnum.EXECUTIVE -> parseExecutives(e.nextElementSibling(), builder)
                    MhdEnum.ADDRESS_TELEPHONE -> parseAddressTelephone(e.nextElementSibling(), builder)
                    MhdEnum.WEBSITE -> parseWebsite(e.nextElementSibling(), builder)
                    MhdEnum.LISTING_IN_SPYUR -> parseListingInSpyur(e.nextElementSibling(), builder)
                }
            }

            val images = fieldSet.select("div[class=div-img]")
            if (!images.isEmpty()) {
                parseImages(images, builder)
            }

            val videos = fieldSet.select("div#Slide_video")
            if (!videos.isEmpty()) {
                parseVideos(videos, builder)
            }
        }
    }

    private fun parseVideos(videos: Elements, builder: ListingResponse.Builder) {
        builder.setVideoUrl(videos.select("a[class=fl curimg]").first().attr("id"))
    }

    private fun parseImages(images: Elements, builder: ListingResponse.Builder) {
        for (imageDiv in images) {
            builder.addImage(imageDiv.select("a").first().attr("abs:href"))
        }
    }

    private fun parseListingInSpyur(element: Element?, builder: ListingResponse.Builder) {
        if (null == element) return
        builder.setListingInSpyur(element.text())
    }

    private fun parseWebsite(element: Element?, builder: ListingResponse.Builder) {
        if (null == element) return

        val websiteDivs = element.select("div > div") ?: return

        for (div in websiteDivs) {
            builder.addWebsite(div.text())
        }
    }

    private fun parseAddressTelephone(address: Element?, builder: ListingResponse.Builder) {
        if (null == address) return

        val divs = address.select("li > div") ?: return
        var contactInfoBuilder: ListingResponse.ContactInfo.Builder
        for (div in divs) {
            contactInfoBuilder = ListingResponse.ContactInfo.Builder()
            val a = div.select("div > a").first()
            if (null != a) {
                try {
                    val lat = java.lang.Double.parseDouble(a.attr("lat"))
                    val lon = java.lang.Double.parseDouble(a.attr("lon"))
                    contactInfoBuilder.setLocation(lat, lon)
                } catch (ignored: NumberFormatException) {
                }

            }
            contactInfoBuilder.setAddress(div.ownText())

            // Region information
            val childDivs = div.select("div > div")
            if (null != childDivs && childDivs.size > 0) {

                var e: Element? = childDivs.first()
                do {
                    val text = e!!.text()
                    if (text.startsWith(PREFIX_DISTRICT)) {
                        contactInfoBuilder.setRegion(text.substring(1, text.length - 1))
                    } else if (text.startsWith(PREFIX_PHONE_NUMBER)) {
                        try {
                            contactInfoBuilder.addPhoneNumber(text
                                    .substring(1)
                                    .trim { it <= ' ' }
                                    .replace("-", ""))
                        } catch (ignored: IndexOutOfBoundsException) {
                            Timber.e(ignored.message)
                        }
                    } else {
                        parseWorkingDays(e, builder)
                    }
                    e = e.nextElementSibling()
                } while (e != null)

            }
            builder.addContactInfo(contactInfoBuilder.build())
        }
    }

    private fun parseWorkingDays(element: Element?, builder: ListingResponse.Builder) {
        if (null == element) return

        val splittedWorkDays = element.text().split("\\s+".toRegex())

        val workingDaysBuilder = ListingResponse.WorkingDays.Builder()

        for (workingDay in splittedWorkDays) {
            if (workingDay.length > 3) continue
            val weekDayEnum = WeekDayEnum.fromString(context, workingDay)

            when (weekDayEnum) {
                WeekDayEnum.MON -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.MON)
                WeekDayEnum.TUE -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.TUE)
                WeekDayEnum.WED -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.WED)
                WeekDayEnum.THU -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.THU)
                WeekDayEnum.FRI -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.FRI)
                WeekDayEnum.SAT -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.SAT)
                WeekDayEnum.SUN -> workingDaysBuilder.setWorkingDay(ListingResponse.WorkingDays.Day.SUN)
            }
        }

        builder.setWorkingDays(workingDaysBuilder.build())
    }

    private fun parseExecutives(executives: Element?, builder: ListingResponse.Builder) {
        if (null == executives) return

        val li = executives.select("li").first()
        if (null != li) {
            val divs = li.select("div") ?: return
            for (e in divs) {
                builder.addExecutive(e.text())
            }
        }
    }

}
