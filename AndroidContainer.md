# Introduction #

This document describes how to setup and run multiple isolated Android user-space instances on a commercial mobile device like Nexus One. Operating system-level virtualization method Linux Containers (LXC) is applied to create and run Android Containers on a single control host. The standard Android Kernel is modified to boot GNU-Linux from SD-card and to provide isolation mechanisms and resource management features for Android user-space.

<br>
<h1>1 Requirements</h1>
<ul><li>Nexus One<br>
</li><li>SD-card (> 2GB recommended)<br>
</li><li>x86-Linux environment<br>
</li><li>Sun Java 6 JDK, JRE<br>
</li><li>Version Control tools Repo and Git (<a href='http://source.android.com/source/downloading.html'>download</a>)</li></ul>

<br>
<h1>2 Building and flashing the kernel</h1>

In this section we describe how to modify the stock Nexus One firmware (mainly boot.img) to be able to boot a GNU-Linux directly from SD-card. This method enables us to run a plain GNU-Linux environment natively (no chroot!) on the device hardware. Additionally kernel configuration options and source code are patched to provide LXC container virtualization environment. Finally we describe how the modified kernel is properly packaged into a boot.img and flashed on the Nexus One device.<br>
<br>
<br>
<h2>2.1 Download Android source and setup build environment</h2>

In order to compile the kernel from scratch a toolchain for cross-compiling is needed. Please follow instructions from Google <a href='http://http://source.android.com/source/initializing.html'>here</a> for setting up a Linux or MacOSX build environment and <a href='http://source.android.com/source/downloading.html'>here</a> for getting the Android SDK which contains a pre-built toolchain. The Android source directory is from now on indicated as ANDROID_DIR.<br>
<br>
<br>
<h2>2.2 Download and patch kernel sources</h2>

The kernel sources for Nexus One can be downloaded through the command:<br>
<pre><code>$ git clone https://android.googlesource.com/kernel/msm.git<br>
The repository is created in the subfolder msm<br>
$ cd &lt;MSM_DIR&gt;<br>
</code></pre>

Note: Currently we can only provide a patch for 2.6.35.7 kernel.<br>
<br>
Download the <a href='http://seek-for-android.googlecode.com/files/sdcard-lxc-kernel.tar.gz'>sdcard-lxc-kernel.tar.gz</a> patch and extract the content:<br>
<br>
<ul><li>sdcard-lxc-kernel.patch - patches kernel for SD-card boot and LXC support<br>
</li><li>readme.txt - description how to build</li></ul>

Apply the patch:<br>
<br>
<pre><code>&lt;MSM_DIR&gt;$ patch -p1 &lt; sdcard-lxc-kernel.patch<br>
</code></pre>

<br>
<h2>2.3 Compile and flash the kernel</h2>

Now we are ready to build the kernel:<br>
<br>
<pre><code>&lt;MSM_DIR&gt;$ make ARCH=arm CROSS_COMPILE=&lt;ANDROID_DIR&gt;/prebuilt/linux-x86/toolchain/arm-eabi-4.4.3/bin/arm-eabi- -j4<br>
</code></pre>

Note: In case the new kernel has not the correct kernel revision number or a -dirty flag is appended, you have to create a .scmversion file inside the kernel directory with a proper revision number and build the kernel again:<br>
<pre><code>&lt;MSM_DIR&gt;$ echo '-g3cc95e3' &gt; .scmversion<br>
&lt;MSM_DIR&gt;$ make ARCH=arm CROSS_COMPILE=&lt;ANDROID_DIR&gt;/prebuilt/linux-x86/toolchain/arm-eabi-4.4.3/bin/arm-eabi- -j4<br>
</code></pre>

The newly created kernel MSM_DIR/arch/arm/boot/zImage needs to be packaged into a boot.img before it can be flashed onto device. In order to create and flash the boot.img we need to compile the necessary tools first:<br>
<br>
<pre><code>$ cd &lt;ANDROID_DIR&gt;<br>
&lt;ANDROID_DIR&gt;$ . build/envsetup.sh<br>
&lt;ANDROID_DIR&gt;$ lunch full_passion-eng<br>
&lt;ANDROID_DIR&gt;$ make -j4<br>
</code></pre>

