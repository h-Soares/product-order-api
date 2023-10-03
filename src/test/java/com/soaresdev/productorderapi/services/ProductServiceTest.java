package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductCategoryInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductInsertDTO;
import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import com.soaresdev.productorderapi.repositories.ProductRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    private Product product;
    private ProductInsertDTO productInsertDTO;
    private Category category;
    private ProductCategoryInsertDTO productCategoryInsertDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldFindAllProducts() {
        when(productRepository.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductDTO> result = productService.findAll(PageRequest.of(0, 2));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(product.getName(), result.getContent().get(0).getName());
        assertEquals(product.getDescription(), result.getContent().get(0).getDescription());
        assertEquals(product.getPrice(), result.getContent().get(0).getPrice());
        assertEquals(product.getImgUrl(), result.getContent().get(0).getImgUrl());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldFindProductByUUID() {
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));

        ProductDTO responseProduct = productService.findByUUID(RANDOM_UUID.toString());

        assertNotNull(responseProduct);
        assertEquals(product.getName(), responseProduct.getName());
        assertEquals(product.getDescription(), responseProduct.getDescription());
        assertEquals(product.getPrice(), responseProduct.getPrice());
        assertEquals(product.getImgUrl(), responseProduct.getImgUrl());
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInFindProductByUUID() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> productService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldInsertProduct() {
        when(modelMapper.map(any(ProductInsertDTO.class), eq(Product.class))).
                thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocationOnMock -> {
            product.setId(RANDOM_UUID);
            return product;
        });

        ProductDTO responseProduct = productService.insert(productInsertDTO);

        assertNotNull(responseProduct);
        assertEquals(product.getId(), responseProduct.getId());
        assertEquals(product.getName(), responseProduct.getName());
        assertEquals(product.getDescription(), responseProduct.getDescription());
        assertEquals(product.getPrice(), responseProduct.getPrice());
        assertEquals(product.getImgUrl(), responseProduct.getImgUrl());
        verify(modelMapper, times(1)).
                map(any(ProductInsertDTO.class), eq(Product.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    void shouldDeleteProductByUUID() {
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));
        doNothing().when(productRepository).delete(any(Product.class));

        productService.deleteByUUID(RANDOM_UUID.toString());

        verify(productRepository, times(1)).findById(any(UUID.class));
        verify(productRepository, times(1)).delete(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInDeleteProductByUUID() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> productService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldUpdateProductByUUID() {
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));
        doNothing().when(modelMapper).map(any(ProductInsertDTO.class), any(Product.class));
        productInsertDTO.setPrice(BigDecimal.TEN);
        when(productRepository.save(any(Product.class))).thenAnswer(invocationOnMock -> {
            product.setId(RANDOM_UUID);
            product.setPrice(productInsertDTO.getPrice());
            return product;
        });

        ProductDTO responseProduct = productService.updateByUUID(RANDOM_UUID.toString(), productInsertDTO);

        assertNotNull(responseProduct);
        assertEquals(product.getId(), responseProduct.getId());
        assertEquals(product.getName(), responseProduct.getName());
        assertEquals(product.getDescription(), responseProduct.getDescription());
        assertEquals(product.getPrice(), responseProduct.getPrice());
        assertEquals(product.getImgUrl(), responseProduct.getImgUrl());
        verify(productRepository, times(1)).findById(any(UUID.class));
        verify(modelMapper, times(1)).
                map(any(ProductInsertDTO.class), any(Product.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInUpdateProductByUUID() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> productService.updateByUUID(RANDOM_UUID.toString(), productInsertDTO));
        assertEquals("Product not found", e.getMessage());
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldAddCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocationOnMock -> {
            product.setId(RANDOM_UUID);
            return product;
        });

        ProductDTO responseProduct = productService.
                addCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO);

        assertNotNull(responseProduct);
        assertEquals(product.getId(), responseProduct.getId());
        assertEquals(product.getName(), responseProduct.getName());
        assertEquals(product.getDescription(), responseProduct.getDescription());
        assertEquals(product.getPrice(), responseProduct.getPrice());
        assertEquals(product.getImgUrl(), responseProduct.getImgUrl());
        assertFalse(responseProduct.getCategories().isEmpty());
        assertEquals(1, responseProduct.getCategories().size());
        assertEquals(category.getName(), responseProduct.getCategories().iterator().next().getName());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInAddCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class, () ->
                productService.addCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Category not found", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInAddCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class, () ->
                productService.addCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Product not found", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenCategoryExistsInProductInAddCategoryInProductByUUID() {
        product.getCategories().add(category);
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));

        Throwable e = assertThrows(EntityExistsException.class, () ->
                productService.addCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Category already exists in this product", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldRemoveCategoryInProductByUUID() {
        product.getCategories().add(category);
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocationOnMock -> {
            product.setId(RANDOM_UUID);
            return product;
        });

        ProductDTO responseProduct = productService.
                removeCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO);

        assertNotNull(responseProduct);
        assertEquals(product.getId(), responseProduct.getId());
        assertEquals(product.getName(), responseProduct.getName());
        assertEquals(product.getDescription(), responseProduct.getDescription());
        assertEquals(product.getPrice(), responseProduct.getPrice());
        assertEquals(product.getImgUrl(), responseProduct.getImgUrl());
        assertTrue(responseProduct.getCategories().isEmpty());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInRemoveCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(false);

        Throwable e = assertThrows(EntityNotFoundException.class, () ->
                productService.removeCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Category not found", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenProductNotExistsInRemoveCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class, () ->
                productService.removeCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Product not found", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotExistsInProductInRemoveCategoryInProductByUUID() {
        when(categoryRepository.existsById(any(UUID.class))).thenReturn(true);
        when(categoryRepository.getReferenceById(any(UUID.class))).thenReturn(category);
        when(productRepository.findById(any(UUID.class))).
                thenReturn(Optional.ofNullable(product));

        Throwable e = assertThrows(EntityNotFoundException.class, () ->
                productService.removeCategoryByUUID(RANDOM_UUID.toString(), productCategoryInsertDTO));
        assertEquals("Category not found in this product", e.getMessage());
        verify(categoryRepository, times(1)).existsById(any(UUID.class));
        verify(categoryRepository, times(1)).getReferenceById(any(UUID.class));
        verify(productRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(categoryRepository);
    }


    private void init() {
        productInsertDTO = new ProductInsertDTO("Test", "Test", BigDecimal.ONE, "www.test.org");
        product = new Product("Test", "Test", BigDecimal.ONE, "www.test.org");
        productCategoryInsertDTO = new ProductCategoryInsertDTO(RANDOM_UUID.toString());
        category = new Category("Test");
    }
}