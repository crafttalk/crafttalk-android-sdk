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
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA"/>
```

При использовании пушей в AndroidManifest необходимо добавить ChatPushService и дефолтный channel id.

Для загрузки файлов необходим FileProvider, для этого в AndroidManifest необходимо указать provider с authorities (он должен совпадать со значением атрибута fileProviderAuthorities в ChatView).

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
        android:id="@+id/chatView"
        android:layout_height="match_parent"
        android:layout_width="match_parent" />
```


#### Шаг 3.

Необходимо инициализировать библиотеку, сделать это нужно как можно раньше (до любого другого вызова из библиотеки). Также с помощью метода Chat.init можно задать поведение чат-бота.

```
Chat.init(
    this,   
    getString(R.string.urlChatScheme),
    getString(R.string.urlChatHost),
    getString(R.string.urlChatNameSpace),   
    fileProviderAuthorities = getString(R.string.chat_file_provider_authorities)
) 
```

- urlChatScheme - указывает, какая схема будет использоваться при подключении
- urlChatHost - указывает, какое доменное имя будет использоваться при подключении
- urlChatNameSpace - указывает, по какому nameSpace будет происходить подключение
- authType - может принимать AUTH_WITH_FORM или AUTH_WITHOUT_FORM. Это поле определяет, как будет проходить аутентификация для пользователя
- operatorPreviewMode - может принимать CACHE (по умолчанию) или ALWAYS_REQUEST. Это поле определяет режим сохранения иконки оператора.
- operatorNameMode - может принимать IMMUTABLE (по умолчанию) или ACTUAL. Это поле определяет режим обновления имени оператора у старых сообщений, отправленным этим оператором.
- clickableLinkMode - может принимать ALL (по умолчанию) или SECURE. Это поле определяет, какие из ссылок будут кликабельными, а какие нет.
- localeLanguage - это поле используется при формировании локали
- localeCountry - это поле используется при формировании локали
- phonePatterns - определяет шаблоны, по которым определяются телефонные номера
- fileProviderAuthorities - значение authorities для FileProvider
- certificatePinning - указывает сертификат, использующийся для включения SSL Pinning
- fileConnectTimeout - устанавливает connectTimeout для отправки файлов (измеряется в секундах)
- fileReadTimeout - устанавливает readTimeout для отправки файлов (измеряется в секундах)
- fileWriteTimeout - устанавливает writeTimeout для отправки файлов (измеряется в секундах)
- fileCallTimeout - устанавливает callTimeout для отправки файлов (измеряется в секундах)


#### Шаг 4.

##### Аутентификация пользователя

Аутентификация может осуществляться двумя способами: явно (AUTH_WITH_FORM) и неявно (AUTH_WITHOUT_FORM) для пользователя. Отличие заключается в том, что при AUTH_WITH_FORM пользователю необходимо самому ввести данные о себе через форму, а при AUTH_WITHOUT_FORM за формирование объекта Visitor, необходимого для аутентификации, отвечает тот, кто использует библиотеку.

##### Добавление новых полей объекту Visitor

Схему авторизации клиент может поменять, для этого необходимо расширять объект Visitor новыми полями. Для этих целей у объекта Visitor есть метод addNewFiled(fieldName: String, fieldValue: Any), который добавляет наименование поля и его значение, и removeAddedField(firstName: String), который удаляет ранее добавленное поле по его наименованию.

##### LogOut

Чтобы разлогинить пользователя необходимо воспользоваться методами Chat.logOut (или Chat.logOutWithUIActionAfter, или Chat.logOutWithIOActionAfter). Отличаются они тем, что в последних двух методах происходит вызов callback'а после "разлогинивания" пользователя. Необходимо помнить, что после вызова этих методов, нельзя переходить в чат без предварительного вызова Chat.wakeUp (с объектом Visitor), поскольку в чате уже не будет данных о пользователе.

##### SSL Pinning

Чтобы включить SSL Pinning необходимо в метод Chat.init передать сертификат, как атрибут certificatePinning. 

##### Push

