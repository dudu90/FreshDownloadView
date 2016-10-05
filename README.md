# FreshDownloadView
##About
FreshDownloadView is a java library for Android，It's a good way to show download progress with a cool animtion.some inspiration are from
[Dribbble](https://dribbble.com/shots/2939772--Daily-gif-Download)
##Demo
![](https://github.com/dudu90/FreshDownloadView/blob/master/screen/screen.gif)
##Usage
###Setp 1:Add it to your project:
####gradle:
add my maven path
```groovy
repositories {
    maven {
        url 'https://dl.bintray.com/dudu90/maven'
    }
}
```
then add dependencies
```groovy
	compile 'com.pitt.fresh.library:freshdownloadview:1.0'
```
####maven:
```xml
<dependency>
  <groupId>com.pitt.fresh.library</groupId>
  <artifactId>freshdownloadview</artifactId>
  <version>1.0</version>
  <type>pom</type>
</dependency>
```
###Setp 1:Add the View to your xml
```xml
<com.pitt.library.fresh.FreshDownloadView
        android:id="@+id/pitt"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#006cc7" />
```
## XML attributes
| Name | Type | Default | Description |
|circular_radius|dimension|80dp|the circular's radius|
|circular_color|color|#4c99d9|the base circular's color|
|circular_progress_color|color|#ffffff|It's the circular's color when the circular show progress,and it is also text color when show progress,it's the symbol'√' and '×''s color|
|circular_width|dimension|3.5dp|the circular width(not means the circular's radius)|
|progress_text_size|dimension|50sp|the text's size when show progress|
##In java
```java
freshDownloadView.upDateProgress(float progress);
freshDownloadView.upDateProgress(int progress);
```
means update the progress
```java
freshDownloadView.reset();
```
this Method will reset all status;
```java
freshDownloadView.showDownloadError();
```
this Method will show error status,if this download error,you can allocate it;
##About me
[Weibo](http://weibo.com/5851968288)
Gmail:[](fengshengq@gmail.com)
License
-------

    Copyright 2016 Pitt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.