# Lens Camera Rental

<p align="center">
  <img src="./docs/images/lens-index-page.png" alt="Lens Camera Rental" width="100%">
</p>

<p align="center">A camera rental management system built with Jakarta EE.</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-000000?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Jakarta%20EE-0B5FFF?style=for-the-badge&logo=jakartaee&logoColor=white" alt="Jakarta EE">
  <img src="https://img.shields.io/badge/JSF-1F1F1F?style=for-the-badge" alt="JSF">
  <img src="https://img.shields.io/badge/EJB-6C2DC7?style=for-the-badge" alt="EJB">
  <img src="https://img.shields.io/badge/JPA-007396?style=for-the-badge" alt="JPA">
  <img src="https://img.shields.io/badge/SQL%20Server-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white" alt="SQL Server">
  <img src="https://img.shields.io/badge/GlassFish-1B6AC9?style=for-the-badge" alt="GlassFish">
  <img src="https://img.shields.io/badge/NetBeans-1B6AC6?style=for-the-badge&logo=apache-netbeans-ide&logoColor=white" alt="Apache NetBeans">
  <img src="https://img.shields.io/badge/Ant-A81C7D?style=for-the-badge" alt="Apache Ant">
</p>

---

## Overview

Lens Camera Rental supports camera discovery, rental requests, and order management. Customers can select rental dates, check availability, build a cart, choose pickup or delivery, and track orders. Admin tools cover user, device model, physical device, and rental order management.

## Core Capabilities

| Customer | Admin |
|---|---|
| Browse the camera catalog and view details | Manage users, device models, and physical devices |
| Select a rental period and check availability | Review, approve, or reject rental orders |
| Manage cart items and proceed through checkout | View the dashboard and monitor rentals |
| Choose store pickup or delivery | Manage rental orders |
| Track rental orders | |

## Architecture

The application is organized into presentation, controller, service, and persistence layers:

```text
XHTML / JSF
    ↓
Controller / Managed Bean
    ↓
Service
    ↓
EJB Facade
    ↓
JPA Entity
    ↓
SQL Server
```

## Rental Lifecycle

```text
PENDING
   ├──→ REJECTED
   ↓
APPROVED
   ↓
ACTIVE
   ↓
COMPLETED
```

## Availability

Rental dates use the half-open interval **[startDate, endDate)**. A requested interval conflicts with an existing rental when:

```text
existing.startDate < requestedEndDate
AND
existing.endDate > requestedStartDate
```

Orders in `PENDING`, `APPROVED`, and `ACTIVE` statuses consume rental capacity. `REJECTED` and `COMPLETED` orders do not.

## Rental Cart

The cart is a temporary selection layer; a `RentalOrder` is created only at checkout. Rental price and deposit are captured when an item is added to the cart.

```text
Rental Subtotal = Duration × Rental Price
Deposit         = Deposit Amount
Item Total      = Rental Subtotal + Deposit
Total Payable   = Rental Subtotal + Deposit Total
```

## Domain Model

| Entity | Responsibility |
|---|---|
| `Users` | Customer and administrator accounts |
| `DeviceModels` | Rentable camera and equipment models |
| `Devices` | Physical equipment units |
| `RentalOrders` | Customer rental requests and order status |
| `RentalItems` | Items and rental periods within an order |

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java |
| Enterprise platform | Jakarta EE |
| Web presentation | JSF / Facelets |
| Business components | EJB |
| Persistence | JPA |
| Database | Microsoft SQL Server |
| Application server | GlassFish |
| IDE / build | Apache NetBeans / Apache Ant |

## Documentation

| Guide | Link |
|---|---|
| Configuration Guide | [docs/configuration_guide.txt](./docs/configuration_guide.txt) |
| Login Accounts | [docs/login_account.txt](./docs/login_account.txt) |

## Project Setup

1. Prepare the SQL Server database using the project database resources.
2. Configure the required GlassFish resources.
3. Configure the persistence unit for the database connection.
4. Open the project in Apache NetBeans.
5. Build and deploy with Apache Ant and GlassFish.

Refer to the [Configuration Guide](./docs/configuration_guide.txt) for environment-specific details.

## Screenshots

<p align="center">
  <img src="./docs/images/lens-index-page.png" alt="Lens Camera Rental customer catalog" width="100%">
</p>

## Development Status

Lens Camera Rental is actively under development. The long-term ambition is to grow it into a dependable, data-informed platform for discovering and managing camera rentals. Current priorities include strengthening rental availability and order-flow quality, with automated tests and rental-demand and equipment-utilization reporting as future improvements.

## Author

**Nguyen Duong Ngoc Han** · [GitHub](https://github.com/dgwhan)
