create database oms_db;

use oms_db;
go


CREATE TABLE orders (
    id INT PRIMARY KEY IDENTITY,
    user_id INT NOT NULL,
    order_date DATETIME,
    order_status VARCHAR(20) CHECK (order_status IN ('Pending','Ordered', 'Cancelled', 'Failed')),          --   "Pending","Ordered", "Cancelled", "Failed"
	promo_discount DECIMAL(10,2),
    order_total DECIMAL(10, 2),
	address_id INT NOT NULL
);


CREATE TABLE order_items (
    id INT PRIMARY KEY IDENTITY,
    order_id INT,
    product_id INT NOT NULL,
    sku VARCHAR(50),
    quantity INT,
    unit_price DECIMAL(10,2),
    discount DECIMAL(10,2),
    final_price DECIMAL(10,2),
    size VARCHAR(20),
    status VARCHAR(50) CHECK (status IN ('Ordered','Pending','Shipped', 'Cancelled', 'Returned', 'Failed')) ,  -- e.g., Ordered, Shipped, Cancelled, Returned, Failed
	seller_id INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
);





CREATE TABLE order_invoice (
    id INT PRIMARY KEY IDENTITY,
    order_id INT,
    invoice_number VARCHAR(100),
    invoice_date DATETIME,
    payment_mode VARCHAR(50) CHECK (payment_mode IN ('Card','Wallet','NetBanking', 'UPI', 'EMI','COD')),         --  "Credit Card", "UPI", "COD"
    invoice_amount DECIMAL(10, 2),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);



CREATE TABLE shipment_items (	
    id INT PRIMARY KEY IDENTITY,
    order_item_id INT,
	item_tracking_id VARCHAR(50),
    item_status VARCHAR(50) CHECK(item_status IN ('Pending','Cancelled', 'InTransit', 'Delivered')),           -- e.g., "Pending", "In Transit", "Delivered"
	shipment_date DATETIME,
	delivered_date DATETIME,
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);


CREATE TABLE order_returns (
    id INT PRIMARY KEY IDENTITY,
    order_item_id INT,
    return_date DATETIME,
    return_reason VARCHAR(255),
    return_status VARCHAR(50) CHECK(return_status IN ('Requested', 'Approved', 'Rejected', 'Completed')), -- e.g., Requested, Approved, Rejected, Completed
    refund_amount DECIMAL(10,2), 
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);