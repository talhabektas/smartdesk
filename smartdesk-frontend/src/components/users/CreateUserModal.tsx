import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { 
  User, 
  Mail, 
  Lock, 
  Phone, 
  Building2, 
  Briefcase, 
  Shield,
  Save,
  X,
  Eye,
  EyeOff
} from 'lucide-react';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../../store/authStore';

// Types
interface CreateUserFormData {
  email: string;
  password: string;
  confirmPassword: string;
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

interface CreateUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUserCreated: () => void;
}

const CreateUserModal: React.FC<CreateUserModalProps> = ({
  isOpen,
  onClose,
  onUserCreated
}) => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors }
  } = useForm<CreateUserFormData>({
    defaultValues: {
      role: 'CUSTOMER',
      companyId: user?.company?.id || 0
    }
  });

  const watchedCompanyId = watch('companyId');
  const watchedPassword = watch('password');

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

  // Load companies (only SUPER_ADMIN can see all companies)
  useEffect(() => {
    const loadCompanies = async () => {
      try {
        if (user?.role === 'SUPER_ADMIN') {
          const response = await api.get('/companies');
          setCompanies(response.companies || []);
        } else if (user?.company) {
          // Non-SUPER_ADMIN users can only create users in their own company
          setCompanies([{
            id: user.company.id,
            name: user.company.name,
            domain: user.company.domain || ''
          }]);
          setValue('companyId', user.company.id);
        }
      } catch (error) {
        console.error('Error loading companies:', error);
        toast.error('Failed to load companies');
      }
    };

    if (isOpen) {
      loadCompanies();
    }
  }, [isOpen, user, setValue]);

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

  const onSubmit = async (data: CreateUserFormData) => {
    if (data.password !== data.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setLoading(true);

    try {
      const createUserRequest = {
        email: data.email,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone || null,
        role: data.role,
        companyId: data.companyId,
        departmentId: data.departmentId || null
      };

      await api.post('/users', createUserRequest);
      
      toast.success('User created successfully');
      reset();
      onUserCreated();
      onClose();

    } catch (error: any) {
      console.error('Error creating user:', error);
      toast.error(error.response?.data?.message || 'Failed to create user');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Create New User"
      description="Add a new user to your organization"
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

        {/* Account Information */}
        <div>
          <h4 className="text-lg font-medium text-gray-900 mb-4 flex items-center gap-2">
            <Lock className="w-5 h-5" />
            Account Information
          </h4>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="relative">
              <Input
                label="Password"
                type={showPassword ? 'text' : 'password'}
                leftIcon={<Lock className="w-4 h-4" />}
                rightIcon={
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                }
                {...register('password', { 
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Password must be at least 8 characters long' }
                })}
                error={errors.password?.message}
                required
              />
            </div>
            
            <div className="relative">
              <Input
                label="Confirm Password"
                type={showConfirmPassword ? 'text' : 'password'}
                leftIcon={<Lock className="w-4 h-4" />}
                rightIcon={
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                }
                {...register('confirmPassword', { 
                  required: 'Please confirm password',
                  validate: value => value === watchedPassword || 'Passwords do not match'
                })}
                error={errors.confirmPassword?.message}
                required
              />
            </div>
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
                Role <span className="text-danger-500">*</span>
              </label>
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
              {errors.role && <p className="mt-1 text-sm text-danger-600">{errors.role.message}</p>}
            </div>

            {/* Company & Department */}
            <div className="space-y-4">
              {/* Company Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Company <span className="text-danger-500">*</span>
                </label>
                <div className="relative">
                  <Building2 className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <select
                    {...register('companyId', { 
                      required: 'Company is required',
                      valueAsNumber: true 
                    })}
                    className="block w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500"
                    disabled={user?.role !== 'SUPER_ADMIN'}
                  >
                    <option value="">Select Company</option>
                    {companies.map((company) => (
                      <option key={company.id} value={company.id}>
                        {company.name}
                      </option>
                    ))}
                  </select>
                </div>
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
                   'Optional - user can be assigned to department later'}
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
            {loading ? 'Creating User...' : 'Create User'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CreateUserModal;