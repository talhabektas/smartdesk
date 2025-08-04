import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { 
  User, 
  Mail, 
  Phone, 
  Building2, 
  Briefcase, 
  Shield,
  Save,
  X,
  Edit
} from 'lucide-react';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../../store/authStore';

// Types
interface EditUserFormData {
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: 'SUPER_ADMIN' | 'MANAGER' | 'AGENT' | 'CUSTOMER';
  companyId: number;
  departmentId?: number;
}

interface Company {
  id: number;
  name: string;
  domain: string;
}

interface Department {
  id: number;
  name: string;
  companyId: number;
}

interface UserToEdit {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: 'SUPER_ADMIN' | 'MANAGER' | 'AGENT' | 'CUSTOMER';
  status: string;
  phone?: string;
  companyId?: number;
  companyName?: string;
  departmentId?: number;
  departmentName?: string;
  permissions: {
    canEdit: boolean;
    canDelete: boolean;
    canChangeRole: boolean;
    canActivateDeactivate: boolean;
    canResetPassword: boolean;
    canUnlock: boolean;
  };
}

interface EditUserModalProps {
  isOpen: boolean;
  user: UserToEdit | null;
  onClose: () => void;
  onUserUpdated: () => void;
}

const EditUserModal: React.FC<EditUserModalProps> = ({
  isOpen,
  user: userToEdit,
  onClose,
  onUserUpdated
}) => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors }
  } = useForm<EditUserFormData>();

  const watchedCompanyId = watch('companyId');

  // Role hierarchy for permissions
  const roleHierarchy = {
    'CUSTOMER': 1,
    'AGENT': 2,
    'MANAGER': 3,
    'SUPER_ADMIN': 4
  };

  const currentUserLevel = roleHierarchy[user?.role as keyof typeof roleHierarchy] || 0;

  // Available roles based on current user permissions
  const availableRoles = [
    { value: 'CUSTOMER', label: 'Customer', level: 1 },
    { value: 'AGENT', label: 'Agent', level: 2 },
    { value: 'MANAGER', label: 'Manager', level: 3 },
    { value: 'SUPER_ADMIN', label: 'Super Admin', level: 4 }
  ].filter(role => role.level < currentUserLevel);

  const roleColors = {
    SUPER_ADMIN: 'danger',
    MANAGER: 'primary',
    AGENT: 'info',
    CUSTOMER: 'success'
  } as const;

  // Check if current user can edit role
  const canEditRole = userToEdit?.permissions?.canChangeRole ?? false;

  // Initialize form when user data changes
  useEffect(() => {
    if (userToEdit && isOpen) {
      reset({
        email: userToEdit.email,
        firstName: userToEdit.firstName,
        lastName: userToEdit.lastName,
        phone: userToEdit.phone || '',
        role: userToEdit.role,
        companyId: userToEdit.companyId || 0,
        departmentId: userToEdit.departmentId || 0
      });
    }
  }, [userToEdit, isOpen, reset]);

  // Load companies
  useEffect(() => {
    const loadCompanies = async () => {
      try {
        if (user?.role === 'SUPER_ADMIN') {
          const response = await api.get('/companies');
          setCompanies(response.companies || []);
        } else if (user?.company) {
          setCompanies([{
            id: user.company.id,
            name: user.company.name,
            domain: user.company.domain || ''
          }]);
        }
      } catch (error) {
        console.error('Error loading companies:', error);
        toast.error('Failed to load companies');
      }
    };

    if (isOpen) {
      loadCompanies();
    }
  }, [isOpen, user]);

  // Load departments when company changes
  useEffect(() => {
    const loadDepartments = async () => {
      if (!watchedCompanyId) {
        setDepartments([]);
        return;
      }

      try {
        const response = await api.get(`/departments/company/${watchedCompanyId}`);
        setDepartments(response.departments || []);
      } catch (error) {
        console.error('Error loading departments:', error);
        setDepartments([]);
      }
    };

    if (watchedCompanyId) {
      loadDepartments();
    }
  }, [watchedCompanyId]);

  const onSubmit = async (data: EditUserFormData) => {
    if (!userToEdit) return;

    setLoading(true);

    try {
      const updateUserRequest = {
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone || null,
        role: canEditRole ? data.role : undefined,
        companyId: user?.role === 'SUPER_ADMIN' ? data.companyId : undefined,
        departmentId: data.departmentId || null
      };

      await api.put(`/users/${userToEdit.id}`, updateUserRequest);
      
      toast.success('User updated successfully');
      onUserUpdated();
      onClose();

    } catch (error: any) {
      console.error('Error updating user:', error);
      toast.error(error.response?.data?.message || 'Failed to update user');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  if (!userToEdit) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={`Edit User: ${userToEdit.fullName}`}
      description="Update user information and permissions"
      size="2xl"
      className="max-h-[90vh] overflow-y-auto"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Personal Information */}
        <div>
          <h4 className="text-lg font-medium text-gray-900 mb-4 flex items-center gap-2">
            <User className="w-5 h-5" />
            Personal Information
          </h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="First Name"
              leftIcon={<User className="w-4 h-4" />}
              {...register('firstName', { 
                required: 'First name is required',
                maxLength: { value: 100, message: 'First name cannot exceed 100 characters' }
              })}
              error={errors.firstName?.message}
              required
            />
            
            <Input
              label="Last Name"
              leftIcon={<User className="w-4 h-4" />}
              {...register('lastName', { 
                required: 'Last name is required',
                maxLength: { value: 100, message: 'Last name cannot exceed 100 characters' }
              })}
              error={errors.lastName?.message}
              required
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <Input
              label="Email Address"
              type="email"
              leftIcon={<Mail className="w-4 h-4" />}
              {...register('email', { 
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Please provide a valid email address'
                }
              })}
              error={errors.email?.message}
              required
            />
            
            <Input
              label="Phone Number"
              type="tel"
              leftIcon={<Phone className="w-4 h-4" />}
              {...register('phone', {
                maxLength: { value: 20, message: 'Phone number cannot exceed 20 characters' }
              })}
              error={errors.phone?.message}
              helperText="Optional"
            />
          </div>
        </div>

        {/* Role & Organization */}
        <div>
          <h4 className="text-lg font-medium text-gray-900 mb-4 flex items-center gap-2">
            <Shield className="w-5 h-5" />
            Role & Organization
          </h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Role Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Role {canEditRole && <span className="text-danger-500">*</span>}
              </label>
              {canEditRole ? (
                <div className="space-y-2">
                  {availableRoles.map((role) => (
                    <label key={role.value} className="flex items-center space-x-3">
                      <input
                        type="radio"
                        value={role.value}
                        {...register('role', { required: 'Role is required' })}
                        className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                      />
                      <div className="flex items-center gap-2">
                        <Badge variant={roleColors[role.value as keyof typeof roleColors]}>
                          {role.label}
                        </Badge>
                      </div>
                    </label>
                  ))}
                </div>
              ) : (
                <div className="p-3 bg-gray-50 rounded-md">
                  <Badge variant={roleColors[userToEdit.role]}>
                    {userToEdit.role.replace('_', ' ')}
                  </Badge>
                  <p className="text-sm text-gray-500 mt-1">
                    You don't have permission to change this user's role
                  </p>
                </div>
              )}
              {errors.role && <p className="mt-1 text-sm text-danger-600">{errors.role.message}</p>}
            </div>

            {/* Company & Department */}
            <div className="space-y-4">
              {/* Company Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Company {user?.role === 'SUPER_ADMIN' && <span className="text-danger-500">*</span>}
                </label>
                {user?.role === 'SUPER_ADMIN' ? (
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <select
                      {...register('companyId', { 
                        required: 'Company is required',
                        valueAsNumber: true 
                      })}
                      className="block w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                    >
                      <option value="">Select Company</option>
                      {companies.map((company) => (
                        <option key={company.id} value={company.id}>
                          {company.name}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div className="p-3 bg-gray-50 rounded-md flex items-center gap-2">
                    <Building2 className="w-4 h-4 text-gray-400" />
                    <span className="text-gray-700">{userToEdit.companyName}</span>
                  </div>
                )}
                {errors.companyId && <p className="mt-1 text-sm text-danger-600">{errors.companyId.message}</p>}
              </div>

              {/* Department Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Department
                </label>
                <div className="relative">
                  <Briefcase className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <select
                    {...register('departmentId', { valueAsNumber: true })}
                    className="block w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                    disabled={!watchedCompanyId || departments.length === 0}
                  >
                    <option value="">Select Department (Optional)</option>
                    {departments.map((department) => (
                      <option key={department.id} value={department.id}>
                        {department.name}
                      </option>
                    ))}
                  </select>
                </div>
                <p className="mt-1 text-sm text-gray-500">
                  {!watchedCompanyId ? 'Select a company first' : 
                   departments.length === 0 ? 'No departments available' : 
                   'Optional - leave empty to remove department assignment'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-200">
          <Button
            type="button"
            variant="secondary"
            onClick={handleClose}
            leftIcon={<X className="w-4 h-4" />}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            leftIcon={<Save className="w-4 h-4" />}
            disabled={loading}
            isLoading={loading}
          >
            {loading ? 'Updating User...' : 'Update User'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default EditUserModal;