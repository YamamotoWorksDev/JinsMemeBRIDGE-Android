# MemeBRIDGE_for_Android
The repositorty for meme BRIDGE.

### LICENSE
This software is released under the MIT License, see LICENSE.txt.

### REQUIREMENTS
To bulid this app, you need to have [JINS developer account](https://developers.jins.com/en/) and create an ID and SECRET for your app.  
After that, please write them to "APP_ID" and "APP_SECRET" in MainActivity.java.

### OSC(Open Sound Control)
The default OSC Setting is as follows.
* Remote IP: 192.168.1.255(Destination IP)
* Remote Port: 10316
* Host Port: 11316

These settings are in MenuFragment.java.
```java
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
