package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.JPAUtil;
import entities.Category;
import entities.Product;
import entities.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jsoup.Jsoup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GearvnCrawlerService {

    public void crawlCollection(String collectionHandle, String categoryName, int fromPage, int toPage) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            for (int page = fromPage; page <= toPage; page++) {
                String url = "https://gearvn.com/collections/" + collectionHandle + "/products.json?page=" + page;

                System.out.println("====================================");
                System.out.println("Crawling collection: " + collectionHandle + " | page: " + page);
                System.out.println(url);

                List<CrawledProduct> crawledProducts = fetchProducts(url, categoryName);

                if (crawledProducts.isEmpty()) {
                    System.out.println("No products found on page " + page);
                    continue;
                }

                for (CrawledProduct cp : crawledProducts) {
                    upsertProduct(em, cp);
                }

                Thread.sleep(1000);
            }

            em.getTransaction().commit();
            System.out.println("Done crawling collection: " + collectionHandle);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private List<CrawledProduct> fetchProducts(String url, String categoryName) {
        List<CrawledProduct> result = new ArrayList<>();

        try {
            String json = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .execute()
                    .body();

            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray products = obj.getAsJsonArray("products");

            if (products == null) {
                return result;
            }

            for (int i = 0; i < products.size(); i++) {
                JsonObject p = products.get(i).getAsJsonObject();

                String name = getString(p, "title");
                if (name == null || name.isBlank()) {
                    continue;
                }

                String description = getString(p, "body_html");
                if (description == null || description.isBlank()) {
                    description = name;
                }

                String imageUrl = null;
                if (p.has("images") && p.getAsJsonArray("images").size() > 0) {
                    JsonObject firstImage = p.getAsJsonArray("images").get(0).getAsJsonObject();
                    imageUrl = getString(firstImage, "src");
                }

                String priceText = "0";
                if (p.has("variants") && p.getAsJsonArray("variants").size() > 0) {
                    JsonObject firstVariant = p.getAsJsonArray("variants").get(0).getAsJsonObject();
                    String tempPrice = getString(firstVariant, "price");
                    if (tempPrice != null && !tempPrice.isBlank()) {
                        priceText = tempPrice;
                    }
                }

                CrawledProduct cp = new CrawledProduct();
                cp.setProductName(name.trim());
                cp.setDescription(stripHtml(description));
                cp.setImageUrl(imageUrl);
                cp.setCurrentPrice(parsePrice(priceText));
                cp.setCategoryName(categoryName);
                cp.setSupplierName(detectSupplier(name));
                cp.setQuantity(10);

                result.add(cp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void upsertProduct(EntityManager em, CrawledProduct cp) {
        String supplierName = cp.getSupplierName();
        if (supplierName == null || supplierName.isBlank()) {
            supplierName = "Unknown";
        }

        Category category = findOrCreateCategory(em, cp.getCategoryName());
        Supplier supplier = findOrCreateSupplier(em, supplierName, cp.getCategoryName());

        Product existing = findExistingProduct(em, cp.getProductName(), supplierName);

        if (existing != null) {
            boolean changed = false;

            if (!safeEquals(existing.getDescription(), cp.getDescription())) {
                existing.setDescription(cp.getDescription());
                changed = true;
            }

            if (!safeEquals(existing.getImageUrl(), cp.getImageUrl())) {
                existing.setImageUrl(cp.getImageUrl());
                changed = true;
            }

            if (!safeBigDecimalEquals(existing.getCurrentPrice(), cp.getCurrentPrice())) {
                existing.setCurrentPrice(cp.getCurrentPrice());
                changed = true;
            }

            if (existing.getCategory() == null || !safeEqualsIgnoreCase(existing.getCategory().getCategoryName(), cp.getCategoryName())) {
                existing.setCategory(category);
                changed = true;
            }

            if (existing.getSupplier() == null || !safeEqualsIgnoreCase(existing.getSupplier().getSupplierName(), supplierName)) {
                existing.setSupplier(supplier);
                changed = true;
            }

            if (existing.getQuantity() == null || !existing.getQuantity().equals(cp.getQuantity())) {
                existing.setQuantity(cp.getQuantity());
                changed = true;
            }

            if (existing.getStatus() == null || !existing.getStatus()) {
                existing.setStatus(true);
                changed = true;
            }

            if (changed) {
                em.merge(existing);
                System.out.println("Updated: " + cp.getProductName());
            } else {
                System.out.println("No change: " + cp.getProductName());
            }

            return;
        }

        Product product = new Product();
        product.setProductName(cp.getProductName());
        product.setDescription(cp.getDescription());
        product.setImageUrl(cp.getImageUrl());
        product.setCurrentPrice(cp.getCurrentPrice());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setQuantity(cp.getQuantity() != null ? cp.getQuantity() : 0);
        product.setStatus(true);

        em.persist(product);

        System.out.println("Created: " + cp.getProductName());
    }

    private Product findExistingProduct(EntityManager em, String productName, String supplierName) {
        TypedQuery<Product> query = em.createQuery(
                "SELECT p FROM Product p " +
                        "LEFT JOIN p.supplier s " +
                        "WHERE LOWER(p.productName) = LOWER(:name) " +
                        "AND LOWER(s.supplierName) = LOWER(:supplier)",
                Product.class
        );
        query.setParameter("name", productName);
        query.setParameter("supplier", supplierName);

        List<Product> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Category findOrCreateCategory(EntityManager em, String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            categoryName = "Other";
        }

        TypedQuery<Category> query = em.createQuery(
                "SELECT c FROM Category c WHERE LOWER(c.categoryName) = LOWER(:name)",
                Category.class
        );
        query.setParameter("name", categoryName);

        List<Category> results = query.getResultList();
        if (!results.isEmpty()) {
            System.out.println("Using existing category: " + categoryName);
            return results.get(0);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setDescription(categoryName);
        category.setImageUrl(null);
        category.setStatus(true);

        em.persist(category);
        System.out.println("Created new category: " + categoryName);
        return category;
    }

    private Supplier findOrCreateSupplier(EntityManager em, String supplierName, String componentType) {
        TypedQuery<Supplier> query = em.createQuery(
                "SELECT s FROM Supplier s WHERE LOWER(s.supplierName) = LOWER(:name)",
                Supplier.class
        );
        query.setParameter("name", supplierName);

        List<Supplier> results = query.getResultList();
        if (!results.isEmpty()) {
            System.out.println("Using existing supplier: " + supplierName);
            return results.get(0);
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierName(supplierName);
        supplier.setContactInfo("N/A");
        supplier.setLeadTimeDays(3);
        supplier.setComponentTypes(componentType);
        supplier.setStatus(true);

        em.persist(supplier);
        System.out.println("Created new supplier: " + supplierName);
        return supplier;
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            return BigDecimal.ZERO;
        }

        String cleaned = priceText.replaceAll("[^0-9]", "");
        if (cleaned.isBlank()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(cleaned);
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }

    private String detectSupplier(String productName) {
        String name = productName.toLowerCase();

        if (name.contains("asus")) return "ASUS";
        if (name.contains("msi")) return "MSI";
        if (name.contains("gigabyte")) return "GIGABYTE";
        if (name.contains("zotac")) return "ZOTAC";
        if (name.contains("colorful")) return "COLORFUL";
        if (name.contains("inno3d")) return "INNO3D";
        if (name.contains("sapphire")) return "SAPPHIRE";
        if (name.contains("intel")) return "Intel";
        if (name.contains("amd")) return "AMD";
        if (name.contains("kingston")) return "Kingston";
        if (name.contains("corsair")) return "Corsair";
        if (name.contains("samsung")) return "Samsung";
        if (name.contains("adata")) return "ADATA";
        if (name.contains("western digital")) return "Western Digital";
        if (name.contains("wd")) return "Western Digital";
        if (name.contains("crucial")) return "Crucial";
        if (name.contains("g.skill")) return "G.SKILL";
        if (name.contains("lexar")) return "Lexar";
        if (name.contains("team")) return "TeamGroup";
        if (name.contains("apacer")) return "Apacer";
        if (name.contains("hiksemi")) return "Hiksemi";
        if (name.contains("acer")) return "Acer";
        if (name.contains("seagate")) return "Seagate";

        return "Unknown";
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private boolean safeBigDecimalEquals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }
}