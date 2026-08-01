# ⚡ E-Shop - Premium Electronics E-Commerce Platform

Welcome to **E-Shop**, a full-stack, enterprise-grade e-commerce web application specifically tailored for high-end electronics. This project features a robust **Spring Boot** backend coupled with a modern, dynamic, and responsive **React** frontend (built using Vite and styled with Tailwind CSS).

---

## 🚀 Key Features

*   **🛒 Premium Catalog**: Clean, modern electronics catalog with realistic branding, product descriptions, pricing, and high-quality studio photography.
*   **🛠️ Admin Dashboard**: Full admin capabilities to manage categories, products (add, edit, delete, upload images), orders, and users.
*   **📱 Smooth User Experience**: Equipped with **Lenis Smooth Scroll** and seamless page transitions.
*   **💳 Stripe Payment Integration**: Secure test checkout flow using Stripe integration.
*   **🔒 Secure Authentication**: Robust security model powered by **Spring Security** and **JWT (JSON Web Tokens)** for session management.
*   **📦 Cart & Order Management**: Comprehensive state-managed shopping cart, user order history, and order placement flows.

---

## 🛠️ Technology Stack

### Backend
*   **Framework**: Spring Boot (Java 21)
*   **Security**: Spring Security + JWT Authentication
*   **Database**: H2 (In-memory for development/testing) / JPA / Hibernate
*   **Build Tool**: Maven

### Frontend
*   **Core**: React (Vite)
*   **Styling**: Tailwind CSS + Material UI icons
*   **State Management**: Redux Toolkit (Redux-Thunk)
*   **Scroll Engine**: Lenis Smooth Scroll
*   **HTTP Client**: Axios

---

## 📂 Project Structure

```
E-Shop/
├── sb-ecom/           # Spring Boot Backend Project
│   ├── src/           # Java Source Code & Resources
│   └── pom.xml        # Maven Dependency Config
│
└── ecom-frontend/     # React Vite Frontend Project
    ├── src/           # Components, Redux Store, Styles
    ├── package.json   # NPM Package Dependencies
    └── vite.config.js # Vite Configuration
```

---

## ⚙️ Getting Started

### Prerequisites
*   **Java**: JDK 21 or higher
*   **Node.js**: v18 or higher
*   **Maven**: Installed or using `mvnw` wrapper

### Running the Backend (`sb-ecom`)
1.  Configure environment variables (if any) or edit `application.properties`.
2.  Set your Stripe Secret Key:
    ```bash
    $env:STRIPE_SECRET_KEY="your_stripe_secret_key"
    ```
3.  Run the application:
    ```bash
    ./mvnw.cmd spring-boot:run
    ```
4.  The server will start on `http://localhost:8080`.

### Running the Frontend (`ecom-frontend`)
1.  Navigate to the directory:
    ```bash
    cd ecom-frontend
    ```
2.  Install packages:
    ```bash
    npm install
    ```
3.  Run in development mode:
    ```bash
    npm run dev
    ```
4.  Access the web application at `http://localhost:5173`.

---

## 📧 Contact Information

For inquiries, support, or feedback, feel free to reach out:

*   **Developer**: Mihir Singh
*   **Email**: [mihir304singh@gmail.com](mailto:mihir304singh@gmail.com)
*   **Location**: Mumbai, India
