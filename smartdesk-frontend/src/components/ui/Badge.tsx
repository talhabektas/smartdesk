import React from 'react';
import { TicketStatus, TicketPriority, UserRole, UserStatus } from '../../types';

interface BadgeProps {
  children: React.ReactNode;
  className?: string;
  variant?: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info';
  size?: 'sm' | 'md' | 'lg';
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  className = '',
  variant = 'default',
  size = 'md'
}) => {
  const baseClasses = 'inline-flex items-center font-medium rounded-full';

  const variantClasses = {
    default: 'bg-gray-100 text-gray-800',
    primary: 'bg-blue-100 text-blue-800',
    secondary: 'bg-gray-100 text-gray-800',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger: 'bg-red-100 text-red-800',
    info: 'bg-blue-100 text-blue-800'
  };

  const sizeClasses = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-0.5 text-sm',
    lg: 'px-3 py-1 text-sm'
  };

  const classes = `${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`;

  return (
    <span className={classes}>
      {children}
    </span>
  );
};

// Status-specific badge components
export const TicketStatusBadge: React.FC<{ status: TicketStatus; size?: 'sm' | 'md' | 'lg' }> = ({
  status,
  size = 'md',
}) => {
  const statusVariants: Record<TicketStatus, 'default' | 'primary' | 'warning' | 'success' | 'danger' | 'secondary' | 'info'> = {
    [TicketStatus.NEW]: 'primary',
    [TicketStatus.OPEN]: 'warning',
    [TicketStatus.IN_PROGRESS]: 'info',
    [TicketStatus.PENDING]: 'secondary',
    [TicketStatus.RESOLVED]: 'success',
    [TicketStatus.CLOSED]: 'default',
    [TicketStatus.CANCELLED]: 'danger',
    [TicketStatus.PENDING_MANAGER_APPROVAL]: 'warning',
    [TicketStatus.MANAGER_APPROVED]: 'info',
    [TicketStatus.PENDING_ADMIN_APPROVAL]: 'secondary',
  };

  const statusLabels: Record<TicketStatus, string> = {
    [TicketStatus.NEW]: 'Yeni',
    [TicketStatus.OPEN]: 'Açık',
    [TicketStatus.IN_PROGRESS]: 'İşlemde',
    [TicketStatus.PENDING]: 'Beklemede',
    [TicketStatus.RESOLVED]: 'Çözüldü',
    [TicketStatus.CLOSED]: 'Kapatıldı',
    [TicketStatus.CANCELLED]: 'İptal Edildi',
    [TicketStatus.PENDING_MANAGER_APPROVAL]: 'Manager Onayı Bekliyor',
    [TicketStatus.MANAGER_APPROVED]: 'Manager Onayladı',
    [TicketStatus.PENDING_ADMIN_APPROVAL]: 'Admin Onayı Bekliyor',
  };

  return (
    <Badge variant={statusVariants[status]} size={size}>
      {statusLabels[status]}
    </Badge>
  );
};

export const TicketPriorityBadge: React.FC<{ priority: TicketPriority; size?: 'sm' | 'md' | 'lg' }> = ({
  priority,
  size = 'md',
}) => {
  const priorityVariants: Record<TicketPriority, 'success' | 'default' | 'warning' | 'danger'> = {
    [TicketPriority.LOW]: 'success',
    [TicketPriority.NORMAL]: 'default',
    [TicketPriority.HIGH]: 'warning',
    [TicketPriority.URGENT]: 'danger',
  };

  const priorityLabels: Record<TicketPriority, string> = {
    [TicketPriority.LOW]: 'Düşük',
    [TicketPriority.NORMAL]: 'Normal',
    [TicketPriority.HIGH]: 'Yüksek',
    [TicketPriority.URGENT]: 'Acil',
  };

  return (
    <Badge variant={priorityVariants[priority]} size={size}>
      {priorityLabels[priority]}
    </Badge>
  );
};

export const UserRoleBadge: React.FC<{ role: UserRole; size?: 'sm' | 'md' | 'lg' }> = ({
  role,
  size = 'md',
}) => {
  const roleVariants: Record<UserRole, 'danger' | 'warning' | 'primary' | 'default'> = {
    [UserRole.SUPER_ADMIN]: 'danger',
    [UserRole.MANAGER]: 'warning',
    [UserRole.AGENT]: 'primary',
    [UserRole.CUSTOMER]: 'default',
  };

  const roleLabels: Record<UserRole, string> = {
    [UserRole.SUPER_ADMIN]: 'Süper Admin',
    [UserRole.MANAGER]: 'Yönetici',
    [UserRole.AGENT]: 'Temsilci',
    [UserRole.CUSTOMER]: 'Müşteri',
  };

  return (
    <Badge variant={roleVariants[role]} size={size}>
      {roleLabels[role]}
    </Badge>
  );
};

export const UserStatusBadge: React.FC<{ status: UserStatus; size?: 'sm' | 'md' | 'lg' }> = ({
  status,
  size = 'md',
}) => {
  const statusVariants: Record<UserStatus, 'success' | 'default' | 'danger' | 'warning'> = {
    [UserStatus.ACTIVE]: 'success',
    [UserStatus.INACTIVE]: 'default',
    [UserStatus.SUSPENDED]: 'danger',
    [UserStatus.PENDING]: 'warning',
  };

  const statusLabels: Record<UserStatus, string> = {
    [UserStatus.ACTIVE]: 'Aktif',
    [UserStatus.INACTIVE]: 'Pasif',
    [UserStatus.SUSPENDED]: 'Askıya Alındı',
    [UserStatus.PENDING]: 'Beklemede',
  };

  return (
    <Badge variant={statusVariants[status]} size={size}>
      {statusLabels[status]}
    </Badge>
  );
};

export default Badge;