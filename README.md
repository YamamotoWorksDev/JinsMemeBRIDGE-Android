# MemeBRIDGE_for_Android
The repositorty for meme BRIDGE.

### LICENSE
This software is released under the MIT License, see [LICENSE.txt](https://github.com/jins-meme/MemeBRIDGE_for_Android/blob/master/LICENSE.txt).

### SUPPORT ANDROID VERSION
Android 6.0(Marshmallow) or later.

### REQUIREMENTS
To bulid this app, you need to have [JINS developer account](https://developers.jins.com/en/) and create an ID and SECRET for your app.  
After that, please write them in strings.xml.
```xml:strings.xml
<resources>
    <string name="app_name">MEME BRIDGE</string>
  
    <!-- PLEASE INPUT YOUR APP_ID & APP_SECRET -->
    <string name="meme_app_id"></string>
    <string name="meme_app_secret"></string>
</resources>
```

### OSC(Open Sound Control)
The default OSC Setting is as follows.
* Remote IP: 192.168.1.255(Destination IP)
* Remote Port: 10316
* Host Port: 11316

These settings are in MenuFragment.java.
```java:MenuFragment.java
  // Initialize OSC
  memeOSC = new MemeOSC();
  memeOSC.setRemoteIP("192.168.1.255");
  memeOSC.setRemotePort(10316);
  memeOSC.setHostPort(11316);
  memeOSC.initSocket();
```
If you want to change the destination IP and port, please rewrite the above codes.

### Bluetooth SPP Test Program for Win/Mac
The MemeBTSPPTester is developed on JetBrain IntelliJ and JDK 1.8.  
[MemeBTSPPTester](https://github.com/tkrworks/MemeBTSPPTester)

### MemeBRIDGE for AndroidWear
The alpha version is released [here](https://github.com/tkrworks/MemeBRIDGE_for_AndroidWear).
