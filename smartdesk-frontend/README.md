# SmartDesk CRM Frontend

A modern, Apple-inspired React TypeScript application for customer support management.

## 🚀 Features

- **Apple-inspired Design**: Clean, minimalist UI with smooth animations
- **Real-time Communication**: WebSocket integration for live updates
- **Role-based Access Control**: Secure authentication with JWT tokens
- **Responsive Design**: Works perfectly on desktop, tablet, and mobile
- **Dark Mode Support**: Beautiful dark theme option
- **Modern Tech Stack**: React 18, TypeScript, Tailwind CSS
- **Performance Optimized**: Fast loading with efficient state management

## 🛠 Tech Stack

- **Frontend Framework**: React 18 with TypeScript
- **Styling**: Tailwind CSS with custom Apple-inspired design system
- **State Management**: Zustand for global state
- **API Client**: Axios with interceptors
- **Real-time**: WebSocket with STOMP protocol
- **Charts**: Recharts for data visualization
- **Forms**: React Hook Form with validation
- **Routing**: React Router v6
- **Icons**: Lucide React
- **Notifications**: React Hot Toast

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smartdesk-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Environment setup**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Start development server**
   ```bash
   npm start
   ```

The application will be available at [http://localhost:3000](http://localhost:3000)

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_URL` | Backend API URL | `http://localhost:8067/api/v1` |
| `REACT_APP_WS_URL` | WebSocket URL | `http://localhost:8067/ws` |
| `REACT_APP_APP_NAME` | Application name | `SmartDesk CRM` |
| `REACT_APP_ENABLE_MOCK_DATA` | Enable mock data | `true` |

### API Integration

The frontend connects to the SmartDesk backend API running on port 8067. Make sure the backend is running before starting the frontend.

## 🎨 Design System

### Colors
- **Primary**: Apple Blue (#007AFF)
- **Success**: Apple Green (#34C759)  
- **Warning**: Apple Orange (#FF9500)
- **Danger**: Apple Red (#FF3B30)
- **Gray Scale**: Apple's neutral colors

### Typography
- **Font**: Inter (Apple's recommended system font alternative)
- **Scale**: Display, Title-1, Title-2, Title-3, Headline, Body, Caption

### Components
- **Cards**: Glass morphism effects with subtle shadows
- **Buttons**: Gradient backgrounds with shimmer effects
- **Inputs**: Clean borders with focus states
- **Animations**: Smooth, Apple-like transitions

## 🔐 Authentication

### Demo Credentials
```
Admin: admin@erdemir.com.tr / password123
Manager: mehmet.yilmaz@erdemir.com.tr / password123
Agent: ali.kaya@erdemir.com.tr / password123
```

### User Roles
- **Super Admin**: Full system access
- **Manager**: Department management + reporting
- **Agent**: Ticket handling + customer support
- **Customer**: Ticket creation + tracking

## 📱 Features by Role

### Dashboard
- **All Users**: Personal metrics and recent activity
- **Managers**: Team performance and analytics
- **Admins**: System-wide statistics

### Ticket Management
- Create, assign, and track support tickets
- Real-time status updates
- File attachments and comments
- SLA monitoring and escalation

### Customer Management
- Customer profiles and interaction history
- Satisfaction tracking
- Communication logs

### Analytics (Manager+)
- Performance metrics and trends
- Custom reports and exports
- Team productivity analysis

## 🔌 WebSocket Integration

Real-time features powered by WebSocket:
- Live ticket updates
- New comment notifications
- Status change alerts
- User presence indicators

## 📊 State Management

### Zustand Stores
- **Auth Store**: User authentication and permissions
- **Notification Store**: Real-time notifications
- **App Store**: Global application state

### Local Storage
- Authentication tokens (JWT)
- User preferences
- Theme settings

## 🎯 Performance

### Optimizations
- Code splitting with React.lazy()
- Image optimization and lazy loading
- Efficient re-renders with React.memo()
- Virtual scrolling for large lists
- Debounced search inputs

### Bundle Analysis
```bash
npm run build
npm install -g serve
serve -s build
```

## 🧪 Testing

```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

## 🚀 Deployment

### Development Build
```bash
npm start
```

### Production Build
```bash
npm run build
npm install -g serve
serve -s build
```

### Docker (Optional)
```bash
docker build -t smartdesk-frontend .
docker run -p 3000:3000 smartdesk-frontend
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Design inspiration from Apple's Human Interface Guidelines
- Icons by Lucide
- Charts by Recharts
- Animations inspired by Apple's motion design

---

Built with ❤️ using React and TypeScript