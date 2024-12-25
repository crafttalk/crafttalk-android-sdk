package com.crafttalk.chat.presentation.feature.pined_message_viewer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.interactors.AuthInteractor
import com.crafttalk.chat.domain.interactors.ConditionInteractor
import com.crafttalk.chat.domain.interactors.ConfigurationInteractor
import com.crafttalk.chat.domain.interactors.FeedbackInteractor
import com.crafttalk.chat.domain.interactors.FileInteractor
import com.crafttalk.chat.domain.interactors.MessageInteractor
import com.crafttalk.chat.domain.interactors.SearchInteractor
import com.crafttalk.chat.presentation.base.BaseViewModel
import com.crafttalk.chat.presentation.feature.view_picture.ShowMediaDialog2
import com.crafttalk.chat.presentation.helper.groupers.groupPageByDate
import com.crafttalk.chat.presentation.helper.mappers.messageModelMapper
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class PinedMessageModel
@Inject constructor(
    private val authChatInteractor: AuthInteractor,
    private val messageInteractor: MessageInteractor,
    private val searchInteractor: SearchInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val feedbackInteractor: FeedbackInteractor,
    private val configurationInteractor: ConfigurationInteractor,
    private val context: Context
) : BaseViewModel(){
    val openDocument = MutableLiveData<Pair<File?, Boolean>?>()
    val replyMessagePosition: MutableLiveData<Int?> = MutableLiveData(null)
    var isAllHistoryLoaded = conditionInteractor.checkFlagAllHistoryLoaded()
    var initialLoadKey = conditionInteractor.getInitialLoadKey()
    val uploadMessagesForUser: MutableLiveData<LiveData<PagedList<MessageModel>>> = MutableLiveData()

    private fun uploadMessages() {
        val config = PagedList.Config.Builder()
            .setPageSize(ChatParams.pageSize)
            .build()
        val dataSource = messageInteractor.getAllMessages()
            .map { (messageModelMapper(it, context)) }
            .mapByPage { groupPageByDate(it) }
        val pagedListBuilder: LivePagedListBuilder<Int, MessageModel> = LivePagedListBuilder(
            dataSource,
            config
        ).setBoundaryCallback(object : PagedList.BoundaryCallback<MessageModel>() {
            override fun onItemAtEndLoaded(itemAtEnd: MessageModel) {
                super.onItemAtEndLoaded(itemAtEnd)
                if (!isAllHistoryLoaded) {
                    Log.e("msg","error1716")
                }
            }
        }).setInitialLoadKey(initialLoadKey)
        uploadMessagesForUser.postValue(pagedListBuilder.build())
    }

    fun downloadOrOpenDocument(
        id: String,
        documentName: String,
        documentUrl: String
    ) {
        launchIO {
            fileInteractor.downloadDocument(
                id = id,
                documentName = documentName,
                documentUrl = documentUrl,
                directory = context.filesDir,
                openDocument = { documentFile ->
                    delay(ChatAttr.getInstance().delayDownloadDocument)
                    openDocument.postValue(Pair(documentFile, true))
                },
                downloadedFail = {
                    openDocument.postValue(Pair(null, false))
                }
            )
        }
    }

    fun openImage(imageName: String, imageUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) {
        val intent = Intent(context, ShowMediaDialog2::class.java)
        intent.putExtra("url",imageUrl)
        intent.putExtra("imageName", imageName)
        intent.putExtra("typeFile", TypeFile.IMAGE.toString())
        startActivity(context,intent,null)
    }

    fun openGif(gifName: String, gifUrl: String, downloadFun: (fileName: String, fileUrl: String, fileType: TypeFile) -> Unit) {
        val intent = Intent(context, ShowMediaDialog2::class.java)
        intent.putExtra("url",gifUrl)
        intent.putExtra("imageName", gifName)
        intent.putExtra("typeFile", TypeFile.GIF.toString())
        startActivity(context,intent,null)

    }

    fun selectAction(messageId: String, actionId: String) {
        launchIO {
            messageInteractor.selectActionInMessage(messageId, actionId)
        }
    }

    fun selectButton(messageId: String, actionId: String, buttonId: String) {
        launchIO {
            messageInteractor.selectButtonInMessage(messageId, actionId, buttonId)
        }
    }

    fun selectReplyMessage(messageId: String) {
        launchIO {
            replyMessagePosition.postValue(messageInteractor.getCountMessagesInclusiveTimestampById(messageId))
        }
    }

    fun updateData(id: String, height: Int, width: Int) {
        launchIO {
            messageInteractor.updateSizeMessage(id, height, width)
        }
    }
}