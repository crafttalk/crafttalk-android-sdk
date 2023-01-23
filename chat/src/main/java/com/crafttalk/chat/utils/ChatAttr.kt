package com.crafttalk.chat.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.helper.extensions.getDimensionOrNull
import com.crafttalk.chat.presentation.helper.extensions.getFloatOrNull
import com.crafttalk.chat.presentation.helper.extensions.getResourceIdOrNull
import com.crafttalk.chat.presentation.helper.ui.getSizeScreenInPx

class ChatAttr
private constructor(
    attrArr: TypedArray,
    context: Context
) {

    val widthScreenInPx = getSizeScreenInPx(context).first.toFloat()
    val heightScreenInPx = getSizeScreenInPx(context).second.toFloat()

    val timeDelayed = attrArr.getInt(R.styleable.ChatView_timeDelayed, 0).toLong()
    val delayDownloadDocument = attrArr.getInt(R.styleable.ChatView_delay_download_document, 1000).toLong()
    val replyEnable = attrArr.getBoolean(R.styleable.ChatView_reply_enable, false)

    val colorMain = attrArr.getColor(R.styleable.ChatView_color_main, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_main))
    val colorBlack = ContextCompat.getColor(context, R.color.com_crafttalk_chat_black)
    val colorTextInternetConnectionWarning = attrArr.getColor(R.styleable.ChatView_color_text_warning, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_warning))
    val colorTextInfo = attrArr.getColor(R.styleable.ChatView_color_company, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorTextUserMessage = attrArr.getColor(R.styleable.ChatView_color_text_user_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_user_message))
    val colorBarrierUserRepliedMessage = attrArr.getColor(R.styleable.ChatView_color_barrier_user_replied_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_barrier_replied_message))
    val colorBarrierOperatorRepliedMessage = attrArr.getColor(R.styleable.ChatView_color_barrier_operator_replied_message, colorMain)
    val colorTextUserRepliedMessage = attrArr.getColor(R.styleable.ChatView_color_text_user_replied_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_user_replied_message))
    val colorTextOperatorRepliedMessage = attrArr.getColor(R.styleable.ChatView_color_text_operator_replied_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_user_replied_message))
    val colorTextOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_text_operator_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_server_message))
    val colorTextOperatorAction = attrArr.getColor(R.styleable.ChatView_color_text_operator_action, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_server_action))
    val colorTextOperatorSelectedAction = attrArr.getColor(R.styleable.ChatView_color_text_operator_selected_action, ContextCompat.getColor(context, R.color.com_crafttalk_chat_white))
    val colorTextOperatorButton = attrArr.getColor(R.styleable.ChatView_color_text_operator_button, colorMain)
    val colorPrimaryTextOperatorButton = attrArr.getColor(R.styleable.ChatView_color_primary_text_operator_button, colorTextOperatorButton)
    val colorSecondaryTextOperatorButton = attrArr.getColor(R.styleable.ChatView_color_secondary_text_operator_button, colorTextOperatorButton)
    val colorNegativeTextOperatorButton = attrArr.getColor(R.styleable.ChatView_color_negative_text_operator_button, colorTextOperatorButton)
    val colorTextOperatorSelectedButton = attrArr.getColor(R.styleable.ChatView_color_text_operator_selected_button, ContextCompat.getColor(context, R.color.com_crafttalk_chat_white))
    val colorPrimaryTextOperatorSelectedButton = attrArr.getColor(R.styleable.ChatView_color_primary_text_operator_selected_button, colorTextOperatorSelectedButton)
    val colorSecondaryTextOperatorSelectedButton = attrArr.getColor(R.styleable.ChatView_color_secondary_text_operator_selected_button, colorTextOperatorSelectedButton)
    val colorNegativeTextOperatorSelectedButton = attrArr.getColor(R.styleable.ChatView_color_negative_text_operator_selected_button, colorTextOperatorSelectedButton)
    private val colorFileName = attrArr.getColor(R.styleable.ChatView_color_file_name, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorUserFileName = attrArr.getColor(R.styleable.ChatView_color_user_file_name, colorFileName)
    val colorUserRepliedFileName = attrArr.getColor(R.styleable.ChatView_color_user_replied_file_name, colorFileName)
    val colorOperatorRepliedFileName = attrArr.getColor(R.styleable.ChatView_color_operator_replied_file_name, colorFileName)
    val colorOperatorFileName = attrArr.getColor(R.styleable.ChatView_color_operator_file_name, colorFileName)
    private val colorFileSize = attrArr.getColor(R.styleable.ChatView_color_file_size, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorUserFileSize = attrArr.getColor(R.styleable.ChatView_color_user_file_size, colorFileSize)
    val colorUserRepliedFileSize = attrArr.getColor(R.styleable.ChatView_color_user_replied_file_size, colorFileSize)
    val colorOperatorRepliedFileSize = attrArr.getColor(R.styleable.ChatView_color_operator_replied_file_size, colorFileSize)
    val colorOperatorFileSize = attrArr.getColor(R.styleable.ChatView_color_operator_file_size, colorFileSize)
    val colorTextUserMessageAuthor = attrArr.getColor(R.styleable.ChatView_color_text_user_message_author, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorTextOperatorMessageAuthor = attrArr.getColor(R.styleable.ChatView_color_text_operator_message_author, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    private val colorUserMessageTime = attrArr.getColor(R.styleable.ChatView_color_user_message_time, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorUserFileMessageTime = attrArr.getColor(R.styleable.ChatView_color_user_file_message_time, colorUserMessageTime)
    val colorUserGifMessageTime = attrArr.getColor(R.styleable.ChatView_color_user_gif_message_time, colorUserMessageTime)
    val colorUserImageMessageTime = attrArr.getColor(R.styleable.ChatView_color_user_image_message_time, colorUserMessageTime)
    val colorUserTextMessageTime = attrArr.getColor(R.styleable.ChatView_color_user_text_message_time, colorUserMessageTime)
    private val colorOperatorMessageTime = attrArr.getColor(R.styleable.ChatView_color_operator_message_time, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorOperatorFileMessageTime = attrArr.getColor(R.styleable.ChatView_color_operator_file_message_time, colorOperatorMessageTime)
    val colorOperatorGifMessageTime = attrArr.getColor(R.styleable.ChatView_color_operator_gif_message_time, colorOperatorMessageTime)
    val colorOperatorImageMessageTime = attrArr.getColor(R.styleable.ChatView_color_operator_image_message_time, colorOperatorMessageTime)
    val colorOperatorTextMessageTime = attrArr.getColor(R.styleable.ChatView_color_operator_text_message_time, colorOperatorMessageTime)
    private val colorUserMessageStatus = attrArr.getColor(R.styleable.ChatView_color_user_message_status, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorUserFileMessageStatus = attrArr.getColor(R.styleable.ChatView_color_user_file_message_status, colorUserMessageStatus)
    val colorUserGifMessageStatus = attrArr.getColor(R.styleable.ChatView_color_user_gif_message_status, colorUserMessageStatus)
    val colorUserImageMessageStatus = attrArr.getColor(R.styleable.ChatView_color_user_image_message_status, colorUserMessageStatus)
    val colorUserTextMessageStatus = attrArr.getColor(R.styleable.ChatView_color_user_text_message_status, colorUserMessageStatus)
    val colorTextDateGrouping = attrArr.getColor(R.styleable.ChatView_color_text_date_grouping, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_info))
    val colorTextLinkOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_text_link_operator_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_link_server_message))
    val colorTextLinkUserMessage = attrArr.getColor(R.styleable.ChatView_color_text_link_user_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_link_user_message))
    val colorTextPhoneOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_text_phone_operator_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_phone_server_message))
    val colorTextPhoneUserMessage = attrArr.getColor(R.styleable.ChatView_color_text_phone_user_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_text_phone_user_message))

    val colorBackgroundUserMessage = attrArr.getColor(R.styleable.ChatView_color_bg_user_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_bg_user_message))
    val colorBackgroundUserMediaFileMessage = attrArr.getColor(R.styleable.ChatView_color_bg_user_media_file_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_bg_user_message))
    val colorBackgroundOperatorMessage = attrArr.getColor(R.styleable.ChatView_color_bg_operator_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_bg_server_message))
    val colorBackgroundOperatorMediaFileMessage = attrArr.getColor(R.styleable.ChatView_color_bg_operator_media_file_message, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_bg_server_message))
    val colorBackgroundOperatorAction = attrArr.getColor(R.styleable.ChatView_color_bg_operator_action, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_bg_server_action))
    val colorBackgroundOperatorSelectedAction = attrArr.getColor(R.styleable.ChatView_color_bg_operator_selected_action, colorMain)
    val backgroundResOperatorButton = attrArr.getInt(R.styleable.ChatView_resource_bg_operator_button, R.drawable.com_crafttalk_chat_background_item_button)
    val backgroundPrimaryResOperatorButton = attrArr.getInt(R.styleable.ChatView_resource_primary_bg_operator_button, backgroundResOperatorButton)
    val backgroundSecondaryResOperatorButton = attrArr.getInt(R.styleable.ChatView_resource_secondary_bg_operator_button, backgroundResOperatorButton)
    val backgroundNegativeResOperatorButton = attrArr.getInt(R.styleable.ChatView_resource_negative_bg_operator_button, backgroundResOperatorButton)
    val backgroundResOperatorSelectedButton = attrArr.getInt(R.styleable.ChatView_resource_bg_operator_selected_button, R.drawable.com_crafttalk_chat_background_item_selected_button)
    val backgroundPrimaryResOperatorSelectedButton = attrArr.getInt(R.styleable.ChatView_resource_primary_bg_operator_selected_button, backgroundResOperatorSelectedButton)
    val backgroundSecondaryResOperatorSelectedButton = attrArr.getInt(R.styleable.ChatView_resource_secondary_bg_operator_selected_button, backgroundResOperatorSelectedButton)
    val backgroundNegativeResOperatorSelectedButton = attrArr.getInt(R.styleable.ChatView_resource_negative_bg_operator_selected_button, backgroundResOperatorSelectedButton)
    val colorBordersOperatorAction = attrArr.getColor(R.styleable.ChatView_color_borders_operator_action, ContextCompat.getColor(context, R.color.com_crafttalk_chat_default_color_borders_server_action))

    val drawableProgressIndeterminate = attrArr.getDrawable(R.styleable.ChatView_drawable_progress_indeterminate)
    val drawableAttachFile = attrArr.getDrawable(R.styleable.ChatView_drawable_attach_file) ?: ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_attach_file)
    val drawableSendMessage = attrArr.getDrawable(R.styleable.ChatView_drawable_send_message) ?: ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_send)
    val drawableDocumentNotDownloadedIcon = attrArr.getDrawable(R.styleable.ChatView_drawable_document_not_downloaded_icon)
    val drawableDocumentDownloadingIcon = attrArr.getDrawable(R.styleable.ChatView_drawable_document_downloading_icon)
    val drawableDocumentDownloadedIcon = attrArr.getDrawable(R.styleable.ChatView_drawable_document_downloaded_icon)
    val drawableReplyMessageIcon = attrArr.getDrawable(R.styleable.ChatView_drawable_reply_message) ?: ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_reply_message)

    val sizeTextInternetConnectionWarning = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_warning, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_warning)).toFloat()
    val sizeTextInfoText = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_info, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_info)).toFloat()
    val sizeTextUserMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_user_message, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_user_message)).toFloat()
    val sizeTextUserRepliedMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_user_replied_message, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_user_replied_message)).toFloat()
    val sizeTextOperatorRepliedMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_replied_message, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_user_replied_message)).toFloat()
    val sizeTextOperatorMessage = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_message, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_server_message)).toFloat()
    val sizeTextOperatorAction = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_action, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_server_action)).toFloat()
    val sizeTextOperatorButton = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_button, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_server_button)).toFloat()
    private val sizeFileName = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_file_name, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_info)).toFloat()
    val sizeUserFileName = attrArr.getDimension(R.styleable.ChatView_size_user_file_name, sizeFileName)
    val sizeUserRepliedFileName = attrArr.getDimension(R.styleable.ChatView_size_user_replied_file_name, sizeFileName)
    val sizeOperatorRepliedFileName = attrArr.getDimension(R.styleable.ChatView_size_operator_replied_file_name, sizeFileName)
    val sizeOperatorFileName = attrArr.getDimension(R.styleable.ChatView_size_operator_file_name, sizeFileName)
    private val sizeFileSize = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_file_size, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_info)).toFloat()
    val sizeUserFileSize = attrArr.getDimension(R.styleable.ChatView_size_user_file_size, sizeFileSize)
    val sizeUserRepliedFileSize = attrArr.getDimension(R.styleable.ChatView_size_user_replied_file_size, sizeFileSize)
    val sizeOperatorRepliedFileSize = attrArr.getDimension(R.styleable.ChatView_size_operator_replied_file_size, sizeFileSize)
    val sizeOperatorFileSize = attrArr.getDimension(R.styleable.ChatView_size_operator_file_size, sizeFileSize)

    val sizeUserMessageAuthor = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_user_message_author, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_author)).toFloat()
    val sizeOperatorMessageAuthor = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_message_author, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_author)).toFloat()
    val sizeOperatorMessageAuthorPreview = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_message_author_preview, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_author_preview))
    val sizeUserMessageTime = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_user_message_time, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_time)).toFloat()
    val sizeOperatorMessageTime = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_operator_message_time, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_time)).toFloat()
    val sizeTextDateGrouping = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_text_date_grouping, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_info)).toFloat()

    private val widthItemUserTextMessageInPercent by lazy { attrArr.getFloat(R.styleable.ChatView_width_item_user_text_message_in_percent, 0.6f) * widthScreenInPx }
    private val widthItemOperatorTextMessageInPercent by lazy { attrArr.getFloat(R.styleable.ChatView_width_item_operator_text_message_in_percent, 0.75f) * widthScreenInPx }
    private val widthItemUserFileIconMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_item_user_file_icon_message_in_percent)?.let { it * widthScreenInPx }
    private val widthItemOperatorFileIconMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_item_operator_file_icon_message_in_percent)?.let { it * widthScreenInPx }
    private val widthItemUserFilePreviewWarningMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_item_user_file_preview_warning_message_in_percent)?.let { it * widthScreenInPx } ?: (widthScreenInPx / 2)
    private val widthItemOperatorFilePreviewWarningMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_item_operator_file_preview_warning_message_in_percent)?.let { it * widthScreenInPx } ?: (widthScreenInPx / 2)
    private val widthElongatedItemUserFilePreviewMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_elongated_item_user_file_preview_message_in_percent)?.let { it * widthScreenInPx } ?: (0.7f * widthScreenInPx)
    private val widthElongatedItemOperatorFilePreviewMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_width_elongated_item_operator_file_preview_message_in_percent)?.let { it * widthScreenInPx } ?: (0.7f * widthScreenInPx)
    private val heightElongatedItemUserFilePreviewMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_height_elongated_item_user_file_preview_message_in_percent)?.let { it * heightScreenInPx } ?: (0.4f * heightScreenInPx)
    private val heightElongatedItemOperatorFilePreviewMessageInPercent = attrArr.getFloatOrNull(R.styleable.ChatView_height_elongated_item_operator_file_preview_message_in_percent)?.let { it * heightScreenInPx } ?: (0.4f * heightScreenInPx)
    val widthItemUserTextMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_user_text_message) ?: widthItemUserTextMessageInPercent).toInt()
    val widthItemOperatorTextMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_operator_text_message) ?: widthItemOperatorTextMessageInPercent).toInt()
    val horizontalSpacingOperatorButton = attrArr.getDimension(R.styleable.ChatView_horizontal_spacing_operator_button, context.resources.getDimension(R.dimen.com_crafttalk_chat_horizontal_spacing_operator_button))
    val verticalSpacingOperatorButton = attrArr.getDimension(R.styleable.ChatView_vertical_spacing_operator_button, context.resources.getDimension(R.dimen.com_crafttalk_chat_vertical_spacing_operator_button))
    val widthItemUserFileIconMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_user_file_icon_message) ?: widthItemUserFileIconMessageInPercent)?.toInt()
    val widthItemOperatorFileIconMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_operator_file_icon_message) ?: widthItemOperatorFileIconMessageInPercent)?.toInt()
    val widthItemUserFilePreviewWarningMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_user_file_preview_warning_message) ?: widthItemUserFilePreviewWarningMessageInPercent).toInt()
    val widthItemOperatorFilePreviewWarningMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_item_operator_file_preview_warning_message) ?: widthItemOperatorFilePreviewWarningMessageInPercent).toInt()
    val widthElongatedItemUserFilePreviewMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_elongated_item_user_file_preview_message) ?: widthElongatedItemUserFilePreviewMessageInPercent).toInt()
    val widthElongatedItemOperatorFilePreviewMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_width_elongated_item_operator_file_preview_message) ?: widthElongatedItemOperatorFilePreviewMessageInPercent).toInt()
    val heightElongatedItemUserFilePreviewMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_height_elongated_item_user_file_preview_message) ?: heightElongatedItemUserFilePreviewMessageInPercent).toInt()
    val heightElongatedItemOperatorFilePreviewMessage = (attrArr.getDimensionOrNull(R.styleable.ChatView_height_elongated_item_operator_file_preview_message) ?: heightElongatedItemOperatorFilePreviewMessageInPercent).toInt()

    private val roundedMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_media_file_preview_message)
    private val roundedGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_gif_file_preview_message)
    private val defaultRoundedMediaFilePreview = context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_rounded_media_file_preview).toFloat()
    private val defaultRoundedGifFilePreview = context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_rounded_gif_file_preview).toFloat()
    val roundedTopLeftUserMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_left_user_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedTopRightUserMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_right_user_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedBottomLeftUserMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_left_user_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedBottomRightUserMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_right_user_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: 0f
    val roundedTopLeftUserGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_left_user_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview
    val roundedTopRightUserGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_right_user_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview
    val roundedBottomLeftUserGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_left_user_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview
    val roundedBottomRightUserGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_right_user_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: 0f
    val roundedTopLeftOperatorMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_left_operator_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedTopRightOperatorMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_right_operator_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedBottomLeftOperatorMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_left_operator_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: 0f
    val roundedBottomRightOperatorMediaFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_right_operator_media_file_preview_message) ?: roundedMediaFilePreviewMessage ?: defaultRoundedMediaFilePreview
    val roundedTopLeftOperatorGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_left_operator_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview
    val roundedTopRightOperatorGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_top_right_operator_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview
    val roundedBottomLeftOperatorGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_left_operator_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: 0f
    val roundedBottomRightOperatorGifFilePreviewMessage = attrArr.getDimensionOrNull(R.styleable.ChatView_rounded_bottom_right_operator_gif_file_preview_message) ?: roundedGifFilePreviewMessage ?: defaultRoundedGifFilePreview

    private val resFontFamilyAllText = attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_all_text)
    val resFontFamilyUserMessage = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_user_message)
    val resFontFamilyOperatorMessage = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_operator_message)
    val resFontFamilyOperatorAction = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_operator_action)
    val resFontFamilyOperatorButton = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_operator_button)
    val resFontFamilyFileInfo = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_file_info)
    val resFontFamilyMessageAuthor = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_message_author)
    val resFontFamilyMessageTime = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_message_time)
    val resFontFamilyMessageDate = resFontFamilyAllText ?: attrArr.getResourceIdOrNull(R.styleable.ChatView_resource_font_family_message_date)

    val marginStartMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_start_media_file, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_margin_start_media_file)).toInt()
    val marginEndMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_end_media_file, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_margin_end_media_file)).toInt()
    val marginTopMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_top_media_file, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_margin_top_media_file)).toInt()
    val marginBottomMediaFile = attrArr.getDimension(R.styleable.ChatView_margin_bottom_media_file, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_margin_bottom_media_file)).toInt()

    val companyName = attrArr.getString(R.styleable.ChatView_company_name) ?: context.getString(R.string.com_crafttalk_chat_name_company)
    val showCompanyName = attrArr.getBoolean(R.styleable.ChatView_show_company_name, false)
    val showInternetConnectionState = attrArr.getBoolean(R.styleable.ChatView_show_internet_connection_state, true)
    val showChatState = attrArr.getBoolean(R.styleable.ChatView_show_chat_state, true)
    val showUpperLimiter = attrArr.getBoolean(R.styleable.ChatView_show_upper_limiter, true)
    val showStartingProgress = attrArr.getBoolean(R.styleable.ChatView_show_starting_progress, true)

    val showUserMessageAuthor = attrArr.getBoolean(R.styleable.ChatView_show_user_message_author, true)
    val showUserMessageStatus = attrArr.getBoolean(R.styleable.ChatView_show_user_message_status, true)

    val bgUserMessageResId = attrArr.getResourceId(R.styleable.ChatView_resource_bg_user_message, R.drawable.com_crafttalk_chat_background_item_simple_user_message)
    val bgOperatorMessageResId = attrArr.getResourceId(R.styleable.ChatView_resource_bg_operator_message, R.drawable.com_crafttalk_chat_background_item_simple_server_message)

    val layoutItemUserTextMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_user_text_message)
    val layoutItemUserImageMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_user_image_message)
    val layoutItemUserGifMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_user_gif_message)
    val layoutItemUserFileMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_user_file_message)
    val layoutItemUserUnionMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_user_union_message)
    val layoutItemOperatorTextMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_operator_text_message)
    val layoutItemOperatorImageMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_operator_image_message)
    val layoutItemOperatorGifMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_operator_gif_message)
    val layoutItemOperatorFileMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_operator_file_message)
    val layoutItemOperatorUnionMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_operator_union_message)
    val layoutItemTransferMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_transfer_message)
    val layoutItemInfoMessage: Int? = attrArr.getResourceIdOrNull(R.styleable.ChatView_layout_item_info_message)

    //    download
    val mediaFileDownloadMode = attrArr.getInt(R.styleable.ChatView_media_file_download_mode, 0).let { MediaFileDownloadMode.values()[it] }
    val colorUserFileMessageDownload = attrArr.getResourceId(R.styleable.ChatView_color_user_file_message_download, R.color.com_crafttalk_chat_color_download_file)
    val colorOperatorFileMessageDownload = attrArr.getResourceId(R.styleable.ChatView_color_operator_file_message_download, R.color.com_crafttalk_chat_color_download_file)
    val sizeUserFileMessageDownload = attrArr.getDimension(R.styleable.ChatView_size_user_file_message_download, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_size_download))
    val sizeOperatorFileMessageDownload = attrArr.getDimension(R.styleable.ChatView_size_operator_file_message_download, context.resources.getDimension(R.dimen.com_crafttalk_chat_default_size_download))
    val backgroundUserFileMessageDownload = attrArr.getResourceId(R.styleable.ChatView_background_user_file_message_download, R.drawable.com_crafttalk_chat_background_download_file)
    val backgroundOperatorFileMessageDownload = attrArr.getResourceId(R.styleable.ChatView_background_operator_file_message_download, R.drawable.com_crafttalk_chat_background_download_file)

