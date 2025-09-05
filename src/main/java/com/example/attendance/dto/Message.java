package com.example.attendance.dto;

import java.time.LocalDateTime;

/**
 * メッセージのデータを表現するDTOクラス。
 * `messages` テーブルに対応します。
 */
public class Message {
    private int messageId;
    private String message;
    private String priority; // DBのpriorityに対応
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;

    public Message() {}

    public Message(int messageId, String message, String priority, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.messageId = messageId;
        this.message = message;
        this.priority = priority;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }
}
