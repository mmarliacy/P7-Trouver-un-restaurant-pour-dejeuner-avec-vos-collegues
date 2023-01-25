# GO4LUNCH - Find a restaurant to eat with your colleagues 
##### *This README.md will allow you to know the features of the project, and will permit you to make changes later if necessary.*


### Summary
   1. [General information](#general-information);
   2. [Project screenshots](#pictures-of-the-project);
   3. [Dependencies and technologies used](#technologies-used);
   4. [Installation of the project](#how-to-install);
   5. [Features developed](#features-developed);
   6. [How can you improve that project](#how-to-improve).
   7. [Android Studio Version](#android-studio-version).

***
#### #General information
***
*Go4Lunch is an app, that allows you to locate restaurants around your position in a map and save it as the one selected for your lunch rest. Your coworkers can see your reservation and join you for lunch.*
Among other things, the application have the following features:
* You can see your colleagues list, and know what did they selected for their lunch rest.
* Retrieve restaurants list with pictures. 
* You can save a restaurant by giving it a like, and that choice will be saved in database. In that way, the user will always know if he liked or not that restaurant for his future choice. 
* On the restaurant sheet, you can also see who is about to come in the same restaurant than you.
* It's possible to make research on a restaurant with it address, not necessarily close to your position by using search bar feeded by nearby search Google API.

***
#### #Pictures of the project
***
* [Home screen capture](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/all-properties.jpeg) 
* [Add a property 1 ](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/add-property-1.jpeg)
* [Add a property 2 ](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/add-property-2.jpeg)
* [All property on Maps](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/Real-estate-on-maps.jpeg)
* [Head-of-property-sheet](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/Head-of-property-sheet.jpeg)
* [Maps-Static-Api](https://github.com/mmarliacy/P9-RealEstateManager/blob/main/P9%20PHOTOS/Maps-Static-Api.jpeg)

***
#### #Technologies used
***
A number of dependencies and libraries were used for this project, among them:
  * [Google Play Services](https://developers.google.com/maps/documentation/android-sdk/config): Version 18.1.0
  * [Retrofit](https://square.github.io/retrofit/): Version 2.9.0
  * [Firebase UI](https://github.com/firebase/FirebaseUI-Android): Version 8.0.1
  * [Firebase Auth](https://firebase.google.com/docs/auth/android/start?hl=fr): Version 21.1.0
  * [Firebase/Firestore](https://firebaseopensource.com/projects/firebase/firebaseui-android/): Version 8.0.1
  * [Lifecycle/LiveData](https://developer.android.com/jetpack/androidx/releases/lifecycle?hl=fr): Version 2.2.0
  * [ROOM](https://developer.android.com/jetpack/androidx/releases/room?hl=fr): Version 1.1.1

Additionally: 
* [Material](https://m2.material.io/develop/android/docs/getting-started): Version 1.7.0

***
#### #How to install
***
1. Download the repository at the following URL: https://github.com/mmarliacy/P9-RealEstateManager.
2. Extract the application installation folder from the "RealEstateManager" archive to a destination folder or to the desktop.
3. Open Android Studio.
4. In order to open the program on Android Studio, follow the following instructions from the Android Studio menu bar: **File -> Open -> Destination folder -> RandomFolder**
5. Android Studio, with the intervention of Gradle will synchronize the project, in case of "deprecated" technologies or dependencies, update the project, clicking again on: **File -> Sync Project with Gradle Files**.

***
#### #Features developed
***
The goal was therefore for this project, to add these additional features for the property:

When you click on a property, you should see:
   * Pictures with their descriptions that represents the property;
   * Property description (rooms number, living area, points of interests around property...);
   * The name of the seller and the property's name;
   * A static map where the seller can clearly identify the property location.
   * The seller can specify the property sold date, by updating the real estate.

***
#### #How to improve
***

* It could be really interesting to upgrade filter functionality, by setting up a filter based on the seller name.
* Integrate graphically material components to improve user experience : Bottom Navigation or Drawer Navigation for handling user profile. 

***
#### #Android Studio version
***
> This project was made using Android Studio 4.2.2, then updated version 2020.3.1
