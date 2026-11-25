-- Room createAllTables from SDK commit c34b733, schema version 10.
CREATE TABLE IF NOT EXISTS `messages` (
    `uuid` TEXT NOT NULL,
    `id` TEXT NOT NULL,
    `is_reply` INTEGER NOT NULL,
    `message_type` INTEGER NOT NULL,
    `parent_msg_id` TEXT,
    `timestamp` INTEGER NOT NULL,
    `arrival_time` INTEGER,
    `message` TEXT,
    `span_structure_list` TEXT NOT NULL,
    `widget` TEXT,
    `actions` TEXT,
    `keyboard` TEXT,
    `attachment_url` TEXT,
    `attachment_type` TEXT,
    `attachment_download_progress_type` TEXT,
    `attachment_name` TEXT,
    `attachment_size` INTEGER,
    `operator_id` TEXT,
    `operator_preview` TEXT,
    `operator_name` TEXT,
    `height` INTEGER,
    `width` INTEGER,
    `replied_message_id` TEXT,
    `replied_message_text` TEXT,
    `replied_message_span_structure_list` TEXT NOT NULL,
    `replied_message_attachment_url` TEXT,
    `replied_message_attachment_type` TEXT,
    `replied_message_attachment_name` TEXT,
    `replied_message_attachment_size` INTEGER,
    `replied_message_attachment_download_progress_type` TEXT,
    `replied_message_attachment_height` INTEGER,
    `replied_message_attachment_width` INTEGER,
    `dialog_id` TEXT,
    PRIMARY KEY(`id`));

CREATE TABLE IF NOT EXISTS `persons` (
    `person_id` TEXT NOT NULL,
    `person_preview` TEXT NOT NULL,
    PRIMARY KEY(`person_id`));

CREATE TABLE IF NOT EXISTS `files` (
    `uuid` TEXT NOT NULL,
    `file_name` TEXT NOT NULL,
    `idKey` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);

CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT);

INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '52592d3131bfe420923de1ee13cc5007');
