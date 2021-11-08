package com.gic.fadv.verification.event.service;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.event.model.VerificationEventStatus;

import com.gic.fadv.verification.pojo.VerificationEventStatusPOJO;
import com.gic.fadv.verification.repository.event.VerificationEventStatusRepository;

@Service
public class VerificationEventStatusImpl  implements  VerificationEventStatusService{
	
	@Autowired
	private VerificationEventStatusRepository  verificationEventStatusRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	public List<VerificationEventStatus> getAllEvents(){
		return verificationEventStatusRepository.findAll();
	}
	
	public VerificationEventStatus save( VerificationEventStatus verificationEventStatus) {
		return verificationEventStatusRepository.save(verificationEventStatus);
		 
	}

	public List<VerificationEventStatus> findByCheckId(String checkId){
		return verificationEventStatusRepository.findByCheckId( checkId);
	}
	
	public List<VerificationEventStatus> getVerificationEventByFilter(VerificationEventStatusPOJO verificationEventStatusPOJO){
		
		boolean isFilter = false;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<VerificationEventStatus> criteriaQuery = criteriaBuilder.createQuery(VerificationEventStatus.class);
		Root<VerificationEventStatus> itemRoot = criteriaQuery.from(VerificationEventStatus.class);

		List<Predicate> predicates = new ArrayList<>();
		List<VerificationEventStatus> verificationEventList = new ArrayList<VerificationEventStatus>();

		if (verificationEventStatusPOJO.getVerificationEventStatusId() != null) {
			predicates.add(criteriaBuilder.equal(itemRoot.get("verificationEventStatusId"), verificationEventStatusPOJO.getVerificationEventStatusId()));
			isFilter = true;
		}
		if (!StringUtils.isEmpty(verificationEventStatusPOJO.getEventName())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("eventName"), verificationEventStatusPOJO.getEventName()));
			isFilter = true;
		}
		

		if (!StringUtils.isEmpty(verificationEventStatusPOJO.getCheckId())) {
			predicates.add(
					criteriaBuilder.equal(itemRoot.get("checkId"), verificationEventStatusPOJO.getCheckId()));
			isFilter = true;
		}
		

		/*
		 * //Logic for Date. If Date Comes take that otherwise today
		 * if(verificationEventStatusPOJO.getCreatedDateTime()!=null) {
		 * predicates.add(criteriaBuilder.equal(itemRoot.get("createdDateTime"),
		 * verificationEventStatusPOJO.getCreatedDateTime())); isFilter = true; }else {
		 * DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); Date today = new
		 * Date(); Date todayWithZeroTime=null; try { todayWithZeroTime =
		 * formatter.parse(formatter.format(today)); } catch (ParseException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 * System.out.println("Value of Date and Time"+todayWithZeroTime);
		 * predicates.add(criteriaBuilder.greaterThanOrEqualTo(itemRoot.get(
		 * "createdDateTime"),todayWithZeroTime)); isFilter = true; }
		 */
		
//		if (!StringUtils.isEmpty(verificationEventStatusPOJO.getcheckId())) {
//			predicates.add(
//					criteriaBuilder.equal(itemRoot.get("checkId"), verificationEventStatusPOJO.getcheckId()));
//			isFilter = true;
//		}
//		

		if(isFilter) {
            
            criteriaQuery.where(predicates.toArray(new Predicate[0]));
            verificationEventList = entityManager.createQuery(criteriaQuery).getResultList();
            }

		return verificationEventList;
	}
}
