import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;

import static org.assertj.core.api.Assertions.assertThat;

public class QItemTest {
    @Test
    void shouldCreateAndUseQItem() {
        QItem qItem = QItem.item;

        assertThat(qItem).isNotNull();
        assertThat(qItem.getType()).isEqualTo(Item.class);

        assertThat(qItem.id).isNotNull();
        assertThat(qItem.name).isNotNull();
        assertThat(qItem.description).isNotNull();
        assertThat(qItem.available).isNotNull();
        assertThat(qItem.owner).isNotNull();
        assertThat(qItem.request).isNotNull();

        BooleanExpression expression = qItem.id.eq(1L).and(qItem.available.isTrue());
        assertThat(expression).isNotNull();
    }
}
