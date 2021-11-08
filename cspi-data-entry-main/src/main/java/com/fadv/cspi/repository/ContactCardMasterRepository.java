package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.ContactCardMaster;

public interface ContactCardMasterRepository extends JpaRepository<ContactCardMaster, Long> {

	List<ContactCardMaster> findByAkaName(String akaName);
}
