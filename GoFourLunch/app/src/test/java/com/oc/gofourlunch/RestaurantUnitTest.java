package com.oc.gofourlunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.location.Location;


import com.oc.gofourlunch.model.GooglePlaces.GeometryModel;
import com.oc.gofourlunch.model.GooglePlaces.OpeningHoursModel;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RestaurantUnitTest {

    PlacesModel place;
    PlacesModel place2;
    List<PlacesModel> restaurantList;
    Location fLocation;
    GeometryModel fGeometryModel;
    User user;
    User newUser;

    String name;
    String mail;
    String photo;
    String restaurantId;
    String restaurantName;
    String type;
    @Before
    public void setUp(){
        place = mock(PlacesModel.class);
        fLocation =mock(Location.class);
        fGeometryModel = mock(GeometryModel.class);
        user = mock(User.class);
        List<String> types = place.getTypes();
        types.add("restaurant");
        when(place.getName()).thenReturn("DABKEH");
        when(place.getPlaceId()).thenReturn("ChIJZ6M2bxm9aowRJAyMEKkNxV0");
        when(place.getTypes()).thenReturn(types);
        when(place.getVicinity()).thenReturn("48 Rue Schoelcher, Sainte Marie 97230");
        when(place.getRating()).thenReturn(4.0);
        when(fLocation.getLatitude()).thenReturn(14.7849022);
        when(fLocation.getLongitude()).thenReturn(-60.9943353);

        place2 = mock(PlacesModel.class);
        when(place2.getName()).thenReturn("Los Altos Taqueria");
        when(place2.getPlaceId()).thenReturn("ChIJhzHBsAe6j4ARvq9oi8u-bqQ");
        when(place2.getTypes()).thenReturn(types);
        when(place2.getVicinity()).thenReturn("2105 Old Middlefield Way E, Mountain View, CA 94043, États-Unis");
        when(place2.getRating()).thenReturn(4.0);
        when(fLocation.getLatitude()).thenReturn(37.4141544);
        when(fLocation.getLongitude()).thenReturn(-122.093488);

        restaurantList = new ArrayList<>();
        restaurantList.add(place);
        restaurantList.add(place2);
        //::--> Create fake user
        name = "Stephen";
        mail = "st.stPatrick@gmail.com";
        photo = "https://i.pinimg.com/736x/32/7f/e4/327fe4dd3a54243a7c8bb5514a13f203.jpg";
        restaurantId = "ChIJN5Nz71W3j4ARhx5bwpTQEGg";
        restaurantName = "Sports Page";
        type = "bar";
        newUser = new User("Laura", "laura.lo@gmail.com", "photo","ChIJN5Nz71W3j4ARhx5bwpTQEGg","bar","Sports Page");

    }

    @Test
    public void getRestaurants() {
        assertFalse(restaurantList.isEmpty());
        assertSame(restaurantList.get(0).getName(), place.getName());
        assertSame(restaurantList.get(1).getName(), place2.getName());
        assertEquals(2, restaurantList.size());
    }

    @Test
    public void theUserChooseItRestaurant() {
        user = new User(name, mail,photo, restaurantId, type, restaurantName);
        assertSame("Sports Page",user.getRestaurantName());
        assertSame("ChIJN5Nz71W3j4ARhx5bwpTQEGg", user.getRestaurantId());
        assertSame("bar", user.getRestaurantType());
        user.setRestaurantId(place.getPlaceId());
        user.setRestaurantType(place.getTypes().get(0));
        user.setRestaurantName(place.getName());
        assertSame(place.getName(), user.getRestaurantName());
        assertSame(place.getPlaceId(), user.getRestaurantId());
        assertSame(place.getTypes().get(0), user.getRestaurantType());
        assertNotSame("Sports Page",user.getRestaurantName());
        assertNotSame("ChIJN5Nz71W3j4ARhx5bwpTQEGg", user.getRestaurantId());
        assertNotSame("bar", user.getRestaurantType());
    }

    @Test
    public void getOpeningsHours() {
        OpeningHoursModel varOpeningHours =mock(OpeningHoursModel.class);
        when(place.getOpeningHours()).thenReturn(varOpeningHours);
        when(varOpeningHours.getOpenNow()).thenReturn(true);
        assertTrue(place.getOpeningHours().getOpenNow());
    }

    @Test
    public void usersHaveChosenRestaurant(){
        //--> Users choose same restaurant
        List<User> restaurantSubscribers = new ArrayList<>();
        List<User> anotherRestaurantList = new ArrayList<>();
        restaurantSubscribers.add(user);
        restaurantSubscribers.add(newUser);
        assertNotEquals(3, restaurantSubscribers.size());
        assertNotEquals(anotherRestaurantList, restaurantSubscribers);
        assertFalse(anotherRestaurantList.contains(user));
        assertFalse(anotherRestaurantList.contains(newUser));

        //--> One of them choose another restaurant finally
        anotherRestaurantList.add(user);
        restaurantSubscribers.remove(user);
        assertFalse(restaurantSubscribers.contains(user));
        assertTrue(restaurantSubscribers.contains(newUser));
        assertTrue(anotherRestaurantList.contains(user));
        assertFalse(anotherRestaurantList.contains(newUser));
    }
}