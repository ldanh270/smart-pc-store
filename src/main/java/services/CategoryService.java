package services;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import dao.CategoryDao;
import dao.JPAUtil;
import dto.category.CategoryRequestDto;
import dto.category.CategoryResponseDto;
import entities.Category;
import utils.SlugUtil;

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
    public Category getById(UUID id) {
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
        category.setSlug(generateUniqueSlug(dto.categoryName, null));

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
        dto.slug = category.getSlug();
        dto.description = category.getDescription();
        dto.imageUrl = category.getImageUrl();
        dto.status = category.getStatus();
        dto.parentId = category.getParent() != null ? category.getParent().getId() : null;
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
    public CategoryResponseDto getByIdDto(UUID id) {
        Category c = getById(id);
        if (c == null) {
            return null;
        }
        CategoryResponseDto dto = toDto(c);
        List<UUID> categoryIds = collectCategoryTreeIds(c.getId());
        dto.products = productService.searchWithCategoryIds(categoryIds, null, null, null, null, null, null);
        return dto;
    }

    /**
     * Retrieve a category by slug as a response DTO.
     */
    public CategoryResponseDto getBySlugDto(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }

        Category c = categoryDao.findBySlug(slug);
        if (c == null) {
            return null;
        }

        CategoryResponseDto dto = toDto(c);
        List<UUID> categoryIds = collectCategoryTreeIds(c.getId());
        dto.products = productService.searchWithCategoryIds(categoryIds, null, null, null, null, null, null);
        return dto;
    }

    /**
     * Update an existing category. Only updates allowed fields (name,
     * description, imageUrl). Validates category name and checks for
     * duplicates.
     *
     * @param id  The category ID.
     * @param dto The category request DTO with updates.
     * @return The updated category entity.
     * @throws IllegalArgumentException if validation fails.
     */
    public Category update(UUID id, CategoryRequestDto dto) {
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
            existing.setSlug(generateUniqueSlug(dto.categoryName, id));
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
    public void delete(UUID id) {
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

    private String generateUniqueSlug(String name, UUID excludeId) {
        String baseSlug = SlugUtil.toSlug(name);
        if (baseSlug.isBlank()) {
            throw new IllegalArgumentException("Category name is invalid for slug generation");
        }

        String candidate = baseSlug;
        int attempt = 1;
        while (categoryDao.existsBySlug(candidate, excludeId)) {
            candidate = baseSlug + "-" + attempt;
            attempt++;
        }

        return candidate;
    }

    private List<UUID> collectCategoryTreeIds(UUID rootCategoryId) {
        Set<UUID> collected = new LinkedHashSet<>();
        Queue<UUID> queue = new ArrayDeque<>();
        queue.add(rootCategoryId);

        while (!queue.isEmpty()) {
            UUID currentId = queue.poll();
            if (!collected.add(currentId)) {
                continue;
            }

            List<UUID> children = categoryDao.findActiveChildIds(currentId);
            for (UUID childId : children) {
                if (!collected.contains(childId)) {
                    queue.add(childId);
                }
            }
        }

        return new ArrayList<>(collected);
    }
}
