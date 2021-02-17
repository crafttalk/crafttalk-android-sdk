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
В [примере](https://github.com/crafttalk/crafttalk-android-sdk/tree/master/app) продемонстрировано, как нужно использовать библиотеку. 

#### Шаг 1.

В AndroidManifest необходимо добавить следующие permissions:
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA"/>
```

При использовании пушей в AndroidManifest необходимо добавить ChatPushService и дефолтный channel id
Для загрузки файлов необходим FileProvider, для этого в AndroidManifest необходимо указать provider с authorities (он должен совпадать с атрибутом fileProviderAuthorities в ChatView).

```
<application
    ...>
    <meta-data
    	android:name="com.google.firebase.messaging.default_notification_channel_id"
	android:value="@string/default_notification_channel_id" />
    ...
    <service
        android:name="com.crafttalk.chat.data.push.ChatPushService"
	android:exported="false">
        <intent-filter>
	    <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
    <provider
    	android:name="androidx.core.content.FileProvider"
	android:authorities="com.crafttalk.chat.fileprovider"
	android:exported="false"
	android:grantUriPermissions="true">
	<meta-data
	    android:name="android.support.FILE_PROVIDER_PATHS"
	    android:resource="@xml/file_paths" />
    </provider>
</application> 
```

#### Шаг 2.

В xml файл, где будет располагаться чат, необходимо добавить ChatView c 5 обязательными атрибутами (auth, urlSocketHost, urlSocketNameSpace, urlUploadHost, urlUploadNameSpace, fileProviderAuthorities)

```
<com.crafttalk.chat.presentation.ChatView
        android:id="@+id/chat_view"
        android:layout_height="match_parent"
        android:layout_width="match_parent"       
        app:auth="AUTH_WITHOUT_FORM"
        app:urlSocketHost="@string/urlSocketHost"
        app:urlSocketNameSpace="@string/urlSocketNameSpace"
        app:urlUploadHost="@string/urlUploadHost"
        app:urlUploadNameSpace="@string/urlUploadNameSpace"
	app:fileProviderAuthorities="@string/chat_file_provider_authorities" />
```


#### Шаг 3.

Необходимо инициализировать библиотеку, сделать это нужно как можно раньше (до любого другого вызова из библиотеки).

```
Chat.init(
    this,
    AuthType.AUTH_WITHOUT_FORM,
    getString(R.string.urlSocketHost),
    getString(R.string.urlSocketNameSpace)
) 
```

#### Шаг 4.

##### Аутентификация пользователя

Аутентификация может осуществляться двумя способами: явно (AUTH_WITH_FORM) и неявно (AUTH_WITHOUT_FORM) для пользователя. Отличие заключается в том, что при AUTH_WITH_FORM пользователю необходимо самому ввести данные о себе через форму, а при AUTH_WITHOUT_FORM за формирование объекта Visitor, необходимого для аутентификации, отвечает тот, кто использует библиотеку.

##### Push

Если аутентификация была успешной, то производится подписка пользователя на push уведомления, если тот ранее не был подписан. В противном случае пользователя отписывают от получение push уведомлений.

##### Сокет

Создания и уничтожения сокета, одна из самых важных шагов при интеграции ChatView.

Создать сокет можно с помощью вызова метода wakeUp, при повторных вызовах сокет пересоздаваться не будет. При вызове метода wakeUp происходит аутентификация пользователя.

Уничтожить сокет можно с помощью вызова метода destroy. Пока сокет 'живет' (между вызовами wakeUp и destroy) уведомления приходить не будут, так как пользователь находится в приложении и в этом нет смысла. Когда сокет 'мертв' (между вызовами destroy и wakeUp) уведомления приходить будут, так как пользователь закрывает приложение.

```
// для AUTH_WITHOUT_FORM
override fun onResume() {
    super.onResume()
    Chat.wakeUp(getVisitor(this))
}

// для AUTH_WITH_FORM
override fun onResume() {
    super.onResume()
    Chat.wakeUp(null)
}

// как для AUTH_WITHOUT_FORM, так и для AUTH_WITH_FORM
override fun onStop() {
    super.onStop()
    Chat.destroy()
}
```

#### Шаг 5.

Необходимо оповестить ChatView о том, что приложение находится в определенном состоянии (onViewCreated, onResume).

```
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    chatView.onCreate(this, viewLifecycleOwner)      
}

