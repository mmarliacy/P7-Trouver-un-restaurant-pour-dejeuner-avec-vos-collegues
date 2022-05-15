package com.oc.gofourlunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.oc.gofourlunch.model.User.User;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserUnitTest {

    User user;
    List<User> usersDb;
    DocumentReference documentReference;
    CollectionReference collectionReference;
    User newUser = new User("Laura", "laura.lo@gmail.com", "photo","ChIJN5Nz71W3j4ARhx5bwpTQEGg","bar","Sports Page");

    @Before
    public void setUp() {
        usersDb = new ArrayList<>();
        //::--> Create fake user
        String name = "Stephen";
        String mail = "st.stPatrick@gmail.com";
        String restaurantId = "ChIJN5Nz71W3j4ARhx5bwpTQEGg";
        String photo = "https://i.pinimg.com/736x/32/7f/e4/327fe4dd3a54243a7c8bb5514a13f203.jpg";
        String restaurantName = "Sports Page";
        String type = "bar";
        user = new User (name, mail,photo, restaurantId, restaurantName, type);
        usersDb.add(user);
        //::--> Mock collection behaviour in database
        collectionReference = mock(CollectionReference.class, RETURNS_DEEP_STUBS);
        documentReference = mock(DocumentReference.class);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn(usersDb.get(0).getName());
    }

    @Test
    public void getUser() {
      assertEquals(usersDb.get(0), user);
      assertEquals(usersDb.get(0).getName(), documentReference.getId());
    }

    @Test
    public void createUser() {
        usersDb.add(newUser);
        assertEquals(usersDb.get(1).getName(), "Laura");
        assertNotEquals(usersDb.get(0), usersDb.get(1));
        assertNotEquals(usersDb.get(1).getName(), documentReference.getId());
        when(documentReference.getId()).thenReturn(usersDb.get(1).getName());
        assertEquals(usersDb.get(1).getName(), documentReference.getId());
    }

    @Test
    public void updateUser() {
        usersDb.add(newUser);
        when(documentReference.getId()).thenReturn(newUser.getName());
        assertEquals(documentReference.getId(), "Laura");
        assertEquals(usersDb.get(1).getName(), "Laura");

        newUser.setName("Karol");
        when(documentReference.getId()).thenReturn(newUser.getName());
        assertEquals(usersDb.get(1).getName(), "Karol");
        assertNotEquals("Laura", usersDb.get(1).getName());
        assertEquals(documentReference.getId(), "Karol");
    }

    @Test
    public void deleteUser() {
        usersDb.add(newUser);
        usersDb.remove(0);
        when(documentReference.getId()).thenReturn(usersDb.get(0).getName());
        assertEquals("Laura", usersDb.get(0).getName());
        assertEquals("Laura", documentReference.getId());
        assertNotEquals("Stephen", documentReference.getId());
        assertNotSame(usersDb.get(0), user);
    }
}
