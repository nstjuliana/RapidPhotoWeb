# RapidPhotoUpload

A high-performance, asynchronous photo upload system capable of handling up to **100 concurrent media uploads** with real-time progress tracking. Built with modern web and mobile technologies, featuring a reactive backend architecture and seamless cross-platform user experience.

## Overview

RapidPhotoUpload is a full-stack application that simulates a high-volume media platform (similar to Google Photos or Drive) where users can upload large batches of images while the application remains fully responsive. The system demonstrates architectural excellence through Domain-Driven Design (DDD), CQRS, and Vertical Slice Architecture (VSA).

### Key Features

- **High-Volume Concurrency:** Supports simultaneous upload of up to 100 photos per user session
- **Asynchronous UI:** Users can navigate and interact with the application while uploads proceed in the background
- **Real-Time Progress:** Individual and batch upload progress indicators with status updates (Uploading, Failed, Complete)
- **Cross-Platform:** Web application (Next.js) and mobile application (React Native/Expo) sharing the same backend API
- **Direct S3 Upload:** Clients upload directly to AWS S3 using presigned URLs, reducing backend load
- **Photo Management:** View, tag, filter, search, and download uploaded photos
- **Secure Authentication:** JWT-based authentication with refresh tokens

## Architecture

The project follows a **monorepo structure** with three main components:

### Backend (Spring Boot + WebFlux)
- **Architecture:** Domain-Driven Design (DDD), CQRS, Vertical Slice Architecture (VSA)
- **Framework:** Spring Boot 3.x with Spring WebFlux (reactive, non-blocking)
- **Database:** PostgreSQL with Spring Data JPA
- **Storage:** AWS S3 for photo storage (presigned URLs for direct client uploads)
- **Authentication:** Spring Security with JWT tokens

### Web Frontend (Next.js)
- **Framework:** Next.js 14+ with App Router and TypeScript
- **UI:** Tailwind CSS + shadcn/ui components
- **State Management:** Zustand (UI state) + TanStack Query (server state)
- **File Upload:** react-dropzone with direct S3 upload

### Mobile Frontend (Expo/React Native)
- **Framework:** Expo (managed workflow) with React Native and TypeScript
- **Navigation:** React Navigation v6+
- **State Management:** Zustand + TanStack Query (shared patterns with web)
- **Image Selection:** expo-image-picker for camera and gallery access

## Project Structure

```
RapidPhotoWeb/
├── backend/              # Spring Boot application
│   ├── src/
│   │   ├── domain/       # Domain layer (DDD)
│   │   ├── application/  # Application layer (CQRS)
│   │   └── infrastructure/ # Infrastructure layer
│   └── pom.xml
├── web/                  # Next.js web application
│   ├── app/              # App Router pages
│   ├── components/       # React components
│   ├── lib/              # Utilities, stores, queries
│   └── package.json
├── mobile/               # Expo/React Native mobile application
│   ├── src/
│   │   ├── screens/      # Screen components
│   │   ├── components/    # Reusable components
│   │   └── lib/          # Utilities, stores, queries
│   └── package.json
└── _docs/                # Project documentation
    ├── Project Overview.md
    ├── user-flow.md
    ├── tech-stack.md
    ├── project-rules.md
    └── phases/            # Development phase documentation
```

## Technology Stack

### Backend
- **Java 17+** with **Spring Boot 3.x**
- **Spring WebFlux** (reactive, non-blocking)
- **Spring Data JPA** with **Hibernate**
- **PostgreSQL** (RDS or local)
- **AWS S3** for object storage
- **Spring Security** with **JWT** authentication

### Web Frontend
- **Next.js 14+** (App Router)
- **TypeScript** (strict mode)
- **React 18+**
- **Tailwind CSS** + **shadcn/ui**
- **Zustand** (state management)
- **TanStack Query** (server state)
- **react-dropzone** (file upload)

