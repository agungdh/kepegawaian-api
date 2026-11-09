package id.my.agungdh.kepegawaian.common.pagination;

import java.util.List;
import java.util.UUID;

public record CursorPageResponse<T>(
        List<T> items,
        UUID next
) {
}
