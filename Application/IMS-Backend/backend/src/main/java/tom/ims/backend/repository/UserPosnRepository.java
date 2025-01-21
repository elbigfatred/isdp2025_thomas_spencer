package tom.ims.backend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.UserPosn;
import tom.ims.backend.model.UserPosnKey;

import java.util.List;

public interface UserPosnRepository extends JpaRepository<UserPosn, UserPosnKey> {


    // Custom query to find UserPosns by employee ID
    List<UserPosn> findByUserId(Integer userId);

    // Custom query to find UserPosns by position ID
    List<UserPosn> findByPosnId(Integer posnId);

    List<UserPosn> findByUser(Employee user);


    // Optionally add delete methods if needed
    void deleteByUserId(Integer userId);
    void deleteByPosnId(Integer posnId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPosn up WHERE up.user = :user")
    void deleteByUser(@Param("user") Employee user);
}