After a successfull build tools like mkbootimg, fastboot and adb are available in the ANDROID_DIR/out/host/linux-x86/bin directory. We add it to the PATH variable:<br>
<br>
<pre><code>$ export PATH=&lt;ANDROID_DIR&gt;/out/host/linux-x86/bin:$PATH<br>
</code></pre>

The goal of the next step is to package the newly created kernel zImage and the unmodified ramdisk.img into a new boot.img. Since we don't want to boot from the ramdisk but directly from the SD-card, the kernel cmdline needs to be modified permanently. The kernel replacement and cmdline modification is achieved by this command:<br>
<br>
<pre><code>$ mkbootimg \ <br>
--kernel &lt;MSM_DIR&gt;/arch/arm/boot/zImage \<br>
--ramdisk &lt;ANDROID_DIR&gt;/out/target/product/passion/ramdisk.img \<br>
--cmdline "no_console_suspend=1 wire.search_count=5 root=/dev/mmcblk0p2 rw rootfs=ext2 init=/sbin/init rootwait noinitrd" \<br>
--base 0x20000000 \<br>
--output &lt;ANDROID_DIR&gt;/out/target/product/passion/boot.img<br>
</code></pre>

Note: For future target devices the correct mkbootimg parameters can be easily determined by recompiling the boot.img with the showcommands option:<br>
<pre><code>&lt;ANDROID_DIR&gt;$ make bootimage showcommands<br>
</code></pre>

Note: If you don't want to build your own image a fully working boot.img can be downloaded here <a href='http://seek-for-android.googlecode.com/files/sdcard-lxc-boot.img'>sdcard-lxc-boot.img</a>

Now we can flash the new boot.img onto the boot partition. The device has to be rooted and the USB permissions have to be setup properly. In case of any problems check <a href='https://www.google.com/search?q=root+nexus+one'>here</a> and  <a href='http://source.android.com/source/initializing.html'>here (section: Configuring USB Access)</a>.<br>
<br>
<pre><code>$ adb reboot bootloader<br>
$ fastboot flash boot &lt;ANDROID_DIR&gt;/out/target/product/passion/boot.img<br>
</code></pre>

Note: Once you replaced the boot.img with modified cmdline to boot from SD-card, you can easily update the kernel zImage on the device without the need to create a boot.img again:<br>
<br>
<pre><code>$ fastboot flash zimage &lt;MSM_DIR&gt;/arch/arm/boot/zImage<br>
</code></pre>

Since we don't touch the original system and userdata partitions, the original boot.img can be flashed to recovery partition to provide a convenient way to boot 'the normal way' from the bootloader menu.<br>
<br>
In the next step we prepare the GNU-linux environment on the SD-card and configure LXC tools for the Android Container.<br>
<br>
<br>
<h1>3 Host System Setup</h1>

For our virtualization solution with Linux Containers (LXC) a standard GNU-linux, in this case Debian (squeeze) distribution is configured to run as host system directly from SD-card on the mobile device. This setup has several advantages, mainly a convenient way of installing new software packages currently available in the Debian repositories for the armel architecture and further avoiding possible space restrictions of the built-in device flash memory.<br>
<br>
<br>
<h2>3.1 Preparing the SD-card</h2>

The following steps have to be performed on any external linux-system with SD-card slot.<br>
<br>
<ul><li>Create two partitions on the SD-card, a small vfat partition as first partition to simulate the standard SD-card and a second bigger ext2 partition (>2GB) where we place our host system and the Android containers (ext3 & ext4 is not recommended due to journaling capabilities which reduce the lifetime of SD-card).<br>
</li><li>Mount the ext2 partition on the linux-system to any directory MNT_DIR and debootstrap a minimal Debian (sqeeze) for the armel architecture:</li></ul>

<pre><code>$ sudo mount /dev/mmcblk0p2 &lt;MNT_DIR&gt;<br>
$ debootstrap --foreign --arch=armel --variant=minbase \ <br>
squeeze &lt;MNT_DIR&gt; http://ftp.debian.org/debian<br>
</code></pre>

