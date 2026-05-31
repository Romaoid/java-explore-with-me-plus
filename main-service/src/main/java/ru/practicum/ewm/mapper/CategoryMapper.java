package ru.practicum.ewm.mapper;

import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.dto.CategoryDto;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}
