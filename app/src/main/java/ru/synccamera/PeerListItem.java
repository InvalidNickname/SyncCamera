package ru.synccamera;

public class PeerListItem {

    private String name;
    private int status;
    private String address;

    PeerListItem(String name, int status, String address) {
        this.name = name;
        this.status = status;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }
}
