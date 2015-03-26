## Introduction ##

[Building the system](BuildingTheSystem.md) might cause difficulties and it's unclear if the flashed test device contains a fully integrated SmartCard API with proper Secure Element access.<br />
When using the patches together with the Mobile Security Card it's unlikely to expect different behaviors but for OEMs that adapt the RIL interface towards the baseband it's required to provide a test tool to ensure the overall system integration.<br />
We need to split the test setup in different scenarios, like
  * SmartCard API system tests
  * Access control scheme tests
  * CTS conformance tests
<br />


## Part1: Performance tester ##

The _PerformanceTester_ is a tool suitable for system integrators or developers who need to verify that the SmartCard API integration was successful and the access to the Secure Element is working.
  * Download and extract the [PerformanceTester](https://code.google.com/p/seek-for-android/downloads/detail?name=PerformanceTester-1_3_0.tgz&can=2&q=)

  * The package contains:
    * PerformanceTester.apk: The Android application
    * PerformanceTester.cap: JavaCard applet that must be downloaded into the SE as counterpart by using JLoad or similar tools

  * PerformaceTester runs all APDU test cases - case-{1,2,3,4} commands - with all possible command and response length in a predefined loop

  * The application determines the performance characteristics of the SmartcardAPI:
    * Time measurement of APDU communication
    * CPU usage of the SmartcardAPI
    * Memory consumption of the !SmartCardAPI
    * Energy consumption during the test

  * Features
    * Each APDU during the test will be verified for correctness.
    * Configuration of the APDU generator to define a test run.
    * Selection of the SE (the counterpart of this test application)
    * Selection of the communication channel
    * Generation of a test report
    * Real time view for observing the communication

Note: _PerformanceTester_ relies on the SmartCard API integrated in the Android platform. Thus, _MSC SmartcardService_ version 1.5 will not work.