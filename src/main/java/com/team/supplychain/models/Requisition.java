package com.team.supplychain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Requisition
 * Represents a purchase requisition request from employees
 */
public class Requisition {

    private Integer requisitionId;
    private String requisitionCode;
    private Integer requestedBy;
    private String requesterName;
    private Integer supplierId;
    private String supplierName;
    private String category;
    private String department;
    private String priority;
    private String justification;
    private String status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime requestDate;
    private Integer reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewDate;
    private String reviewNotes;
    private List<RequisitionItem> items;

    public Requisition() {
        this.items = new ArrayList<>();
        this.status = "Pending";
        this.requestDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getRequisitionId() {
        return requisitionId;
    }

    public void setRequisitionId(Integer requisitionId) {
        this.requisitionId = requisitionId;
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public void setRequisitionCode(String requisitionCode) {
        this.requisitionCode = requisitionCode;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public List<RequisitionItem> getItems() {
        return items;
    }

    public void setItems(List<RequisitionItem> items) {
        this.items = items;
    }

    public void addItem(RequisitionItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Requisition{" +
                "requisitionId=" + requisitionId +
                ", requisitionCode='" + requisitionCode + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", requestDate=" + requestDate +
                '}';
    }
}
