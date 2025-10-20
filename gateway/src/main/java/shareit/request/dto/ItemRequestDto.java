package shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание вещи не может быть пустым")
    @Size(max = 2000, message = "Описание вещи не должно превышать 2000 символов")
    private String description;

    private UserDto requestor;

    private LocalDateTime created;
}