Если аутентификация была успешной, то производится подписка пользователя на push уведомления, если тот ранее не был подписан. В противном случае пользователя отписывают от получение push уведомлений.

##### Сессия

В рамках сессии пользователь решает свои вопросы, в пределах одной сессии 'живет' сокет.

Создать сессию можно с помощью вызова метода Chat.createSession, при повторных вызовах этого метода сокет пересоздаваться не будет, если не был вызван метод Chat.destroySession. Уничтожение сессии осуществляется с помощью вызова метода Chat.destroySession.

Методы Chat.wakeUp и drop управляют активностью сокета (они определяют авторизован ли пользователь). При вызове метода Chat.wakeUp происходит 'активизация сокета' и дальнейшая авторизация пользователя, при повторном вызове авторизация повторно не происходит. При вызове метода Chat.drop происходит отключение сокета (пользователь находится в неавторизованном состоянии). Чтобы возобновить работу необходимо вызвать метод Chat.wakeUp, произойдет подключение с последующей авторизацией пользователя.

Пока сокет 'активен' (между вызовами Chat.wakeUp и Chat.drop) уведомления приходить не будут, так как пользователь находится в приложении. Когда сокет 'спит' (между вызовами Chat.drop и Chat.wakeUp) уведомления приходить будут, так как пользователь закрывает приложение.

Важно!!! Создание сессии должно происходить (вызов метода Chat.createSession) после инициализации чат-бота (вызов метода Chat.init).

Важно!!! В случае если методы Chat.wakeUp и Chat.drop вызываются в том же фрагменте, где располагается сам чат-бот, то вызов метода chatView.onResume должен идти после Chat.wakeUp.

Важно!!! Также существует метод Chat.wakeUp без аргументов. Этот метод стоит использовать, когда Chat.wakeUp вызывается на экране чата, в таком случае этот метод должен вызываться перед вызовом chatView.onResume.

```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ...
    Chat.createSession()
}

override fun onResume() {
    super.onResume()
    Chat.wakeUp(getVisitor(this))
    ...
}

override fun onStop() {
    super.onStop()
    Chat.drop()
}

override fun onDestroy() {
    super.onDestroy()
    Chat.destroySession()
}
```

#### Шаг 5.

Необходимо оповестить ChatView о том, что приложение находится в определенном состоянии (onViewCreated, onResume).

```
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    chatView.onViewCreated(this, viewLifecycleOwner)     
}

override fun onResume() {
    super.onResume()
    chatView.onResume()
}

override fun onStop() {
    super.onStop()
    chatView.onStop()
}

override fun onDestroyView() {
    super.onDestroyView()
    chatView.onDestroyView()
}
```

## Кастомизация

Настройка ChatView осуществляется в файле xml.
Для кастомизации используются атрибуты. Атрибуты делятся на две группы. Одни задают поведение чата, другие определяют визуальную составляющую.


Атрибуты, настраивающие поведение:
- timeDelayed - выставляет минимальное время отображения троббера
- delay_download_document - минимальное время, отображения иконки, установленной через атрибут drawable_document_downloading_icon


Атрибуты, настраивающие внешний вид:

