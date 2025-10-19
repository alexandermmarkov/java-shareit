package shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"email"})
public class UserDto {
    private Long id;

    private String name;

    @Email(message = "Некорректный формат электронной почты")
    private String email;
}
