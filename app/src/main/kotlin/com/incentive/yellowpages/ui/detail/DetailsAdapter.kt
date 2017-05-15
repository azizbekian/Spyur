package com.incentive.yellowpages.ui.detail

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.misc.inflate
import com.incentive.yellowpages.utils.LogUtils
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.row_content_contact_info.view.*
import kotlinx.android.synthetic.main.row_content_executive.view.*
import kotlinx.android.synthetic.main.row_content_listing_in_spyur_url.view.*
import kotlinx.android.synthetic.main.row_content_website.view.*
import kotlinx.android.synthetic.main.row_title_executives.view.*
import java.util.*

class DetailsAdapter constructor(
        val executives: List<String>,
        val contactInfos: List<ListingResponse.ContactInfo>,
        val websites: List<String>,
        val listingInSpyur: String?,
        val addressClickConsumer: Consumer<LatLng?>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TITLE_TO_CONTENT_MAP = SparseIntArray(4).apply {
            put(R.layout.row_title_executives, R.layout.row_content_executive)
            put(R.layout.row_title_contact_info, R.layout.row_content_contact_info)
            put(R.layout.row_title_website, R.layout.row_content_website)
            put(R.layout.row_title_listing_in_spyur, R.layout.row_content_listing_in_spyur_url)
        }
    }

    private val viewTypeHolder: IntArray
    private val contactInfoContentOffset: Int = (executives.size + if (executives.isNotEmpty()) 1 else 0) +
            if (contactInfos.isNotEmpty()) 1 else 0
    private val websitesContentOffset: Int = contactInfoContentOffset + contactInfos.size +
            (if (websites.isNotEmpty()) 1 else 0) +
            (if (contactInfos.isNotEmpty() && contactInfos.size > 1) contactInfos.size - 1 else 0)  /* divider between contactInfos*/
    private val size: Int = (executives.size + if (executives.isNotEmpty()) 1 else 0) +
            (contactInfos.size + if (contactInfos.isNotEmpty()) 1 else 0) +
            (if (contactInfos.isNotEmpty() && contactInfos.size > 1) contactInfos.size - 1 else 0) + /* divider between contactInfos*/
            (websites.size + if (websites.isNotEmpty()) 1 else 0) +
            if (!listingInSpyur.isNullOrBlank()) 2 else 0

    init {

        viewTypeHolder = IntArray(size)

        val sourceSizes: LinkedList<Int> = LinkedList()
        val sourceLayoutIds: LinkedList<Int> = LinkedList()

        sourceSizes.apply {
            sourceSizes.add(executives.size)
            sourceSizes.add(contactInfos.size)
            sourceSizes.add(websites.size)
            sourceSizes.add(1)
        }
        sourceLayoutIds.apply {
            sourceLayoutIds.add(R.layout.row_title_executives)
            sourceLayoutIds.add(R.layout.row_title_contact_info)
            sourceLayoutIds.add(R.layout.row_title_website)
            sourceLayoutIds.add(R.layout.row_title_listing_in_spyur)
        }

        var index = 0

        while (index < size) {
            if (sourceSizes.isEmpty()) break

            val count = sourceSizes.first
            if (count == 0) {
                // do not show this item
                sourceSizes.removeFirst()
                sourceLayoutIds.removeFirst()
                continue
            }

            viewTypeHolder[index++] = sourceLayoutIds.first
            val contentLayoutId = TITLE_TO_CONTENT_MAP[sourceLayoutIds.first]
            if (contentLayoutId == R.layout.row_content_contact_info) {
                var j = index
                for (i in 0 until count) {
                    viewTypeHolder[j] = contentLayoutId
                    j += 2
                }
                j = index + 1
                for (i in 0 until count - 1) {
                    viewTypeHolder[j] = R.layout.divider
                    j += 2
                }
                index += count + (count - 1)
            } else {
                viewTypeHolder.fill(contentLayoutId, index, index + count)
                index += count
            }

            sourceSizes.removeFirst()
            sourceLayoutIds.removeFirst()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val inflatedView = parent.inflate(viewType, parent)
        return when (viewType) {
            R.layout.row_title_executives -> ExecutivesTitle(inflatedView)
            R.layout.row_content_executive -> ExecutivesContent(inflatedView)
            R.layout.row_title_contact_info -> ContactInfoTitle(inflatedView)
            R.layout.row_content_contact_info -> ContactInfoContent(inflatedView)
            R.layout.divider -> Divider(inflatedView)
            R.layout.row_title_website -> WebsiteTitle(inflatedView)
            R.layout.row_content_website -> WebsiteContent(inflatedView)
            R.layout.row_title_listing_in_spyur -> ListingInSpyurTitle(inflatedView)
            R.layout.row_content_listing_in_spyur_url -> ListingInSpyurContent(inflatedView)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        LogUtils.i(position)
        when (holder) {
            is ExecutivesTitle -> holder.bind(executives.size)
            is ExecutivesContent -> holder.bind(executives[holder.adapterPosition - 1])
            is ContactInfoContent -> holder.bind(contactInfos[(holder.adapterPosition - contactInfoContentOffset) / 2])
            is WebsiteContent -> holder.bind(websites[holder.adapterPosition - websitesContentOffset])
            is ListingInSpyurContent -> holder.bind(listingInSpyur)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        if (holder is ContactInfoContent) {
            holder.itemView.phoneNumberContainer.removeAllViews()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int = viewTypeHolder[position]

    override fun getItemCount(): Int = size

    inner class ExecutivesTitle(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(executivesCount: Int) {
            itemView.executives_title.text = itemView.resources.getQuantityString(R.plurals.executive, executivesCount)
        }
    }

    inner class ExecutivesContent(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(executive: String) {
            itemView.executive_content.text = executive
        }
    }

    inner class ContactInfoTitle(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ContactInfoContent(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contactInfo: ListingResponse.ContactInfo) {
            itemView.contactInfoAddress.text = contactInfo.address
            if (contactInfo.loc != null) {
                itemView.contactInfoAddress.setOnClickListener { v -> addressClickConsumer.accept(contactInfo.loc) }
            }
            if (TextUtils.isEmpty(contactInfo.region)) {
                itemView.contactInfoRegion.visibility = View.GONE
            } else {
                itemView.contactInfoRegion.visibility = View.VISIBLE
                itemView.contactInfoRegion.text = contactInfo.region
            }

            if (contactInfo.phoneNumbers.isEmpty()) {
                itemView.phoneNumberContainer.visibility = View.GONE
            } else {
                itemView.phoneNumberContainer.visibility = View.VISIBLE
                for (phoneNumber in contactInfo.phoneNumbers) {
                    val phoneNumberRow = itemView.inflate(R.layout.row_content_phone_number,
                            itemView.phoneNumberContainer)
                    val phoneNumberTextView = phoneNumberRow
                            .findViewById(R.id.contactInfoPhoneNumber) as TextView
                    phoneNumberTextView.text = phoneNumber
                    itemView.phoneNumberContainer.addView(phoneNumberRow)
                }
            }
        }
    }

    inner class Divider(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class WebsiteTitle(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class WebsiteContent(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(website: String) {
            itemView.website.text = website
        }
    }

    inner class ListingInSpyurTitle(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ListingInSpyurContent(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(listing: String?) {
            itemView.listingInSpyur.text = listing
        }
    }

}