Цвета:
- color_main - устанавливает главный цвет чата
- color_bg_user_message - устанавливает цвет фона пользовательского сообщения
- color_bg_user_media_file_message - устанавливает цвет фона пользовательского сообщения с фото или гифкой
- color_bg_operator_message - устанавливает цвет фона сообщения бота/оператора
- color_bg_operator_media_file_message - устанавливает цвет фона сообщения бота/оператора с фото или гифкой
- color_text_user_message - устанавливает цвет текста пользовательского сообщения
- color_text_operator_message - устанавливает цвет текста сообщения бота/оператора
- color_text_operator_action - устанавливает цвет текста подсказки бота
- color_bg_operator_selected_action - устанавливает цвет фона выбранной подсказки бота
- color_text_operator_selected_action - устанавливает цвет текста выбранной подсказки бота
- color_text_operator_button - устанавливает дефолтный цвет текста кнопки-подсказки бота
- color_primary_text_operator_button - устанавливает цвет текста для кнопки-подсказки бота со стилем primary
- color_secondary_text_operator_button - устанавливает цвет текста для кнопки-подсказки бота со стилем secondary
- color_negative_text_operator_button - устанавливает цвет текста для кнопки-подсказки бота со стилем negative
- color_text_operator_selected_button - устанавливает дефолтный цвет текста выбранной кнопки-подсказки бота
- color_primary_text_operator_selected_button - устанавливает цвет текста для выбранной кнопки-подсказки бота со стилем primary
- color_secondary_text_operator_selected_button - устанавливает цвет текста для выбранной кнопки-подсказки бота со стилем secondary
- color_negative_text_operator_selected_button - устанавливает цвет текста для выбранной кнопки-подсказки бота со стилем negative
- color_file_name - устанавливает цвет имени файла для всех сообщений
- color_user_file_name - устанавливает цвет имени файла для пользовательского сообщения
- color_operator_file_name - устанавливает цвет имени файла для сообщения бота/оператора
- color_file_size - устанавливает цвет размера файла для всех сообщений
- color_user_file_size - устанавливает цвет размера файла для пользовательского сообщения
- color_operator_file_size - устанавливает цвет размера файла для сообщения бота/оператора
- color_text_user_message_author -  устанавливает цвет текста имени пользователя
- color_text_operator_message_author -  устанавливает цвет текста имени бота/оператора
- color_user_message_time - устанавливает цвет времени сообщения для всех типов от пользователя
- color_user_file_message_time - устанавливает цвет времени сообщения с файлом от пользователя, если атрибут не указан, то используется значение из color_user_message_time
- color_user_gif_message_time - устанавливает цвет времени сообщения с гифкой от пользователя, если атрибут не указан, то используется значение из color_user_message_time
- color_user_image_message_time - устанавливает цвет времени сообщения с фото от пользователя, если атрибут не указан, то используется значение из color_user_message_time
- color_user_text_message_time - устанавливает цвет времени сообщения с текстом и для смешанного сообщения (текст + фото/гифка/файл) от пользователя, если атрибут не указан, то используется значение из color_user_message_time
- color_operator_message_time - устанавливает цвет времени сообщения для всех типов от бота/оператора
- color_operator_file_message_time - устанавливает цвет времени сообщения с файлом от бота/оператора, если атрибут не указан, то используется значение из color_operator_message_time
- color_operator_gif_message_time - устанавливает цвет времени сообщения с гифкой от бота/оператора, если атрибут не указан, то используется значение из color_operator_message_time
- color_operator_image_message_time - устанавливает цвет времени сообщения с фото от бота/оператора, если атрибут не указан, то используется значение из color_operator_message_time
- color_operator_text_message_time - устанавливает цвет времени сообщения с текстом и для смешанного сообщения (текст + фото/гифка/файл) от бота/оператора, если атрибут не указан, то используется значение из color_operator_message_time
- color_operator_widget_message_time - устанавливает цвет времени сообщения для виджета от бота/оператора, если атрибут не указан, то используется значение из color_operator_message_time
- color_user_message_status - устанавливает цвет иконок статуса для всех типов сообщений
- color_user_file_message_status - устанавливает цвет иконок статуса для сообщения с файлом, если не указано, то используется значение из color_user_message_status
- color_user_gif_message_status - устанавливает цвет иконок статуса для сообщения с гифкой, если не указано, то используется значение из color_user_message_status
- color_user_image_message_status - устанавливает цвет иконок статуса для сообщения с фото, если не указано, то используется значение из color_user_message_status
- color_user_text_message_status - устанавливает цвет иконок статуса для сообщения с текстом и для смешанного сообщения (текст + фото/гифка/файл), если не указано, то используется значение из color_user_message_status
- color_text_link_operator_message - устанавливает цвет текста ссылки в сообщении от бота/оператора
- color_text_link_user_message - устанавливает цвет текста ссылки в пользовательском сообщении
- color_text_warning - устанавливает цвет текста, сообщающего о состоянии соединения
- color_company - устанавливает цвет текста названия компании (имеет смысл, есть атрибут show_company_name выставлен в true)
- color_text_date_grouping - устанавливает цвет текста даты, группирующей сообщения
- color_text_phone_operator_message - устанавливает цвет текста телефона в сообщении от бота/оператора
- color_text_phone_user_message - устанавливает цвет текста телефона в пользовательском сообщении

