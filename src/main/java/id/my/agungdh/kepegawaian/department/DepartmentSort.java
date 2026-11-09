package id.my.agungdh.kepegawaian.department;

import id.my.agungdh.kepegawaian.common.exception.BadRequestException;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import java.util.Map;

/**
 * Whitelisted sort options for the department list endpoint.
 */
public record DepartmentSort(String field, String property, Sort.Direction direction) {

    private static final Map<String, String> WHITELIST = Map.of(
            "id", "id",
            "code", "code",
            "name", "name",
            "created_at", "createdAt"
    );

    public static DepartmentSort parse(String sort) {
        if (sort == null || sort.isBlank()) {
            return new DepartmentSort("id", "id", Sort.Direction.DESC);
        }

        String[] parts = sort.split(",", -1);
        if (parts.length > 2) {
            throw new BadRequestException("Invalid sort format, expected <field>,<asc|desc>");
        }

        String field = parts[0].trim();
        String property = WHITELIST.get(field);
        if (property == null) {
            throw new BadRequestException("Invalid sort field: " + field);
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length == 2) {
            String rawDirection = parts[1].trim().toLowerCase(Locale.ROOT);
            direction = switch (rawDirection) {
                case "asc" -> Sort.Direction.ASC;
                case "desc" -> Sort.Direction.DESC;
                default -> throw new BadRequestException("Invalid sort direction: " + parts[1].trim());
            };
        }

        return new DepartmentSort(field, property, direction);
    }

    public Sort toSort() {
        Sort sort = Sort.by(direction, property);
        return "id".equals(property) ? sort : sort.and(Sort.by(direction, "id"));
    }

    public Object valueOf(Department department) {
        return switch (property) {
            case "code" -> department.getCode();
            case "name" -> department.getName();
            case "createdAt" -> department.getCreatedAt();
            default -> department.getId();
        };
    }
}
