package com.sentinel.forensics.hash;
import java.util.List;
import java.util.ArrayList;

public class VerificationResult {
    private int tamperedCount = 0;
    private int checkedCount = 0;
    private List<String> tamperedEvents = new ArrayList<>();
    
    public int getTamperedCount() { return tamperedCount; }
    public void setTamperedCount(int tamperedCount) { this.tamperedCount = tamperedCount; }
    public int getChecked() { return checkedCount; }
    public void setChecked(int checkedCount) { this.checkedCount = checkedCount; }
    public List<String> getTamperedEvents() { return tamperedEvents; }
    public void setTamperedEvents(List<String> tamperedEvents) { this.tamperedEvents = tamperedEvents; }
}
