package ru.synccamera;

public class PeerListItem {

    private String name;
    private String status;
    private String address;

    PeerListItem(String name, String status, String address) {
        this.name = name;
        this.status = status;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }
}
