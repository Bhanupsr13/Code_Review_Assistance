package com.example.codereview.dto;

public class AnalyzeRequest {

    private String code;
    private String filename;

    public AnalyzeRequest() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }
}
