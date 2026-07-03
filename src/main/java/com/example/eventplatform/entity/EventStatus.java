package com.example.eventplatform.entity;

public enum EventStatus {
    RECEIVED,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRYING,
    DEAD_LETTER
}
