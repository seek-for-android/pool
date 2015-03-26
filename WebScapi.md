# Introduction #

The SIMalliance OpenMobile API can get integrated in the Android platform with the SmartCard API. Android using the shared library can get access to Secure Elements using the interfaces defined in the OMAPI spec.<br><br>
However, browser based applications gain importance with new HTML5 features and the benefit that browser applications are platform independent compared to <i>native</i> applications for the mobile device. Making the OpenMobile API available in the browser provides the possibility to create security aware applications using features of Secure Elements in HTML applications as well.<br><br>
The WebScapi (SmartCard API for the Web) project is still a very <b>early prototype</b> but will get extended over the time.<br><br>

<h1>Building</h1>
<ul><li>add the SmartCard API patch file(s) as normal to build the system<br>
</li><li>download the <a href='http://code.google.com/p/seek-for-android/downloads/detail?name=webscapi-1_0_0.tgz'>webscapi-1_0_0.tgz</a> and extract the content<br>
</li><li>apply the WebScapi patch with<br>
<pre><code>$ cd &lt;ANDROID_ROOT_DIR&gt;<br>
$ patch -p1 &lt; &lt;path_to_my_patches&gt;/webscapi.patch<br>
</code></pre>
</li><li>build & flash the system as normal...not yet...</li></ul>

<h3>Building Issues</h3>
When the WebScapi is compiled with <code>make</code> and flashed on a device the corresponding files are included in the <code>system.img</code> properly but when accessing the browser plugin it will cause an exception similar to:<br>
<pre><code>E/dalvikvm(  667): Dex cache directory isn't writable: /data/dalvik-cache<br>
I/dalvikvm(  667): Unable to open or create cache for /system/app/WebScapi.apk (/data/dalvik-cache/system@app@WebScapi.apk@classes.dex)<br>
E/PluginManager(  667): Can't find plugin's class: com.gieseckedevrient.android.webscapi.TransportPlugin<br>
E/browser (  667): Console: Uncaught TypeError: Object #&lt;HTMLObjectElement&gt; has no method 'createSEService' file:///sdcard/WebScapi.js:23<br>
</code></pre>
This looks like a browser plugin needs to be in odex format, either when building the platform or through dalvik-cache?!<br>
<br>
<h4>Workarounds</h4>
<ul><li>either build the system with odex files:<br>
<pre><code>$ make -jX WITH_DEXPREOPT=true <br>
</code></pre>
and flash normally or<br>
</li><li>install the browser plugin APK like a normal application:<br>
<pre><code>$ adb remount<br>
$ adb shell rm /system/app/WebScapi.apk<br>
$ cd &lt;ANDROID_ROOT_DIR&gt;<br>
$ adb install out/target/product/&lt;platform&gt;/system/app/WebScapi.apk<br>
</code></pre>
and reboot.<br>
Even now, it turned out that the current implementation is only accessible when builing the ENG target, USERDEBUG & USER will not work.<br>
Ensure to compile with <code>lunch full_crespo-eng</code>
<br><br></li></ul>

<h1>Using</h1>
For a simple test, push the small HTML demo on the device and open it in the Android browser:<br>
<pre><code>$ adb push packages/apps/WebScapi/html/index.html /sdcard/<br>
$ adb push packages/apps/WebScapi/html/WebScapi.js /sdcard/<br>
</code></pre>

<table><tr>
<td width='40%' valign='top'>Within the browser, open the URI<br>
<pre><code>file:///sdcard/index.html<br>
</code></pre>
The PerformanceTester application will be converted to HTML in a next step.<br /><br />
<h4>Creating an application</h4>
Include the Javascript code from <code>WebScapi.js</code> as provided in the sample application, create an instance of <code>SEService</code> and use all interfaces of the OpenMobile API as is <i>native</i> Android applications.<br>
</td>
<td width='10%'>
</td>
<td><img width='60%' src='http://seek-for-android.googlecode.com/svn/wiki/img/webscapi-sample.png' />
</td></tr></table>
Developed and tested with android-4.0.3_r1 with Nexus S and SIM & SmartMX as Secure Element.