For the second step of debootstrapping the system we need to access the debian ext2 partition from the mobile device. In this step all the necessary device nodes etc. are created, thus this task has to be performed in our target system environment, i.e. in the adb shell after a regular boot:<br>
<br>
<pre><code>$ adb shell<br>
# mount -t ext2 /dev/block/mmcblk0p2 &lt;MNT_DIR&gt;<br>
# chroot &lt;MNT_DIR&gt; /debootstrap/debootstrap --second-stage<br>
</code></pre>

Note: mount/chroot can be executed with help of a busybox which can be downloaded <a href='http://busybox.net/downloads/binaries/1.19.0/'>here</a>. How to use busybox on an Android device is explained for example <a href='http://www.saurik.com/id/10'>here</a>.<br>
<br>
<br>
<br>
<h2>3.2 Configuring the Host System</h2>

After a basic Debian system on the ext2 partition of the SD-card is created, several configuration changes have to be made in order to be able to directly boot from it. Thus minimum required settings are presented to perform the very first successful boot and then additional configurations are shown like networking, sound, X11, etc.<br>
<br>
Since there is usually no serial cable available (example how to build a serial cable can be found <a href='http://trac.osuosl.org/trac/replicant/wiki/NexusSBootloader'>here</a>) we have to ensure that the GNU/Linux environment boots up without errors and the adb daemon is started at system startup. If that fails there is no possibility to access the system.<br>
<br>
Perform a regular Android boot and execute these commands:<br>
<pre><code>$ adb shell<br>
<br>
# mount -t ext2 /dev/block/mmcblk0p2 &lt;MNT_DIR&gt;<br>
# cp /sbin/adbd &lt;MNT_DIR&gt;/sbin/adbd<br>
# cat &gt; &lt;MNT_DIR&gt;/etc/rc.local &lt;&lt; EOF<br>
&gt; /sbin/adbd &amp;<br>
&gt; exit 0<br>
&gt; EOF<br>
<br>
# mkdir -p &lt;MNT_DIR&gt;/system/bin/<br>
# ln -s /bin/bash &lt;MNT_DIR&gt;/system/bin/sh<br>
<br>
# mkdir -p /cgroup /media/system /media/cache /media/userdata<br>
# cat &gt; &lt;MNT_DIR&gt;/etc/fstab &lt;&lt; EOF <br>
&gt; /dev/block/mmcblk0p2 / ext2 defaults 1 0<br>
&gt; proc /proc proc defaults 0 0<br>
&gt; devpts /dev/pts devpts defaults 0 0 <br>
&gt; sysfs /sys sysfs defaults 0 0<br>
&gt;<br>
&gt; none /cgroup cgroup defaults 0 0 <br>
&gt; /dev/mtdblock3 /media/system yaffs defaults 0 0<br>
&gt; /dev/mtdblock4 /media/cache yaffs defaults 0 0<br>
&gt; /dev/mtdblock5 /media/userdata yaffs defaults 0 0<br>
&gt; EOF<br>
</code></pre>

Note: Mounting of cgroup partition is needed later for LXC and the yaffs partitions gives us a conventient access to stock Android files, e.g. firmware.<br>
<br>
Now the Debian host system is ready for the first native boot! Test after a few minutes via adb and if that works reboot the device normally to perform further configuration steps. Otherwise check previous steps for errors.<br>
<br>
Next we need to install some packages from the repository, e.g. wpasupplicant for networking. Since we need obviously internet connection to do this, we have to boot Android normally and establish an internet connection by its means. Then we chroot into Debian to install the necessary packages:<br>
<br>
<pre><code>$ adb shell<br>
# mount -t ext2 /dev/block/mmcblk0p2 &lt;MNT_DIR&gt;<br>
# chroot &lt;MNT_DIR&gt; /bin/bash<br>
$ export PATH=/usr/bin:/usr/sbin:/bin:$PATH<br>
$ export TERM=linux<br>
$ echo 'deb http://ftp.debian.org/debian squeeze main' &gt; /etc/apt/sources.list<br>
$ apt-get update<br>
$ apt-get install wpasupplicant wireless-tools<br>
$ apt-get install locales vi<br>
$ exit<br>
</code></pre>

