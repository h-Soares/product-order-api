package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    private Category category;
    private CategoryInsertDTO categoryInsertDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldFindAllCategories() {
        when(categoryRepository.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(category)));

        Page<CategoryDTO> result = categoryService.findAll(PageRequest.of(0, 2));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(category.getName(), result.getContent().get(0).getName());
        verify(categoryRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldFindCategoryByUUID() {
        when(categoryRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(category));

        CategoryDTO responseCategory = categoryService.findByUUID(RANDOM_UUID.toString());

        assertNotNull(responseCategory);
        assertEquals(category.getName(), responseCategory.getName());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInFindCategoryByUUID() {
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> categoryService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Category not found", e.getMessage());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldInsertCategory() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(modelMapper.map(any(CategoryInsertDTO.class), eq(Category.class))).
                thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocationOnMock -> {
            category.setId(RANDOM_UUID);
            return category;
        });

        CategoryDTO responseCategory = categoryService.insert(categoryInsertDTO);

        assertNotNull(responseCategory);
        assertEquals(category.getId(), responseCategory.getId());
        assertEquals(category.getName(), responseCategory.getName());
        verify(categoryRepository, times(1)).existsByName(anyString());
        verify(modelMapper, times(1)).
                map(any(CategoryInsertDTO.class), eq(Category.class));
        verify(categoryRepository, times(1)).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenCategoryExistsInInsertCategory() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> categoryService.insert(categoryInsertDTO));
        assertEquals("Category already exists", e.getMessage());
        verify(categoryRepository, times(1)).existsByName(anyString());
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldDeleteCategoryByUUID() {
        category.setId(RANDOM_UUID);
        when(categoryRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(category));
        doNothing().when(categoryRepository).deleteByUUID(any(UUID.class));

        categoryService.deleteByUUID(RANDOM_UUID.toString());

        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verify(categoryRepository, times(1)).deleteByUUID(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInDeleteCategoryByUUID() {
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> categoryService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("Category not found", e.getMessage());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldUpdateCategoryByUUID() {
        categoryInsertDTO.setName("Other name");
        when(categoryRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(category));
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocationOnMock -> {
            category.setId(RANDOM_UUID);
            return category;
        });

        CategoryDTO responseCategory = categoryService.updateByUUID(RANDOM_UUID.toString(), categoryInsertDTO);

        assertNotNull(responseCategory);
        assertEquals(category.getId(), responseCategory.getId());
        assertEquals(category.getName(), responseCategory.getName());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verify(categoryRepository, times(1)).existsByName(anyString());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInUpdateCategoryByUUID() {
        categoryInsertDTO.setName("Other name");
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> categoryService.updateByUUID(RANDOM_UUID.toString(), categoryInsertDTO));
        assertEquals("Category not found", e.getMessage());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenNameIsDifferentAndCategoryExistsInUpdateCategoryByUUID() {
        categoryInsertDTO.setName("Other name");
        when(categoryRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(category));
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> categoryService.updateByUUID(RANDOM_UUID.toString(), categoryInsertDTO));
        assertEquals("Category name already exists", e.getMessage());
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verify(categoryRepository, times(1)).existsByName(anyString());
        verifyNoMoreInteractions(categoryRepository);
    }

    private void init() {
        category = new Category("Testing");
        categoryInsertDTO = new CategoryInsertDTO("Testing");
    }
}