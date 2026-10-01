package Configuration;

public class PlatformConfiguration {

    private static PlatformConfiguration instance;

    private double baseDeliveryFee;
    private double extraKmFee;
    private int poolSize;
    private String filePath;

    private PlatformConfiguration() {
        baseDeliveryFee = 15.0;
        extraKmFee = 3.0;
        poolSize = 10;
        filePath = "data/";
    }

    public static PlatformConfiguration getInstance() {
        if (instance == null) {
            instance = new PlatformConfiguration();
        }
        return instance;
    }

    public double getBaseDeliveryFee() {
        return baseDeliveryFee;
    }

    public double getExtraKmFee() {
        return extraKmFee;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public String getFilePath() {
        return filePath;
    }
}
