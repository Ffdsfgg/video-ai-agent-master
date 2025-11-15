package com.hip.aiteachingvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QwenMessage {
    private String role;
    private String content;
}