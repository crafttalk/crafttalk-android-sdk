# ChatView

[![](https://jitpack.io/v/crafttalk/crafttalk-android-sdk.svg)](https://jitpack.io/#crafttalk/crafttalk-android-sdk)

## Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
	repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library to your module `build.gradle`
```gradle
dependencies {
    implementation 'com.github.crafttalk:crafttalk-android-sdk:latest.release.version'
}
```

## Usage
There is a [sample](https://github.com/crafttalk/crafttalk-android-sdk/tree/master/app) provided which shows how to use the library. 
Order of steps:
```xml
<com.crafttalk.chat.presentation.ChatView
        android:id="@+id/chat_view"
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        app:color_main="@color/color_main"
        app:color_bg_user_message="@color/color_main"
        app:color_text_server_action="@color/color_main"
        app:color_text_user_message="@color/color_user_message"
        app:progressIndeterminateDrawable="@drawable/spinner"
        app:auth="AUTH_WITHOUT_FORM"
        app:timeDelayed="1000"
        app:urlSocketHost="@string/urlSocketHost"
        app:urlSocketNameSpace="@string/urlSocketNameSpace"
        app:urlUploadHost="@string/urlUploadHost"
        app:urlUploadNameSpace="@string/urlUploadNameSpace"/>
```

There are two modes of operation:
- AUTH_WITHOUT_FORM
- AUTH_WITH_FORM

As the name suggests, the first type allows authentication implicitly for the user, the second type, on the contrary, explicitly.

The init and wakeUp methods should be called as early as possible

When using the first option (AUTH_WITHOUT_FORM)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Chat.init(
            this,
            AuthType.AUTH_WITHOUT_FORM,
            getString(R.string.urlSocketHost),
            getString(R.string.urlSocketNameSpace)
        )      
    }

    override fun onResume() {
        super.onResume()
        Chat.wakeUp(getVisitor(this))
    }

    override fun onStop() {
        super.onStop()
        Chat.destroy()
    }
```

When using the second option (AUTH_WITH_FORM)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Chat.init(
            this,
            AuthType.AUTH_WITH_FORM,
            getString(R.string.urlSocketHost),
            getString(R.string.urlSocketNameSpace)
        )      
    }

    override fun onResume() {
        super.onResume()
        Chat.wakeUp(null)
    }

    override fun onStop() {
        super.onStop()
        Chat.destroy()
    }
```

The code to add to the fragment with the ChatView (for all types)
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chat_view.onCreate(this)      
    }

    override fun onResume() {
        super.onResume()
        chat_view.onResume(this)
    }
```

License
--------

    Copyright 2018 Chris Banes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
