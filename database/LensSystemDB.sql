CREATE DATABASE LensSystemDB;
GO

USE LensSystemDB;
GO

CREATE TABLE Users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',

    status VARCHAR(20) NOT NULL CONSTRAINT DF_Users_Status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL  DEFAULT GETDATE(),

    CONSTRAINT CK_Users_Role
        CHECK (role IN ('CUSTOMER', 'ADMIN')),

    CONSTRAINT CK_Users_Status
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

-- device models
CREATE TABLE DeviceModels (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL,
    brand NVARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    model NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    rental_price DECIMAL(12,0) NOT NULL,
    deposit_amount DECIMAL(12,0) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_DeviceModels_Brand_Model 
        UNIQUE (brand, model), 

    CONSTRAINT CK_DeviceModels_RentalPrice
        CHECK (rental_price >= 0),

    CONSTRAINT CK_DeviceModels_Deposit
        CHECK (deposit_amount >= 0)
);
GO

-- devices
CREATE TABLE Devices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    device_model_id INT NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Devices_DeviceModels
        FOREIGN KEY (device_model_id)
        REFERENCES DeviceModels(id),

    CONSTRAINT CK_Devices_Status
        CHECK (status IN ('AVAILABLE', 'RENTING'))
);
GO


-- rental orders
CREATE TABLE RentalOrders (
    id INT IDENTITY(1,1) PRIMARY KEY,

    user_id INT NOT NULL,

    customer_name NVARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        
    subtotal DECIMAL(12,0) NOT NULL DEFAULT 0,
    deposit_total DECIMAL(12,0) NOT NULL DEFAULT 0,
    total_payable DECIMAL(12,0) NOT NULL DEFAULT 0,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_RentalOrders_Users
        FOREIGN KEY (user_id)
        REFERENCES Users(id),

    CONSTRAINT CK_RentalOrders_Status
        CHECK (status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'ACTIVE',
            'COMPLETED'
        )),

    CONSTRAINT CK_RentalOrders_Subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT CK_RentalOrders_DepositTotal
        CHECK (deposit_total >= 0),

    CONSTRAINT CK_RentalOrders_TotalAmount
        CHECK (total_payable >= 0)
);
GO


-- rental items
CREATE TABLE RentalItems (
    id INT IDENTITY(1,1) PRIMARY KEY,

    rental_order_id INT NOT NULL,
    device_model_id INT NOT NULL,
    assigned_device_id INT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INT NOT NULL,

    rental_price DECIMAL(12,0) NOT NULL,
    deposit_amount DECIMAL(12,0) NOT NULL,
    subtotal DECIMAL(12,0) NOT NULL,

    CONSTRAINT FK_RentalItems_RentalOrders
        FOREIGN KEY (rental_order_id)
        REFERENCES RentalOrders(id),

    CONSTRAINT FK_RentalItems_DeviceModels
        FOREIGN KEY (device_model_id)
        REFERENCES DeviceModels(id),

    CONSTRAINT FK_RentalItems_Devices
        FOREIGN KEY (assigned_device_id)
        REFERENCES Devices(id),

    CONSTRAINT CK_RentalItems_Date
        CHECK (end_date > start_date),

    CONSTRAINT CK_RentalItems_Duration
        CHECK (duration > 0),

    CONSTRAINT CK_RentalItems_RentalPrice
        CHECK (rental_price >= 0),

    CONSTRAINT CK_RentalItems_DepositAmount
        CHECK (deposit_amount >= 0),

    CONSTRAINT CK_RentalItems_Subtotal
        CHECK (subtotal >= 0)
);
GO


-- payment
ALTER TABLE RentalOrders
ADD payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID';
GO

ALTER TABLE RentalOrders
ADD CONSTRAINT CK_RentalOrders_PaymentMethod
    CHECK (payment_method IN ('CASH'));
GO

ALTER TABLE RentalOrders
ADD CONSTRAINT CK_RentalOrders_PaymentStatus
    CHECK (payment_status IN ('UNPAID', 'PAID'));
GO


-- cart items
CREATE TABLE CartItems (
    id INT IDENTITY(1,1) PRIMARY KEY,

    user_id INT NOT NULL,
    device_model_id INT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INT NOT NULL,

    rental_price DECIMAL(12,0) NOT NULL,
    deposit_amount DECIMAL(12,0) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_CartItems_Users
        FOREIGN KEY (user_id)
        REFERENCES Users(id),

    CONSTRAINT FK_CartItems_DeviceModels
        FOREIGN KEY (device_model_id)
        REFERENCES DeviceModels(id),

    CONSTRAINT CK_CartItems_Date
        CHECK (end_date > start_date),

    CONSTRAINT CK_CartItems_Duration
        CHECK (duration > 0),

    CONSTRAINT CK_CartItems_RentalPrice
        CHECK (rental_price >= 0),

    CONSTRAINT CK_CartItems_DepositAmount
        CHECK (deposit_amount >= 0)
);
GO

CREATE INDEX IX_CartItems_UserId ON CartItems(user_id);
GO

ALTER TABLE Users
ADD address NVARCHAR(255) NULL;
GO

ALTER TABLE RentalOrders
ADD note NVARCHAR(1000) NULL;
GO

CREATE TABLE RentalHandovers (
    id INT IDENTITY(1,1) PRIMARY KEY,

    rental_order_id INT NOT NULL,

    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    delivery_address NVARCHAR(255) NULL,

    failure_reason VARCHAR(50) NULL,
    note NVARCHAR(1000) NULL,

    confirmed_at DATETIME2 NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_RentalHandovers_Order
        UNIQUE (rental_order_id),

    CONSTRAINT FK_RentalHandovers_RentalOrders
        FOREIGN KEY (rental_order_id)
        REFERENCES RentalOrders(id),

    CONSTRAINT CK_RentalHandovers_Method
        CHECK (method IN ('DELIVERY', 'STORE_PICKUP')),

    CONSTRAINT CK_RentalHandovers_Status
        CHECK (status IN (
            'PENDING',
            'DELIVERING',
            'FAILED',
            'COMPLETED'
        ))
);
GO

ALTER TABLE RentalOrders
ADD deposit_refund_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REFUNDED';
GO

ALTER TABLE RentalOrders
ADD CONSTRAINT CK_RentalOrders_DepositRefundStatus
    CHECK (deposit_refund_status IN ('NOT_REFUNDED', 'REFUNDED'));
GO

INSERT INTO Users (username, password, full_name, email, phone, role)
VALUES ('admin', 'admin', N'Administrator', 'admin@lens.com', '0900000000', 'ADMIN');
GO