In order to setup networking for the Debian host system, we have to copy the wifi kernel module and the firmware from the regular Android file system, thus they can be loaded at startup from SD-card. Then wpasupplicant is configured to be able to establish wifi connections from within the Debian environment. These commands should be executed inside the Android shell (not chroot!):<br>
<br>
<pre><code>$ mkdir -p &lt;MNT_DIR&gt;/lib/modules/`uname -r`/drivers/net/wireless/<br>
$ cp /system/lib/modules/bcm4329.ko \<br>
  &lt;MNT_DIR&gt;/lib/modules/`uname -r`/drivers/net/wireless/<br>
$ mkdir -p &lt;MNT_DIR&gt;/system/vendor/firmware/<br>
$ cp /system/vendor/firmware/fw_bcm4329.bin \<br>
  &lt;MNT_DIR&gt;/system/vendor/firmware/<br>
$ echo 'bcm4329' &gt; &lt;MNT_DIR&gt;/etc/modules<br>
<br>
$ cat &gt; &lt;MNT_DIR&gt;/etc/network/interfaces &lt;&lt; EOF <br>
&lt; auto lo<br>
&lt; iface lo inet loopback<br>
&lt;<br>
&lt; auto eth0<br>
&lt; iface eth0 inet manual<br>
&lt;   wpa-driver wext<br>
&lt;<br>
&lt; wpa-roam /etc/wpa_supplicant/wpa_supplicant.conf<br>
&lt;<br>
&lt; iface your_wlan_name inet dhcp<br>
&lt; EOF<br>
<br>
$ cat &gt; &lt;MNT_DIR&gt;/etc/wpa_supplicant/wpa_supplicant.conf &lt;&lt; EOF<br>
&lt; ctrl_interface=/var/run/wpa_supplicant<br>
&lt; ap_scan=2<br>
&lt; fast_reauth=1<br>
&lt; network={<br>
&lt;         ssid="your_wlan_ssid"<br>
&lt;         id_str="your_wlan_name"<br>
&lt;         scan_ssid=1<br>
&lt;         mode=0<br>
&lt;         proto=WPA<br>
&lt;         key_mgmt=WPA-PSK<br>
&lt;         pairwise=CCMP TKIP<br>
&lt;         group=TKIP<br>
&lt;         psk="your_wlan_password"<br>
&lt; }<br>
&lt; EOF<br>
<br>
</code></pre>

In case the wpasupplicant settings were correct, we should be able to connect to the local access point and establish an internet connection. The wpa-roam option makes it possible to preconfigure several APs which are dynamically chosen according to current location of the device.<br>
<br>
Since the regular Android environment for networking activities is not required anymore, the Nexus One device can be booted directly from SD-card and all the following steps can be performed in the plain Debian environment accessible through the adb shell.<br>
<br>
For convenience a desktop environment should be installed. Currently XFCE4 works best since it's lightweight and does not start with a login screen, which is a problem due the lack of a hardware keyboard. We use the Debian backports version of xserver-xorg 1:7.6+8~bpo60+1, since the stable version 1:7.5+8+squeeze1 has minor touchscreen handling issues.<br>
<br>
<pre><code>$ apt-get install xfce4 <br>
$ echo 'deb http://backports.debian.org/debian-backports squeeze-backports main' \<br>
  &gt;&gt; /etc/apt/sources.list<br>
$ apt-get update<br>
$ apt-get install xserver-xorg<br>
$ startx &amp;<br>
</code></pre>

Note: To trigger the start of XFCE4 at startup, place 'startx &' inside the /etc/rc.local configuration file before the 'exit 0' statement.<br>
A decent software keyboard solution is matchbox, which provides well proportioned buttons and also can be activated in foreground when a input field is active.<br>
<br>
As next we fix the sound inside Debian to avoid crashing of our Android containers when a sound action is performed. We copy the according binaries to the standard firmware folder where the kernel can find it:<br>
<br>
<pre><code>$ cp -pR /media/system/etc/firmware/default*.acdb /lib/firmware<br>
</code></pre>

In this section we have demonstrated how the basic Debian host system should be configured to enable networking, desktop environment and sound. Further possibilities should be explored to enable GSM radio and bluetooth.<br>
<br>
<br>
<h2>3.3 Setup LXC userspace tools</h2>

