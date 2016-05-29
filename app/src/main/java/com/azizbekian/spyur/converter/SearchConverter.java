package com.azizbekian.spyur.converter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.rest.SpyurApi;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchConverter implements Converter<ResponseBody, SearchResponse> {

    public static final class Factory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            return INSTANCE;
        }
    }

    private SearchConverter() {
    }

    static final SearchConverter INSTANCE = new SearchConverter();

    @Override
    public SearchResponse convert(ResponseBody value) throws IOException {
        Document document = Jsoup.parse(value.string(), SpyurApi.ENDPOINT);
        final Elements elements = document.select("div[class=current-company]");
        if (elements.isEmpty()) {
            return new SearchResponse(Collections.emptyList(), false);
        }
        final List<SearchResponse.SearchItem> items = new ArrayList<>(elements.size());
        for (Element element : elements) {
            final SearchResponse.SearchItem item = parseItem(element);
            if (null != item) items.add(item);
        }

        boolean b = hasNextPage(document.select("center[class=mtb10]").first());
        return new SearchResponse(items, b);
    }

    private static SearchResponse.SearchItem parseItem(Element element) {

        SearchResponse.SearchItem.Builder builder = new SearchResponse.SearchItem.Builder();
        Element main = element.select("a").first();
        builder.setTitle(main.attr("title"));
        String href = main.attr("href");
        builder.setHref(href.substring(1, href.length()));
        builder.setLogo(main.select("img").attr("abs:src"));

        String s;
        for (Element e : element.select("div[class=addition-place]")) {
            s = e.select("a").first().select("img").attr("src");
            if (s.equals("/img/photos.png"))
                builder.setHasAdditionalPlacePhoto();
            else if (s.equals("/img/video.png"))
                builder.setHasAdditionalPlaceVideo();
        }
        for (Element e : element.select("div[class^=found]")) {
            builder.addFound(e.text());
        }

        return builder.build();
    }

    private static boolean hasNextPage(Element element) {
        if (null != element) {
            Element navigation = element.select("div[class=navigation]").first();
            if (null != navigation) {
                return navigation.select("span").first().nextElementSibling() != null;
            }
        }
        return false;
    }
}
