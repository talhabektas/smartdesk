// Backend enum'larına karşılık gelen frontend type'ları

export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  MANAGER = 'MANAGER',
  AGENT = 'AGENT',
  CUSTOMER = 'CUSTOMER'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING = 'PENDING'
}

export enum TicketStatus {
  NEW = 'NEW',
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  PENDING = 'PENDING',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED',
  PENDING_MANAGER_APPROVAL = 'PENDING_MGR',
  MANAGER_APPROVED = 'MGR_APPROVED',
  PENDING_ADMIN_APPROVAL = 'PENDING_ADMIN'
}

export enum TicketPriority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export enum TicketCategory {
  TECHNICAL_SUPPORT = 'TECHNICAL_SUPPORT',
  BILLING = 'BILLING',
  ACCOUNT_MANAGEMENT = 'ACCOUNT_MANAGEMENT',
  FEATURE_REQUEST = 'FEATURE_REQUEST',
  BUG_REPORT = 'BUG_REPORT',
  GENERAL_INQUIRY = 'GENERAL_INQUIRY',
  COMPLAINT = 'COMPLAINT',
  REFUND_REQUEST = 'REFUND_REQUEST'
}

export enum TicketSource {
  EMAIL = 'EMAIL',
  WEB_FORM = 'WEB_FORM',
  PHONE = 'PHONE',
  CHAT = 'CHAT',
  API = 'API'
}

export enum CustomerSegment {
  BASIC = 'BASIC',
  STANDARD = 'STANDARD',
  PREMIUM = 'PREMIUM',
  VIP = 'VIP'
}

export enum NotificationType {
  INFO = 'INFO',
  SUCCESS = 'SUCCESS',
  WARNING = 'WARNING',
  ERROR = 'ERROR'
}

// API Response Types
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  path: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  companyId?: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface TokenValidationResponse {
  valid: boolean;
  user?: User;
  expiresIn?: number;
  error?: string;
  timestamp: string;
}

// User Types
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: UserRole;
  status: UserStatus;
  avatarUrl?: string;
  lastLogin?: string;
  emailVerified: boolean;
  company?: Company;
  department?: Department;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: UserRole;
  companyId?: number;
  departmentId?: number;
}

export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  role?: UserRole;
  status?: UserStatus;
  departmentId?: number;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// Company Types
export interface Company {
  id: number;
  name: string;
  domain: string;
  phone?: string;
  address?: string;
  website?: string;
  timezone: string;
  planType: string;
  maxUsers: number;
  maxTicketsPerMonth: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCompanyRequest {
  name: string;
  domain: string;
  phone?: string;
  address?: string;
  website?: string;
  timezone?: string;
  planType?: string;
}

export interface UpdateCompanyRequest {
  name?: string;
  domain?: string;
  phone?: string;
  address?: string;
  website?: string;
  timezone?: string;
  planType?: string;
  maxUsers?: number;
  maxTicketsPerMonth?: number;
  isActive?: boolean;
}

// Department Types
export interface Department {
  id: number;
  companyId: number;
  name: string;
  description?: string;
  email?: string;
  managerId?: number;
  manager?: User;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Customer Types
export interface Customer {
  id: number;
  companyId: number;
  email: string;
  phone?: string;
  firstName: string;
  lastName: string;
  segment: CustomerSegment;
  companyName?: string;
  address?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  segment?: CustomerSegment;
  companyName?: string;
  address?: string;
}

export interface UpdateCustomerRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  segment?: CustomerSegment;
  companyName?: string;
  address?: string;
  isActive?: boolean;
}

// Ticket Types
export interface Ticket {
  id: number;
  companyId: number;
  customerId: number;
  creatorUserId?: number;
  assignedAgentId?: number;
  departmentId: number;
  ticketNumber: string;
  title: string;
  description: string;
  priority: TicketPriority;
  status: TicketStatus;
  category: TicketCategory;
  source: TicketSource;
  slaDeadline?: string;
  resolvedAt?: string;
  isInternal: boolean;
  escalationLevel: number;
  customer: Customer;
  customerName?: string;
  customerEmail?: string;
  customerCompanyName?: string;
  assignedAgent?: User;
  assignedAgentName?: string;
  department: Department;
  departmentName?: string;
  comments: TicketComment[];
  attachments: TicketAttachment[];
  createdAt: string;
  updatedAt: string;
  lastActivityAt: string;
}

export interface CreateTicketRequest {
  customerId?: number;
  title: string;
  description: string;
  priority: TicketPriority;
  category: TicketCategory;
  source: TicketSource;
  departmentId?: number;
  companyId?: number;
  creatorUserId?: number;
  isInternal?: boolean;
  tags?: string;
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  status?: TicketStatus;
  category?: TicketCategory;
  assignedAgentId?: number;
  departmentId?: number;
}

export interface AssignTicketRequest {
  assignedAgentId: number;
}

// Ticket Comment Types
export interface TicketComment {
  id: number;
  ticketId: number;
  authorId: number;
  message: string;
  isInternal: boolean;
  isAutoGenerated: boolean;
  commentType: string;
  author: User;
  createdAt: string;
  updatedAt: string;
}

export interface AddCommentRequest {
  message: string;
  isInternal?: boolean;
}

// Ticket Attachment Types
export interface TicketAttachment {
  id: number;
  ticketId: number;
  fileName: string;
  originalName: string;
  fileSize: number;
  fileType: string;
  uploadedBy: number;
  uploadedAt: string;
}

// Dashboard Types
export interface DashboardStats {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  averageResolutionTime: number;
  customerSatisfactionScore: number;
  activeAgents: number;
  ticketsByPriority: { [key in TicketPriority]: number };
  ticketsByStatus: { [key in TicketStatus]: number };
  recentActivity: ActivityItem[];
}

export interface ActivityItem {
  id: number;
  type: string;
  description: string;
  userId?: number;
  ticketId?: number;
  user?: User;
  ticket?: Ticket;
  timestamp: string;
}

export interface PerformanceMetrics {
  agentId: number;
  agentName: string;
  ticketsResolved: number;
  averageResolutionTime: number;
  customerSatisfactionScore: number;
  responseTime: number;
  period: string;
}

// Notification Types
export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  message: string;
  data?: any;
  isRead: boolean;
  createdAt: string;
}

