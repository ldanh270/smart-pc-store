package listeners;

import dao.JPAUtil;
import dao.SessionDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import utils.EnvHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@WebListener
public class SessionCleanupContextListener implements ServletContextListener {

    private static final int DEFAULT_INTERVAL_SECONDS = 900;

    private ScheduledExecutorService scheduler;
    private SessionDao sessionDao;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!isCleanupEnabled()) {
            System.out.println("Session cleanup job is disabled");
            return;
        }

        sessionDao = new SessionDao();
        int intervalSeconds = parsePositiveInt();

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "session-cleanup-job");
            thread.setDaemon(true);
            return thread;
        };

        scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduler.scheduleWithFixedDelay(this::runCleanupSafely, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        System.out.println("Session cleanup job started, interval=" + intervalSeconds + "s");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        JPAUtil.closeEntityManager();
    }

    private void runCleanupSafely() {
        if (sessionDao == null) {
            return;
        }

        try {
            int deleted = sessionDao.deleteExpiredSessions();
            if (deleted > 0) {
                System.out.println("Session cleanup deleted: " + deleted);
            }
        } catch (Exception e) {
            System.err.println("Session cleanup failed: " + e.getMessage());
        } finally {
            JPAUtil.closeEntityManager();
        }
    }

    private boolean isCleanupEnabled() {
        String value = EnvHelper.get("SESSION_CLEANUP_ENABLED", "true");
        return value == null || !"false".equalsIgnoreCase(value.trim());
    }

    private int parsePositiveInt() {
        try {
            String value = EnvHelper.get("SESSION_CLEANUP_INTERVAL_SECONDS", String.valueOf(SessionCleanupContextListener.DEFAULT_INTERVAL_SECONDS));
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : SessionCleanupContextListener.DEFAULT_INTERVAL_SECONDS;
        } catch (RuntimeException e) {
            return SessionCleanupContextListener.DEFAULT_INTERVAL_SECONDS;
        }
    }
}
