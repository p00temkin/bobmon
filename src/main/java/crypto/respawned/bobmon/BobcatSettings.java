package crypto.respawned.bobmon;

import crypto.forestfish.utils.SystemUtils;
import crypto.forestfish.utils.URLUtils;

public class BobcatSettings {
    
    private String bobcatStatusURL = "";
    private String apiTokenApp = "";
    private String apiTokenUser = "";
    private int sleepTimeInSeconds = 3600;
    private int stuckThreshold = 3;
    
    public BobcatSettings() {
        super();
    }

    public String getBobcatStatusURL() {
        return bobcatStatusURL;
    }

    public void setBobcatStatusURL(String bobcatStatusURL) {
        this.bobcatStatusURL = bobcatStatusURL;
    }

    public String getApiTokenApp() {
        return apiTokenApp;
    }

    public void setApiTokenApp(String apiTokenApp) {
        this.apiTokenApp = apiTokenApp;
    }

    public String getApiTokenUser() {
        return apiTokenUser;
    }

    public void setApiTokenUser(String apiTokenUser) {
        this.apiTokenUser = apiTokenUser;
    }

    public int getStuckThreshold() {
        return stuckThreshold;
    }

    public void setStuckThreshold(int stuckThreshold) {
        this.stuckThreshold = stuckThreshold;
    }

    public void print() {
        System.out.println("Settings:");
        System.out.println(" - apiTokenApp: " + this.getApiTokenApp());
        System.out.println(" - apiTokenUser: " + this.getApiTokenUser());
        System.out.println(" - bobcatStatusURL: " + this.getBobcatStatusURL());
        System.out.println(" - stuckThreshold: " + this.getStuckThreshold());
        System.out.println(" - sleepTimeInSeconds: " + this.getSleepTimeInSeconds());
    }

    public void sanityCheck() {
        boolean allgood = true;
        
        String bobcatHostname = URLUtils.extractURLHostnameFromURL(this.getBobcatStatusURL());
        if ("".equals(bobcatHostname)) {
            System.out.println(" - bobcatStatusURL INVALID: " + this.getBobcatStatusURL());
            allgood = false;
        }
        
        if (!allgood) {
            SystemUtils.halt();
        }
    }

    public int getSleepTimeInSeconds() {
        return sleepTimeInSeconds;
    }

    public void setSleepTimeInSeconds(int sleepTimeInSeconds) {
        this.sleepTimeInSeconds = sleepTimeInSeconds;
    }
    
}
