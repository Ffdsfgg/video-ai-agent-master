package com.hip.aiteachingvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Message {
    private String role;
    private List<ContentItem> content;

    @Data
    @Builder
    public static class ContentItem {
        private String type;
        private String text;
        private ImageUrl image_url;
        private VideoUrl video_url;

        //因为文档要求的格式
        @Data
        @AllArgsConstructor
        public static class ImageUrl {
            private String url;
        }

        @Data
        @AllArgsConstructor
        public static class VideoUrl {
            private String url;
        }
    }


}