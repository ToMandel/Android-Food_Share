package com.Tom.foodshare.Class;

import java.io.Serializable;
import java.util.Objects;

public class FoodItem implements Serializable {
    private String id;  // Firestore document ID or a unique identifier
    private String description;
    private int amount;
    private String unit;
    private String url;
    private User user;

    // Default constructor required for Firestore serialization
    public FoodItem() {
        // No UUID generation here; it should only happen when creating a new item.
    }

    // Constructor with all fields
    public FoodItem(String description, int amount, String unit, String url, User user, String id) {
        this.description = description;
        this.amount = amount;
        this.unit = unit;
        this.url = url;
        this.user = user;
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();  // Use provided ID or generate a new one only if ID is null
    }

    // Constructor without URL (defaults to empty string)
    public FoodItem(String description, int amount, String unit, User user, String id) {
        this(description, amount, unit, "", user, id);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return Objects.equals(id, foodItem.id);  // Compare by unique ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);  // Hash by unique ID
    }
}
