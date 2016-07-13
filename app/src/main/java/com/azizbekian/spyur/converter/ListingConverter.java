package com.azizbekian.spyur.converter;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.model.LegendEnum;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.MhdEnum;
import com.azizbekian.spyur.model.WeekDayEnum;
import com.azizbekian.spyur.rest.SpyurApi;
import com.azizbekian.spyur.utils.LogUtils;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.FRI;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.MON;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.SAT;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.SUN;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.THU;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.TUE;
import static com.azizbekian.spyur.model.ListingResponse.WorkingDays.Day.WED;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingConverter implements Converter<ResponseBody, ListingResponse> {

    private static final String PREFIX_PHONE_NUMBER = "•";

    private Context mContext;

    public static final class Factory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            return INSTANCE;
        }

    }

    private ListingConverter() {
        // mContext = SpyurApplication.getComponent().getApplicationContext();
    }

    static final ListingConverter INSTANCE = new ListingConverter();

    @Override
    public ListingResponse convert(ResponseBody value) throws IOException {

        ListingResponse.Builder builder = new ListingResponse.Builder();

        Document document = Jsoup.parse(value.string(), SpyurApi.ENDPOINT);
        final Elements fieldSets = document.select("fieldset");
        if (null == fieldSets) return null;

        for (Element fieldSet : fieldSets) {
            LegendEnum legendEnum = LegendEnum.fromString(mContext, fieldSet.select("legend").text());
            if (null == legendEnum) continue;

            switch (legendEnum) {
                case CONTACT_INFORMATION:
                    parseContactInformation(fieldSet, builder);
                    break;
            }
        }

        return builder.build();
    }

    private void parseContactInformation(Element fieldSet, ListingResponse.Builder builder) {
        final Element ulClassContactInfo = fieldSet.select("ul[class=contactInfo]").first();
        if (null != ulClassContactInfo) {
            final Elements mhds = ulClassContactInfo.select("li[class=mhd]");
            if (null == mhds) return;
            for (Element e : mhds) {
                MhdEnum mhdEnum = MhdEnum.fromString(mContext, e.text());
                if (null == mhdEnum) continue;
                switch (mhdEnum) {
                    case EXECUTIVE:
                        parseExecutives(e.nextElementSibling(), builder);
                        break;
                    case ADDRESS_TELEPHONE:
                        parseAddressTelephone(e.nextElementSibling(), builder);
                        break;
                    case WORKING_DAYS_HOURS:
                        parseWorkingDays(e.nextElementSibling(), builder);
                        break;
                    case WEBSITE:
                        parseWebsite(e.nextElementSibling(), builder);
                        break;
                    case LISTING_IN_SPYUR:
                        parseListingInSpyur(e.nextElementSibling(), builder);
                        break;
                }
            }

            final Elements images = fieldSet.select("div[class=div-img]");
            if (!images.isEmpty()) {
                parseImages(images, builder);
            }

            final Elements videos = fieldSet.select("div#Slide_video");
            if (!videos.isEmpty()) {
                parseVideos(videos, builder);
            }
        }
    }

    private void parseVideos(Elements videos, ListingResponse.Builder builder) {
        builder.setVideoUrl(videos.select("a[class=fl curimg]").first().attr("id"));
    }

    private void parseImages(Elements images, ListingResponse.Builder builder) {
        for (Element imageDiv : images) {
            builder.addImage(imageDiv.select("a").first().attr("abs:href"));
        }
    }

    private void parseListingInSpyur(Element element, ListingResponse.Builder builder) {
        if (null == element) return;
        builder.setListingInSpyur(element.text());
    }

    private void parseWebsite(Element element, ListingResponse.Builder builder) {
        if (null == element) return;

        Elements websiteDivs = element.select("div > div");
        if (null == websiteDivs) return;

        for (Element div : websiteDivs) {
            builder.addWebsite(div.text());
        }
    }

    private void parseWorkingDays(Element element, ListingResponse.Builder builder) {
        if (null == element) return;

        Element tr = element.select("tr").first();
        if (null == tr) return;

        // removing &nbsp;
        String workingDays = tr.select("td").get(0).text().replace("\u00a0", "").trim();
        String[] splittedWorkDays = workingDays.split("\\s+");

        ListingResponse.WorkingDays.Builder workingDaysBuilder
                = new ListingResponse.WorkingDays.Builder();

        for (String s : splittedWorkDays) {
            WeekDayEnum weekDayEnum = WeekDayEnum.fromString(mContext, s);
            if (null == weekDayEnum) continue;
            switch (weekDayEnum) {
                case MON:
                    workingDaysBuilder.setWorkingDay(MON);
                    break;
                case TUE:
                    workingDaysBuilder.setWorkingDay(TUE);
                    break;
                case WED:
                    workingDaysBuilder.setWorkingDay(WED);
                    break;
                case THU:
                    workingDaysBuilder.setWorkingDay(THU);
                    break;
                case FRI:
                    workingDaysBuilder.setWorkingDay(FRI);
                    break;
                case SAT:
                    workingDaysBuilder.setWorkingDay(SAT);
                    break;
                case SUN:
                    workingDaysBuilder.setWorkingDay(SUN);
                    break;
            }
        }

        builder.setWorkingDays(workingDaysBuilder.build());
    }

    private void parseAddressTelephone(Element address, ListingResponse.Builder builder) {
        if (null == address) return;

        Elements divs = address.select("li > div");
        if (null == divs) return;
        ListingResponse.ContactInfo.Builder contactInfoBuilder;
        for (Element div : divs) {
            contactInfoBuilder = new ListingResponse.ContactInfo.Builder();
            Element a = div.select("div > a").first();
            if (null != a) {
                try {
                    double lat = Double.parseDouble(a.attr("lat"));
                    double lon = Double.parseDouble(a.attr("lon"));
                    contactInfoBuilder.setLocation(lat, lon);
                } catch (NumberFormatException ignored) {
                }
            }
            contactInfoBuilder.setAddress(div.ownText());

            // Region information
            Elements childDivs = div.select("div > div");
            if (null != childDivs && childDivs.size() > 0) {

                Element e = childDivs.first();
                do {
                    String text = e.text();
                    if (!text.startsWith(PREFIX_PHONE_NUMBER)) {
                        contactInfoBuilder.setRegion(text.substring(1, text.length() - 1));
                    } else {
                        try {
                            contactInfoBuilder.addPhoneNumber(text
                                    .substring(1)
                                    .trim()
                                    .replace("-", ""));
                        } catch (IndexOutOfBoundsException ignored) {
                            LogUtils.e(ignored.getMessage());
                        }
                    }
                    e = e.nextElementSibling();
                } while (e != null);

            }
            builder.addContactInfo(contactInfoBuilder.build());
        }
    }

    private void parseExecutives(Element executives, ListingResponse.Builder builder) {
        if (null == executives) return;

        Element li = executives.select("li").first();
        if (null != li) {
            Elements divs = li.select("div");
            if (null == divs) return;
            for (Element e : divs) {
                builder.addExecutive(e.text());
            }
        }
    }

}
