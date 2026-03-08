package listeners;

import dao.CartDao;
import dao.CartItemDao;
import dao.JPAUtil;
import dao.OrderDAO;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.UserDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import services.PurchaseCheckoutService;
import utils.EnvHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@WebListener
public class SepayCheckerContextListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private PurchaseCheckoutService purchaseCheckoutService;
    private int batchLimit;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!isCheckerEnabled()) {
            System.out.println("SePay single-thread checker is disabled");
            return;
        }

        purchaseCheckoutService = new PurchaseCheckoutService(
                new OrderDAO(),
                new OrderDetailDao(),
                new ProductDao(),
                new UserDao(),
                new CartDao(),
                new CartItemDao()
        );

        int intervalSeconds = parseInt("SEPAY_CHECK_INTERVAL_SECONDS", 3);
        batchLimit = parseInt("SEPAY_CHECK_BATCH_LIMIT", 100);

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "sepay-single-thread-checker");
            thread.setDaemon(true);
            return thread;
        };
        scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduler.scheduleWithFixedDelay(this::runCheckCycleSafely, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        System.out.println("SePay single-thread checker started, interval=" + intervalSeconds + "s");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        JPAUtil.closeEntityManager();
    }

    private void runCheckCycleSafely() {
        try {
            int completed = purchaseCheckoutService.runSingleThreadCheckCycle(batchLimit);
            if (completed > 0) {
                System.out.println("SePay checker completed orders: " + completed);
            }
        } catch (Exception e) {
            System.err.println("SePay checker failed: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    private boolean isCheckerEnabled() {
        String value = EnvHelper.get("SEPAY_CHECKER_ENABLED", "true");
        return !"false".equalsIgnoreCase(value.trim());
    }

    private int parseInt(String key, int defaultValue) {
        try {
            String value = EnvHelper.get(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}
