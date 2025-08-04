import React, { useState, useEffect, useCallback } from 'react';
import { 
  Bell, 
  X, 
  Check, 
  Info, 
  AlertTriangle, 
  CheckCircle, 
  XCircle,
  Clock,
  User,
  Ticket,
  MessageSquare,
  Settings,
  ChevronDown,
  MoreVertical,
  Trash2
} from 'lucide-react';
import { Button } from './Button';
import { Badge } from './Badge';
import { Card } from './Card';
import { useAuthStore } from '../../store/authStore';
import { webSocketService } from '../../services/websocket';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';

interface Notification {
  id: number;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR' | 'TICKET_UPDATE' | 'NEW_COMMENT' | 'ASSIGNMENT';
  title: string;
  message: string;
  createdAt: string;
  isRead: boolean;
  userId: number;
  relatedEntityId?: number;
  relatedEntityType?: 'TICKET' | 'USER' | 'COMPANY';
  actionUrl?: string;
}

const NotificationCenter: React.FC = () => {
  const { user } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<'all' | 'unread' | 'tickets' | 'system'>('all');

  // Load notifications from API
  const loadNotifications = useCallback(async () => {
    if (!user) return;

    try {
      setLoading(true);
      const response = await api.get(`/notifications/user/${user.id}`);
      setNotifications(response.data.content || []);
    } catch (error: any) {
      console.error('Failed to load notifications:', error);
      // Use mock data for demo
      setNotifications([
        {
          id: 1,
          type: 'TICKET_UPDATE',
          title: 'Ticket Updated',
          message: 'Ticket #TK-2024-001 status changed to In Progress',
          createdAt: new Date(Date.now() - 300000).toISOString(), // 5 min ago
          isRead: false,
          userId: user.id,
          relatedEntityId: 1,
          relatedEntityType: 'TICKET',
          actionUrl: '/tickets/1'
        },
        {
          id: 2,
          type: 'NEW_COMMENT',
          title: 'New Comment',
          message: 'Ali Kaya added a comment to your ticket',
          createdAt: new Date(Date.now() - 900000).toISOString(), // 15 min ago
          isRead: false,
          userId: user.id,
          relatedEntityId: 2,
          relatedEntityType: 'TICKET',
          actionUrl: '/tickets/2'
        },
        {
          id: 3,
          type: 'ASSIGNMENT',
          title: 'New Assignment',
          message: 'You have been assigned to ticket #TK-2024-003',
          createdAt: new Date(Date.now() - 1800000).toISOString(), // 30 min ago
          isRead: true,
          userId: user.id,
          relatedEntityId: 3,
          relatedEntityType: 'TICKET',
          actionUrl: '/tickets/3'
        },
        {
          id: 4,
          type: 'SUCCESS',
          title: 'System Update',
          message: 'System maintenance completed successfully',
          createdAt: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
          isRead: true,
          userId: user.id
        }
      ]);
    } finally {
      setLoading(false);
    }
  }, [user]);

  // Handle real-time notifications via WebSocket
  const handleRealtimeNotification = useCallback((message: any) => {
    console.log('New real-time notification:', message);
    
    const newNotification: Notification = {
      id: Date.now(), // Temporary ID
      type: message.type || 'INFO',
      title: message.data?.title || 'New Notification',
      message: message.data?.message || message.message || 'You have a new notification',
      createdAt: new Date().toISOString(),
      isRead: false,
      userId: user?.id || 0,
      relatedEntityId: message.data?.entityId,
      relatedEntityType: message.data?.entityType,
      actionUrl: message.data?.actionUrl
    };

    // Add to notifications list
    setNotifications(prev => [newNotification, ...prev]);

    // Show toast notification
    switch (message.type) {
      case 'SUCCESS':
        toast.success(newNotification.message);
        break;
      case 'WARNING':
        toast(newNotification.message, { icon: '⚠️' });
        break;
      case 'ERROR':
        toast.error(newNotification.message);
        break;
      default:
        toast(newNotification.message);
    }
  }, [user?.id]);

  // Mark notification as read
  const markAsRead = async (notificationId: number) => {
    try {
      await api.patch(`/notifications/${notificationId}/read`);
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      );
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      // Optimistic update
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      );
    }
  };

  // Mark all as read
  const markAllAsRead = async () => {
    try {
      await api.patch(`/notifications/user/${user?.id}/read-all`);
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      toast.success('All notifications marked as read');
    } catch (error) {
      console.error('Failed to mark all as read:', error);
      // Optimistic update
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    }
  };

  // Delete notification
  const deleteNotification = async (notificationId: number) => {
    try {
      await api.delete(`/notifications/${notificationId}`);
      setNotifications(prev => prev.filter(n => n.id !== notificationId));
      toast.success('Notification deleted');
    } catch (error) {
      console.error('Failed to delete notification:', error);
      // Optimistic update
      setNotifications(prev => prev.filter(n => n.id !== notificationId));
    }
  };

  // Setup WebSocket subscriptions
  useEffect(() => {
    if (user) {
      loadNotifications();
      
      // Setup WebSocket connection
      webSocketService.connect(user.id);
      
      // Subscribe to notification events
      webSocketService.addEventListener('NOTIFICATION', handleRealtimeNotification);
      webSocketService.addEventListener('TICKET_UPDATE', handleRealtimeNotification);
      webSocketService.addEventListener('NEW_COMMENT', handleRealtimeNotification);
      webSocketService.addEventListener('ASSIGNMENT', handleRealtimeNotification);
      
      return () => {
        webSocketService.removeEventListener('NOTIFICATION', handleRealtimeNotification);
        webSocketService.removeEventListener('TICKET_UPDATE', handleRealtimeNotification);
        webSocketService.removeEventListener('NEW_COMMENT', handleRealtimeNotification);
        webSocketService.removeEventListener('ASSIGNMENT', handleRealtimeNotification);
      };
    }
  }, [user, loadNotifications, handleRealtimeNotification]);

  // Filter notifications
  const filteredNotifications = notifications.filter(notification => {
    switch (filter) {
      case 'unread':
        return !notification.isRead;
      case 'tickets':
        return ['TICKET_UPDATE', 'NEW_COMMENT', 'ASSIGNMENT'].includes(notification.type);
      case 'system':
        return ['INFO', 'SUCCESS', 'WARNING', 'ERROR'].includes(notification.type);
      default:
        return true;
    }
  });

  // Get notification icon
  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'SUCCESS':
        return <CheckCircle className="w-4 h-4 text-success-500" />;
      case 'WARNING':
        return <AlertTriangle className="w-4 h-4 text-warning-500" />;
      case 'ERROR':
        return <XCircle className="w-4 h-4 text-danger-500" />;
      case 'TICKET_UPDATE':
        return <Ticket className="w-4 h-4 text-info-500" />;
      case 'NEW_COMMENT':
        return <MessageSquare className="w-4 h-4 text-primary-500" />;
      case 'ASSIGNMENT':
        return <User className="w-4 h-4 text-success-500" />;
      default:
        return <Info className="w-4 h-4 text-gray-500" />;
    }
  };

  // Format time ago
  const formatTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) return 'Just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
    return `${Math.floor(diffInSeconds / 86400)}d ago`;
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="relative">
      {/* Notification Bell */}
      <Button
        variant="ghost"
        size="sm"
        onClick={() => setIsOpen(!isOpen)}
        className="relative"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <Badge 
            variant="danger" 
            className="absolute -top-2 -right-2 text-xs min-w-[1.25rem] h-5 flex items-center justify-center"
          >
            {unreadCount > 99 ? '99+' : unreadCount}
          </Badge>
        )}
      </Button>

      {/* Notification Panel */}
      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-96 z-50">
          <Card className="shadow-lg border-0">
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-gray-200">
              <div className="flex items-center gap-2">
                <h3 className="font-semibold text-gray-900">Notifications</h3>
                {unreadCount > 0 && (
                  <Badge variant="primary" className="text-xs">
                    {unreadCount} new
                  </Badge>
                )}
              </div>
              <div className="flex items-center gap-2">
                {unreadCount > 0 && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={markAllAsRead}
                    className="text-xs"
                  >
                    Mark all read
                  </Button>
                )}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setIsOpen(false)}
                >
                  <X className="w-4 h-4" />
                </Button>
              </div>
            </div>

            {/* Filters */}
            <div className="p-3 border-b border-gray-100">
              <div className="flex gap-2 flex-wrap">
                {(['all', 'unread', 'tickets', 'system'] as const).map((filterOption) => (
                  <Button
                    key={filterOption}
                    variant={filter === filterOption ? 'primary' : 'secondary'}
                    size="sm"
                    onClick={() => setFilter(filterOption)}
                    className="text-xs"
                  >
                    {filterOption.charAt(0).toUpperCase() + filterOption.slice(1)}
                  </Button>
                ))}
              </div>
            </div>

            {/* Notifications List */}
            <div className="max-h-96 overflow-y-auto">
              {loading ? (
                <div className="p-4 text-center text-gray-500">
                  <Clock className="w-5 h-5 animate-spin mx-auto mb-2" />
                  Loading notifications...
                </div>
              ) : filteredNotifications.length === 0 ? (
                <div className="p-4 text-center text-gray-500">
                  <Bell className="w-8 h-8 mx-auto mb-2 text-gray-300" />
                  No notifications found
                </div>
              ) : (
                filteredNotifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`p-3 border-b border-gray-100 hover:bg-gray-50 transition-colors ${
                      !notification.isRead ? 'bg-blue-50' : ''
                    }`}
                  >
                    <div className="flex items-start gap-3">
                      <div className="flex-shrink-0 mt-1">
                        {getNotificationIcon(notification.type)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <p className={`text-sm ${!notification.isRead ? 'font-medium text-gray-900' : 'text-gray-700'}`}>
                              {notification.title}
                            </p>
                            <p className="text-xs text-gray-600 mt-1">
                              {notification.message}
                            </p>
                            <p className="text-xs text-gray-400 mt-1">
                              {formatTimeAgo(notification.createdAt)}
                            </p>
                          </div>
                          <div className="flex items-center gap-1 ml-2">
                            {!notification.isRead && (
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => markAsRead(notification.id)}
                                className="text-xs"
                              >
                                <Check className="w-3 h-3" />
                              </Button>
                            )}
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => deleteNotification(notification.id)}
                              className="text-xs text-gray-400 hover:text-danger-500"
                            >
                              <Trash2 className="w-3 h-3" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Footer */}
            {filteredNotifications.length > 0 && (
              <div className="p-3 border-t border-gray-100 text-center">
                <Button variant="ghost" size="sm" className="text-xs">
                  View All Notifications
                </Button>
              </div>
            )}
          </Card>
        </div>
      )}
    </div>
  );
};

export default NotificationCenter;