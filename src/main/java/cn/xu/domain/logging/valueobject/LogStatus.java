package cn.xu.domain.logging.valueobject;

public class LogStatus {
    private final int status;

    public LogStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == 1;
    }

    public boolean isFailure() {
        return status == 0;
    }
} 