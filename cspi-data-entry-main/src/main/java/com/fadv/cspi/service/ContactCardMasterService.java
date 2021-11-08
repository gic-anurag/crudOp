package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.ContactCardMaster;

@Service
public interface ContactCardMasterService {

	ContactCardMaster getContactCardByAkaName(String akaName);

}