This section is intended to give an overview on setting up the Linux Container (LXC) environment in our Debian host system running natively on a Nexus One device. Following prerequisites are described to run basically any Linux Container on this system setup without the restriction to a particular container like Android Container.<br>
<br>
Since the control group file system (cgroup) was setup and mounted in previous steps, we continue with installing the patched LXC package.<br>
<br>
Download this patch <a href='http://seek-for-android.googlecode.com/files/lxc-0.7.5-fd-patch.tar.gz'>lxc-0.7.5-fd-patch.tar.gz</a> and apply it to <a href='http://lxc.sourceforge.net/download/lxc/lxc-0.7.5.tar.gz'>lxc-0.7.5.tar.gz</a> sources, compile and install:<br>
<br>
<pre><code>&lt;LXC_SRC_DIR&gt;$ patch -p1 &lt; lxc-0.7.5-fd.patch<br>
&lt;LXC_SRC_DIR&gt;$ ./configure <br>
&lt;LXC_SRC_DIR&gt;$ make<br>
&lt;LXC_SRC_DIR&gt;$ make install<br>
</code></pre>

Before we create a specific Android container, network bridging has to be configured to enable network access from within the isolated container environment:<br>
<br>
<pre><code>$ cat &gt;&gt; /etc/network/interfaces &lt;&lt; EOF<br>
&gt; auto br0<br>
&gt;<br>
&gt; iface br0 inet static<br>
&gt;   address 192.168.99.1<br>
&gt;   broadcast 0.0.0.0<br>
&gt;   netmask 255.255.255.0<br>
&gt;   bridge_ports none<br>
&gt;   bridge_fd 0<br>
&gt;   bridge_maxwait 0<br>
&gt;   bridge_hello 0<br>
&gt;   bridge_maxage 12<br>
&gt;   bridge_stp off<br>
&gt;   post-up ip route add 192.168.99.201 dev br0<br>
&gt; EOF<br>
$ ifup br0<br>
</code></pre>

Note: An additional bridge device br0 is created with a static ip address. It's not connected to the Debian eth0 interface (bridge_ports none) since the lunch of the Android container alters the MAC address of the bridge which results in connectivity loss to the access point. Instead we use netfilter iptables to route the ip packets to the outer world. For DNS forwarding install dnsmasq and configure as local DNS server. Note: activating the ebtables kernel option in the kernel results in an immediate crash and reboot of the device when a network connection is about to be established. Keep that in mind if you decide to modify the kernel options.<br>
<br>
Now we are ready to create our first Android Container inside our Debian host.<br>
<br>
<br>
<h1>4 Android Container Setup</h1>

In order to create a complete Android system, for instance, the file system structure has to be recreated inside the container rootfs. Following folder tree is created, where ANDROID_LXC contains the rootfs subfolder and the neccesary configuration file for LXC:<br>
<pre><code>+-- &lt;ANDROID_LXC&gt;<br>
        |-- config<br>
        +-- rootfs<br>
</code></pre>

Download the <a href='http://seek-for-android.googlecode.com/files/sdcard-android-lxc-rootfs.tar.gz'>sdcard-android-lxc-rootfs.tar.gz</a> tarball, copy to ANDROID_LXC/rootfs folder and extract:<br>
<br>
<pre><code>&lt;ANDROID_LXC&gt;/rootfs$ tar -xzvf sdcard-android-lxc-rootfs.tar.gz<br>
</code></pre>

Note: All in this image included modifications were applied to Android 2.3.x Gingerbread and are explained in section 4.3.<br>
<br>
<br>
<h2>4.1 Container Configuration File</h2>

As next step we introduce the LXC configuration file settings which enable us to run the Android Container (to be placed inside the ANDROID_LXC/config file):<br>
<br>
<pre><code>lxc.utsname = android<br>
lxc.tty = 4<br>
lxc.rootfs = &lt;ANDROID_LXC&gt;//rootfs<br>
<br>
# network<br>
lxc.network.type = veth<br>
lxc.network.flags = up<br>
lxc.network.link = br0<br>
lxc.network.ipv4 = 192.168.99.202 0.0.0.0 <br>
lxc.network.name = eth0<br>
lxc.network.veth.pair = vethvm2<br>
<br>
lxc.cgroup.devices.deny = a # deny all first<br>
<br>
# mount points<br>
lxc.mount.entry=none &lt;ANDROID_LXC&gt;//rootfs/proc proc defaults 0 0<br>
lxc.mount.entry=none &lt;ANDROID_LXC&gt;//rootfs/sys sysfs defaults 0 0<br>
lxc.mount.entry=/lib &lt;ANDROID_LXC&gt;//rootfs/lib none ro,bind 0 0<br>
lxc.mount.entry=/usr/share/locale &lt;ANDROID_LXC&gt;//rootfs/usr/share/locale none rw,bind 0 0<br>
<br>
</code></pre>


