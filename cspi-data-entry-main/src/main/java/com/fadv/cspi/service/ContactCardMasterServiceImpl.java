package com.fadv.cspi.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.ContactCardMaster;
import com.fadv.cspi.repository.ContactCardMasterRepository;

@Service
public class ContactCardMasterServiceImpl implements ContactCardMasterService {

	@Autowired
	private ContactCardMasterRepository contactCardMasterRepository;

	@Override
	public ContactCardMaster getContactCardByAkaName(String akaName) {

		List<ContactCardMaster> contactCardMasters = contactCardMasterRepository.findByAkaName(akaName);
		if (CollectionUtils.isNotEmpty(contactCardMasters)) {
			return contactCardMasters.get(0);
		}
		return null;
	}
}
