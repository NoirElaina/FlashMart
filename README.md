# FlashMart

FlashMart is a full-stack shopping mall learning project built around a real e-commerce growth path.

It is not only a flash-sale demo. The goal of this repository is to evolve into a complete mall system that covers:

- user authentication and authorization
- product browsing and product detail
- shopping cart and order flow
- backend engineering standards
- later extensions such as RBAC, Redis, search, file upload, Docker, and microservice preparation

## Project Description

FlashMart is designed as a practical Java backend learning project with a matching Vue frontend.

The project focuses on building a complete single-application mall first, then gradually improving it with more professional backend capabilities. Instead of jumping straight to microservices, the repository follows a more realistic path:

1. build a usable mall core flow
2. standardize backend structure and coding style
3. complete cart, checkout, and order modules
4. upgrade authentication and platform capabilities
5. prepare for more advanced architecture later

## Tech Stack

### Backend

- Java 17
- Spring Boot
- MyBatis / MyBatis-Plus
- MySQL
- JWT
- Jakarta Validation

### Frontend

- Vue 3
- TypeScript
- Vite
- Pinia
- Tailwind CSS

## Current Status

The repository already has these foundations:

- homepage, login page, product detail page, and cart page
- backend modules for `auth`, `user`, `product`, and `cart`
- unified `Result<T>` response structure
- global exception handling
- environment-based configuration
- JWT-based login interceptor

The current development priority is:

`cart -> checkout -> order`

That means the project is now moving from engineering cleanup into the main transaction flow.

## Repository Structure

```text
FlashMart
├── docs
├── FlashMart-Web
├── src/main/java/org/example/flashmart
│   ├── auth
│   ├── cart
│   ├── common
│   ├── product
│   └── user
├── src/main/resources
└── pom.xml
```

## Key Documents

- [Backend learning roadmap](./docs/FLASHMART_BACKEND_LEARNING_ROADMAP.md)
- [Execution plan](./docs/FLASHMART_NEXT_STEPS_EXECUTION_PLAN.md)

## Development Notes

- The project currently follows a domain-oriented package structure.
- Naming is being unified around `DO / DTO / VO / Query / Result`.
- The backend is being improved step by step toward enterprise-style standards.

## Next Step

The next milestone is to finish the shopping cart closed loop:

- complete cart update and delete operations
- connect frontend cart actions to backend APIs
- add stock and limit validation
- prepare for checkout and order creation
