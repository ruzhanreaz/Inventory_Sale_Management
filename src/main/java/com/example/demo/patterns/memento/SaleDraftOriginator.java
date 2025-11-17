package com.example.demo.patterns.memento;

import java.util.List;
import com.example.demo.models.Customer;
import com.example.demo.models.Product;

public class SaleDraftOriginator {
    private List<Product> cartItems;
    private Customer customer;

    public void setState(List<Product> cartItems, Customer customer) {
        this.cartItems = cartItems;
        this.customer = customer;
    }

    public SaleDraftMemento saveToMemento() {
        return new SaleDraftMemento(cartItems, customer);
    }

    public void restoreFromMemento(SaleDraftMemento memento) {
        this.cartItems = memento.getCartItems();
        this.customer = memento.getCustomer();
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public Customer getCustomer() {
        return customer;
    }
}

