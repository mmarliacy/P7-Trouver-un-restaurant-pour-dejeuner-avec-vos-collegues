package com.oc.gofourlunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.oc.gofourlunch.controller.utils.BottomNavigationItemViewMatcher.withIsChecked;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.oc.gofourlunch.view.activities.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class AppInstrumentedTest {

    //-- RULE/BEFORE -->
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        MainActivity mActivity = mActivityRule.getActivity();
        assertThat(mActivity, notNullValue());
    }

    //Test opening the app finds the users location and googleMaps fragment is displayed
    @Test
    public void getMapFragment() {
        onView(withId(R.id.map_view)).check(matches(withIsChecked(true)));
        onView(withId(R.id.list_view)).check(matches(withIsChecked(false)));
        onView(withId(R.id.workmates)).check(matches(withIsChecked(false)));
    }

    @Test
    public void getListFragment() {
        onView(withId(R.id.list_view)).perform(click());
        onView(withId(R.id.map_view)).check(matches(withIsChecked(false)));
        onView(withId(R.id.list_view)).check(matches(withIsChecked(true)));
        onView(withId(R.id.workmates)).check(matches(withIsChecked(false)));
    }


    @Test
    public void getWorkmatesFragment() {
        onView(allOf(withId(R.id.workmates))).perform(click());
        onView(withId(R.id.map_view)).check(matches(withIsChecked(false)));
        onView(withId(R.id.list_view)).check(matches(withIsChecked(false)));
        onView(withId(R.id.workmates)).check(matches(withIsChecked(true)));
    }

    @Test
    public void recyclerViewAppears() {
        onView(withId(R.id.map_view)).check(matches(withIsChecked(true)));
        onView(withId(R.id.fab_my_location)).perform(click());
        try {
            Thread.sleep(5000); //::--> wait 5 seconds to open app after authentication
        } catch (InterruptedException e) {
            //get the devices location
            e.printStackTrace();
        }
        onView(withId(R.id.list_view)).perform(click());
        onView(allOf(withId(R.id.restaurant_recyclerview), isDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test
    public void navDrawerCanOpen() {
        onView(withContentDescription(R.string.navigation_drawer_open))
                .perform(click());
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()));
    }
}