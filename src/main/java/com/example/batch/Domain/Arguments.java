package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Arguments")
public class Arguments {
    private Argument key;
    private Argument apiCode;
    private Argument keyword;

    public Argument getKey() {
        return key;
    }

    @XmlElement(name = "Argument")
    public void setKey(Argument key) {
        this.key = key;
    }

    public Argument getApiCode() {
        return apiCode;
    }

    @XmlElement(name = "Argument")
    public void setApiCode(Argument apiCode) {
        this.apiCode = apiCode;
    }

    public Argument getKeyword() {
        return keyword;
    }

    @XmlElement(name = "Argument")
    public void setKeyword(Argument keyword) {
        this.keyword = keyword;
    }
}