### Mobile Frontend
- **Expo SDK** (managed workflow)
- **React Native**
- **TypeScript**
- **React Navigation v6+**
- **Zustand** + **TanStack Query**
- **expo-image-picker**

## Code Conventions

This project follows an **AI-first codebase** approach with strict conventions for modularity and maintainability:

### File Size Limit
- **Maximum 500 lines per file** to maximize AI tool compatibility
- Split large files into smaller, focused modules

### Documentation
- **File headers:** Every file must have a header comment explaining its purpose
- **Function documentation:** All public functions documented with JSDoc/TSDoc
- **Inline comments:** Explain "why" not "what" (code should be self-explanatory)

### Naming Conventions

**Backend (Java):**
- Classes: `PascalCase` (e.g., `PhotoController`, `UploadPhotoCommand`)
- Methods: `camelCase` (e.g., `generatePresignedUrl`)
- Packages: `lowercase.dot.separated` (e.g., `com.rapidphotoupload.domain.photo`)

**Frontend (TypeScript/React):**
- Components: `PascalCase` (e.g., `PhotoCard`, `UploadDropzone`)
- Hooks: `camelCase` with `use` prefix (e.g., `useAuth`, `usePhotoUpload`)
- Files: Match primary export (PascalCase for components, camelCase for utilities)

### Architecture Patterns

**Backend:**
- **Domain-Driven Design (DDD):** Rich domain models with business logic
- **CQRS:** Separate command handlers (mutations) and query handlers (reads)
- **Vertical Slice Architecture (VSA):** Code organized by features, not technical layers

**Frontend:**
- **Component-based:** Modular, reusable React components
- **Feature-based organization:** Group related components, hooks, and utilities by feature
- **State separation:** Zustand for UI state, TanStack Query for server state

## Development Setup

### Prerequisites
- **Java JDK 17+**
- **Node.js 18+** and npm/yarn
- **PostgreSQL** (local or RDS access)
- **AWS Account** with S3 access
- **Git**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RapidPhotoWeb
   ```

2. **Backend Setup**
   ```bash
   cd backend
   # Configure application.yml with database and AWS credentials
   mvn clean install
   mvn spring-boot:run
   ```

3. **Web Frontend Setup**
   ```bash
   cd web
   npm install
   # Configure NEXT_PUBLIC_API_URL in .env.local
   npm run dev
   ```

4. **Mobile Setup**
   ```bash
   cd mobile
   npm install
   # Configure API URL in environment variables
   npx expo start
   ```

## Documentation

Comprehensive documentation is available in the `_docs/` directory:

- **[Project Overview](_docs/Project%20Overview.md)** - Product requirements and goals
- **[User Flow](_docs/user-flow.md)** - Complete user journey and interactions
- **[Tech Stack](_docs/tech-stack.md)** - Detailed technology choices and best practices
- **[Project Rules](_docs/project-rules.md)** - Coding standards, conventions, and architecture patterns
- **[Development Phases](_docs/phases/)** - Iterative development plan (12 phases)

## Performance Benchmarks

- **Concurrency:** Handles 100 concurrent photo uploads (2MB each) within 90 seconds
- **UI Responsiveness:** Application remains fully responsive during peak upload operations
- **Scalability:** Direct S3 uploads reduce backend load and enable horizontal scaling

## Security

- **JWT Authentication:** Short-lived access tokens (15 min) with refresh tokens
- **Secure Storage:** Passwords hashed with BCrypt
- **S3 Security:** Presigned URLs with expiration and restricted permissions
- **CORS:** Configured for web client access
- **Input Validation:** All API endpoints validate input

## Contributing

This project follows strict coding standards. Before contributing:

1. Review [Project Rules](_docs/project-rules.md) for conventions
2. Ensure files do not exceed 500 lines
3. Add comprehensive documentation to all code
4. Follow DDD/CQRS/VSA patterns (backend)
5. Maintain type safety (TypeScript strict mode)

## License

[Add license information here]

---

For detailed information about architecture, development phases, and best practices, see the documentation in `_docs/`.

