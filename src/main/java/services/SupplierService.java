package services;

import dao.SupplierDao;
import dto.supplier.SupplierRequestDto;
import dto.supplier.SupplierResponseDto;
import entities.Supplier;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for supplier management.
 * Handles supplier CRUD operations, validation, and DTO mapping.
 */
public class SupplierService {

    private final SupplierDao supplierDao;
    private final EntityManager em;

    /**
     * Constructor.
     *
     * @param supplierDao Supplier DAO.
     * @param em JPA EntityManager.
     */
    public SupplierService(SupplierDao supplierDao, EntityManager em) {
        this.supplierDao = supplierDao;
        this.em = em;
    }

    /**
     * Retrieve suppliers as DTO list with optional name filter.
     *
     * @param q Optional keyword filter.
     * @return Supplier DTO list.
     */
    public List<SupplierResponseDto> getAllDtos(String q) {
        List<Supplier> suppliers;
        if (q == null || q.isBlank()) suppliers = supplierDao.findAllActive();
        else suppliers = supplierDao.searchByName(q);
        return suppliers.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Retrieve supplier by id as DTO.
     *
     * @param id Supplier ID.
     * @return Supplier DTO or null if not found.
     */
    public SupplierResponseDto getByIdDto(Integer id) {
        Supplier supplier = supplierDao.findById(id);
        return supplier == null ? null : toDto(supplier);
    }

    /**
     * Create a supplier after validating request data.
     *
     * @param dto Supplier creation request.
     * @return Created supplier DTO.
     */
    public SupplierResponseDto create(SupplierRequestDto dto) {
        validate(dto);
        Supplier supplier = new Supplier();
        apply(dto, supplier);

        try {
            em.getTransaction().begin();
            supplierDao.create(supplier);
            em.getTransaction().commit();
            return toDto(supplier);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Update supplier information by id.
     *
     * @param id Supplier ID.
     * @param dto Supplier update request.
     * @return Updated supplier DTO.
     */
    public SupplierResponseDto update(Integer id, SupplierRequestDto dto) {
        validate(dto);
        Supplier existing = supplierDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Supplier not found");
        apply(dto, existing);

        try {
            em.getTransaction().begin();
            Supplier merged = supplierDao.update(existing);
            em.getTransaction().commit();
            return toDto(merged);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Soft-delete a supplier by setting status to false.
     *
     * @param id Supplier ID.
     */
    public void delete(Integer id) {
        Supplier existing = supplierDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Supplier not found");

        try {
            em.getTransaction().begin();
            existing.setStatus(false);
            supplierDao.update(existing);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Convert Supplier entity to SupplierResponseDto.
     *
     * @param supplier Supplier entity.
     * @return Mapped DTO object.
     */
    public SupplierResponseDto toDto(Supplier supplier) {
        SupplierResponseDto dto = new SupplierResponseDto();
        dto.id = supplier.getId();
        dto.supplierName = supplier.getSupplierName();
        dto.contactInfo = supplier.getContactInfo();
        dto.componentTypes = supplier.getComponentTypes();
        dto.leadTimeDays = supplier.getLeadTimeDays();
        dto.status = supplier.getStatus();
        return dto;
    }

    private void apply(SupplierRequestDto dto, Supplier supplier) {
        supplier.setSupplierName(dto.supplierName);
        supplier.setContactInfo(dto.contactInfo);
        supplier.setComponentTypes(dto.componentTypes);
        supplier.setLeadTimeDays(dto.leadTimeDays);
        if (dto.status != null) supplier.setStatus(dto.status);
    }

    private void validate(SupplierRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (dto.supplierName == null || dto.supplierName.isBlank()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        if (dto.leadTimeDays == null || dto.leadTimeDays < 0) {
            throw new IllegalArgumentException("Lead time must be >= 0");
        }
    }
}