// Chat Message Types
export enum ChatMessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  SYSTEM = 'SYSTEM',
  NOTIFICATION = 'NOTIFICATION',
  EMOJI = 'EMOJI',
  QUICK_REPLY = 'QUICK_REPLY',
  TYPING = 'TYPING',
  READ_RECEIPT = 'READ_RECEIPT'
}

export interface ChatMessage {
  id: number;
  content: string;
  messageType: ChatMessageType;
  isInternal: boolean;
  isRead: boolean;
  readAt?: string;
  replyToMessageId?: number;
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  fileType?: string;
  senderId: number;
  senderName: string;
  senderEmail: string;
  senderRole: UserRole;
  senderAvatar?: string;
  ticketId: number;
  ticketNumber: string;
  createdAt: string;
  updatedAt: string;
  preview?: string;
  ageInMinutes?: number;
  hasAttachment?: boolean;
  isSystemMessage?: boolean;
  isAgentMessage?: boolean;
  isCustomerMessage?: boolean;
}

// WebSocket Message Types
export interface WebSocketMessage {
  type: 'TICKET_UPDATE' | 'NEW_COMMENT' | 'ASSIGNMENT' | 'STATUS_CHANGE' | 'NOTIFICATION' | 'CHAT_MESSAGE' | 'TYPING_STATUS' | 'READ_RECEIPT';
  data: any;
  timestamp: string;
}

// Filter Types
export interface TicketFilters {
  status?: TicketStatus[];
  priority?: TicketPriority[];
  category?: TicketCategory[];
  assignedAgentId?: number;
  customerId?: number;
  departmentId?: number;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
}

export interface UserFilters {
  role?: UserRole[];
  status?: UserStatus[];
  companyId?: number;
  departmentId?: number;
  search?: string;
}

// Report Types
export interface ReportRequest {
  reportType: string;
  filters: any;
  dateRange: {
    from: string;
    to: string;
  };
  format: 'PDF' | 'EXCEL' | 'CSV';
}

// File Upload Types
export interface FileUploadResponse {
  fileName: string;
  originalName: string;
  fileSize: number;
  fileType: string;
  uploadUrl: string;
}

// Pagination Types
export interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

// Search Types
export interface SearchParams {
  query: string;
  filters?: any;
  pagination?: PaginationParams;
}

// Form Types
export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'password' | 'textarea' | 'select' | 'multiselect' | 'date' | 'file';
  required?: boolean;
  options?: Array<{ label: string; value: any }>;
  validation?: any;
}

// Chart Data Types
export interface ChartData {
  labels: string[];
  datasets: Array<{
    label: string;
    data: number[];
    backgroundColor?: string[];
    borderColor?: string;
    borderWidth?: number;
  }>;
}

// Settings Types
export interface SystemSettings {
  companyName: string;
  companyLogo?: string;
  supportEmail: string;
  defaultLanguage: string;
  timezone: string;
  ticketAutoAssign: boolean;
  emailNotifications: boolean;
  smsNotifications: boolean;
  businessHours: {
    enabled: boolean;
    startTime: string;
    endTime: string;
    workingDays: number[];
  };
}

// Theme Types
export interface Theme {
  name: string;
  colors: {
    primary: string;
    secondary: string;
    accent: string;
    background: string;
    surface: string;
    text: string;
  };
}

export interface AppState {
  user: User | null;
  company: Company | null;
  theme: Theme;
  notifications: Notification[];
  isLoading: boolean;
  error: string | null;
}