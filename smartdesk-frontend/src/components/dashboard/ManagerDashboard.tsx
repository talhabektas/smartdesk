import React from 'react';
import { 
  Users, 
  Ticket, 
  TrendingUp,
  Clock,
  CheckCircle,
  AlertTriangle,
  Star,
  BarChart3,
} from 'lucide-react';
import { Card, StatsCard } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { TicketStatus, TicketPriority } from '../../types';

const ManagerDashboard: React.FC = () => {
  // Mock manager data
  const managerStats = {
    totalTickets: 347,
    openTickets: 28,
    inProgressTickets: 15,
    resolvedToday: 12,
    teamMembers: 8,
    avgResolutionTime: 3.5,
    customerSatisfaction: 4.7,
    slaCompliance: 94.2,
  };

  const teamPerformance = [
    { id: 1, name: 'Ali Kaya', tickets: 23, resolved: 19, satisfaction: 4.8 },
    { id: 2, name: 'Fatma Öztürk', tickets: 18, resolved: 16, satisfaction: 4.6 },
    { id: 3, name: 'Can Arslan', tickets: 21, resolved: 17, satisfaction: 4.9 },
    { id: 4, name: 'Zeynep Yılmaz', tickets: 19, resolved: 18, satisfaction: 4.7 },
  ];

  const priorityDistribution = [
    { priority: 'Critical', count: 3, color: 'bg-error-500' },
    { priority: 'High', count: 12, color: 'bg-warning-500' },
    { priority: 'Normal', count: 18, color: 'bg-info-500' },
    { priority: 'Low', count: 7, color: 'bg-success-500' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Manager Dashboard
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Team performance and ticket management overview
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="secondary" leftIcon={<BarChart3 className="w-4 h-4" />}>
            Reports
          </Button>
          <Button variant="primary" leftIcon={<Users className="w-4 h-4" />}>
            Manage Team
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Open Tickets"
          value={managerStats.openTickets.toString()}
          icon={<Ticket className="w-5 h-5" />}
          trend={{ value: 5, isPositive: false }}
          color="warning"
        />
        <StatsCard
          title="In Progress"
          value={managerStats.inProgressTickets.toString()}
          icon={<Clock className="w-5 h-5" />}
          trend={{ value: 2, isPositive: true }}
          color="info"
        />
        <StatsCard
          title="Resolved Today"
          value={managerStats.resolvedToday.toString()}
          icon={<CheckCircle className="w-5 h-5" />}
          trend={{ value: 15, isPositive: true }}
          color="success"
        />
        <StatsCard
          title="Team Members"
          value={managerStats.teamMembers.toString()}
          icon={<Users className="w-5 h-5" />}
          color="primary"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Performance Metrics */}
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900">Performance Metrics</h2>
              <Button variant="ghost" size="sm">View Details</Button>
            </div>
            
            <div className="grid grid-cols-3 gap-4 mb-6">
              <div className="text-center p-4 bg-success-50 rounded-lg">
                <p className="text-2xl font-bold text-success-900">
                  {managerStats.avgResolutionTime}h
                </p>
                <p className="text-sm text-success-600">Avg Resolution</p>
              </div>
              <div className="text-center p-4 bg-primary-50 rounded-lg">
                <p className="text-2xl font-bold text-primary-900">
                  {managerStats.customerSatisfaction}★
                </p>
                <p className="text-sm text-primary-600">Satisfaction</p>
              </div>
              <div className="text-center p-4 bg-info-50 rounded-lg">
                <p className="text-2xl font-bold text-info-900">
                  {managerStats.slaCompliance}%
                </p>
                <p className="text-sm text-info-600">SLA Compliance</p>
              </div>
            </div>

            {/* Priority Distribution */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3">Ticket Priority Distribution</h3>
              <div className="space-y-2">
                {priorityDistribution.map((item, index) => (
                  <div key={index} className="flex items-center gap-3">
                    <div className={`w-3 h-3 rounded-full ${item.color}`}></div>
                    <span className="text-sm text-gray-600 flex-1">{item.priority}</span>
                    <span className="text-sm font-medium text-gray-900">{item.count}</span>
                  </div>
                ))}
              </div>
            </div>
          </Card>
        </div>

        {/* Team Performance */}
        <div>
          <Card>
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Team Performance</h2>
            <div className="space-y-4">
              {teamPerformance.map((member) => (
                <div key={member.id} className="p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-medium text-gray-900">{member.name}</span>
                    <div className="flex items-center gap-1">
                      <Star className="w-3 h-3 text-yellow-500 fill-current" />
                      <span className="text-xs text-gray-600">{member.satisfaction}</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between text-sm text-gray-600">
                    <span>{member.tickets} tickets</span>
                    <span className="text-success-600">{member.resolved} resolved</span>
                  </div>
                  <div className="mt-2 w-full bg-gray-200 rounded-full h-1">
                    <div 
                      className="bg-success-500 h-1 rounded-full" 
                      style={{ width: `${(member.resolved / member.tickets) * 100}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="ghost" size="sm" fullWidth className="mt-4">
              View Full Team
            </Button>
          </Card>
        </div>
      </div>

      {/* Recent Alerts */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Recent Alerts</h2>
          <Badge variant="warning">3 Active</Badge>
        </div>
        <div className="space-y-3">
          <div className="flex items-center gap-3 p-3 bg-error-50 border border-error-200 rounded-lg">
            <AlertTriangle className="w-5 h-5 text-error-500" />
            <div className="flex-1">
              <p className="text-sm font-medium text-error-900">SLA Breach Alert</p>
              <p className="text-xs text-error-700">Ticket #TK-2024-001 exceeded response time</p>
            </div>
            <Button variant="ghost" size="sm">View</Button>
          </div>
          <div className="flex items-center gap-3 p-3 bg-warning-50 border border-warning-200 rounded-lg">
            <Clock className="w-5 h-5 text-warning-500" />
            <div className="flex-1">
              <p className="text-sm font-medium text-warning-900">High Priority Queue</p>
              <p className="text-xs text-warning-700">12 high priority tickets awaiting assignment</p>
            </div>
            <Button variant="ghost" size="sm">Assign</Button>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default ManagerDashboard;