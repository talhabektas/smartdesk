import React, { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '../store/authStore';
import { 
  Users as UsersIcon, 
  User, 
  Settings, 
  Shield, 
  Eye, 
  Edit, 
  Trash2, 
  UserPlus, 
  Search, 
  Filter, 
  MoreVertical, 
  Lock, 
  Unlock, 
  RefreshCw,
  ChevronDown,
  Calendar,
  Mail,
  Phone,
  Building2,
  Briefcase,
  CheckCircle,
  XCircle,
  AlertCircle,
  Clock,
  UserCheck,
  UserX,
  Download
} from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { ConfirmationModal } from '../components/ui/ConfirmationModal';
import { toast } from 'react-hot-toast';
import { api } from '../services/api';
import CreateUserModal from '../components/users/CreateUserModal';
import EditUserModal from '../components/users/EditUserModal';

// Debug: Check if imports are valid
console.log('Debug - CreateUserModal:', CreateUserModal);
console.log('Debug - EditUserModal:', EditUserModal);
console.log('Debug - ConfirmationModal:', ConfirmationModal);

// Types
interface UserPermissions {
  canEdit: boolean;
  canDelete: boolean;
  canChangeRole: boolean;
  canActivateDeactivate: boolean;
  canResetPassword: boolean;
  canUnlock: boolean;
}

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: 'SUPER_ADMIN' | 'MANAGER' | 'AGENT' | 'CUSTOMER';
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING' | 'SUSPENDED';
  phone?: string;
  companyId?: number;
  companyName?: string;
  departmentId?: number;
  departmentName?: string;
  emailVerified?: boolean;
  accountLocked?: boolean;
  loginAttempts?: number;
  createdAt: string;
  updatedAt?: string;
  lastLoginAt?: string;
  permissions: UserPermissions;
}

interface UserSearchRequest {
  searchTerm?: string;
  roles?: string[];
  statuses?: string[];
  companyId?: number;
  departmentId?: number;
  createdAfter?: string;
  createdBefore?: string;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
  activeOnly?: boolean;
  emailVerified?: boolean;
  includeLocked?: boolean;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  isFirst: boolean;
  isLast: boolean;
}

