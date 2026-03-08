package crawler;

public class CrawlerRunner {
    public static void main(String[] args) {
        GearvnCrawlerService service = new GearvnCrawlerService();

        // GPU
        service.crawlCollection("vga-card-man-hinh", "GPU", 1, 3);

        // CPU
        service.crawlCollection("cpu-bo-vi-xu-ly", "CPU", 1, 2);

        // RAM
        service.crawlCollection("ram-pc", "RAM", 1, 2);

        // SSD
        service.crawlCollection("ssd-o-cung-the-ran", "Storage", 1, 2);
    }
}