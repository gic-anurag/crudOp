package com.gic.fadv.spoc.pojo;

import java.util.List;

import lombok.Data;

@Data
public class FileUploadPOJO
{
    private List<String> verificationReplyDocument;

    private String directory;
}