const Users: React.FC = () => {
  const { user } = useAuthStore();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [selectedStatuses, setSelectedStatuses] = useState<string[]>([]);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [selectedUsers, setSelectedUsers] = useState<Set<number>>(new Set());
  const [createUserModalOpen, setCreateUserModalOpen] = useState(false);
  const [editUserModalOpen, setEditUserModalOpen] = useState(false);
  const [selectedUserForEdit, setSelectedUserForEdit] = useState<User | null>(null);
  
  // Bulk operation states
  const [bulkOperationLoading, setBulkOperationLoading] = useState(false);
  const [confirmationModal, setConfirmationModal] = useState<{
    isOpen: boolean;
    type: 'activate' | 'deactivate' | 'delete';
    title: string;
    message: string;
    confirmText: string;
    confirmVariant: 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost' | 'outline' | 'info';
    icon?: React.ReactNode;
  }>({ 
    isOpen: false, 
    type: 'activate', 
    title: '', 
    message: '', 
    confirmText: '', 
    confirmVariant: 'primary' 
  });

  // Role-based permissions
  const roleHierarchy = {
    'CUSTOMER': 1,
    'AGENT': 2,
    'MANAGER': 3,
    'SUPER_ADMIN': 4
  };

  const currentUserLevel = roleHierarchy[user?.role as keyof typeof roleHierarchy] || 0;

  // Permissions
  const canViewAllUsers = currentUserLevel >= 3; // MANAGER and above
  const canCreateUser = currentUserLevel >= 3;
  const canBulkActions = currentUserLevel >= 3;

  const roleColors = {
    SUPER_ADMIN: 'danger',
    MANAGER: 'primary',
    AGENT: 'info',
    CUSTOMER: 'success'
  } as const;

  const statusColors = {
    ACTIVE: 'success',
    INACTIVE: 'warning',
    PENDING: 'info',
    SUSPENDED: 'danger'
  } as const;

  const statusIcons = {
    ACTIVE: CheckCircle,
    INACTIVE: XCircle,
    PENDING: Clock,
    SUSPENDED: AlertCircle
  };

  // Load users with advanced search
  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);

      if (!user || !canViewAllUsers) {
        console.log('User permissions insufficient for user management');
        return;
      }

      const searchRequest: UserSearchRequest = {
        searchTerm: searchQuery || undefined,
        roles: selectedRoles.length > 0 ? selectedRoles : undefined,
        statuses: selectedStatuses.length > 0 ? selectedStatuses : undefined,
        companyId: user.role !== 'SUPER_ADMIN' ? user.company?.id : undefined,
        sortBy,
        sortDirection,
        page: currentPage,
        size: pageSize,
        activeOnly: false,
        includeLocked: true
      };

      console.log('🔍 Searching users with:', searchRequest);

      const response: PageResponse<User> = await api.post('/users/search', searchRequest);

      setUsers(response.content);
      setTotalElements(response.totalElements);
      setTotalPages(response.totalPages);

      console.log(`✅ Loaded ${response.content.length} users`);

    } catch (error: any) {
      console.error('❌ Error loading users:', error);
      toast.error(error.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [
    user,
    searchQuery,
    selectedRoles,
    selectedStatuses,
    sortBy,
    sortDirection,
    currentPage,
    pageSize,
    canViewAllUsers
  ]);

  useEffect(() => {
    if (user) {
      loadUsers();
    }
  }, [loadUsers]);

  // Handle user actions
  const handleEditUser = (userId: number) => {
    const userToEdit = users.find(u => u.id === userId);
    if (userToEdit) {
      setSelectedUserForEdit(userToEdit);
      setEditUserModalOpen(true);
    } else {
      toast.error('User not found');
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      await api.delete(`/users/${userId}`);
      toast.success('User deleted successfully');
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete user');
    }
  };

  const handleToggleUserStatus = async (userId: number, currentStatus: string) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const action = newStatus === 'ACTIVE' ? 'activate' : 'deactivate';

    try {
      await api.patch(`/users/${userId}/${action}`);
      toast.success(`User ${action}d successfully`);
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || `Failed to ${action} user`);
    }
  };

  const handleUnlockUser = async (userId: number) => {
    try {
      await api.patch(`/users/${userId}/unlock`);
      toast.success('User unlocked successfully');
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to unlock user');
    }
  };

  // Export functionality
  const handleExport = (format: 'csv' | 'excel' = 'csv') => {
    if (selectedUsers.size === 0) {
      toast.error('Please select users to export');
      return;
    }

    const selectedUserData = users.filter(user => selectedUsers.has(user.id));
    const exportData = selectedUserData.map(user => ({
      'Full Name': user.fullName,
      'Email': user.email,
      'Role': user.role.replace('_', ' '),
      'Status': user.status,
      'Company': user.companyName || '',
      'Department': user.departmentName || '',
      'Phone': user.phone || '',
      'Email Verified': user.emailVerified ? 'Yes' : 'No',
      'Account Locked': user.accountLocked ? 'Yes' : 'No',
      'Created Date': new Date(user.createdAt).toLocaleDateString(),
      'Last Login': user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleDateString() : 'Never'
    }));

    if (format === 'csv') {
      // Convert to CSV
      const headers = Object.keys(exportData[0]);
      const csvContent = [
        headers.join(','),
        ...exportData.map(row => 
          headers.map(header => {
            const value = row[header as keyof typeof row];
            // Escape commas and quotes in CSV
            return typeof value === 'string' && (value.includes(',') || value.includes('"')) 
              ? `"${value.replace(/"/g, '""')}"` 
              : value;
          }).join(',')
        )
      ].join('\n');

      // Download CSV file
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `users_export_${new Date().toISOString().split('T')[0]}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      toast.success(`Exported ${selectedUsers.size} users to CSV`);
    } else if (format === 'excel') {
      // For Excel export, we'll use a simple TSV format that Excel can open
      const headers = Object.keys(exportData[0]);
      const tsvContent = [
        headers.join('\t'),
        ...exportData.map(row => 
          headers.map(header => row[header as keyof typeof row]).join('\t')
        )
      ].join('\n');

      // Download Excel-compatible file
      const blob = new Blob([tsvContent], { type: 'application/vnd.ms-excel;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `users_export_${new Date().toISOString().split('T')[0]}.xlsx`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      toast.success(`Exported ${selectedUsers.size} users to Excel`);
    }

    // Clear selection after export
    setSelectedUsers(new Set());
  };

  // Bulk actions
  const handleBulkAction = (action: 'activate' | 'deactivate' | 'delete') => {
    if (selectedUsers.size === 0) {
      toast.error('Please select users first');
      return;
    }

    const selectedCount = selectedUsers.size;
    const actionConfigs = {
      activate: {
        title: 'Activate Users',
        message: `Are you sure you want to activate ${selectedCount} selected user${selectedCount > 1 ? 's' : ''}? This will enable their access to the system.`,
        confirmText: 'Activate Users',
        confirmVariant: 'success' as const,
        icon: <UserCheck className="w-6 h-6 text-success-500" />
      },
      deactivate: {
        title: 'Deactivate Users',
        message: `Are you sure you want to deactivate ${selectedCount} selected user${selectedCount > 1 ? 's' : ''}? This will disable their access to the system.`,
        confirmText: 'Deactivate Users',
        confirmVariant: 'warning' as const,
        icon: <UserX className="w-6 h-6 text-warning-500" />
      },
      delete: {
        title: 'Delete Users',
        message: `Are you sure you want to permanently delete ${selectedCount} selected user${selectedCount > 1 ? 's' : ''}? This action cannot be undone and will remove all associated data.`,
        confirmText: 'Delete Users',
        confirmVariant: 'danger' as const,
        icon: <Trash2 className="w-6 h-6 text-danger-500" />
      }
    };

    const config = actionConfigs[action];
    setConfirmationModal({
      isOpen: true,
      type: action,
      ...config
    });
  };

  const executeBulkAction = async () => {
    if (selectedUsers.size === 0) return;

    setBulkOperationLoading(true);
    const userIds = Array.from(selectedUsers);
    const { type } = confirmationModal;
    
    try {
      const promises = userIds.map(userId => {
        switch (type) {
          case 'activate':
            return api.patch(`/users/${userId}/activate`);
          case 'deactivate':
            return api.patch(`/users/${userId}/deactivate`);
          case 'delete':
            return api.delete(`/users/${userId}`);
          default:
            throw new Error('Invalid bulk action type');
        }
      });

      const results = await Promise.allSettled(promises);
      
      // Process results
      const successful = results.filter(result => result.status === 'fulfilled').length;
      const failed = results.filter(result => result.status === 'rejected').length;
      
      if (successful === userIds.length) {
        // All operations successful
        const actionPastTense = type === 'delete' ? 'deleted' : `${type}d`;
        toast.success(`Successfully ${actionPastTense} ${successful} user${successful > 1 ? 's' : ''}`);
      } else if (successful > 0) {
        // Partial success
        const actionPastTense = type === 'delete' ? 'deleted' : `${type}d`;
        toast.success(`Successfully ${actionPastTense} ${successful} user${successful > 1 ? 's' : ''}`);
        toast.error(`Failed to ${type} ${failed} user${failed > 1 ? 's' : ''}`);
      } else {
        // All failed
        toast.error(`Failed to ${type} selected users`);
      }
      
      // Clear selection and reload users
      setSelectedUsers(new Set());
      await loadUsers();
      
    } catch (error: any) {
      console.error(`❌ Error in bulk ${type}:`, error);
      toast.error(error.response?.data?.message || `Failed to ${type} users`);
    } finally {
      setBulkOperationLoading(false);
      setConfirmationModal(prev => ({ ...prev, isOpen: false }));
    }
  };

  // Pagination
  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
  };

  // Sorting
  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setCurrentPage(0);
  };

  // Filters
  const clearFilters = () => {
    setSearchQuery('');
    setSelectedRoles([]);
    setSelectedStatuses([]);
    setCurrentPage(0);
  };

  const handleRoleFilter = (role: string) => {
    setSelectedRoles(prev =>
      prev.includes(role)
        ? prev.filter(r => r !== role)
        : [...prev, role]
    );
    setCurrentPage(0);
  };

  const handleStatusFilter = (status: string) => {
    setSelectedStatuses(prev =>
      prev.includes(status)
        ? prev.filter(s => s !== status)
        : [...prev, status]
    );
    setCurrentPage(0);
  };

  // User selection
  const handleSelectUser = (userId: number) => {
    setSelectedUsers(prev => {
      const newSet = new Set(prev);
      if (newSet.has(userId)) {
        newSet.delete(userId);
      } else {
        newSet.add(userId);
      }
      return newSet;
    });
  };

  const handleSelectAll = () => {
    if (selectedUsers.size === users.length && users.length > 0) {
      setSelectedUsers(new Set());
    } else {
      setSelectedUsers(new Set(users.map(u => u.id)));
    }
  };

  if (!canViewAllUsers) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <Card className="text-center p-8">
          <Shield className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Access Restricted
          </h3>
          <p className="text-gray-600">
            You don't have permission to view user management.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold flex items-center gap-2">
            <UsersIcon className="w-8 h-8" />
            User Management
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Manage users, roles, and permissions for your organization
          </p>
        </div>
        <div className="flex items-center gap-3">
          {canBulkActions && selectedUsers.size > 0 && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-600">
                {selectedUsers.size} selected
              </span>
              <Button
                variant="success"
                size="sm"
                onClick={() => handleBulkAction('activate')}
                leftIcon={<UserCheck className="w-4 h-4" />}
                disabled={bulkOperationLoading}
              >
                Activate
              </Button>
              <Button
                variant="warning"
                size="sm"
                onClick={() => handleBulkAction('deactivate')}
                leftIcon={<UserX className="w-4 h-4" />}
                disabled={bulkOperationLoading}
              >
                Deactivate
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={() => handleBulkAction('delete')}
                leftIcon={<Trash2 className="w-4 h-4" />}
                disabled={bulkOperationLoading}
              >
                Delete
              </Button>
              <Button
                variant="secondary" 
                size="sm"
                onClick={() => handleExport('csv')}
                leftIcon={<Download className="w-4 h-4" />}
                disabled={bulkOperationLoading}
              >
                Export CSV
              </Button>
              <Button
                variant="secondary"
                size="sm" 
                onClick={() => handleExport('excel')}
                leftIcon={<Download className="w-4 h-4" />}
                disabled={bulkOperationLoading}
              >
                Export Excel
              </Button>
            </div>
          )}
          <Button
            variant="secondary"
            onClick={loadUsers}
            disabled={loading}
            leftIcon={<RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          {canCreateUser && (
            <Button
              variant="primary"
              leftIcon={<UserPlus className="w-4 h-4" />}
              onClick={() => setCreateUserModalOpen(true)}
            >
              Add User
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card>
        <div className="space-y-4">
          {/* Search and Filter Toggle */}
          <div className="flex items-center gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <Input
                type="text"
                placeholder="Search users by name or email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button
              variant="secondary"
              leftIcon={<Filter className="w-4 h-4" />}
              rightIcon={<ChevronDown className={`w-4 h-4 transition-transform ${filtersOpen ? 'rotate-180' : ''}`} />}
              onClick={() => setFiltersOpen(!filtersOpen)}
            >
              Filters
            </Button>
            {(selectedRoles.length > 0 || selectedStatuses.length > 0) && (
              <Button
                variant="secondary"
                onClick={clearFilters}
              >
                Clear Filters
              </Button>
            )}
          </div>

          {/* Advanced Filters */}
          {filtersOpen && (
            <div className="border-t pt-4 space-y-4">
              {/* Role Filters */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Filter by Role
                </label>
                <div className="flex flex-wrap gap-2">
                  {['SUPER_ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER'].map(role => (
                    <Button
                      key={role}
                      variant={selectedRoles.includes(role) ? 'primary' : 'secondary'}
                      size="sm"
                      onClick={() => handleRoleFilter(role)}
                    >
                      {role.replace('_', ' ')}
                    </Button>
                  ))}
                </div>
              </div>

              {/* Status Filters */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Filter by Status
                </label>
                <div className="flex flex-wrap gap-2">
                  {['ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED'].map(status => (
                    <Button
                      key={status}
                      variant={selectedStatuses.includes(status) ? 'primary' : 'secondary'}
                      size="sm"
                      onClick={() => handleStatusFilter(status)}
                    >
                      {status}
                    </Button>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      </Card>

      {/* Users Table */}
      <Card>
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <RefreshCw className="w-8 h-8 text-gray-400 animate-spin mx-auto mb-4" />
              <p className="text-gray-600">Loading users...</p>
            </div>
          </div>
        ) : users.length === 0 ? (
          <div className="text-center py-12">
            <UsersIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-600">No users found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4">
                    <input
                      type="checkbox"
                      checked={selectedUsers.size === users.length && users.length > 0}
                      onChange={handleSelectAll}
                      className="rounded"
                    />
                  </th>
                  <th 
                    className="text-left py-3 px-4 cursor-pointer hover:bg-gray-50"
                    onClick={() => handleSort('firstName')}
                  >
                    <div className="flex items-center gap-2">
                      Name
                      {sortBy === 'firstName' && (
                        <span className="text-xs">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-left py-3 px-4 cursor-pointer hover:bg-gray-50"
                    onClick={() => handleSort('email')}
                  >
                    <div className="flex items-center gap-2">
                      Email
                      {sortBy === 'email' && (
                        <span className="text-xs">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th className="text-left py-3 px-4">Role</th>
                  <th className="text-left py-3 px-4">Status</th>
                  <th className="text-left py-3 px-4">Company</th>
                  <th 
                    className="text-left py-3 px-4 cursor-pointer hover:bg-gray-50"
                    onClick={() => handleSort('createdAt')}
                  >
                    <div className="flex items-center gap-2">
                      Created
                      {sortBy === 'createdAt' && (
                        <span className="text-xs">
                          {sortDirection === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                  <th className="text-left py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const StatusIcon = statusIcons[user.status];
                  
                  // Debug: Log if StatusIcon is undefined
                  if (!StatusIcon) {
                    console.error('StatusIcon undefined for user:', user.email, 'status:', user.status);
                    console.log('Available statusIcons:', Object.keys(statusIcons));
                    console.log('statusColors keys:', Object.keys(statusColors));
                  }
                  
                  return (
                    <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-3 px-4">
                        <input
                          type="checkbox"
                          checked={selectedUsers.has(user.id)}
                          onChange={() => handleSelectUser(user.id)}
                          className="rounded"
                        />
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                            <User className="w-4 h-4 text-primary-600" />
                          </div>
                          <div>
                            <p className="font-medium text-gray-900">{user.fullName}</p>
                            {user.phone && (
                              <p className="text-sm text-gray-500 flex items-center gap-1">
                                <Phone className="w-3 h-3" />
                                {user.phone}
                              </p>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          <Mail className="w-4 h-4 text-gray-400" />
                          <span className="text-sm text-gray-900">{user.email}</span>
                          {user.emailVerified && (
                            <CheckCircle className="w-4 h-4 text-success-500" />
                          )}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <Badge variant={roleColors[user.role]}>
                          {user.role.replace('_', ' ')}
                        </Badge>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          {StatusIcon ? (
                            <StatusIcon className={`w-4 h-4 text-${statusColors[user.status]}-500`} />
                          ) : (
                            <AlertCircle className={`w-4 h-4 text-gray-500`} />
                          )}
                          <Badge variant={statusColors[user.status] || 'secondary'}>
                            {user.status}
                          </Badge>
                          {user.accountLocked && (
                            <Lock className="w-4 h-4 text-danger-500" />
                          )}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          <Building2 className="w-4 h-4 text-gray-400" />
                          <div>
                            <p className="text-sm text-gray-900">{user.companyName}</p>
                            {user.departmentName && (
                              <p className="text-xs text-gray-500 flex items-center gap-1">
                                <Briefcase className="w-3 h-3" />
                                {user.departmentName}
                              </p>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2 text-sm text-gray-500">
                          <Calendar className="w-4 h-4" />
                          {new Date(user.createdAt).toLocaleDateString()}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          {user.permissions.canEdit && (
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => handleEditUser(user.id)}
                              leftIcon={<Edit className="w-4 h-4" />}
                            >
                              Edit
                            </Button>
                          )}
                          {user.permissions.canActivateDeactivate && (
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => handleToggleUserStatus(user.id, user.status)}
                            >
                              {user.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                            </Button>
                          )}
                          {user.permissions.canUnlock && user.accountLocked && (
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => handleUnlockUser(user.id)}
                              leftIcon={<Unlock className="w-4 h-4" />}
                            >
                              Unlock
                            </Button>
                          )}
                          {user.permissions.canDelete && (
                            <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleDeleteUser(user.id)}
                              leftIcon={<Trash2 className="w-4 h-4" />}
                            >
                              Delete
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="border-t border-gray-200 px-4 py-3 flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} users
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-600">
                Page {currentPage + 1} of {totalPages}
              </span>
              <Button
                variant="secondary"
                size="sm"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* Create User Modal */}
      <CreateUserModal
        isOpen={createUserModalOpen}
        onClose={() => setCreateUserModalOpen(false)}
        onUserCreated={loadUsers}
      />

      {/* Edit User Modal */}
      <EditUserModal
        isOpen={editUserModalOpen}
        user={selectedUserForEdit}
        onClose={() => {
          setEditUserModalOpen(false);
          setSelectedUserForEdit(null);
        }}
        onUserUpdated={loadUsers}
      />

      {/* Bulk Action Confirmation Modal */}
      <ConfirmationModal
        isOpen={confirmationModal.isOpen}
        onClose={() => setConfirmationModal(prev => ({ ...prev, isOpen: false }))}
        onConfirm={executeBulkAction}
        title={confirmationModal.title}
        message={confirmationModal.message}
        confirmText={confirmationModal.confirmText}
        confirmVariant={confirmationModal.confirmVariant}
        isLoading={bulkOperationLoading}
        icon={confirmationModal.icon}
        type={confirmationModal.type === 'delete' ? 'danger' : confirmationModal.type === 'activate' ? 'success' : 'warning'}
      />
    </div>
  );
};

export default Users;