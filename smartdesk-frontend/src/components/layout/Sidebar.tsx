import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { clsx } from 'clsx';
import { 
  Home, 
  Ticket, 
  Users, 
  Building2, 
  BarChart3, 
  Settings, 
  HelpCircle,
  ChevronLeft,
  ChevronRight,
  Bell,
  Search,
  UserCircle,
  Globe,
} from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { UserRole } from '../../types';

interface SidebarProps {
  isCollapsed?: boolean;
  onToggleCollapse?: () => void;
}

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: number;
  requiredRole?: UserRole;
}

const navigation: NavigationItem[] = [
  { name: 'Dashboard', href: '/', icon: Home },
  { name: 'Tickets', href: '/tickets', icon: Ticket, badge: 5 },
  { name: 'Users', href: '/users', icon: UserCircle },
  { name: 'Customers', href: '/customers', icon: Users, requiredRole: UserRole.AGENT },
  { name: 'Companies', href: '/companies', icon: Building2, requiredRole: UserRole.MANAGER },
  { name: 'Analytics', href: '/analytics', icon: BarChart3, requiredRole: UserRole.AGENT },
  { name: 'API Test', href: '/api-test', icon: Globe, requiredRole: UserRole.AGENT },
  { name: 'Knowledge Base', href: '/knowledge', icon: HelpCircle },
  { name: 'Settings', href: '/settings', icon: Settings, requiredRole: UserRole.MANAGER },
];

const Sidebar: React.FC<SidebarProps> = ({ 
  isCollapsed = false, 
  onToggleCollapse 
}) => {
  const location = useLocation();
  const { user, hasPermission } = useAuthStore();
  const [searchQuery, setSearchQuery] = useState('');

  const filteredNavigation = navigation.filter(item => 
    !item.requiredRole || hasPermission(item.requiredRole)
  );

  return (
    <div className={clsx(
      'flex flex-col h-screen bg-white border-r border-gray-100 transition-all duration-300 ease-apple',
      isCollapsed ? 'w-16' : 'w-64'
    )}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        {!isCollapsed && (
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-gradient-to-r from-primary-500 to-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">SD</span>
            </div>
            <h1 className="text-headline font-semibold text-gray-900">
              SmartDesk
            </h1>
          </div>
        )}
        
        <button
          onClick={onToggleCollapse}
          className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors duration-200"
        >
          {isCollapsed ? (
            <ChevronRight className="w-4 h-4 text-gray-500" />
          ) : (
            <ChevronLeft className="w-4 h-4 text-gray-500" />
          )}
        </button>
      </div>

      {/* Search */}
      {!isCollapsed && (
        <div className="p-4 border-b border-gray-100">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border-0 rounded-lg text-sm placeholder-gray-500 focus:bg-white focus:ring-2 focus:ring-primary-500 focus:ring-offset-0 transition-all duration-200"
            />
          </div>
        </div>
      )}

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        {filteredNavigation.map((item) => {
          const isActive = location.pathname === item.href;
          
          return (
            <Link
              key={item.name}
              to={item.href}
              className={clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group relative',
                isActive ? [
                  'bg-primary-50',
                  'text-primary-700',
                  'shadow-sm',
                ] : [
                  'text-gray-600',
                  'hover:bg-gray-50',
                  'hover:text-gray-900',
                ],
                isCollapsed && 'justify-center'
              )}
            >
              {/* Active indicator */}
              {isActive && (
                <div className="absolute left-0 top-1/2 transform -translate-y-1/2 w-1 h-6 bg-primary-600 rounded-r-full" />
              )}
              
              <item.icon className={clsx(
                'w-5 h-5 flex-shrink-0 transition-colors duration-200',
                isActive ? 'text-primary-600' : 'text-gray-400 group-hover:text-gray-600'
              )} />
              
              {!isCollapsed && (
                <>
                  <span className="flex-1">{item.name}</span>
                  {item.badge && (
                    <span className="bg-primary-100 text-primary-700 text-xs font-medium px-2 py-0.5 rounded-full">
                      {item.badge}
                    </span>
                  )}
                </>
              )}
              
              {/* Tooltip for collapsed state */}
              {isCollapsed && (
                <div className="absolute left-full ml-2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50 whitespace-nowrap">
                  {item.name}
                  {item.badge && (
                    <span className="ml-2 bg-primary-600 text-white px-1.5 py-0.5 rounded-full text-xs">
                      {item.badge}
                    </span>
                  )}
                </div>
              )}
            </Link>
          );
        })}
      </nav>

      {/* User Profile */}
      <div className="p-4 border-t border-gray-100">
        <div className={clsx(
          'flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors duration-200 cursor-pointer',
          isCollapsed && 'justify-center'
        )}>
          <div className="relative">
            <div className="w-8 h-8 bg-gradient-to-r from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
              {user?.avatarUrl ? (
                <img 
                  src={user.avatarUrl} 
                  alt={user.firstName}
                  className="w-full h-full rounded-full object-cover"
                />
              ) : (
                <UserCircle className="w-5 h-5 text-white" />
              )}
            </div>
            <div className="absolute -top-1 -right-1 w-3 h-3 bg-success-500 border-2 border-white rounded-full" />
          </div>
          
          {!isCollapsed && user && (
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">
                {user.firstName} {user.lastName}
              </p>
              <p className="text-xs text-gray-500 truncate">
                {user.role.toLowerCase().replace('_', ' ')}
              </p>
            </div>
          )}
          
          {!isCollapsed && (
            <Bell className="w-4 h-4 text-gray-400" />
          )}
        </div>
      </div>
    </div>
  );
};

export default Sidebar;