Ссылки на ресурсы:
- resource_bg_user_message - устанавливает background для сообщений пользователя
- resource_bg_operator_message - устанавливает background для сообщений бота/оператора
- resource_bg_operator_button - устанавливает дефолтный background для кнопки-подсказки бота
- resource_primary_bg_operator_button - устанавливает background для кнопки-подсказки бота со стилем primary
- resource_secondary_bg_operator_button - устанавливает background для кнопки-подсказки бота со стилем secondary
- resource_negative_bg_operator_button - устанавливает background для кнопки-подсказки бота со стилем negative
- resource_bg_operator_selected_button - устанавливает дефолтный background для выбранной кнопки-подсказки бота
- resource_primary_bg_operator_selected_button - устанавливает background для выбранной кнопки-подсказки бота со стилем primary
- resource_secondary_bg_operator_selected_button - устанавливает background для выбранной кнопки-подсказки бота со стилем secondary
- resource_negative_bg_operator_selected_button - устанавливает background для выбранной кнопки-подсказки бота со стилем negative

Drawable:
- drawable_progress_indeterminate - устанавливает цвет всех тробберов в чате
- drawable_attach_file - устанавливает иконку элемента, предназначенного для прикрепления файлов
- drawable_send_message - устанавливает иконку элемента, предназначенного для отправки сообщений
- drawable_document_not_downloaded_icon - устанавливает иконку для файла, который еще не был скачан, в сообщении, содержащем файл
- drawable_document_downloading_icon - устанавливает иконку для файла, который скачивается, в сообщении, содержащем файл
- drawable_document_downloaded_icon - устанавливает иконку для файла, который уже скачан, в сообщении, содержащем файл
        
Размеры текста:
- size_user_message - устанавливает размер текста пользовательского сообщения
- size_operator_message - устанавливает размер текста сообщения бота/оператора
- size_operator_action - устанавливает размер текста подсказки бота
- size_operator_button - устанавливает размер текста кнопки-подсказки бота
- size_file_name - устанавливает размер текста имени файла для всех сообщений
- size_user_file_name - устанавливает размер текста имени файла пользовательского сообщения
- size_operator_file_name - устанавливает размер текста имени файла сообщения бота/оператора
- size_file_size - устанавливает размер текста размера файла для всех сообщений
- size_user_file_size - устанавливает размер текста размера файла пользовательского сообщения
- size_operator_file_size - устанавливает размер текста размера файла сообщения бота/оператора
- size_user_message_author - устанавливает размер имени пользователя в сообщении
- size_operator_message_author - устанавливает размер имени бота/оператора в сообщении
- size_user_message_time - устанавливает размер текста времени в сообщении пользователя
- size_operator_message_time - устанавливает размер текста времени в сообщении бота/оператора
- size_warning - устанавливает размер текста, сообщающего о состоянии соединения
- size_info - устанавливает размер текста информационного сообщения (название компании/сообщение, сообщающие о том, что оператор набирает сообщение)
- size_text_date_grouping - устанавливает размер текста даты, группирующей сообщения

