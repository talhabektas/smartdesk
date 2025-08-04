import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { toast } from 'react-hot-toast';
import { tokenManager } from './api';
import { WebSocketMessage, ChatMessage, NotificationType, UserRole, ChatMessageType } from '../types';

export type WebSocketEventType =
  | 'TICKET_UPDATE'
  | 'NEW_COMMENT'
  | 'ASSIGNMENT'
  | 'STATUS_CHANGE'
  | 'NOTIFICATION'
  | 'USER_ONLINE'
  | 'USER_OFFLINE'
  | 'USER_UPDATE'
  | 'SYSTEM_UPDATE';

export interface WebSocketEventHandler {
  (message: WebSocketMessage): void;
}

class WebSocketService {
  private static instance: WebSocketService;
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private eventHandlers: Map<WebSocketEventType, WebSocketEventHandler[]> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000; // 5 seconds
  private isConnecting = false;
  private userId: number | null = null;

  static getInstance(): WebSocketService {
    if (!WebSocketService.instance) {
      WebSocketService.instance = new WebSocketService();
    }
    return WebSocketService.instance;
  }

  // Initialize WebSocket connection
  async connect(userId?: number): Promise<void> {
    if (this.isConnecting || this.isConnected()) {
      return;
    }

    this.isConnecting = true;
    this.userId = userId || this.userId;

    const token = tokenManager.getAccessToken();
    if (!token) {
      console.warn('No access token available for WebSocket connection');
      this.isConnecting = false;
      return;
    }

    try {
      // Create STOMP client with SockJS
      const wsUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8067/ws';
      console.log('🔌 Connecting to WebSocket URL:', wsUrl);
      
      this.client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (process.env.NODE_ENV === 'development') {
            console.log('STOMP Debug:', str);
          }
        },
        reconnectDelay: this.reconnectInterval,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      // Connection success handler
      this.client.onConnect = (frame) => {
        console.log('WebSocket Connected:', frame);
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.setupDefaultSubscriptions();
        toast.success('Gerçek zamanlı bağlantı kuruldu');
      };

      // Connection error handler
      this.client.onStompError = (frame) => {
        console.error('STOMP Error:', frame);
        this.isConnecting = false;
        this.handleConnectionError();
      };

      // WebSocket error handler
      this.client.onWebSocketError = (error) => {
        console.error('WebSocket Error:', error);
        this.isConnecting = false;
        this.handleConnectionError();
      };

      // Disconnection handler
      this.client.onDisconnect = (frame) => {
        console.log('WebSocket Disconnected:', frame);
        this.isConnecting = false;
        this.clearSubscriptions();
      };

      // Activate the connection
      this.client.activate();

    } catch (error) {
      console.error('WebSocket connection failed:', error);
      this.isConnecting = false;
      this.handleConnectionError();
    }
  }

  // Disconnect WebSocket
  disconnect(): void {
    if (this.client) {
      this.clearSubscriptions();
      this.client.deactivate();
      this.client = null;
    }
    this.isConnecting = false;
    this.reconnectAttempts = 0;
    this.userId = null;
  }

  // Check if connected
  isConnected(): boolean {
    return this.client?.connected || false;
  }

  // Setup default subscriptions
  private setupDefaultSubscriptions(): void {
    if (!this.client || !this.client.connected) return;

    // Subscribe to global notifications
    this.subscribe('/topic/notifications', (message) => {
      this.handleGlobalNotification(JSON.parse(message.body));
    });

    // Subscribe to user-specific messages
    if (this.userId) {
      this.subscribe(`/user/${this.userId}/notifications`, (message) => {
        this.handleUserNotification(JSON.parse(message.body));
      });

      this.subscribe(`/user/${this.userId}/tickets`, (message) => {
        this.handleTicketUpdate(JSON.parse(message.body));
      });
    }

    // Subscribe to system-wide updates
    this.subscribe('/topic/system', (message) => {
      this.handleSystemUpdate(JSON.parse(message.body));
    });
  }

  // Subscribe to a destination
  subscribe(destination: string, callback: (message: any) => void): string {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot subscribe: WebSocket not connected');
      return '';
    }

    const subscriptionId = `${destination}_${Date.now()}`;
    const subscription = this.client.subscribe(destination, callback);
    this.subscriptions.set(subscriptionId, subscription);

    return subscriptionId;
  }

  // Unsubscribe from a destination
  unsubscribe(subscriptionId: string): void {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(subscriptionId);
    }
  }

  // Clear all subscriptions
  private clearSubscriptions(): void {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
  }

  // Send message to server
  sendMessage(destination: string, body: any, headers?: any): void {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot send message: WebSocket not connected');
      return;
    }

    const token = tokenManager.getAccessToken();
    const messageHeaders = {
      Authorization: token ? `Bearer ${token}` : '',
      ...headers
    };

    this.client.publish({
      destination,
      body: JSON.stringify(body),
      headers: messageHeaders,
    });
  }

  // Send chat message
  sendChatMessage(ticketId: number, message: string): void {
    const chatMessage = {
      ticketId,
      senderId: this.userId!,
      senderName: '', // Will be filled by server
      senderRole: UserRole.AGENT, // Will be determined by server
      content: message,
      createdAt: new Date().toISOString(),
      messageType: ChatMessageType.TEXT,
    };

    this.sendMessage(`/app/ticket/${ticketId}/chat`, chatMessage);
  }

  // Send ticket status update
  sendTicketStatusUpdate(ticketId: number, status: string): void {
    this.sendMessage(`/app/ticket/${ticketId}/status`, { status });
  }

  // Add event handler
  addEventListener(eventType: WebSocketEventType, handler: WebSocketEventHandler): void {
    if (!this.eventHandlers.has(eventType)) {
      this.eventHandlers.set(eventType, []);
    }
    this.eventHandlers.get(eventType)!.push(handler);
  }

  // Remove event handler
  removeEventListener(eventType: WebSocketEventType, handler: WebSocketEventHandler): void {
    const handlers = this.eventHandlers.get(eventType);
    if (handlers) {
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }

  // Emit event to handlers
  private emitEvent(eventType: WebSocketEventType, message: WebSocketMessage): void {
    const handlers = this.eventHandlers.get(eventType);
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(message);
        } catch (error) {
          console.error('Event handler error:', error);
        }
      });
    }
  }

  // Handle global notifications
  private handleGlobalNotification(message: WebSocketMessage): void {
    console.log('Global notification:', message);

    if (message.type === 'NOTIFICATION') {
      const notification = message.data;

      switch (notification.type) {
        case 'INFO':
          toast(notification.message, { icon: 'ℹ️' });
          break;
        case 'SUCCESS':
          toast.success(notification.message);
          break;
        case 'WARNING':
          toast(notification.message, { icon: '⚠️' });
          break;
        case 'ERROR':
          toast.error(notification.message);
          break;
        default:
          toast(notification.message);
      }
    }

    this.emitEvent(message.type as WebSocketEventType, message);
  }

  // Handle user-specific notifications
  private handleUserNotification(message: WebSocketMessage): void {
    console.log('User notification:', message);

    switch (message.type) {
      case 'TICKET_UPDATE':
        toast.success('Ticket güncellendi');
        break;
      case 'NEW_COMMENT':
        toast.success('Yeni yorum eklendi');
        break;
      case 'ASSIGNMENT':
        toast.success('Size yeni bir ticket atandı');
        break;
      default:
        break;
    }

    this.emitEvent(message.type as WebSocketEventType, message);
  }

  // Handle ticket updates
  private handleTicketUpdate(message: WebSocketMessage): void {
    console.log('Ticket update:', message);
    this.emitEvent(message.type as WebSocketEventType, message);
  }

  // Handle system updates
  private handleSystemUpdate(message: WebSocketMessage): void {
    console.log('System update:', message);

    if (message.type === 'NOTIFICATION') {
      const notification = message.data;
      toast(notification.message, {
        duration: 5000,
        icon: '🔔'
      });
    }

    this.emitEvent(message.type as WebSocketEventType, message);
  }

  // Handle connection errors and reconnection
  private handleConnectionError(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`WebSocket reconnection attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);

      setTimeout(() => {
        if (this.userId) {
          this.connect(this.userId);
        }
      }, this.reconnectInterval * this.reconnectAttempts);
    } else {
      console.error('Max reconnection attempts reached');
      toast.error('Gerçek zamanlı bağlantı kurulamadı');
    }
  }

  // Get connection status
  getConnectionStatus(): 'connected' | 'connecting' | 'disconnected' {
    if (this.isConnected()) return 'connected';
    if (this.isConnecting) return 'connecting';
    return 'disconnected';
  }
}

export const webSocketService = WebSocketService.getInstance();
export default webSocketService;