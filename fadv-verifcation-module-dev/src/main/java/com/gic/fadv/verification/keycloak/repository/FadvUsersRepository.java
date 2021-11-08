package com.gic.fadv.verification.keycloak.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.keycloak.interfaces.UserInterface;
import com.gic.fadv.verification.keycloak.model.FadvUsers;

@Repository
public interface FadvUsersRepository extends JpaRepository<FadvUsers, Long> {

	@Query(value = "SELECT UE.email AS emailId, UE.first_name AS firstName, UE.last_name AS lastName, "
			+ "FU.fadv_user_id AS userId, FU.employee_id AS employeeId, "
			+ "FU.mobile_number AS mobileNumber, FU.user_location AS userLocation, "
			+ "FU.user_role AS userRole, FU.user_type AS userType "
			+ "FROM keycloak.user_entity UE, verification.fadv_users FU "
			+ "WHERE UE.id = FU.keycloak_user_id AND UE.id = :userId Limit 1", nativeQuery = true)
	UserInterface getUserByUserId(String userId);
	
	@Query(value = "SELECT UE.email AS emailId, UE.first_name AS firstName, UE.last_name AS lastName, "
			+ "FU.fadv_user_id AS userId, FU.employee_id AS employeeId, "
			+ "FU.mobile_number AS mobileNumber, FU.user_location AS userLocation, "
			+ "FU.user_role AS userRole, FU.user_type AS userType "
			+ "FROM keycloak.user_entity UE, verification.fadv_users FU "
			+ "WHERE UE.id = FU.keycloak_user_id", nativeQuery = true)
	List<UserInterface> getUserList();
}