Размеры элементов:
- size_operator_message_author_preview - устанавливает размер иконки бота/оператора в сообщении
- width_item_user_text_message - устанавливает максимальную ширину бабла сообщения с текстом и смешанного сообщения (текст + фото/гифка/файл) от пользователя
- width_item_operator_text_message - устанавливает максимальную ширину бабла сообщения с текстом и смешанного сообщения (текст + фото/гифка/файл) от бота/оператора
- width_item_user_file_icon_message - устанавливает размер (ширину/высоту) иконки файла в сообщении от пользователя
- width_item_operator_file_icon_message - устанавливает размер (ширину/высоту) иконки файла в сообщении от бота/оператора
- width_item_user_file_preview_warning_message - устанавливает размер (ширину/высоту) в сообщении с фото/гифкой (если оно не грузится) от пользователя
- width_item_operator_file_preview_warning_message - устанавливает размер (ширину/высоту) в сообщении с фото/гифкой (если оно не грузится) от бота/оператора
- width_elongated_item_user_file_preview_message - устанавливает ширину фото/гифки (если ширина ресурса больше высоты) в сообщении от пользователя
- width_elongated_item_operator_file_preview_message - устанавливает ширину фото/гифки (если ширина ресурса больше высоты) в сообщении от бота/оператора
- height_elongated_item_user_file_preview_message - устанавливает высоту фото/гифки (если высота ресурса больше ширины) в сообщении от пользователя
- height_elongated_item_operator_file_preview_message - устанавливает высоту фото/гифки (если высота ресурса больше ширины) в сообщении от бота/оператора
- width_item_user_text_message_in_percent - устанавливает максимальную ширину бабла сообщения с текстом и смешанного сообщения (текст + фото/гифка/файл) от пользователя в процентном соотношении
- width_item_operator_text_message_in_percent - устанавливает максимальную ширину бабла сообщения с текстом и смешанного сообщения (текст + фото/гифка/файл) от бота/оператора в процентном соотношении
- width_item_user_file_icon_message_in_percent - устанавливает размер (ширину/высоту) иконки файла в сообщении от пользователя в процентном соотношении
- width_item_operator_file_icon_message_in_percent - устанавливает размер (ширину/высоту) иконки файла в сообщении от бота/оператора в процентном соотношении
- width_item_user_file_preview_warning_message_in_percent - устанавливает размер (ширину/высоту) в сообщении с фото/гифкой (если оно не грузится) от пользователя в процентном соотношении
- width_item_operator_file_preview_warning_message_in_percent - устанавливает размер (ширину/высоту) в сообщении с фото/гифкой (если оно не грузится) от бота/оператора в процентном соотношении
- width_elongated_item_user_file_preview_message_in_percent - устанавливает ширину фото/гифки (если ширина ресурса больше высоты) в сообщении от пользователя в процентном соотношении
- width_elongated_item_operator_file_preview_message_in_percent - устанавливает ширину фото/гифки (если ширина ресурса больше высоты) в сообщении от бота/оператора в процентном соотношении
- height_elongated_item_user_file_preview_message_in_percent - устанавливает высоту фото/гифки (если высота ресурса больше ширины) в сообщении от пользователя в процентном соотношении
- height_elongated_item_operator_file_preview_message_in_percent - устанавливает высоту фото/гифки (если высота ресурса больше ширины) в сообщении от бота/оператора в процентном соотношении
- horizontal_spacing_operator_button - устанавливает горизонтальное расстояние между соседними кнопками-подсказками бота 
- vertical_spacing_operator_button - устанавливает вертикальное расстояние между соседними кнопками-подсказками бота 

Скругления:
- rounded_media_file_preview_message - устанавливает значения скругления углов для медиафайла
- rounded_gif_file_preview_message - устанавливает значения скругления углов для gif'ок
- rounded_top_left_user_media_file_preview_message - устанавливает значение скругления верхнего левого угла для медиафайла от пользователя
- rounded_top_right_user_media_file_preview_message - устанавливает значение скругления верхнего правого угла для медиафайла от пользователя
- rounded_bottom_left_user_media_file_preview_message - устанавливает значение скругления нижнего левого угла для медиафайла от пользователя
- rounded_bottom_right_user_media_file_preview_message - устанавливает значение скругления нижнего правого угла для медиафайла от пользователя
- rounded_top_left_user_gif_file_preview_message - устанавливает значение скругления верхнего левого угла для gif'ок пользователя
- rounded_top_right_user_gif_file_preview_message - устанавливает значение скругления верхнего правого угла для gif'ок пользователя
- rounded_bottom_left_user_gif_file_preview_message - устанавливает значение скругления нижнего левого угла для gif'ок пользователя
- rounded_bottom_right_user_gif_file_preview_message - устанавливает значение скругления нижнего правого угла для gif'ок пользователя
- rounded_top_left_operator_media_file_preview_message - устанавливает значение скругления верхнего левого угла для медиафайла от бота/оператора
- rounded_top_right_operator_media_file_preview_message - устанавливает значение скругления верхнего правого угла для медиафайла от бота/оператора
- rounded_bottom_left_operator_media_file_preview_message - устанавливает значение скругления нижнего левого угла для медиафайла от бота/оператора
- rounded_bottom_right_operator_media_file_preview_message - устанавливает значение скругления нижнего правого угла для медиафайла от бота/оператора
- rounded_top_left_operator_gif_file_preview_message - устанавливает значение скругления верхнего левого угла для gif'ок бота/оператора
- rounded_top_right_operator_gif_file_preview_message - устанавливает значение скругления верхнего правого угла для gif'ок бота/оператора
- rounded_bottom_left_operator_gif_file_preview_message - устанавливает значение скругления нижнего левого угла для gif'ок бота/оператора
- rounded_bottom_right_operator_gif_file_preview_message - устанавливает значение скругления нижнего правого угла для gif'ок бота/оператора

