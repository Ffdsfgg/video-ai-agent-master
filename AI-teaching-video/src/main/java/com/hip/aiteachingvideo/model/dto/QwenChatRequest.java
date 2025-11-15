package com.hip.aiteachingvideo.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QwenChatRequest {
    private String model;
    private boolean stream;
    private List<QwenMessage> messages;
    private StreamOptions stream_options;

    // Getters and Setters
    public static class StreamOptions {
        private boolean include_usage;

        // Getters and Setters
        public boolean isInclude_usage() { return include_usage; }
        public void setInclude_usage(boolean include_usage) { this.include_usage = include_usage; }
    }

}