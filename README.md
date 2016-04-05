Basic Map Sample for ArcGIS Runtime Quartz
====================

This repository contains code for two sample apps--one for Java and one for Android--that display a map using ArcGIS Runtime Quartz. The app demonstrates the major themes of ArcGIS Runtime Quartz. These themes are discussed in the release notes [for Java](https://developers.arcgis.com/java/beta/guide/release-notes-java.htm) and [for Android](https://developers.arcgis.com/android/beta/guide/release-notes-android.htm) and are listed here:

Beta 2:
- Vector tiled layers
- Mobile map packages
- Take layers offline
- Offline geocoding and routing
- Identify layers
- Loadable features

Beta 1:
- Runtime common API
- New mapping API
- Loadable pattern
- Centralized handling of authentication
- Geometries and geometry builders

## Build and run

To run the Java application, you need the [ArcGIS Runtime SDK for Java, version Quartz](https://developers.arcgis.com/java/beta/). The beta site directs you to join the Early Adopter community and download the SDK. Unzip the SDK and copy the `ArcGISRuntime100.0.0` directory to the `source/BasicMapQuartzBeta1_JavaSE` directory in this repository. Then compile and run the app using Ant or NetBeans. The code should work in other IDEs and build environments as well.

To run the Android application, you need the [ArcGIS Runtime SDK for Android, version Quartz](https://developers.arcgis.com/android/beta/), which this repository's Android Studio project loads from online libraries automatically. If desired, you can download the Runtime SDK and use it locally instead of the online SDK. Compile and run the app using Android Studio.

## Feedback

If you have feedback specific to code in this repository, please submit an issue. If you would like to contribute code changes or additions, please create a pull request.

If you have feedback about ArcGIS Runtime Quartz beta releases, please login to the [Early Adopter site](https://earlyadopter.esri.com) and submit feedback. If you have feedback about a released version of ArcGIS Runtime, please [submit a support incident](http://support.esri.com/).

## Licensing

Copyright 2015-2016 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt) file.
