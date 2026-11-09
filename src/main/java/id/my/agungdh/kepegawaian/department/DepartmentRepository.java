package id.my.agungdh.kepegawaian.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findFirstByUuid(UUID uuid);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
