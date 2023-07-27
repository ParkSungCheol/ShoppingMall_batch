package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Request")
// XML정보를 매핑할 CLASS
public class Request {
    private Arguments arguments;
    private String processingTime;

    public Arguments getArguments() {
        return arguments;
    }

    @XmlElement(name = "Arguments")
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public String getProcessingTime() {
        return processingTime;
    }

    @XmlElement(name = "ProcessingTime")
    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }
}
