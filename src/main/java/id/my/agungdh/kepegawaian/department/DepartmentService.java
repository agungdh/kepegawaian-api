package id.my.agungdh.kepegawaian.department;

import id.my.agungdh.kepegawaian.common.exception.BadRequestException;
import id.my.agungdh.kepegawaian.common.exception.ConflictException;
import id.my.agungdh.kepegawaian.common.exception.NotFoundException;
import id.my.agungdh.kepegawaian.common.pagination.CursorPageResponse;
import id.my.agungdh.kepegawaian.department.dto.DepartmentCreateRequest;
import id.my.agungdh.kepegawaian.department.dto.DepartmentResponse;
import id.my.agungdh.kepegawaian.department.dto.DepartmentUpdateRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final int PAGE_SIZE = 20;

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<DepartmentResponse> list(
            String after,
            String sort,
            String code,
            String name
    ) {
        DepartmentSort sortSpec = DepartmentSort.parse(sort);
        Department cursor = resolveCursor(after);

        Specification<Department> specification = buildSpecification(code, name, sortSpec, cursor);

        List<Department> rows = departmentRepository.findBy(
                specification,
                query -> query.sortBy(sortSpec.toSort()).limit(PAGE_SIZE + 1).all()
        );

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<Department> pageRows = hasNext ? rows.subList(0, PAGE_SIZE) : rows;

        List<DepartmentResponse> items = pageRows.stream()
                .map(this::toResponse)
                .toList();
        UUID next = hasNext ? pageRows.get(pageRows.size() - 1).getUuid() : null;

        return new CursorPageResponse<>(items, next);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID uuid) {
        return toResponse(findActive(uuid));
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();

        if (departmentRepository.existsByCode(code)) {
            throw new ConflictException("Department code already exists");
        }

        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setDescription(normalizeDescription(request.description()));

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(UUID uuid, DepartmentUpdateRequest request) {
        Department department = findActive(uuid);

        String code = request.code().trim();
        String name = request.name().trim();

        if (departmentRepository.existsByCodeAndIdNot(code, department.getId())) {
            throw new ConflictException("Department code already exists");
        }

        department.setCode(code);
        department.setName(name);
        department.setDescription(normalizeDescription(request.description()));

        return toResponse(departmentRepository.saveAndFlush(department));
    }

    @Transactional
    public void delete(UUID uuid) {
        Department department = findActive(uuid);
        department.softDelete(null);
        departmentRepository.save(department);
    }

    private Department findActive(UUID uuid) {
        return departmentRepository.findFirstByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private Department resolveCursor(String after) {
        if (!StringUtils.hasText(after)) {
            return null;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(after.trim());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid cursor: " + after);
        }

        return departmentRepository.findFirstByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Invalid cursor: department not found"));
    }

    private Specification<Department> buildSpecification(
            String code,
            String name,
            DepartmentSort sort,
            Department cursor
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(code)) {
                predicates.add(cb.like(
                        cb.lower(root.get("code")),
                        "%" + code.trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + name.trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (cursor != null) {
                predicates.add(keysetPredicate(root, cb, sort, sort.valueOf(cursor), cursor.getId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate keysetPredicate(
            Root<Department> root,
            CriteriaBuilder cb,
            DepartmentSort sort,
            Object value,
            Long cursorId
    ) {
        Path path = root.get(sort.property());
        Comparable comparable = (Comparable) value;
        boolean ascending = sort.direction().isAscending();

        Predicate outside = ascending
                ? cb.greaterThan(path, comparable)
                : cb.lessThan(path, comparable);

        Predicate sameValue = cb.equal(path, comparable);
        Predicate afterId = ascending
                ? cb.greaterThan(root.get("id"), cursorId)
                : cb.lessThan(root.get("id"), cursorId);

        return cb.or(outside, cb.and(sameValue, afterId));
    }

    private String normalizeDescription(String description) {
        return StringUtils.hasText(description) ? description.trim() : null;
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getUuid(),
                department.getCode(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