Шрифты:
- resource_font_family_all_text - шрифт для всех сообщений, если указан этот атрибут, то остальные игнорируются
- resource_font_family_user_message
- resource_font_family_operator_message
- resource_font_family_operator_action
- resource_font_family_operator_button
- resource_font_family_file_info
- resource_font_family_message_author
- resource_font_family_message_time
- resource_font_family_message_date

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
- show_starting_progress - указывает о необходимости отобразить троббер при загрузки чата (значение по умолчанию true)
- show_user_message_author - указывает о необходимости отобразить имя автора пользователя
- show_user_message_status - указывает о необходимости отобразить статус пользовательского сообщения
- show_chat_state - указывает о необходимости отобразить дефолтную панель с состоянием синхронизации

Кнопка для скачивания файлов:
- media_file_download_mode - указывает о необходимости отобразить кнопку для скачивания файлов (дефолтное значение ONLY_IN_VIEWER). ONLY_IN_VIEWER - предоставляет возможность скачать фото только при просмотре этого фото, ONLY_IN_CHAT - предоставляет возможность скачать фото из чата, All_PLACES -  предоставляет возможность скачать фото из любого места.
- color_user_file_message_download - устанавливает цвет текста кнопки для пользовательского сообщения
- color_operator_file_message_download - устанавливает цвет текста кнопки для сообщения от бота/оператора
- size_user_file_message_download - устанавливает размер текста кнопки для пользовательского сообщения
- size_operator_file_message_download - устанавливает размер текста кнопки для сообщения от бота/оператора
- background_user_file_message_download - устанавливает фон кнопки для пользовательского сообщения
- background_operator_file_message_download - устанавливает фон кнопки для сообщения от бота/оператора

Цитирование:
- reply_enable - включает (true) или отключает (false) цитирование
- drawable_reply_message - устанавливает иконку "процесса цитирования", при свайпе влево
- color_barrier_user_replied_message - устанавливает цвет барьера для процитированного блока в сообщении пользователя
- color_barrier_operator_replied_message - устанавливает цвет барьера для процитированного блока в сообщении оператора
- color_text_user_replied_message - устанавливает цвет текста процитированного сообщения пользователем
- color_text_operator_replied_message - устанавливает цвет текста процитированного сообщения оператором
- color_user_replied_file_name - устанавливает цвет текста имени файла процитированного сообщения пользователем
- color_operator_replied_file_name - устанавливает цвет текста имени файла процитированного сообщения оператором
- color_user_replied_file_size - устанавливает цвет текста размера файла процитированного сообщения пользователем
- color_operator_replied_file_size - устанавливает цвет текста размера файла процитированного сообщения оператором
- size_user_replied_message - устанавливает размер текста процитированного сообщения пользователем
- size_operator_replied_message - устанавливает размер текста процитированного сообщения оператором
- size_user_replied_file_name - устанавливает размер текста имени файла процитированного сообщения пользователем
- size_operator_replied_file_name - устанавливает размер текста имени файла процитированного сообщения оператором
- size_user_replied_file_size - устанавливает размер текста размера файла процитированного сообщения пользователем
- size_operator_replied_file_size - устанавливает размер текста размера файла процитированного сообщения оператором

