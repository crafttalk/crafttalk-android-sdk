                                            Как начать работу с чат-ботом CraftTalk для Android
    Шаг 1. Установление зависимости
	
    Шаг 2. Использование
1. Добавьте чат в layout:

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


    Пример приложения

    Пример находится в пакете com.crafttalk.sampleChat



