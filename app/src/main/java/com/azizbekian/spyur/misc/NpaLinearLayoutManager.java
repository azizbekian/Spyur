package com.azizbekian.spyur.misc;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Workaround for inconsistency issues.
 * <p>
 * Created on May 01, 2016.
 *
 * @author Kas Hunt
 * @see <a href="http://stackoverflow.com/a/33985508/1083957">StackOverflow post</a>
 */
public class NpaLinearLayoutManager extends LinearLayoutManager {

    public NpaLinearLayoutManager(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public NpaLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
                                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("unused")
    public NpaLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

}
