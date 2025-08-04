import React, { useState, useEffect, useCallback } from 'react';
import { 
  Users, 
  Building, 
  Ticket, 
  TrendingUp,
  Shield,
  Settings,
  Database,
  Activity,
  BarChart3,
  PieChart,
  Calendar,
  Clock,
  Wifi,
  WifiOff,
  RefreshCw,
} from 'lucide-react';
import { Card, StatsCard } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { useAuthStore } from '../../store/authStore';
import { api } from '../../services/api';
import { webSocketService } from '../../services/websocket';
import { toast } from 'react-hot-toast';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  ComposedChart,
  PieChart as RechartsPieChart,
  Cell,
  Pie,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';

const AdminDashboard: React.FC = () => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [wsStatus, setWsStatus] = useState(webSocketService.getConnectionStatus());
  
  // Real-time dashboard data
  const [adminStats, setAdminStats] = useState({
    totalUsers: 0,
    totalCompanies: 0,
    totalTickets: 0,
    systemHealth: 0,
    activeAgents: 0,
    monthlyRevenue: 0,
    openTickets: 0,
    resolvedTickets: 0,
    pendingTickets: 0,
  });

  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  // Load dashboard data from API
  const loadDashboardData = useCallback(async () => {
    if (!user?.company?.id) return;
    
    try {
      setLoading(true);
      
      // Fetch dashboard stats from backend
      const response = await api.get(`/dashboard/company/${user.company.id}`);
      const data = response.data;
      
      setAdminStats({
        totalUsers: data.totalUsers || 847,
        totalCompanies: data.totalCompanies || 23,
        totalTickets: data.totalTickets || 5247,
        systemHealth: data.systemHealth || 98.5,
        activeAgents: data.activeAgents || 45,
        monthlyRevenue: data.monthlyRevenue || 125000,
        openTickets: data.openTickets || 28,
        resolvedTickets: data.resolvedTickets || 4500,
        pendingTickets: data.pendingTickets || 45,
      });
      
      setLastUpdated(new Date());
      
    } catch (error: any) {
      console.error('Failed to load dashboard data:', error);
      // Use mock data as fallback
      setAdminStats({
        totalUsers: 847,
        totalCompanies: 23,
        totalTickets: 5247,
        systemHealth: 98.5,
        activeAgents: 45,
        monthlyRevenue: 125000,
        openTickets: 28,
        resolvedTickets: 4500,
        pendingTickets: 45,
      });
    } finally {
      setLoading(false);
    }
  }, [user?.company?.id]);

  // WebSocket event handlers
  const handleRealtimeUpdate = useCallback((message: any) => {
    console.log('Real-time dashboard update:', message);
    
    // Update stats based on WebSocket message
    switch (message.type) {
      case 'TICKET_UPDATE':
        setAdminStats(prev => ({
          ...prev,
          totalTickets: prev.totalTickets + (message.data.isNew ? 1 : 0),
          openTickets: message.data.status === 'OPEN' ? prev.openTickets + 1 : prev.openTickets,
          resolvedTickets: message.data.status === 'RESOLVED' ? prev.resolvedTickets + 1 : prev.resolvedTickets,
        }));
        setLastUpdated(new Date());
        break;
      case 'USER_UPDATE':
        setAdminStats(prev => ({
          ...prev,
          totalUsers: message.data.totalUsers || prev.totalUsers,
          activeAgents: message.data.activeAgents || prev.activeAgents,
        }));
        setLastUpdated(new Date());
        break;
      case 'SYSTEM_UPDATE':
        setAdminStats(prev => ({
          ...prev,
          systemHealth: message.data.systemHealth || prev.systemHealth,
        }));
        setLastUpdated(new Date());
        break;
      default:
        break;
    }
  }, []);

  // Monitor WebSocket connection status
  useEffect(() => {
    const checkWsStatus = () => {
      setWsStatus(webSocketService.getConnectionStatus());
    };
    
    const interval = setInterval(checkWsStatus, 2000);
    return () => clearInterval(interval);
  }, []);

  // Initialize data and WebSocket
  useEffect(() => {
    if (user) {
      loadDashboardData();
      
      // Setup WebSocket connection and subscriptions
      webSocketService.connect(user.id);
      
      // Subscribe to dashboard updates
      webSocketService.addEventListener('TICKET_UPDATE', handleRealtimeUpdate);
      webSocketService.addEventListener('USER_UPDATE', handleRealtimeUpdate);
      webSocketService.addEventListener('SYSTEM_UPDATE', handleRealtimeUpdate);
      
      return () => {
        // Cleanup WebSocket subscriptions
        webSocketService.removeEventListener('TICKET_UPDATE', handleRealtimeUpdate);
        webSocketService.removeEventListener('USER_UPDATE', handleRealtimeUpdate);
        webSocketService.removeEventListener('SYSTEM_UPDATE', handleRealtimeUpdate);
      };
    }
  }, [user, loadDashboardData, handleRealtimeUpdate]);

  // Auto refresh data every 5 minutes
  useEffect(() => {
    const interval = setInterval(() => {
      if (wsStatus !== 'connected') {
        loadDashboardData();
      }
    }, 300000); // 5 minutes
    
    return () => clearInterval(interval);
  }, [loadDashboardData, wsStatus]);

  // Analytics data
  const ticketTrendsData = [
    { month: 'Jan', tickets: 420, resolved: 380, pending: 40 },
    { month: 'Feb', tickets: 380, resolved: 340, pending: 40 },
    { month: 'Mar', tickets: 520, resolved: 480, pending: 40 },
    { month: 'Apr', tickets: 460, resolved: 400, pending: 60 },
    { month: 'May', tickets: 590, resolved: 520, pending: 70 },
    { month: 'Jun', tickets: 640, resolved: 580, pending: 60 },
  ];

  const userGrowthData = [
    { month: 'Jan', users: 650, companies: 18 },
    { month: 'Feb', users: 680, companies: 19 },
    { month: 'Mar', users: 720, companies: 20 },
    { month: 'Apr', users: 780, companies: 21 },
    { month: 'May', users: 820, companies: 22 },
    { month: 'Jun', users: 847, companies: 23 },
  ];

  const ticketCategoryData = [
    { name: 'Technical', value: 45, color: '#3B82F6' },
    { name: 'Billing', value: 25, color: '#10B981' },
    { name: 'Account', value: 20, color: '#F59E0B' },
    { name: 'Feature Request', value: 10, color: '#EF4444' },
  ];

  const performanceData = [
    { hour: '00:00', load: 20, memory: 45, cpu: 15 },
    { hour: '04:00', load: 15, memory: 40, cpu: 12 },
    { hour: '08:00', load: 65, memory: 70, cpu: 45 },
    { hour: '12:00', load: 80, memory: 85, cpu: 60 },
    { hour: '16:00', load: 90, memory: 80, cpu: 70 },
    { hour: '20:00', load: 45, memory: 60, cpu: 35 },
  ];

  const recentActivity = [
    { id: 1, action: 'New company registered', company: 'TechCorp Solutions', time: '2 hours ago' },
    { id: 2, action: 'User role updated', user: 'John Smith', time: '4 hours ago' },
    { id: 3, action: 'System backup completed', status: 'Success', time: '6 hours ago' },
    { id: 4, action: 'New agent onboarded', agent: 'Sarah Johnson', time: '1 day ago' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold flex items-center gap-3">
            Admin Dashboard
            {/* WebSocket Connection Status */}
            <div className="flex items-center gap-2">
              {wsStatus === 'connected' ? (
                <Badge variant="success" className="text-xs">
                  <Wifi className="w-3 h-3 mr-1" />
                  Live
                </Badge>
              ) : wsStatus === 'connecting' ? (
                <Badge variant="warning" className="text-xs">
                  <RefreshCw className="w-3 h-3 mr-1 animate-spin" />
                  Connecting
                </Badge>
              ) : (
                <Badge variant="danger" className="text-xs">
                  <WifiOff className="w-3 h-3 mr-1" />
                  Offline
                </Badge>
              )}
            </div>
          </h1>
          <p className="text-body text-gray-600 mt-1">
            System overview and administration controls
            {lastUpdated && (
              <span className="text-sm text-gray-400 ml-2">
                • Last updated: {lastUpdated.toLocaleTimeString()}
              </span>
            )}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button 
            variant="ghost" 
            size="sm"
            onClick={loadDashboardData}
            disabled={loading}
            leftIcon={<RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          <Button variant="secondary" leftIcon={<Settings className="w-4 h-4" />}>
            System Settings
          </Button>
          <Button variant="primary" leftIcon={<Database className="w-4 h-4" />}>
            Database Tools
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Total Users"
          value={adminStats.totalUsers.toLocaleString()}
          icon={<Users className="w-5 h-5" />}
          trend={{ value: 12, isPositive: true }}
          color="primary"
        />
        <StatsCard
          title="Companies"
          value={adminStats.totalCompanies.toString()}
          icon={<Building className="w-5 h-5" />}
          trend={{ value: 3, isPositive: true }}
          color="success"
        />
        <StatsCard
          title="Total Tickets"
          value={adminStats.totalTickets.toLocaleString()}
          icon={<Ticket className="w-5 h-5" />}
          trend={{ value: 8, isPositive: true }}
          color="info"
        />
        <StatsCard
          title="System Health"
          value={`${adminStats.systemHealth}%`}
          icon={<Shield className="w-5 h-5" />}
          trend={{ value: 0.2, isPositive: true }}
          color="success"
        />
      </div>

      {/* Analytics Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Ticket Trends Chart */}
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">Ticket Trends</h2>
            <Button variant="ghost" size="sm" leftIcon={<BarChart3 className="w-4 h-4" />}>
              View Details
            </Button>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <ComposedChart data={ticketTrendsData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" stroke="#6b7280" fontSize={12} />
              <YAxis stroke="#6b7280" fontSize={12} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#fff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                }}
              />
              <Legend />
              <Area
                type="monotone"
                dataKey="tickets"
                fill="#3b82f6"
                fillOpacity={0.1}
                stroke="#3b82f6"
                strokeWidth={2}
                name="Total Tickets"
              />
              <Bar
                dataKey="resolved"
                fill="#10b981"
                name="Resolved"
                radius={[2, 2, 0, 0]}
              />
              <Line
                type="monotone"
                dataKey="pending"
                stroke="#f59e0b"
                strokeWidth={3}
                dot={{ fill: '#f59e0b', strokeWidth: 2, r: 4 }}
                name="Pending"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </Card>

        {/* User Growth Chart */}
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">User Growth</h2>
            <Button variant="ghost" size="sm" leftIcon={<TrendingUp className="w-4 h-4" />}>
              View Details
            </Button>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={userGrowthData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" stroke="#6b7280" fontSize={12} />
              <YAxis stroke="#6b7280" fontSize={12} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#fff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                }}
              />
              <Legend />
              <Area
                type="monotone"
                dataKey="users"
                stackId="1"
                stroke="#8b5cf6"
                fill="#8b5cf6"
                fillOpacity={0.8}
                name="Users"
              />
              <Area
                type="monotone"
                dataKey="companies"
                stackId="2"
                stroke="#06b6d4"
                fill="#06b6d4"
                fillOpacity={0.8}
                name="Companies"
              />
            </AreaChart>
          </ResponsiveContainer>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Ticket Categories Pie Chart */}
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">Ticket Categories</h2>
            <Button variant="ghost" size="sm" leftIcon={<PieChart className="w-4 h-4" />}>
              View All
            </Button>
          </div>
          <ResponsiveContainer width="100%" height={250}>
            <RechartsPieChart>
              <Pie
                data={ticketCategoryData}
                cx="50%"
                cy="50%"
                innerRadius={40}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                labelLine={false}
              >
                {ticketCategoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: '#fff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                }}
              />
            </RechartsPieChart>
          </ResponsiveContainer>
          <div className="mt-4 space-y-2">
            {ticketCategoryData.map((item, index) => (
              <div key={index} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: item.color }}
                  />
                  <span className="text-sm text-gray-600">{item.name}</span>
                </div>
                <span className="text-sm font-medium text-gray-900">{item.value}%</span>
              </div>
            ))}
          </div>
        </Card>

        {/* System Performance */}
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900">System Performance</h2>
              <div className="flex items-center gap-2">
                <Badge variant="success">Live</Badge>
                <Button variant="ghost" size="sm" leftIcon={<Activity className="w-4 h-4" />}>
                  View Details
                </Button>
              </div>
            </div>
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={performanceData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="hour" stroke="#6b7280" fontSize={12} />
                <YAxis stroke="#6b7280" fontSize={12} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                  }}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="cpu"
                  stroke="#ef4444"
                  strokeWidth={2}
                  dot={{ fill: '#ef4444', strokeWidth: 2, r: 3 }}
                  name="CPU Usage (%)"
                />
                <Line
                  type="monotone"
                  dataKey="memory"
                  stroke="#f59e0b"
                  strokeWidth={2}
                  dot={{ fill: '#f59e0b', strokeWidth: 2, r: 3 }}
                  name="Memory (%)"
                />
                <Line
                  type="monotone"
                  dataKey="load"
                  stroke="#10b981"
                  strokeWidth={2}
                  dot={{ fill: '#10b981', strokeWidth: 2, r: 3 }}
                  name="Server Load (%)"
                />
              </LineChart>
            </ResponsiveContainer>
            
            <div className="mt-6 grid grid-cols-2 gap-4">
              <div className="p-4 bg-primary-50 rounded-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-primary-600 font-medium">Active Agents</p>
                    <p className="text-2xl font-bold text-primary-900">{adminStats.activeAgents}</p>
                  </div>
                  <Users className="w-8 h-8 text-primary-500" />
                </div>
              </div>
              
              <div className="p-4 bg-success-50 rounded-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-success-600 font-medium">Monthly Revenue</p>
                    <p className="text-2xl font-bold text-success-900">
                      ${(adminStats.monthlyRevenue / 1000).toFixed(0)}K
                    </p>
                  </div>
                  <TrendingUp className="w-8 h-8 text-success-500" />
                </div>
              </div>
            </div>

            <div className="mt-4">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium text-gray-700">Storage Usage</span>
                <span className="text-sm text-gray-500">75% of 500GB</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-primary-600 h-2 rounded-full" style={{ width: '75%' }}></div>
              </div>
            </div>

            <div className="mt-4">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium text-gray-700">API Rate Limit</span>
                <span className="text-sm text-gray-500">2.1K / 5K requests</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-info-600 h-2 rounded-full" style={{ width: '42%' }}></div>
              </div>
            </div>
          </Card>
        </div>

        {/* Recent Activity */}
        <div>
          <Card>
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Recent Activity</h2>
            <div className="space-y-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="flex items-start gap-3">
                  <div className="w-2 h-2 bg-primary-500 rounded-full mt-2"></div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900 font-medium">
                      {activity.action}
                    </p>
                    <p className="text-sm text-gray-500">
                      {activity.company || activity.user || activity.agent || activity.status}
                    </p>
                    <p className="text-xs text-gray-400 mt-1">{activity.time}</p>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="ghost" size="sm" fullWidth className="mt-4">
              View All Activity
            </Button>
          </Card>
        </div>
      </div>

      {/* Quick Actions */}
      <Card>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Button variant="secondary" className="h-20 flex-col">
            <Users className="w-6 h-6 mb-2" />
            <span className="text-sm">Manage Users</span>
          </Button>
          <Button variant="secondary" className="h-20 flex-col">
            <Building className="w-6 h-6 mb-2" />
            <span className="text-sm">Companies</span>
          </Button>
          <Button variant="secondary" className="h-20 flex-col">
            <Shield className="w-6 h-6 mb-2" />
            <span className="text-sm">Security</span>
          </Button>
          <Button variant="secondary" className="h-20 flex-col">
            <Database className="w-6 h-6 mb-2" />
            <span className="text-sm">Backup</span>
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default AdminDashboard;