<br>
<h2>4.2 Device Nodes</h2>

Through the extensive device nodes permission setting we are able to deny access to any particular device node throgh the LXC resource isolation mechanism. These settings have to be appended to the configuration file:<br>
<br>
<pre><code># /dev/null and zero<br>
lxc.cgroup.devices.allow = c 1:3 rwm<br>
lxc.cgroup.devices.allow = c 1:5 rwm<br>
# consoles<br>
lxc.cgroup.devices.allow = c 5:* rwm<br>
lxc.cgroup.devices.allow = c 4:* rwm<br>
# /dev/{,u}random<br>
lxc.cgroup.devices.allow = c 1:9 rwm<br>
lxc.cgroup.devices.allow = c 1:8 rwm<br>
# /dev/pts/* - pts namespaces<br>
lxc.cgroup.devices.allow = c 136:* rwm<br>
lxc.cgroup.devices.allow = c 5:2 rwm<br>
# rtc<br>
lxc.cgroup.devices.allow = c 254:0 rwm<br>
# nexus specific<br>
lxc.cgroup.devices.allow = c 1:7 rwm # dev/full<br>
lxc.cgroup.devices.allow = c 1:11 rwm # dev/kmsg<br>
lxc.cgroup.devices.allow = c 7:* rwm # <br>
# cpu and memory<br>
#lxc.cgroup.cpuset.cpus = 0<br>
#lxc.cgroup.cpu.shares = 1024<br>
#lxc.cgroup.memory.limit_in_bytes = 512M<br>
#lxc.cgroup.memory.memsw.limit_in_bytes = 512M<br>
<br>
# 10:* devices<br>
lxc.cgroup.devices.allow = c 10:0 rwm # dev/pmem<br>
lxc.cgroup.devices.allow = c 10:1 rwm # dev/pmem_adsp<br>
lxc.cgroup.devices.allow = c 10:2 rwm # dev/pmem_camera<br>
lxc.cgroup.devices.allow = c 10:223 rwm # dev/uinput <br>
<br>
lxc.cgroup.devices.allow = c 10:30 rwm<br>
lxc.cgroup.devices.allow = c 10:31 rwm<br>
lxc.cgroup.devices.allow = c 10:32 rwm<br>
lxc.cgroup.devices.allow = c 10:33 rwm<br>
lxc.cgroup.devices.allow = c 10:34 rwm<br>
lxc.cgroup.devices.allow = c 10:35 rwm<br>
lxc.cgroup.devices.allow = c 10:36 rwm<br>
lxc.cgroup.devices.allow = c 10:37 rwm<br>
lxc.cgroup.devices.allow = c 10:39 rwm<br>
<br>
lxc.cgroup.devices.allow = c 10:40 rwm # /dev/keychord<br>
lxc.cgroup.devices.allow = c 10:41 rwm<br>
lxc.cgroup.devices.allow = c 10:42 rwm<br>
lxc.cgroup.devices.allow = c 10:43 rwm<br>
lxc.cgroup.devices.allow = c 10:44 rwm<br>
lxc.cgroup.devices.allow = c 10:45 rwm<br>
lxc.cgroup.devices.allow = c 10:46 rwm<br>
lxc.cgroup.devices.allow = c 10:47 rwm<br>
lxc.cgroup.devices.allow = c 10:48 rwm<br>
lxc.cgroup.devices.allow = c 10:49 rwm<br>
<br>
lxc.cgroup.devices.allow = c 10:50 rwm<br>
lxc.cgroup.devices.allow = c 10:51 rwm<br>
lxc.cgroup.devices.allow = c 10:52 rwm<br>
lxc.cgroup.devices.allow = c 10:53 rwm<br>
lxc.cgroup.devices.allow = c 10:54 rwm<br>
lxc.cgroup.devices.allow = c 10:55 rwm<br>
lxc.cgroup.devices.allow = c 10:56 rwm<br>
lxc.cgroup.devices.allow = c 10:57 rwm<br>
lxc.cgroup.devices.allow = c 10:58 rwm<br>
lxc.cgroup.devices.allow = c 10:59 rwm<br>
<br>
lxc.cgroup.devices.allow = c 10:60 rwm<br>
lxc.cgroup.devices.allow = c 10:61 rwm<br>
lxc.cgroup.devices.allow = c 10:62 rwm<br>
lxc.cgroup.devices.allow = c 10:63 rwm<br>
<br>
# dev/input/*<br>
lxc.cgroup.devices.allow = c 13:64 rwm # event0 -&gt; lightsensor-level<br>
lxc.cgroup.devices.allow = c 13:65 rwm # event1 -&gt; h2w headset<br>
lxc.cgroup.devices.allow = c 13:66 rwm # event2 -&gt; compass<br>
lxc.cgroup.devices.allow = c 13:67 rwm # event3 -&gt; synaptics-rmi-touchscreen<br>
lxc.cgroup.devices.allow = c 13:68 rwm # event4 -&gt; proximity<br>
lxc.cgroup.devices.allow = c 13:69 rwm # event5 -&gt; mahimahi-keypad<br>
lxc.cgroup.devices.allow = c 13:70 rwm # event6 -&gt; mahimahi-nav<br>
<br>
lxc.cgroup.devices.allow = c 29:0 rwm # dev/fb0<br>
lxc.cgroup.devices.allow = c 31:* rwm # dev/block/mtdblock*<br>
lxc.cgroup.devices.allow = c 90:* rwm # dev/mtd<br>
lxc.cgroup.devices.allow = c 108:0 rwm # dev/ppp<br>
lxc.cgroup.devices.allow = c 179:* rwm # dev/block/mmcblk0*<br>
lxc.cgroup.devices.allow = c 248:* rwm # control0, config0, frame0<br>
lxc.cgroup.devices.allow = c 251:0 rwm # dev/q6venc<br>
lxc.cgroup.devices.allow = c 252:0 rwm # dev/vdec<br>
lxc.cgroup.devices.allow = c 250:0 rwm # dev/ttyHS0<br>
lxc.cgroup.devices.allow = c 253:* rwm<br>
<br>
</code></pre>

