package crypto.respawned.bobmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class BobcatStatus {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BobcatStatus.class);
    
    private String status = null;
    private String gap = null;
    private String miner_height = null;
    private String blockchain_height = null;
    private String epoch = null;

    public BobcatStatus() {
        super();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGap() {
        return gap;
    }

    public void setGap(String gap) {
        this.gap = gap;
    }

    public String getMiner_height() {
        return miner_height;
    }

    public void setMiner_height(String miner_height) {
        this.miner_height = miner_height;
    }

    public String getBlockchain_height() {
        return blockchain_height;
    }

    public void setBlockchain_height(String blockchain_height) {
        this.blockchain_height = blockchain_height;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    @Override
    public String toString() {
        return "status=" + this.status + " gap=" + this.gap + " miner_height=" + this.miner_height + " blockchain_height=" + this.blockchain_height + " epoch=" + this.epoch;
    }
    
    public static BobcatStatus parseBobcatStatus(String jsonSTR) {
        BobcatStatus ev = null;
        try {
            ev = JSON.parseObject(jsonSTR, BobcatStatus.class);
        } catch (Exception e) {
            LOGGER.error("Exception during JSON parsing: " + e.getClass() + ": " + e.getMessage(), e);
            LOGGER.error("JSON string for above error: " + jsonSTR);
        }
        return ev;
    }
    
}
