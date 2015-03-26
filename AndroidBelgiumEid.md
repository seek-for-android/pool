# Introduction #

This page explains how to make the Belgium eID application for android work, using an android phone (version 1.6 or higher) with the G&D smart card access libraries available, and a [G&D secure microSD card](http://www.gd-sfs.com/).
To make the application work you will need to personalise your eID Java Card applet. This is done using the open source eID Quick Key Toolset application found [here](http://code.google.com/p/eid-quick-key-toolset/).


# Downloads #

From the download page get:
  * The android eID application: [EidForAndroid.apk](http://code.google.com/p/seek-for-android/downloads/detail?name=EidForAndroid.apk&can=2&q=)
  * The specimen Belgium eID data file: [test\_card.xml](http://code.google.com/p/seek-for-android/downloads/detail?name=test_card.xml&can=2&q=)
  * The eID Quick Key Toolset: [eid-toolset-package-1.0-SNAPSHOT.zip](http://code.google.com/p/seek-for-android/downloads/detail?name=eid-toolset-package-1.0-SNAPSHOT.zip&can=2&q=)

Make sure you have Java SDK and JDK version 1.6 or above installed on your system. You will also need a Java Card application uploading tool such as Jload from G&D (provided with the secure microSD card).

# Card Personalisation #

First make your secure microSD card accessible as a smartcard to your machine through a PCSC library. Once this is done you can unpack the toolset zip file to your preferred folder. In this folder you can start the card personalization application by running either eid-quick-key-toolset.exe under Windows or ./eid-quick-key-toolset.sh under Linux. The application to personalize your card will now extract some .jar files and start.
NOTE: under Linux it may be that the application complains about executable right on the 'converter' script. Go to /java\_card\_kit-2\_2\_1/bin and change the permissions of the 'converter' file to executable to fix this problem.

During the .jar files extraction, the Java Card eID .cap file was put in the /be/fedict/eidapplet/javacard/eidapplet.cap folder. As the eID Quick Key Toolset does not support the G&D microSD card, you will need to upload the .cap file using your own tool. Once this is done you can proceed further.

In the application go to 'File' and then 'Load' and select the test\_card.xml file you just downloaded. If everything goes fine you should see the following:

![http://seek-for-android.googlecode.com/svn/wiki/img/screenshot_eidtoolset.png](http://seek-for-android.googlecode.com/svn/wiki/img/screenshot_eidtoolset.png)

Then you should go to 'Actions' and click 'Write'. Make sure that when you do so under the 'Readers' tab, the currently selected writer is the secure microSD card.
The application will ask you if you want to write an empty eID first. As the application does not support installation of ,cap files on the G&D card, you should click 'No'. The Java Card eID will now be personalized with the data from the test\_card.xml file. This can take some time, but eventually the application will ask you if you want to activate the applet. Activating the applet means its data will not be modifiable anymore.

# The android eID application #

After personalization, you can insert the secure microSD card in the microSD slot of your android phone. You should then install the EidForAndroid.apk file on your android phone and finally when starting up the application you should get something like this:

![http://seek-for-android.googlecode.com/svn/wiki/img/screenshot_android.png](http://seek-for-android.googlecode.com/svn/wiki/img/screenshot_android.png)

You are now able to use the application functionalities such as viewing your card data, saving certificates or testing your PIN (the default PIN value is '1234').
Note that as this is an alfa version of the application some functionalities do not work yet (such as storing your card data or loading someone else's), and that other functionalities like signing files or checking someone else's validity are not implemented yet.