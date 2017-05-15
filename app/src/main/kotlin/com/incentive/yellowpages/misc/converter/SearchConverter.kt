package com.incentive.yellowpages.misc.converter

import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.data.remote.ApiContract
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

class SearchConverter private constructor() : Converter<ResponseBody, SearchResponse> {

    class Factory : Converter.Factory() {

        override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?,
                                           retrofit: Retrofit?): Converter<ResponseBody, *> {
            return INSTANCE
        }
    }

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): SearchResponse {
        val document = Jsoup.parse(value.string(), ApiContract.BASE)
        val elements = document.select("div[class=current-company]")
        if (elements.isEmpty()) {
            return SearchResponse(mutableListOf<SearchResponse.SearchItem>(), false)
        }
        val items = ArrayList<SearchResponse.SearchItem>(elements.size)
        elements.mapTo(items) { parseItem(it) }

        val b = hasNextPage(document.select("center[class=mtb10]").first())
        return SearchResponse(items, b)
    }

    companion object {

        internal val INSTANCE = SearchConverter()

        private fun parseItem(element: Element): SearchResponse.SearchItem {

            val builder = SearchResponse.SearchItem.Builder()
            val main = element.select("a").first()
            builder.setTitle(main.attr("title"))
            val href = main.attr("href")
            builder.setHref(href.substring(1, href.length))
            builder.setLogo(main.select("img").attr("abs:src"))

            var s: String
            for (e in element.select("div[class=addition-place]")) {
                s = e.select("a").first().select("img").attr("src")
                if (s == "/img/photos.png")
                    builder.setHasAdditionalPlacePhoto()
                else if (s == "/img/video.png")
                    builder.setHasAdditionalPlaceVideo()
            }
            for (e in element.select("div[class^=found]")) {
                builder.addFound(e.text())
            }

            return builder.build()
        }

        private fun hasNextPage(element: Element?): Boolean {
            if (null != element) {
                val navigation = element.select("div[class=navigation]").first()
                if (null != navigation) {
                    return navigation.select("span").first().nextElementSibling() != null
                }
            }
            return false
        }
    }
}