//    warnings
    val titleSuccessDownloadFileWarning = attrArr.getString(R.styleable.ChatView_title_success_download_file_warning) ?: context.getString(R.string.com_crafttalk_chat_download_file_success)
    val colorSuccessDownloadFileWarning = attrArr.getColor(R.styleable.ChatView_color_success_download_file_warning, ContextCompat.getColor(context, R.color.com_crafttalk_chat_white))
    val backgroundSuccessDownloadFileWarning = attrArr.getColor(R.styleable.ChatView_background_success_download_file_warning, ContextCompat.getColor(context, R.color.com_crafttalk_chat_success))
    val titleFailDownloadFileWarning = attrArr.getString(R.styleable.ChatView_title_fail_download_file_warning) ?: context.getString(R.string.com_crafttalk_chat_download_file_fail)
    val colorFailDownloadFileWarning = attrArr.getColor(R.styleable.ChatView_color_fail_download_file_warning, ContextCompat.getColor(context, R.color.com_crafttalk_chat_white))
    val backgroundFailDownloadFileWarning = attrArr.getColor(R.styleable.ChatView_background_fail_download_file_warning, ContextCompat.getColor(context, R.color.com_crafttalk_chat_error))

//    feedback
    val delayFeedbackScreenAppears = attrArr.getInt(R.styleable.ChatView_delay_feedback_screen_appears, 1000).toLong()
    val colorFeedbackTitle = attrArr.getColor(R.styleable.ChatView_color_feedback_title, ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray_707070))
    val sizeFeedbackTitle = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_feedback_title, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_feedback_title)).toFloat()
    val colorFeedbackStar = attrArr.getColor(R.styleable.ChatView_color_feedback_star, colorMain)

