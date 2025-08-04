import React from 'react';
import { 
  Ticket, 
  Clock, 
  CheckCircle,
  AlertCircle,
  Star,
  MessageSquare,
  User,
  TrendingUp,
  Calendar,
  Activity,
} from 'lucide-react';
import { Card, StatsCard } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge, TicketStatusBadge, TicketPriorityBadge } from '../ui/Badge';
import { TicketStatus, TicketPriority } from '../../types';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';

const AgentDashboard: React.FC = () => {
  // Mock agent data
  const agentStats = {
    assignedTickets: 23,
    inProgressTickets: 8,
    resolvedToday: 5,
    avgResponseTime: 45, // minutes
    customerRating: 4.8,
    totalResolved: 342,
  };

  const recentTickets = [
    {
      id: 'TK-2024-001',
      title: 'Login issue with mobile app',
      customer: 'Ahmet Çelik',
      priority: TicketPriority.HIGH,
      status: TicketStatus.IN_PROGRESS,
      assignedAt: '2 hours ago',
    },
    {
      id: 'TK-2024-002',
      title: 'Email notification not working',
      customer: 'Zeynep Kurt',
      priority: TicketPriority.NORMAL,
      status: TicketStatus.OPEN,
      assignedAt: '4 hours ago',
    },
    {
      id: 'TK-2024-003',
      title: 'Feature request: Dark mode',
      customer: 'Emre Şahin',
      priority: TicketPriority.LOW,
      status: TicketStatus.PENDING,
      assignedAt: '1 day ago',
    },
  ];

  const quickStats = [
    { label: 'Response Time', value: `${agentStats.avgResponseTime}m`, trend: 'down', good: true },
    { label: 'Resolution Rate', value: '87%', trend: 'up', good: true },
    { label: 'Customer Rating', value: agentStats.customerRating.toString(), trend: 'up', good: true },
  ];

  // Analytics data for charts
  const dailyActivity = [
    { day: 'Mon', resolved: 8, created: 5, responseTime: 42 },
    { day: 'Tue', resolved: 12, created: 7, responseTime: 38 },
    { day: 'Wed', resolved: 6, created: 9, responseTime: 55 },
    { day: 'Thu', resolved: 9, created: 6, responseTime: 41 },
    { day: 'Fri', resolved: 11, created: 8, responseTime: 39 },
    { day: 'Sat', resolved: 3, created: 2, responseTime: 35 },
    { day: 'Sun', resolved: 2, created: 1, responseTime: 40 },
  ];

  const weeklyPerformance = [
    { week: 'Week 1', tickets: 45, satisfaction: 4.6, avgTime: 48 },
    { week: 'Week 2', tickets: 52, satisfaction: 4.7, avgTime: 44 },
    { week: 'Week 3', tickets: 38, satisfaction: 4.5, avgTime: 51 },
    { week: 'Week 4', tickets: 49, satisfaction: 4.8, avgTime: 42 },
    { week: 'This Week', tickets: 23, satisfaction: 4.8, avgTime: 45 },
  ];

  const timeDistribution = [
    { hour: '09:00', tickets: 3 },
    { hour: '10:00', tickets: 5 },
    { hour: '11:00', tickets: 4 },
    { hour: '12:00', tickets: 2 },
    { hour: '13:00', tickets: 1 },
    { hour: '14:00', tickets: 6 },
    { hour: '15:00', tickets: 7 },
    { hour: '16:00', tickets: 4 },
    { hour: '17:00', tickets: 3 },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            My Dashboard
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Your personal workspace and assigned tickets
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="secondary" leftIcon={<MessageSquare className="w-4 h-4" />}>
            Knowledge Base
          </Button>
          <Button variant="primary" leftIcon={<Ticket className="w-4 h-4" />}>
            New Ticket
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Assigned"
          value={agentStats.assignedTickets.toString()}
          icon={<Ticket className="w-5 h-5" />}
          trend={{ value: 3, isPositive: true }}
          color="primary"
        />
        <StatsCard
          title="In Progress"
          value={agentStats.inProgressTickets.toString()}
          icon={<Clock className="w-5 h-5" />}
          color="info"
        />
        <StatsCard
          title="Resolved Today"
          value={agentStats.resolvedToday.toString()}
          icon={<CheckCircle className="w-5 h-5" />}
          trend={{ value: 12, isPositive: true }}
          color="success"
        />
        <StatsCard
          title="Customer Rating"
          value={`${agentStats.customerRating}★`}
          icon={<Star className="w-5 h-5" />}
          trend={{ value: 0.2, isPositive: true }}
          color="warning"
        />
      </div>

      {/* Analytics Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Daily Activity */}
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">Daily Activity</h2>
            <Button variant="ghost" size="sm" leftIcon={<Activity className="w-4 h-4" />}>
              View Details
            </Button>
          </div>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={dailyActivity}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="day" stroke="#6b7280" fontSize={12} />
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
              <Bar
                dataKey="resolved"
                fill="#10b981"
                name="Resolved"
                radius={[2, 2, 0, 0]}
              />
              <Bar
                dataKey="created"
                fill="#3b82f6"
                name="Created"
                radius={[2, 2, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        {/* Response Time Trend */}
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">Response Time Trend</h2>
            <Button variant="ghost" size="sm" leftIcon={<Clock className="w-4 h-4" />}>
              Optimize
            </Button>
          </div>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={dailyActivity}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="day" stroke="#6b7280" fontSize={12} />
              <YAxis 
                stroke="#6b7280" 
                fontSize={12}
                label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#fff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                }}
                formatter={(value) => [value + 'm', 'Response Time']}
              />
              <Line
                type="monotone"
                dataKey="responseTime"
                stroke="#f59e0b"
                strokeWidth={3}
                dot={{ fill: '#f59e0b', strokeWidth: 2, r: 4 }}
                name="Response Time (min)"
              />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* My Tickets */}
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900">My Tickets</h2>
              <Button variant="ghost" size="sm">View All</Button>
            </div>
            
            <div className="space-y-4">
              {recentTickets.map((ticket) => (
                <div key={ticket.id} className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-medium text-gray-900">{ticket.id}</span>
                        <TicketPriorityBadge priority={ticket.priority} />
                      </div>
                      <h3 className="text-sm font-medium text-gray-900 mb-1">
                        {ticket.title}
                      </h3>
                      <p className="text-xs text-gray-600">
                        Customer: {ticket.customer} • {ticket.assignedAt}
                      </p>
                    </div>
                    <TicketStatusBadge status={ticket.status} />
                  </div>
                  <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm">
                      View
                    </Button>
                    <Button variant="secondary" size="sm">
                      Reply
                    </Button>
                  </div>
                </div>
              ))}
            </div>
            
            {/* Weekly Performance Chart */}
            <div className="mt-6 pt-6 border-t border-gray-200">
              <h3 className="text-sm font-medium text-gray-900 mb-4">Weekly Performance</h3>
              <ResponsiveContainer width="100%" height={150}>
                <AreaChart data={weeklyPerformance}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="week" stroke="#6b7280" fontSize={12} />
                  <YAxis stroke="#6b7280" fontSize={12} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="tickets"
                    stroke="#8b5cf6"
                    fill="#8b5cf6"
                    fillOpacity={0.3}
                    name="Tickets Handled"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </Card>
        </div>

        {/* Performance Summary & Hourly Distribution */}
        <div>
          <Card>
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Performance</h2>
            
            <div className="space-y-4 mb-6">
              {quickStats.map((stat, index) => (
                <div key={index} className="p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">{stat.label}</span>
                    <div className="flex items-center gap-1">
                      <span className="font-medium text-gray-900">{stat.value}</span>
                      {stat.trend === 'up' ? (
                        <TrendingUp className={`w-3 h-3 ${stat.good ? 'text-success-500' : 'text-error-500'}`} />
                      ) : (
                        <TrendingUp className={`w-3 h-3 rotate-180 ${stat.good ? 'text-success-500' : 'text-error-500'}`} />
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Hourly Activity Distribution */}
            <div className="mb-6">
              <h3 className="text-sm font-medium text-gray-900 mb-3">Today's Activity</h3>
              <ResponsiveContainer width="100%" height={120}>
                <BarChart data={timeDistribution}>
                  <XAxis 
                    dataKey="hour" 
                    stroke="#6b7280" 
                    fontSize={10}
                    interval={1}
                  />
                  <YAxis hide />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                    }}
                    formatter={(value) => [value, 'Tickets']}
                  />
                  <Bar
                    dataKey="tickets"
                    fill="#06b6d4"
                    radius={[2, 2, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>

            <div className="p-4 bg-success-50 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <CheckCircle className="w-5 h-5 text-success-500" />
                <span className="font-medium text-success-900">Great Job!</span>
              </div>
              <p className="text-sm text-success-700">
                You've resolved {agentStats.totalResolved} tickets total with excellent customer feedback.
              </p>
            </div>
          </Card>
        </div>
      </div>

      {/* Quick Actions */}
      <Card>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Button variant="secondary" className="h-16 flex-col">
            <Ticket className="w-5 h-5 mb-1" />
            <span className="text-sm">Create Ticket</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <MessageSquare className="w-5 h-5 mb-1" />
            <span className="text-sm">Knowledge Base</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <User className="w-5 h-5 mb-1" />
            <span className="text-sm">Customer Info</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <AlertCircle className="w-5 h-5 mb-1" />
            <span className="text-sm">Report Issue</span>
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default AgentDashboard;