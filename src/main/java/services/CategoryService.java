package services;

import java.util.List;
import dto.product.ProductResponseDto;
import java.util.stream.Collectors;

import dao.CategoryDao;
import dao.JPAUtil;
import dto.category.CategoryRequestDto;
import dto.category.CategoryResponseDto;
import entities.Category;

/**
 * Service class for handling category management operations. Manages CRUD
 * operations, search, and DTO conversions.
 */
public class CategoryService {

    private final CategoryDao categoryDao;
    private final ProductService productService;

    public CategoryService(CategoryDao categoryDao, ProductService productService) {
        this.categoryDao = categoryDao;
        this.productService = productService;
    }

    /**
     * Retrieve all active categories from the database.
     */
    public List<Category> getAll() {
        return categoryDao.findAllActive();
    }

    /**
     * Retrieve a category by its ID.
     */
    public Category getById(Integer id) {
        return categoryDao.findById(id);
    }

    /**
     * Create a new category with validation. Validates category name and checks
     * for duplicates.
     *
     * @param dto The category request DTO containing details.
     * @return The created category entity.
     * @throws IllegalArgumentException if validation fails.
     */
    public Category create(CategoryRequestDto dto) {
        if (dto.categoryName == null || dto.categoryName.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        if (categoryDao.existsByName(dto.categoryName, null)) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = new Category();
        category.setCategoryName(dto.categoryName);
        category.setDescription(dto.description);
        category.setImageUrl(dto.imageUrl);

        try {
            JPAUtil.getEntityManager().getTransaction().begin();
            categoryDao.create(category);
            JPAUtil.getEntityManager().getTransaction().commit();
            return category;
        } catch (Exception e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Convert Category entity to Response DTO.
     */
    public CategoryResponseDto toDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.id = category.getId();
        dto.categoryName = category.getCategoryName();
        dto.description = category.getDescription();
        dto.imageUrl = category.getImageUrl();
        dto.status = category.getStatus();
        dto.parentId = category.getParentId();
        return dto;
    }

    /**
     * Convert a list of categories to a list of response DTOs.
     */
    public List<CategoryResponseDto> toDtoList(List<Category> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Search categories by keyword.
     */
    public List<CategoryResponseDto> search(String keyword) {
        List<Category> list = categoryDao.search(keyword);
        return toDtoList(list);
    }

    /**
     * Retrieve all active categories as response DTOs.
     */
    public List<CategoryResponseDto> getAllDtos() {
        return toDtoList(getAll());
    }

    /**
     * Retrieve a category by ID as a response DTO.
     */
    public CategoryResponseDto getByIdDto(Integer id) {
        Category c = getById(id);
        if (c == null) {
            return null;
        }
        CategoryResponseDto dto = toDto(c);
        dto.products = productService.searchWithFilters(id, null, null, null, null, null, null);
        return dto;
    }

    /**
     * Update an existing category. Only updates allowed fields (name,
     * description, imageUrl). Validates category name and checks for
     * duplicates.
     *
     * @param id The category ID.
     * @param dto The category request DTO with updates.
     * @return The updated category entity.
     * @throws IllegalArgumentException if validation fails.
     */
    public Category update(Integer id, CategoryRequestDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("Category id is required for update");
        }

        Category existing = categoryDao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found");
        }

        // Merge allowed fields only
        if (dto.categoryName != null) {
            if (dto.categoryName.isBlank()) {
                throw new IllegalArgumentException("Category name cannot be blank");
            }
            if (categoryDao.existsByName(dto.categoryName, id)) {
                throw new IllegalArgumentException("Category name already exists");
            }
            existing.setCategoryName(dto.categoryName);
        }

        if (dto.description != null) {
            existing.setDescription(dto.description);
        }
        if (dto.imageUrl != null) {
            existing.setImageUrl(dto.imageUrl);
        }

        try {
            JPAUtil.getEntityManager().getTransaction().begin();
            Category merged = categoryDao.update(existing);
            JPAUtil.getEntityManager().getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Soft delete a category by setting its status to false.
     *
     * @param id The category ID.
     * @throws IllegalArgumentException if category not found or ID is null.
     */
    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Category id is required");
        }

        Category existing = categoryDao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found");
        }

        try {
            JPAUtil.getEntityManager().getTransaction().begin();
            existing.setStatus(false); // SOFT DELETE
            categoryDao.update(existing);
            JPAUtil.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            if (JPAUtil.getEntityManager().getTransaction().isActive()) {
                JPAUtil.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }
}
