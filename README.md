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

AndroidManifest (for all types)
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA"/>

<application
    ...>
    <meta-data
    	android:name="com.google.firebase.messaging.default_notification_channel_id"
	android:value="@string/default_notification_channel_id" />
    <activity
    	android:screenOrientation="portrait"
	android:name=".MainActivity"
	android:windowSoftInputMode="adjustPan|stateAlwaysHidden" >
        <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
    </activity>
    <service
        android:name="com.crafttalk.chat.data.push.ChatPushService"
	android:exported="false">
        <intent-filter>
	    <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application> 
```

## Кастомизация

Настройка ChatView осуществляется в файле xml.
Для кастомизации используются атрибуты. Атрибуты делятся на две группы. Одни задают поведение чата, другие определяют визуальную составляющую.


Атрибуты, настраивающие поведение:
- auth - может принимать AUTH_WITH_FORM или AUTH_WITHOUT_FORM. Этот атрибут определяет, как будет проходить аутентификация для пользователя
- timeDelayed - выставляет минимальное время отображения троббера
- urlSocketNameSpace - указывает, по какому nameSpace подключать сокет
- urlSocketHost - указывает, по какому host подключать сокет
- urlUploadNameSpace - указывает, по какому nameSpace будут грузиться файлы
- urlUploadHost - указывает, по какому baseUrl будут грузиться файлы     


Атрибуты, настраивающие внешний вид:

Цвета:
- color_main - 
- color_bg_user_message - устанавливает цвет фона пользовательского сообщения
- color_bg_server_message - устанавливает цвет фона сообщения бота/оператора
- color_text_user_message - устанавливает цвет текста пользовательского сообщения
- color_text_server_message - устанавливает цвет текста сообщения бота/оператора
- color_text_server_action - устанавливает цвет текста подсказки бота
- color_time_mark - устанавливает цвет текста под сообщением (дата + автор + статус сообщения)
- color_text_warning - устанавливает цвет текста, сообщающего о состоянии соединения
- color_company - устанавливает цвет текста названия компании (имеет смысл, есть атрибут show_company_name выставлен в true)

Ресурсы:
- progressIndeterminateDrawable - устанавливает цвет всех тробберов в чате
        
Размеры:
- size_user_message - устанавливает размер текста пользовательского сообщения
- size_server_message - устанавливает размер текста сообщения бота/оператора
- size_server_action - устанавливает размер текста подсказки бота
- size_time_mark - устанавливает размер текста под сообщением (дата + автор + статус сообщения)
- size_warning - устанавливает размер текста, сообщающего о состоянии соединения
- size_info - устанавливает размер текста информационного сообщения (название компании/сообщение, сообщающие о том, что оператор набирает сообщение)
        
Элементы UI:
- company_name - указывает название компании (имеет смысл, есть атрибут show_company_name выставлен в true)
- show_company_name - указывает о необходимости отобразить название компании

## Listeners

Имеется набор listeners, позволяющих более гибко работать с ChatView.

#### ChatMessageListener

Этот listener устанавливается через объект Chat с помощью метода setOnChatMessageListener. При уходе с экрана чата необходимо оповещать пользователя о том, что появилось новое сообщение в чате. Соответственно для этих нужд можно использовать ChatMessageListener. Необходимо переопределить метод getNewMessages, он возвращает количество новых сообщений при условии, что пользователь ушел с экрана чата, но при этом все еще находится в приложении.

```
Chat.setOnChatMessageListener(object : ChatMessageListener {
    override fun getNewMessages(countMessages: Int) {
    	Log.d("TEST", "Сount new messages = ${countMessages};")
    }
})
```

#### ChatPermissionListener

Этот listener устанавливается через ChatView с помощью метода setOnPermissionListener. При работе с чатом пользователь может отправить файлы. Для доступа к хранилищу или камере необходимо запросить соответствующее разрешение, если ранее такой разрешение не было получено, то необходимо его запросить в чате. В самом чате реализован запрос необходимого разрешения, однако для оповещения пользователя используется Snackbar. Если Snackbar не удовлетворяет дизайну, можно установить свое решение с помощью ChatPermissionListener.

```
chatView.setOnPermissionListener(object : ChatPermissionListener {
    override fun requestedPermissions(permissions: Array<Permission>, messages: Array<String>) {
    	permissions.forEachIndexed { index, permission ->
	    showWarning(messages[index])
	}
    }
})
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
