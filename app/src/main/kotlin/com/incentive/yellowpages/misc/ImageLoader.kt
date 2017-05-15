package com.incentive.yellowpages.misc

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.incentive.yellowpages.injection.ApplicationContext
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Incentive Inc., on January 19, 2017.

 * @author Andranik Azizbekian (andranik.azizbekian@gmail.com)
 */
@Singleton
class ImageLoader @Inject constructor(@ApplicationContext context: Context) {

    private val glide: RequestManager = Glide.with(context)

    fun load(url: String, imageView: ImageView,
             onImageSuccess: Consumer<Bitmap?>? = null, onImageFailure: Consumer<Exception?>? = null) {
        glide
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .listener(object : RequestListener<String, Bitmap> {
                    override fun onException(e: Exception?, model: String?,
                                             target: Target<Bitmap>?,
                                             isFirstResource: Boolean): Boolean {
                        onImageFailure?.accept(e)
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: String?,
                                                 target: Target<Bitmap>?,
                                                 isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        onImageSuccess?.accept(resource)
                        return false
                    }
                })
                .into(imageView)
    }
}
