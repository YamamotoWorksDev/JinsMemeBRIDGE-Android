# MemeBRIDGE_for_Android
The repositorty for meme BRIDGE.

### PROGRAMMER
[Nariaki Iwatani](https://github.com/nariakiiwatani)([Anno Lab Inc.](http://annolab.com/annoentrance/)) and [Shunichi Yamamoto](https://github.com/tkrworks)([Yamamoto Works Ltd.](http://atelier.tkrworks.net))

### DESIGNER
Tsuyoshi Otabe([Invisible Designs Lab.](http://invisi.jp))

### LICENSE
This software's source codes are released under the MIT License, see [LICENSE.txt](https://github.com/jins-meme/MemeBRIDGE_for_Android/blob/master/LICENSE.txt).  
And the drawable folder's PNGs by Tsuyoshi Otabe are licensed under a [Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/).

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

Also, it's editable through the app such as the following screen shot.

<image src="https://github.com/jins-meme/JinsMemeBRIDGE-Android/blob/image/basic_setting.png" width="320px">

### OSC(Open Sound Control)
The default OSC Setting is as follows.
* Remote IP: xxx.xxx.xxx.255(Destination IP / Local Network Broadcast Address)
* Remote Port: 10316
* Host Port: 11316

These settings are editable on OSC SETTING.

<image src="https://github.com/jins-meme/JinsMemeBRIDGE-Android/blob/image/osc_setting.png" width="320px">

### Bluetooth SPP Test Program for Win/Mac
The MemeBTSPPTester is developed on JetBrain IntelliJ and JDK 1.8.  
[MemeBTSPPTester](https://github.com/tkrworks/MemeBTSPPTester)

### MemeBRIDGE for AndroidWear
The alpha version is released [here](https://github.com/tkrworks/MemeBRIDGE_for_AndroidWear).
