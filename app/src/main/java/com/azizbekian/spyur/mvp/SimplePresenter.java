package com.azizbekian.spyur.mvp;

import android.content.Intent;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SimplePresenter implements BaseContract.Presenter {

    @Override public void create() {

    }

    @Override public void start() {

    }

    @Override public void resume() {

    }

    @Override public void pause() {

    }

    @Override public void stop() {

    }

    @Override public void destroy() {

    }

    @Override public boolean onBackPressed() {
        return false;
    }

    @Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}
