package id.my.agungdh.kepegawaian.department.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
        UUID uuid,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
