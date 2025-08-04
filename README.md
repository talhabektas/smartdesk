# SmartDesk CRM

A comprehensive Customer Relationship Management (CRM) system built with Spring Boot and React, designed for modern businesses to manage customers, tickets, companies, and user interactions efficiently.

## 🚀 Features

### Core Functionality
- **Customer Management** - Complete CRUD operations for customer records
- **Ticket System** - Advanced ticketing with SLA tracking and status management
- **Company Management** - Multi-company support with department hierarchies
- **User Management** - Role-based access control (SUPER_ADMIN, MANAGER, AGENT, CUSTOMER)
- **Real-time Chat** - WebSocket-powered messaging system
- **Analytics Dashboard** - Comprehensive reporting and analytics

### Advanced Features
- **Department Management** - Organize users within companies by departments
- **Role-based Permissions** - Granular access control across all modules
- **Search & Filtering** - Advanced search capabilities with pagination
- **Real-time Notifications** - Live updates for tickets and messages
- **Responsive Design** - Modern UI with Tailwind CSS
- **Multi-language Support** - Turkish and English interface

## 🛠️ Technology Stack

### Backend
- **Java 17** - Modern Java features and performance
- **Spring Boot 3.1.4** - Enterprise-grade framework
- **Spring Security** - JWT-based authentication
- **Spring Data JPA** - Database abstraction layer
- **PostgreSQL** - Robust relational database
- **WebSocket** - Real-time communication
- **Maven** - Dependency management

### Frontend
- **React 18** - Modern React with hooks
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS** - Utility-first CSS framework
- **Headless UI** - Accessible UI components
- **Zustand** - Lightweight state management
- **React Hot Toast** - Beautiful notifications
- **Lucide React** - Modern icon library

## 📋 Prerequisites

- **Java 17** or higher
- **Node.js 18** or higher
- **PostgreSQL 13** or higher
- **Maven 3.8** or higher
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd smartdesk-crm
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE smartdesk_db;

-- Create user (optional)
CREATE USER smartdesk_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE smartdesk_db TO smartdesk_user;
```

### 3. Backend Configuration
```bash
# Navigate to backend directory
cd smartdesk-backend

# Update application.properties
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit database connection settings
nano src/main/resources/application.properties
```

**application.properties:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smartdesk_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
app.jwt.secret=your-secret-key
app.jwt.expiration=86400000

# Server Configuration
server.port=8067
```

### 4. Start Backend
```bash
# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

### 5. Frontend Setup
```bash
# Navigate to frontend directory
cd smartdesk-frontend

# Install dependencies
npm install

# Start development server
npm start
```

### 6. Access the Application
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8067
- **API Documentation:** http://localhost:8067/swagger-ui.html

## 👤 Default Users

The system comes with pre-configured users for testing:

### Super Admin

- **Role:** SUPER_ADMIN
- **Permissions:** Full system access

### Manager

- **Role:** MANAGER
- **Permissions:** Company and user management

### Agent

- **Role:** AGENT
- **Permissions:** Ticket and customer management

### Customer

- **Role:** CUSTOMER
- **Permissions:** View own tickets and profile

## 🏗️ Architecture

### Backend Architecture
```
src/
├── main/
│   ├── java/com/example/smartdeskbackend/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── enums/          # Enumerations
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.properties
│       ├── data.sql        # Initial data
│       └── db/migration/   # Database migrations
```

### Frontend Architecture
```
src/
├── components/
│   ├── ui/                 # Reusable UI components
│   ├── layout/             # Layout components
│   └── common/             # Common components
├── pages/                  # Page components
├── store/                  # State management
├── types/                  # TypeScript definitions
├── utils/                  # Utility functions
└── App.tsx                 # Main application
```

## 🔑 Key Features

### User Roles & Permissions
- **SUPER_ADMIN:** Complete system control
- **MANAGER:** Company and department management
- **AGENT:** Customer and ticket handling
- **CUSTOMER:** Self-service portal

### Ticket Management
- Priority levels (Low, Medium, High, Critical)
- Status tracking (New, In Progress, Resolved, Closed)
- SLA deadline calculation
- Automated notifications
- Ticket history and comments

### Company & Department Structure
- Multi-company support
- Hierarchical department organization
- Role-based access within companies
- Department-specific user assignments

## 🌐 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/refresh` - Token refresh

### Users
- `GET /api/v1/users` - List users
- `POST /api/v1/users` - Create user
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user

### Companies
- `GET /api/v1/companies` - List companies
- `POST /api/v1/companies` - Create company
- `PUT /api/v1/companies/{id}` - Update company
- `DELETE /api/v1/companies/{id}` - Delete company

### Departments
- `GET /api/v1/departments/company/{companyId}` - List departments
- `POST /api/v1/departments` - Create department
- `PUT /api/v1/departments/{id}` - Update department
- `DELETE /api/v1/departments/{id}` - Delete department

### Tickets
- `GET /api/v1/tickets` - List tickets
- `POST /api/v1/tickets` - Create ticket
- `PUT /api/v1/tickets/{id}` - Update ticket
- `GET /api/v1/tickets/{id}/history` - Ticket history

## 🔧 Development

### Running Tests
```bash
# Backend tests
cd smartdesk-backend
mvn test

# Frontend tests
cd smartdesk-frontend
npm test
```

### Building for Production
```bash
# Backend
mvn clean package

# Frontend
npm run build
```

### Database Migrations
The application uses Flyway for database migrations. Migration files are located in `src/main/resources/db/migration/`.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful frontend library
- Tailwind CSS for the utility-first styling approach
- All open-source contributors who made this project possible

## 📞 Support

If you have any questions or need help, please:
- Open an issue on GitHub
- Contact us at support@smartdesk.com
- Check our [documentation](https://docs.smartdesk.com)

---

**Built with ❤️ using Spring Boot and React**
