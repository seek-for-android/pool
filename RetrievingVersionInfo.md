# Introduction #

Sometimes it is needed to know the SmartCard API version info.

This can be accomplished without bothering an SmartCard API function but using following method.

# Details #

Following method retrieves the version info of the SmartCard API:
```
    private String getScapiVersion() {
        try {
	    PackageInfo packageInfo = getPackageManager().getPackageInfo("android.smartcard", 0);
	    return packageInfo.versionName;
	} catch (PackageManager.NameNotFoundException e1) {
	    try {
		PackageInfo packageInfo = getPackageManager().getPackageInfo("org.simalliance.openmobileapi.service", 0);
		return packageInfo.versionName;
	    } catch (PackageManager.NameNotFoundException e2) {
		return "";
	    }
	}
    }
```