package com.library.dto;

public class MemberBorrowingStats {
    private String memberName;
    private Long totalBorrowings;
    private Long lateBorrowings;
    private Long currentBorrowings;

     public MemberBorrowingStats(String memberName, Long totalBorrowings, Long lateBorrowings, Long currentBorrowings) {
        this.memberName = memberName;
        this.totalBorrowings = totalBorrowings != null ? totalBorrowings : 0L;
        this.lateBorrowings = lateBorrowings != null ? lateBorrowings : 0L;
        this.currentBorrowings = currentBorrowings != null ? currentBorrowings : 0L;
    }

    // Getters et Setters
   // Getters
    public String getMemberName() { return memberName; }
    public Long getTotalBorrowings() { return totalBorrowings; }
    public Long getLateBorrowings() { return lateBorrowings; }
    public Long getCurrentBorrowings() { return currentBorrowings; }
}
