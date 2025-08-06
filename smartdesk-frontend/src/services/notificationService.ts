import { apiClient } from './apiClient';

export interface Notification {
  id: number;
  message: string;
  type: string;
  targetUrl: string;
  isRead: boolean;
  sentAt: string;
  recipientUser: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
  company: {
    id: number;
    name: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface NotificationResponse {
  notifications: Notification[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
  userId: number;
}

export interface UnreadNotificationResponse {
  notifications: Notification[];
  count: number;
  userId: number;
}

export const notificationService = {
  // Get user notifications with pagination
  async getUserNotifications(userId: number, params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<NotificationResponse> {
    const response = await apiClient.get(`/notifications/user/${userId}`, { 
      params: {
        page: params?.page || 0,
        size: params?.size || 20,
        sortBy: params?.sortBy || 'sentAt',
        sortDir: params?.sortDir || 'desc'
      }
    });
    return response.data;
  },

  // Get unread notifications
  async getUnreadNotifications(userId: number): Promise<UnreadNotificationResponse> {
    const response = await apiClient.get(`/notifications/user/${userId}/unread`);
    return response.data;
  },

  // Get unread notification count
  async getUnreadNotificationCount(userId: number): Promise<{ count: number; userId: number }> {
    const response = await apiClient.get(`/notifications/user/${userId}/unread/count`);
    return response.data;
  },

  // Mark notification as read
  async markNotificationAsRead(notificationId: number): Promise<void> {
    await apiClient.put(`/notifications/${notificationId}/read`);
  },

  // Delete notification
  async deleteNotification(notificationId: number): Promise<void> {
    await apiClient.delete(`/notifications/${notificationId}`);
  }
};