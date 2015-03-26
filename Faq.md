<br />
The following summary is a list of (frequent) questions that we received through Google Groups, Email and/or direct contacts over the time.
<br /><br />

<table>

<tr><td valign='top'><b>Q</b></td><td><font color='red'><b>I've filed a comment but I do not get an answer?</b></font></td></tr>
<tr><td valign='top'><b>A</b></td><td><font color='red'><b>Please use the mailing list for questions and support and Wiki page comments to improve the documentation only.</font></td></tr></b><tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>What version of Android is supported by SEEK?</td></tr>
<tr><td valign='top'><b>A</b></td><td>The latest version of Android is supported.</td></tr>
<tr><td valign='top'></td><td>...due to limited resources of the team and the variety of Android versions we only concentrate on the latest Android version as good as possible.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>When applying the patches I get a message with 'HUNK failed' - what is wrong?</td></tr>
<tr><td valign='top'><b>A</b></td><td>Nothing as long as the modifications have not been rejected.</td></tr>
<tr><td valign='top'></td><td>...we built and tested the patch files with e.g. android-2.3.1_r1 and in the meantime you work with android-2.3.3_r1 where files might differ already.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>Do you plan to submit the SmartCard API into the Android platform and when?</td></tr>
<tr><td valign='top'><b>A</b></td><td>Yes - that's the overall goal of this project.</td></tr>
<tr><td valign='top'></td><td>...we submitted the patches mid of 2011 - now it's not under our control when/whether they get accepted or not.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>Does SmartCard API support the UICC?</td></tr>
<tr><td valign='top'><b>A</b></td><td>In the Android emulator: <i>yes</i></td></tr>
<tr><td valign='top'></td><td>On a real device: see <i><a href='Devices.md'>Supported Devices</a></i></td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>Why did you not contribute this or work on that topic?</td></tr>
<tr><td valign='top'><b>A</b></td><td>Please file a new feature request under <i><a href='http://code.google.com/p/seek-for-android/issues/list'>Issues</a></i>.</td></tr>
<tr><td valign='top'></td><td>...we only have limited resources so we cannot prioritize everything at the same time.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>What is the issue with a PC/SC interface for this project?</td></tr>
<tr><td valign='top'><b>A</b></td><td>Please read <a href='PCSCLite.md'>PC/SC Lite</a>.</td></tr>
<tr><td valign='top'></td><td>...we do not see PC/SC as a suitable interface for mobile phones, however we need a PC/SC link for native applications.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>Why is the SmartCard API not exporting <i>Reset</i>, <i>PowerOn</i> or <i>PowerOff</i> methods?</td></tr>
<tr><td valign='top'><b>A</b></td><td>In short: because the Secure Elements are under control of the phone, not the user in opposite to a PC.</td></tr>
<tr><td valign='top'></td><td>...think about a <i>Reset</i> for the UICC where the phone goes offline before the user enters the PIN again or a <i>PowerOff</i> on the MSC while the Email client tries to sign the mail.</td></tr>
<tr><td><br /></td><td></td></tr>

<tr><td valign='top'><b>Q</b></td><td>I have an interesting project with Android and Secure Elements, can you help me?</td></tr>
<tr><td valign='top'><b>A</b></td><td>Please make your idea public! If interesting we might be able to support you with hardware, at least support.</td></tr>
<tr><td><br /></td><td></td></tr>

</table>