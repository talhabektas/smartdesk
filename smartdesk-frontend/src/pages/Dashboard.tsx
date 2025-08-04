import React from 'react';
import { useAuthStore } from '../store/authStore';
import { UserRole } from '../types';

// Role-based dashboard components
import AdminDashboard from '../components/dashboard/AdminDashboard';
import ManagerDashboard from '../components/dashboard/ManagerDashboard';
import AgentDashboard from '../components/dashboard/AgentDashboard';
import CustomerDashboard from '../components/dashboard/CustomerDashboard';

const Dashboard: React.FC = () => {
  const { user } = useAuthStore();

  // Render appropriate dashboard based on user role
  const renderDashboard = () => {
    if (!user) return null;

    switch (user.role) {
      case UserRole.SUPER_ADMIN:
        return <AdminDashboard />;
      case UserRole.MANAGER:
        return <ManagerDashboard />;
      case UserRole.AGENT:
        return <AgentDashboard />;
      case UserRole.CUSTOMER:
        return <CustomerDashboard />;
      default:
        return <CustomerDashboard />; // Default fallback
    }
  };

  return (
    <div className="min-h-full">
      {renderDashboard()}
    </div>
  );
};

export default Dashboard;