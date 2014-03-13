package org.ourgrid.cloud;

public class ResourcesInfo {
    
    private String id;
    private String cpuIdle;
    private String cpuInUse;
    private String memIdle;
    private String memInUse;

    public ResourcesInfo(String id, String cpuIdle, String cpuInUse, String memIdle, String memInUse){
        this.id = id;
        this.cpuIdle = cpuIdle;
        this.cpuInUse = cpuInUse;
        this.memIdle = memIdle;
        this.memInUse = memInUse;
    }

   
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(String cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public String getCpuInUse() {
        return cpuInUse;
    }

    public void setCpuInUse(String cpuInUse) {
        this.cpuInUse = cpuInUse;
    }

    public String getMemIdle() {
        return memIdle;
    }

    public void setMemIdle(String memIdle) {
        this.memIdle = memIdle;
    }

    public String getMemInUse() {
        return memInUse;
    }

    public void setMemInUse(String memInUse) {
        this.memInUse = memInUse;
    }

}
