import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { useAuthStore, initializeAuth } from './store/authStore';
import { webSocketService } from './services/websocket';
import AuthDebugPanel from './components/debug/AuthDebugPanel';

// Layout Components
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Tickets from './pages/Tickets';
import TicketDetail from './pages/TicketDetail';
import Customers from './pages/Customers';
import Companies from './pages/Companies';
import Analytics from './pages/Analytics';
import Settings from './pages/Settings';
import ApiTest from './pages/ApiTest';
import Users from './pages/Users';

// Protected Route Component
interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const { isAuthenticated, hasPermission, user, isLoading } = useAuthStore();
  
  console.log('🛡️ ProtectedRoute check:', {
    isAuthenticated,
    isLoading,
    user: user?.email,
    requiredRole,
    hasPermission: requiredRole ? hasPermission(requiredRole as any) : 'N/A'
  });

  // Show loading while authentication is being determined
  if (isLoading) {
    return <LoadingScreen />;
  }

  if (!isAuthenticated) {
    console.log('❌ Not authenticated, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasPermission(requiredRole as any)) {
    console.log('❌ Insufficient permissions, redirecting to home');
    return <Navigate to="/" replace />;
  }

  console.log('✅ Access granted');
  return <>{children}</>;
};

// Main Layout Component
const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

  const toggleSidebarCollapse = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed);
  };

  const toggleMobileSidebar = () => {
    setIsMobileSidebarOpen(!isMobileSidebarOpen);
  };

  return (
    <div className="h-screen flex bg-gray-50">
      {/* Desktop Sidebar */}
      <div className="hidden lg:block">
        <Sidebar 
          isCollapsed={isSidebarCollapsed} 
          onToggleCollapse={toggleSidebarCollapse}
        />
      </div>

      {/* Mobile Sidebar Overlay */}
      {isMobileSidebarOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div 
            className="absolute inset-0 bg-black opacity-50"
            onClick={toggleMobileSidebar}
          />
          <div className="absolute left-0 top-0 h-full">
            <Sidebar onToggleCollapse={toggleMobileSidebar} />
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        <Header 
          onToggleSidebar={toggleMobileSidebar}
          isSidebarOpen={isMobileSidebarOpen}
        />
        <main className="flex-1 overflow-auto">
          <div className="container-apple py-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

// Loading Component
const LoadingScreen: React.FC = () => (
  <div className="min-h-screen bg-gray-50 flex items-center justify-center">
    <div className="text-center">
      <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary-500 to-primary-600 rounded-2xl shadow-primary mb-4 animate-pulse">
        <span className="text-white font-bold text-xl">SD</span>
      </div>
      <div className="w-8 h-8 border-2 border-primary-200 border-t-primary-600 rounded-full animate-apple-spin mx-auto mb-4" />
      <p className="text-gray-600">Loading SmartDesk...</p>
    </div>
  </div>
);

// Create Query Client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

// Main App Component
const App: React.FC = () => {
  const { isAuthenticated, user } = useAuthStore();
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const initialize = async () => {
      try {
        console.log('🚀 App initializing auth...');
        await initializeAuth();
        console.log('✅ Auth initialization completed');
      } catch (error) {
        console.error('❌ Failed to initialize auth:', error);
      } finally {
        setIsInitializing(false);
      }
    };

    initialize();
  }, []);

  // Initialize WebSocket connection when user is authenticated
  useEffect(() => {
    if (isAuthenticated && user?.id) {
      console.log('🔌 Initializing WebSocket connection for user:', user.id);
      webSocketService.connect(user.id);
    } else {
      console.log('🔌 Disconnecting WebSocket');
      webSocketService.disconnect();
    }

    return () => {
      webSocketService.disconnect();
    };
  }, [isAuthenticated, user?.id]);

  if (isInitializing) {
    return <LoadingScreen />;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <Router 
        future={{
          v7_startTransition: true,
          v7_relativeSplatPath: true
        }}
      >
        <div className="App theme-light">
          <Routes>
            {/* Public Routes */}
            <Route 
              path="/login" 
              element={
                isAuthenticated ? (
                  <Navigate to="/" replace />
                ) : (
                  <Login />
                )
              } 
            />

            <Route 
              path="/register" 
              element={
                isAuthenticated ? (
                  <Navigate to="/" replace />
                ) : (
                  <Register />
                )
              } 
            />

            {/* Protected Routes */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <Dashboard />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/tickets"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <Tickets />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/tickets/:id"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <TicketDetail />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/customers"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <Customers />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/companies"
              element={
                <ProtectedRoute requiredRole="MANAGER">
                  <AppLayout>
                    <Companies />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/analytics"
              element={
                <ProtectedRoute requiredRole="AGENT">
                  <AppLayout>
                    <Analytics />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/settings"
              element={
                <ProtectedRoute requiredRole="MANAGER">
                  <AppLayout>
                    <Settings />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/api-test"
              element={
                <ProtectedRoute requiredRole="AGENT">
                  <AppLayout>
                    <ApiTest />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path="/users"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <Users />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            {/* Catch all route */}
            <Route 
              path="*" 
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <div className="text-center py-16">
                      <h1 className="text-title-1 text-gray-900 mb-4">
                        Page Not Found
                      </h1>
                      <p className="text-body text-gray-600 mb-8">
                        The page you're looking for doesn't exist.
                      </p>
                      <Navigate to="/" replace />
                    </div>
                  </AppLayout>
                </ProtectedRoute>
              } 
            />
          </Routes>

          {/* Debug Panel (development only) */}
          <AuthDebugPanel />

          {/* Global Toast Notifications */}
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: 'white',
                color: '#374151',
                border: '1px solid #E5E7EB',
                borderRadius: '12px',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                fontSize: '14px',
                padding: '16px',
              },
              success: {
                iconTheme: {
                  primary: '#10B981',
                  secondary: '#FFFFFF',
                },
              },
              error: {
                iconTheme: {
                  primary: '#EF4444',
                  secondary: '#FFFFFF',
                },
              },
            }}
          />
        </div>
      </Router>
    </QueryClientProvider>
  );
};

export default App;