Голосовой ввод
- show_voice_input - включает (true) или отключает (false) голосовой ввод 
- delay_voice_input_post_recording - устанавливает задержку для дозаписи
- delay_voice_input_between_recurring_warnings - устанавливает задержку между двумя предупреждениями при использовании голосового ввода
- drawable_voice_input_mic_on - устанавливает иконку для включенного микрофона
- drawable_voice_input_mic_off - устанавливает иконку для выключенного микрофона

Оценка работы оператора:
- delay_feedback_screen_appears - выставляет время в мс (при выставлении оценки), по истечении которого сворачивается плашка оценки работы оператора (по умолчанию 1000 мс)
- color_feedback_title - устанавливает цвет заголовка на плашке оценки работы оператора
- size_feedback_title - устанавливает размер заголовка на плашке оценки работы оператора
- color_feedback_star - устанавливает цвет звезд

Поиск по чату:
- enable_search - включает поиск (дефолтное значение - false)
- enable_auto_search - включает автопоиск (дефолтное значение - false)
- color_select_search_text - устанавливает цвет выделения текста, подходящего под шаблон 
- color_current_select_search_text - устанавливает цвет выделения текущего текста
- color_search_coincidence_text - устанавливает цвет текста сообщения о совпадениях
- background_search_switch - устанавливает цвет фона блока навигации по совпадениям поиска
- color_search_top - устанавливает цвет иконки навигации по поиску вверх
- color_search_bottom - устанавливает цвет иконки навигации по поиску вниз
- size_text_search_coincidence - устанавливает размер текста сообщения о совпадениях

Предупреждение:
- title_success_download_file_warning - устанавливает сообщение которое отображается при успешном скачивании файла (дефолтное значение - "Файл успешно скачан")
- color_success_download_file_warning - устанавливает цвет текста информационного сообщения при успешном скачивании файла
- background_success_download_file_warning -  устанавливает цвет фона информационного сообщения при успешном скачивании файла
- title_fail_download_file_warning - устанавливает сообщение которое отображается при неудачном скачивании файла (дефолтное значение - "Файл не удалось скачать")
- color_fail_download_file_warning - устанавливает цвет текста информационного сообщения при неудачном скачивании файла
- background_fail_download_file_warning - устанавливает цвет фона информационного сообщения при неудачном скачивании файла

