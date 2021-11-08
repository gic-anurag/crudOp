package com.gic.fadv.spoc.mail;


import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Component
public class EmailFormatter {

    private final ResourceLoader resourceLoader;

    @Autowired
    public EmailFormatter(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    /**
     * Formats a given message with the parameters
     * @param message email content
     * @param params parameters to map
     * @return formatted string message
     */
    private  String formatMessage(String message, Map<String,String> params) {
        if(message == null){
            return "";
        }
        String formattedMessage = message;
        for(Map.Entry<String, String> pair : params.entrySet()){
            String key="##"+pair.getKey()+"##";
            formattedMessage = formattedMessage.replace(key,pair.getValue());
        }
        return formattedMessage;

    }

    /**
     * Reads the resource file and Formats a given message with the parameters
     * @param template path where resource file is available
     * @param params parameters to map
     * @return formatted email message
     */
     String formatResourceMessage(String template,Map<String,String> params) {
        try {
            String templateName=template+".html";
            Resource res =  resourceLoader.getResource("classpath:templates/"+templateName);

            InputStream resourceAsStream =  res.getInputStream();
            String message = CharStreams.toString(new InputStreamReader(resourceAsStream, Charsets.UTF_8));
            return formatMessage(message,params);
        }catch (Exception ex){
            return "";
        }

    }
}