//    voice input
    val showVoiceInput = attrArr.getBoolean(R.styleable.ChatView_show_voice_input, false)
    val delayVoiceInputPostRecording = attrArr.getInt(R.styleable.ChatView_delay_voice_input_post_recording, 1000).toLong()
    val delayVoiceInputBetweenRecurringWarnings = attrArr.getInt(R.styleable.ChatView_delay_voice_input_between_recurring_warnings, 2000).toLong()
    val drawableVoiceInputMicOn = attrArr.getDrawable(R.styleable.ChatView_drawable_voice_input_mic_on) ?: ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_voice_mic_on)
    val drawableVoiceInputMicOff = attrArr.getDrawable(R.styleable.ChatView_drawable_voice_input_mic_off) ?: ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_ic_voice_mic_off)

//    search
    val enableSearch = attrArr.getBoolean(R.styleable.ChatView_enable_search, false)
    val enableAutoSearch = attrArr.getBoolean(R.styleable.ChatView_enable_auto_search, false)
    val colorSelectSearchText = attrArr.getColor(R.styleable.ChatView_color_select_search_text, ContextCompat.getColor(context, R.color.com_crafttalk_chat_yellow_ffeb3b))
    val colorCurrentSelectSearchText = attrArr.getColor(R.styleable.ChatView_color_current_select_search_text, ContextCompat.getColor(context, R.color.com_crafttalk_chat_orange_ffb300))
    val colorTextSearchCoincidence = attrArr.getColor(R.styleable.ChatView_color_search_coincidence_text, colorBlack)
    val backgroundSearchSwitch = attrArr.getColor(R.styleable.ChatView_background_search_switch, ContextCompat.getColor(context, R.color.com_crafttalk_chat_gray))
    val colorSearchTop = attrArr.getColor(R.styleable.ChatView_color_search_top, colorBlack)
    val colorSearchBottom = attrArr.getColor(R.styleable.ChatView_color_search_bottom, colorBlack)
    val sizeTextSearchCoincidenceText = attrArr.getDimensionPixelSize(R.styleable.ChatView_size_text_search_coincidence, context.resources.getDimensionPixelSize(R.dimen.com_crafttalk_chat_default_size_search_coincidence)).toFloat()

    val drawableBackgroundSignInButton: Drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.com_crafttalk_chat_background_sign_in_auth_form)!!).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(this, colorMain)
        } else {
            this.mutate().setColorFilter(colorMain, PorterDuff.Mode.SRC_IN)
        }
    }

    companion object {
        private var INSTANCE: ChatAttr? = null

        fun getInstance(attrArr: TypedArray? = null, context: Context? = null): ChatAttr =
            if (attrArr == null) {
                INSTANCE!!
            } else {
                synchronized(this) {
                    ChatAttr(attrArr, context!!).also { INSTANCE = it }
                }
            }
    }

}