<br>
<h2>4.3 Android modifications</h2>

Here we describe which changes that had to be applied to Android 2.3.x Gingerbread to run the Android Container on the Nexus One device. We only list the components which were modified since an extensive description would extend the scope of this document:<br>
init.rc, init, DalvikVM, system_server, busybox (route), dns, etc...<br>
<br>
<br>
<h1>5 Notes</h1>
We advice to perform a file system check on the SD-card regularly with fsck.ext2, since in some cases (e.g. after system crash) the system is not able to boot because of temporary system files.<br>
<br>
If neccessary a button trigger daemon can be installed to manage the start of Android Container by pressing predefined hardware buttons. For this purpose the Debian package <a href='http://packages.debian.org/sid/triggerhappy'>triggerhappy</a> can be installed to execute a shell script which starts the Android Container and which is triggered by events on /dev/input/event<code>*</code> devices.<br>
<br>
<br>
<h1>Preview</h1>
Here is a list of ongoing feature development:<br>
<ul><li>sdcard emulation<br>
</li><li>lxc-console<br>
</li><li>parallel fb access<br>
</li><li>secureSD</li></ul>


<br>
<h1>References</h1>
<a href='http://en.wikipedia.org/wiki/Operating_system-level_virtualization'>http://en.wikipedia.org/wiki/Operating_system-level_virtualization</a> <br>
<a href='http://lxc.sourceforge.net/'>http://lxc.sourceforge.net/</a><br>
<a href='http://lxc.teegra.net/'>http://lxc.teegra.net/</a><br>
<a href='http://www.irregular-expression.com/?p=30'>http://www.irregular-expression.com/?p=30</a><br>
<a href='http://www.saurik.com/id/10'>http://www.saurik.com/id/10</a><br>



<br>
<h1>Useful Resources</h1>
<a href='http://source.android.com/source/downloading.html'>http://source.android.com/source/downloading.html</a><br>

<br>
<br>
<br>
<br>