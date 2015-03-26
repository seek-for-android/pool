# Introduction #

The [Compatibility Test Suite (CTS)](http://source.android.com/compatibility/cts-intro.html) can be extended so it can be used also to test the Open Mobile API (Smartcard API 2.3.X).

All test cases for testing the Open Mobile API are contained in a test plan called 'OpenMobile'.

The tests include and utilize a mock plug-in reader that simulates a secure element with an appropriate applet. So the tests can be executed without a real card.

The following paragraphs describe
  * how to build an extended CTS containing the additional Open Mobile API tests,
  * how to run the tests, and
  * where to find documentation and results of the tests.

# How to Build the CTS #

For building CTS with Open Mobile API tests
  * change into directory of Android build with Open Mobile API
  * extract file `smartcard-api-2_3_2/cts.patch` from archive `smartcard-api-2_3_2.tgz` ([download](http://seek-for-android.googlecode.com/files/smartcard-api-2_3_2.tgz)) and apply it as patch and
  * build the CTS:

```
$ cd ANDROID_ROOT_DIR
$ tar -xOf PATH_TO_PATCH_ARCHIVE/smartcard-api-2_3_2.tgz smartcard-api-2_3_2/cts.patch | patch -p1 
$ make cts
```

# How to Run the CTS Tests #
After the CTS has been built successfully with the Open Mobile API
test extensions, in directory `out/host/linux-x86/cts/android-cts/tools`
the test tool `startcts` (Android 2.3.x) / `cts-tradefed` (Android 4.0.x) can be found .<br>

Execution of the whole test plan for the Open Mobile API is started by the following command:<br>
for Android 2.3.x:<br>
<pre><code>$ ./out/host/linux-x86/cts/android-cts/tools/startcts start --plan OpenMobile<br>
</code></pre>

for Android 4.0.x:<br>
<pre><code>$ ./out/host/linux-x86/cts/android-cts/tools/cts-tradefed run cts --plan OpenMobile<br>
</code></pre>

It is also possible to execute only a part of a test plan.<br>
E.g. to execute only tests of test case <code>TestConformanceRequirements</code> use the following command:<br>
for Android 2.3.x:<br>
<pre><code>$ ./out/host/linux-x86/cts/android-cts/tools/startcts start --plan OpenMobile -p org.simalliance.openmobileapi.cts.TestConformanceRequirements<br>
</code></pre>

for Android 4.0.x:<br>
<pre><code>$ ./out/host/linux-x86/cts/android-cts/tools/cts-tradefed run cts --class org.simalliance.openmobileapi.cts.TestConformanceRequirements<br>
</code></pre>

If logging of test plan results is not required (e.g. for quick tests) the tests can also be executed without the CTS test tool by following two <code>adb</code> commands:<br>
<br>
First install the test package...<br>
<pre><code>$ adb install -r out/host/linux-x86/cts/android-cts/repository/testcases/CtsOpenMobileApiTestCases.apk<br>
</code></pre>

...then start the execution:<br>
<pre><code>$ adb shell am instrument -w [ -e reader &lt;reader&gt; ] [ -e class &lt;class&gt; ] org.simalliance.openmobileapi.cts/org.simalliance.openmobileapi.cts.InstrumentationTestRunnerParameterized<br>
</code></pre>
Parameters <code>-e reader &lt;reader&gt;</code> and <code>-e class &lt;class&gt;</code> are optional.<br>
If the <code>reader</code> parameter is not present "CTSMock" reader is used.<br>
If the <code>class</code> parameter is not present all test classes of the package are executed.<br>
<br>
E.g.<br>
<pre><code>adb shell am instrument -w org.simalliance.openmobileapi.cts/org.simalliance.openmobileapi.cts.InstrumentationTestRunnerParameterized<br>
</code></pre>
or<br>
<pre><code>adb shell am instrument -w -e reader "CTSMock" -e class org.simalliance.openmobileapi.cts.TestConformanceRequirements org.simalliance.openmobileapi.cts/org.simalliance.openmobileapi.cts.InstrumentationTestRunnerParameterized<br>
</code></pre>

<h1>Documentation and Results of CTS Tests</h1>

The documentation of the test cases of the test plans for the Open Mobile API are located in<br>
<code>out/host/linux-x86/cts/android-cts/repository/testcases/CtsOpenMobileTestCases.xml</code>

The results of test runs are stored in directory<br>
<code>out/host/linux-x86/cts/android-cts/repository/results</code>.<br>
<br>
Sample result files of a test run of this test plan can be downloaded <a href='http://seek-for-android.googlecode.com/files/CTS_results-2_3_0.zip'>here</a>.<br>
<br><br>