package id.my.agungdh.kepegawaian.department;

import id.my.agungdh.kepegawaian.common.pagination.CursorPageResponse;
import id.my.agungdh.kepegawaian.department.dto.DepartmentCreateRequest;
import id.my.agungdh.kepegawaian.department.dto.DepartmentResponse;
import id.my.agungdh.kepegawaian.department.dto.DepartmentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public CursorPageResponse<DepartmentResponse> list(
            @RequestParam(required = false) String after,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name
    ) {
        return departmentService.list(after, sort, code, name);
    }

    @GetMapping("/{uuid}")
    public DepartmentResponse get(@PathVariable UUID uuid) {
        return departmentService.get(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentCreateRequest request) {
        return departmentService.create(request);
    }

    @PutMapping("/{uuid}")
    public DepartmentResponse update(
            @PathVariable UUID uuid,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return departmentService.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid) {
        departmentService.delete(uuid);
    }
}