override fun onResume() {
    super.onResume()
    chatView.onResume(viewLifecycleOwner)
}
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
- fileProviderAuthorities - значение authorities для FileProvider


Атрибуты, настраивающие внешний вид:

Цвета:
- color_main - устанавливает главный цвет чата
- color_bg_user_message - устанавливает цвет фона пользовательского сообщения
- color_bg_server_message - устанавливает цвет фона сообщения бота/оператора
- color_text_user_message - устанавливает цвет текста пользовательского сообщения
- color_text_server_message - устанавливает цвет текста сообщения бота/оператора
- color_text_server_action - устанавливает цвет текста подсказки бота
- color_time_mark - устанавливает цвет текста под сообщением (дата + автор + статус сообщения)
- color_text_warning - устанавливает цвет текста, сообщающего о состоянии соединения
- color_company - устанавливает цвет текста названия компании (имеет смысл, есть атрибут show_company_name выставлен в true)
- color_text_date_grouping - устанавливает цвет текста даты, группирующей сообщения
- 
Ресурсы:
- progressIndeterminateDrawable - устанавливает цвет всех тробберов в чате
        
Размеры:
- size_user_message - устанавливает размер текста пользовательского сообщения
- size_server_message - устанавливает размер текста сообщения бота/оператора
- size_server_action - устанавливает размер текста подсказки бота
- size_time_mark - устанавливает размер текста под сообщением (дата + автор + статус сообщения)
- size_warning - устанавливает размер текста, сообщающего о состоянии соединения
- size_info - устанавливает размер текста информационного сообщения (название компании/сообщение, сообщающие о том, что оператор набирает сообщение)
- size_text_date_grouping - устанавливает размер текста даты, группирующей сообщения
      
Отступы:
- margin_start_media_file - устанавливает margin слева от медиа файла
- margin_end_media_file - устанавливает margin справа от медиа файла
- margin_top_media_file - устанавливает margin сверху от медиа файла
- margin_bottom_media_file - устанавливает margin снизу от медиа файла

Элементы UI:
- company_name - указывает название компании (имеет смысл, есть атрибут show_company_name выставлен в true)
- show_company_name - указывает о необходимости отобразить название компании
- show_internet_connection_state - указывает о необходимости отобразить дефолтную панель с состоянием сети
- show_upper_limiter - указывает о необходимости отобразить верхний разграничитель

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

#### UploadFileListener

Этот listener устанавливается через ChatView с помощью метода setOnUploadFileListener. При загрузке файлов может пойти что-то не так, поэтому необходимо уведомить об этом пользователя. Если дефолтный Snackbar не удовлетворяет требованиям, тогда можно установить UploadFileListener и обрабатывать подобные ситуации самому.

```
chatView.setOnUploadFileListener(object : UploadFileListener {
    override fun successUpload() {}
    override fun failUpload(message: String, type: TypeFailUpload) {}
})
```

#### ChatInternetConnectionListener

Этот listener устанавливается через ChatView с помощью метода setOnInternetConnectionListener. Этот listener позволяет самостоятельно реализовать Toolbar. Для этого необходимо выставить show_internet_connection_state="false" и show_upper_limiter="false" и установить listener.

```
chatView.setOnInternetConnectionListener(object : ChatInternetConnectionListener {
    override fun connect() { status_connection.visibility = View.GONE }
    override fun failConnect() { status_connection.visibility = View.VISIBLE }
    override fun lossConnection() { status_connection.visibility = View.VISIBLE }
    override fun reconnect() { status_connection.visibility = View.GONE }
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
