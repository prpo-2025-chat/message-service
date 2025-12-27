package com.prpo.chat.message.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaAttachment {
    private String id;
    private String uploaderId;
    private String filename;
    private String contentType;
    private long size;
    private Date uploadedAt;
    private String downloadUrl;
}
