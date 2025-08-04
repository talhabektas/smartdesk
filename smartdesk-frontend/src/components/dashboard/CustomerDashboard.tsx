import React from 'react';
import { 
  Ticket, 
  Plus,
  Clock,
  CheckCircle,
  MessageCircle,
  FileText,
  Star,
  HelpCircle,
} from 'lucide-react';
import { Card, StatsCard } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge, TicketStatusBadge, TicketPriorityBadge } from '../ui/Badge';
import { TicketStatus, TicketPriority } from '../../types';

const CustomerDashboard: React.FC = () => {
  // Mock customer data
  const customerStats = {
    totalTickets: 12,
    openTickets: 3,
    resolvedTickets: 9,
    avgResponseTime: 2.5, // hours
  };

  const myTickets = [
    {
      id: 'TK-2024-010',
      title: 'Unable to access my account',
      status: TicketStatus.IN_PROGRESS,
      priority: TicketPriority.HIGH,
      createdAt: '2 hours ago',
      lastUpdate: '30 minutes ago',
      agent: 'Ali Kaya',
    },
    {
      id: 'TK-2024-008',
      title: 'Feature request: Mobile notifications',
      status: TicketStatus.OPEN,
      priority: TicketPriority.NORMAL,
      createdAt: '1 day ago',
      lastUpdate: '6 hours ago',
      agent: 'Unassigned',
    },
    {
      id: 'TK-2024-005',
      title: 'Billing inquiry',
      status: TicketStatus.RESOLVED,
      priority: TicketPriority.NORMAL,
      createdAt: '3 days ago',
      lastUpdate: '1 day ago',
      agent: 'Fatma Öztürk',
    },
  ];

  const knowledgeBaseArticles = [
    { id: 1, title: 'How to reset your password', category: 'Account', views: 1243 },
    { id: 2, title: 'Setting up two-factor authentication', category: 'Security', views: 892 },
    { id: 3, title: 'Understanding your billing statement', category: 'Billing', views: 567 },
    { id: 4, title: 'Mobile app troubleshooting guide', category: 'Technical', views: 734 },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Welcome back!
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Manage your support tickets and find helpful resources
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="secondary" leftIcon={<HelpCircle className="w-4 h-4" />}>
            Help Center
          </Button>
          <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
            New Ticket
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Total Tickets"
          value={customerStats.totalTickets.toString()}
          icon={<Ticket className="w-5 h-5" />}
          color="primary"
        />
        <StatsCard
          title="Open Tickets"
          value={customerStats.openTickets.toString()}
          icon={<Clock className="w-5 h-5" />}
          color="warning"
        />
        <StatsCard
          title="Resolved"
          value={customerStats.resolvedTickets.toString()}
          icon={<CheckCircle className="w-5 h-5" />}
          color="success"
        />
        <StatsCard
          title="Avg Response"
          value={`${customerStats.avgResponseTime}h`}
          icon={<MessageCircle className="w-5 h-5" />}
          color="info"
        />
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
              {myTickets.map((ticket) => (
                <div key={ticket.id} className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-medium text-gray-900">{ticket.id}</span>
                        <TicketPriorityBadge priority={ticket.priority} />
                      </div>
                      <h3 className="text-sm font-medium text-gray-900 mb-2">
                        {ticket.title}
                      </h3>
                      <div className="flex items-center gap-4 text-xs text-gray-600">
                        <span>Created: {ticket.createdAt}</span>
                        <span>Updated: {ticket.lastUpdate}</span>
                        <span>Agent: {ticket.agent}</span>
                      </div>
                    </div>
                    <TicketStatusBadge status={ticket.status} />
                  </div>
                  <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm">
                      View Details
                    </Button>
                    <Button variant="secondary" size="sm">
                      Add Comment
                    </Button>
                  </div>
                </div>
              ))}
            </div>

            {customerStats.openTickets === 0 && (
              <div className="text-center py-12">
                <CheckCircle className="w-12 h-12 text-success-500 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  All caught up!
                </h3>
                <p className="text-gray-600 mb-4">
                  You don't have any open tickets at the moment.
                </p>
                <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
                  Create New Ticket
                </Button>
              </div>
            )}
          </Card>
        </div>

        {/* Knowledge Base */}
        <div>
          <Card>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900">Knowledge Base</h2>
              <FileText className="w-5 h-5 text-gray-400" />
            </div>
            
            <div className="space-y-3">
              {knowledgeBaseArticles.map((article) => (
                <div key={article.id} className="p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                  <h3 className="text-sm font-medium text-gray-900 mb-1">
                    {article.title}
                  </h3>
                  <div className="flex items-center justify-between">
                    <Badge variant="secondary" size="sm">{article.category}</Badge>
                    <span className="text-xs text-gray-500">{article.views} views</span>
                  </div>
                </div>
              ))}
            </div>
            
            <Button variant="ghost" size="sm" fullWidth className="mt-4">
              Browse All Articles
            </Button>
          </Card>
        </div>
      </div>

      {/* Quick Actions */}
      <Card>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Button variant="secondary" className="h-16 flex-col">
            <Plus className="w-5 h-5 mb-1" />
            <span className="text-sm">New Ticket</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <FileText className="w-5 h-5 mb-1" />
            <span className="text-sm">Knowledge Base</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <MessageCircle className="w-5 h-5 mb-1" />
            <span className="text-sm">Live Chat</span>
          </Button>
          <Button variant="secondary" className="h-16 flex-col">
            <Star className="w-5 h-5 mb-1" />
            <span className="text-sm">Feedback</span>
          </Button>
        </div>
      </Card>

      {/* Support Contact */}
      <Card>
        <div className="text-center py-6">
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Need immediate help?
          </h3>
          <p className="text-gray-600 mb-4">
            Our support team is available 24/7 to assist you
          </p>
          <div className="flex items-center justify-center gap-4">
            <Button variant="secondary">
              <MessageCircle className="w-4 h-4 mr-2" />
              Start Live Chat
            </Button>
            <Button variant="outline">
              Call Support
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default CustomerDashboard;