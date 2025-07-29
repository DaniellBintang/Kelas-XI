package com.example.recyclercardview.model;

public class Person {
    private int id;
    private String name;
    private String address;
    private String dateAdded;

    // Constructor
    public Person(int id, String name, String address, String dateAdded) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dateAdded = dateAdded;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", dateAdded='" + dateAdded + '\'' +
                '}';
    }
}