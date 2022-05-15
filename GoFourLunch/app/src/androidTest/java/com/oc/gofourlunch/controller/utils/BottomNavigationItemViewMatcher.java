package com.oc.gofourlunch.controller.utils;

import android.view.View;

import androidx.test.espresso.matcher.BoundedMatcher;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class BottomNavigationItemViewMatcher {
    private BottomNavigationItemViewMatcher(){}

    public static Matcher<View> withIsChecked(final boolean isChecked) {
        return new BoundedMatcher<View, BottomNavigationItemView>(BottomNavigationItemView.class) {

            private boolean triedMatching;

            @Override
            public void describeTo(Description description) {
                if (triedMatching) {
                    description.appendText("with isChecked: " + String.valueOf(isChecked));
                    description.appendText("But was: " + String.valueOf(!isChecked));
                }
            }

            @Override
            protected boolean matchesSafely(BottomNavigationItemView item) {
                triedMatching = true;
                return item.getItemData().isChecked() == isChecked;
            }
        };
    }

    public static Matcher<View> withTitle(final String titleTested) {
        return new BoundedMatcher<View, BottomNavigationItemView>(BottomNavigationItemView.class) {

            private boolean triedMatching;
            private String title;

            @Override
            public void describeTo(Description description) {
                if (triedMatching) {
                    description.appendText("with title: " + titleTested);
                    description.appendText("But was: " + String.valueOf(title));
                }
            }

            @Override
            protected boolean matchesSafely(BottomNavigationItemView item) {
                this.triedMatching = true;
                this.title = item.getItemData().getTitle().toString();
                return title.equals(titleTested);
            }
        };
    }
}

