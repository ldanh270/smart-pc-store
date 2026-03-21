package services;

import dao.ProductDao;
import dao.PurchaseOrderDao;
import dao.PurchaseOrderItemDao;
import entities.Product;
import entities.PurchaseOrder;
import entities.PurchaseOrderItem;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderService - 仕入注文と在庫調整のロジックを管理するサービス
 */
public class PurchaseOrderService {

    private final PurchaseOrderDao poDao;
    private final PurchaseOrderItemDao poiDao;
    private final ProductDao productDao;

    public PurchaseOrderService() {
        this.poDao = new PurchaseOrderDao();
        this.poiDao = new PurchaseOrderItemDao();
        this.productDao = new ProductDao();
    }

    /**
     * 通常の仕入注文を作成し、在庫を更新する
     *
     * @param order 注文エンティティ
     * @param items 注文明細のリスト
     * @return 作成された注文
     */
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order, List<PurchaseOrderItem> items) {
        EntityManager em = poDao.getEntityManager();
        try {
            em.getTransaction().begin();

            order.setType("NORMAL");
            order.setOrderDate(LocalDate.now());
            poDao.create(order);

            for (PurchaseOrderItem item : items) {
                item.setPo(order);
                poiDao.create(item);

                // 在庫の更新
                Product product = item.getProduct();
                if (product != null) {
                    product = productDao.findById(product.getId());
                    int newQty = (product.getQuantity() != null ? product.getQuantity() : 0) + item.getQuantity();
                    product.setQuantity(newQty);
                    productDao.update(product);
                }
            }

            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("仕入注文の作成に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * 調整注文を作成し、在庫を更新する（負の数量を許可）
     *
     * @param parentId 元の注文ID
     * @param items 調整明細のリスト（数量は負の値を指定可能）
     * @return 作成された調整注文
     */
    public PurchaseOrder createAdjustmentOrder(UUID parentId, List<PurchaseOrderItem> items) {
        EntityManager em = poDao.getEntityManager();
        try {
            em.getTransaction().begin();

            PurchaseOrder parent = poDao.findById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("元の注文が見つかりません ID: " + parentId);
            }

            PurchaseOrder adjustment = new PurchaseOrder();
            adjustment.setSupplier(parent.getSupplier());
            adjustment.setOrderDate(LocalDate.now());
            adjustment.setType("ADJUSTMENT");
            adjustment.setParentOrder(parent);
            poDao.create(adjustment);

            for (PurchaseOrderItem item : items) {
                // 在庫チェック: 調整後の在庫が負にならないか確認
                Product product = productDao.findById(item.getProduct().getId());
                int currentStock = (product.getQuantity() != null ? product.getQuantity() : 0);
                int adjustedQty = item.getQuantity(); // 通常は負の値

                if (currentStock + adjustedQty < 0) {
                    throw new IllegalStateException("在庫が不足しているため、調整できません。製品: " + product.getProductName());
                }

                item.setPo(adjustment);
                poiDao.create(item);

                // 在庫の更新
                product.setQuantity(currentStock + adjustedQty);
                productDao.update(product);
            }

            em.getTransaction().commit();
            return adjustment;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("調整注文の作成に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * 特定の注文における製品の調整後数量を取得する
     *
     * @param poId 注文ID
     * @param productId 製品ID
     * @return 合計数量（元の数量 + すべての調整数量）
     */
    public int getAdjustedQuantity(UUID poId, UUID productId) {
        List<PurchaseOrderItem> familyItems = poiDao.findByProductAndOrderFamily(productId, poId);
        int total = 0;
        for (PurchaseOrderItem item : familyItems) {
            total += (item.getQuantity() != null ? item.getQuantity() : 0);
        }
        return total;
    }

    /**
     * 注文の詳細（調整を含む）を取得する
     * 
     * @param poId 注文ID
     * @return 明細リスト
     */
    public List<PurchaseOrderItem> getOrderDetails(UUID poId) {
        return poiDao.findByPurchaseOrderId(poId);
    }
}
