**Как начать работу с чат-ботом CraftTalk для Android:**

**Поддерживаемая минимальная версия**

minSdkVersion - 21 (Android 5.0)

**Шаг 1. Установление зависимости**

SDK вынесена в отдельный модуль. На данный момент его необходимо затягивать в проект "ручками".
	
**Шаг 2. Использование**
1. Добавьте чат в layout:

	```
	<com.crafttalk.chat.presentation.ChatView
  		android:id="@+id/chat_view"
  		android:layout_height="match_parent"
   		android:layout_width="match_parent"
   		app:color_title="@color/color_main"
   		app:title_text="Sample chat"
   		app:color_main="@color/color_main"
   		app:color_bg_user_message="@color/color_main"
   		app:color_text_server_action="@color/color_main"
   		app:auth_with_hash="true"
   		app:auth_with_form="true" />
	
2. Настройка во Fragment:
	
	Если используется атрибут auth_with_form=”true”, то в chat_view.onCreate не передается Visitor.

	```
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
	    super.onViewCreated(view, savedInstanceState)
	    chat_view.onCreate(
	        this,
	        object:
	            ChatView.EventListener {
	                override fun onErrorAuth() {}
	                override fun onAuth() {}
	            },
	        Visitor(
	            "test_uuid",
	            "Ivan",
	            "Ivanov",
	            "ivan@mail.ru",
	            "89119864972",
	            "contract_test",
	            "17.09.1982"
	        )
	    )
    }


	override fun onResume() {
	    super.onResume()
	    chat_view.onResume(this)
    } 


**Пример приложения**

Пример находится в пакете com.crafttalk.sampleChat


**Настройка внешнего вида чата**

Настройка внешнего вида осуществляется в ChatView в layout через атрибуты.

- `color_main` = задает общий цвет для чата
- `color_bg_user_message` - задает цвет заднего фона пользовательского сообщения
- `color_bg_server_message` - задает цвет заднего фона сообщения бота/оператора
- `color_text_user_message` - задает цвет пользовательского сообщения
- `color_text_server_message` - задает цвет сообщения бота/оператора
- `color_text_server_action` - задает цвет кнопок-события бота/оператора
- `color_text_warning` - задает цвет сообщения о дисконекте


**Настройка поведения чата**

Настройка поведения чата осуществляется в ChatView в layout через атрибуты.

- `auth_with_form` - Если установлено значение true, тогда используется форма аутентификации, если информации о пользователе нет в локальных данных приложения.
                   Если установлено значение false, тогда аутентификация происходит не явно для пользователя. Однако в таком случае необходимо передать объект Visitor в методе onCreate.
- `auth_with_hash` - Если установлено значение true, `auth_with_form = false` и hash указывается в объекте Visitor(который передается через метод onCreate), тогда при аутентификации пользователя используется hash-функция, в противном случае аутентификация происходит без hash.


**Локальное хранение данных**

Для хранения сообщений чата используется Room, данные хранятся локально на устройстве пользователя. Если аутентификация пользователя вынесена за пределы SDK(`auth_with_form`=false), то данные о пользователе не сохраняются нигде. В обратном случае(`auth_with_form`=true) - для хранения информации о пользователе используются SharedPreferences.


**Аутентификация**

Поддерживается аутентификация только одного пользователя.

Значение соли должно храниться на клиентском сервере, там же происходит генерация hash. Это необходимо для обеспечения безопасности. Sdk не хранит соль и не отвечает за создание hash для аутентификации пользователя. Если `auth_with_form` имеет значение true, то аутентификация пользователя происходит без хэша. В противном случае аутентификация по хэшу определяется этим параметром - `auth_with_hash`.

**Permission**

Для работы с файлами необходимо приложению-хосту установить следующие разрешения и запросить соответствующие:

- READ_EXTERNAL_STORAGE
- CAMERA