Собственные layouts:
- layout_item_user_text_message - задает layout для текстового сообщения пользователя
- layout_item_user_image_message - задает layout для сообщения с фото от пользователя
- layout_item_user_gif_message - задает layout для сообщения с гифкой от пользователя
- layout_item_user_file_message - задает layout для сообщения с файлом от пользователя
- layout_item_user_union_message - задает layout для смешанного(текст + фото/гифка/файл) сообщения от пользователя
- layout_item_operator_text_message - задает layout для текстового сообщения бота/оператора
- layout_item_operator_image_message - задает layout для сообщения с фото от бота/оператора
- layout_item_operator_gif_message - задает layout для сообщения с гифкой от бота/оператора
- layout_item_operator_file_message - задает layout для сообщения с файлом от бота/оператора
- layout_item_operator_union_message - задает layout для смешанного(текст + фото/гифка/файл) сообщения от бота/оператора
- layout_item_transfer_message - задает layout для сообщения от бота о переводе на оператора
- layout_item_info_message - задает layout для сообщения склейки от бота
- layout_item_widget_message - задает layout для виджетов

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
    override fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit) {
    	registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
	    	action()
	    } else {
	    	showWarning(messages[0])
	    }
	}.launch(permissions[0])
    }
})
```

#### UploadFileListener

Этот listener устанавливается через ChatView с помощью метода setOnUploadFileListener. При загрузке файлов может пойти что-то не так, поэтому необходимо уведомить об этом пользователя. Если дефолтный Snackbar не удовлетворяет требованиям, тогда можно установить UploadFileListener и обрабатывать подобные ситуации самостоятельно.

```
chatView.setOnUploadFileListener(object : UploadFileListener {
    override fun successUpload() { ... }
    override fun failUpload(message: String, type: TypeFailUpload) { ... }
})
```

#### DownloadFileListener

Этот listener устанавливается через ChatView с помощью метода setOnDownloadFileListener. При скачивании медиафайлов может пойти что-то не так, поэтому необходимо уведомить об этом пользователя. Если дефолтный Snackbar не удовлетворяет требованиям, тогда можно установить DownloadFileListener и обрабатывать подобные ситуации самостоятельно.

```
chatView.setOnDownloadFileListener(object : DownloadFileListener {
    override fun successDownload() { ... }
    override fun failDownload() { ... }
    override fun failDownload(title: String) { ... }
})
```

#### ChatInternetConnectionListener

Этот listener устанавливается через ChatView с помощью метода setOnInternetConnectionListener. Этот listener позволяет самостоятельно реализовать Toolbar. Для этого необходимо выставить show_internet_connection_state="false" и show_upper_limiter="false".

```
chatView.setOnInternetConnectionListener(object : ChatInternetConnectionListener {
    override fun connect() { status_connection.visibility = View.GONE }
    override fun failConnect() { status_connection.visibility = View.VISIBLE }
    override fun lossConnection() { status_connection.visibility = View.VISIBLE }
    override fun reconnect() { status_connection.visibility = View.GONE }
})
```

#### ChatStateListener

Этот listener устанавливается через ChatView с помощью метода setOnChatStateListener. Этот listener позволяет самостоятельно реализовать Toolbar. Для этого необходимо выставить show_chat_state="false" и show_upper_limiter="false". Рекомендуется установить show_chat_state = "false", если show_internet_connection_state выставлен как "false".

```
chatView.setOnChatStateListener(object : ChatStateListener {
    override fun startSynchronization() { ... }
    override fun endSynchronization() { ... }  
})
```

#### MergeHistoryListener

Этот listener устанавливается через ChatView с помощью метода setMergeHistoryListener. Этот listener позволяет самостоятельно обработать слияние истории двух пользователей. Метод ChatView.mergeHistory позволяет слить истории двух пользователей в одну.

```
chatView.setMergeHistoryListener(object : MergeHistoryListener {
    override fun showDialog() { ... }
    override fun startMerge() { ... }
    override fun endMerge() { ... }
})
```

#### StateStartingProgressListener

Этот listener устанавливается через ChatView с помощью метода setOnStateStartingProgressListener. Этот listener позволяет самостоятельно реализовать троббер, появляющийся при загрузки чата. Для этого необходимо выставить show_starting_progress="false".

```
chatView.setOnStateStartingProgressListener(object : StateStartingProgressListener {
    override fun start() { loader.visibility = View.VISIBLE }
    override fun stop() { loader.visibility = View.GONE }   
})
```

## Собственные layouts

Реализация собственных layouts позволяет задать свое расположение элементов. Необходимо соблюдать некоторые правила:
- тип элемента должен быть такой же, как и в реализации по умолчанию
- id должен быть такой же, как и в реализации по умолчанию

Пример можно найти [тут](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/app/src/main/res/layout/layout_item_user_text_message.xml)

Реализации по умолчанию:
- [текстовое сообщение пользователя](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_user_text_message.xml)
- [сообщение с фото от пользователя](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_user_image_message.xml)
- [сообщение с гифкой от пользователя](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_user_gif_message.xml)
- [сообщение с файлом от пользователя](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_user_file_message.xml)
- [смешанное(текст + фото/гифка/файл) сообщение от пользователя](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_user_union_message.xml)
- [текстовое сообщение бота/оператора](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_server_text_message.xml)
- [сообщение с фото от бота/оператора](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_server_image_message.xml)
- [сообщение с гифкой от бота/оператора](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_server_gif_message.xml)
- [сообщение с файлом от бота/оператора](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_server_file_message.xml)
- [смешанное(текст + фото/гифка/файл) сообщение от бота/оператора](https://github.com/crafttalk/crafttalk-android-sdk/blob/master/chat/src/main/res/layout/item_server_union_message.xml)

License
--------


    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
