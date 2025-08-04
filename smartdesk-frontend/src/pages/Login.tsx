import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Eye, EyeOff, Mail, Lock, ArrowRight } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { authDebugger } from '../utils/authDebugger';

interface LoginFormData {
  email: string;
  password: string;
  rememberMe: boolean;
}

const Login: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading, isAuthenticated } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);

  const from = (location.state as any)?.from?.pathname || '/';

  const {
    register,
    handleSubmit,
    formState: { errors },
    setFocus,
    setValue,
  } = useForm<LoginFormData>({
    defaultValues: {
      email: '',
      password: '',
      rememberMe: false,
    },
  });

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, from]);

  // Focus email input on mount
  useEffect(() => {
    setFocus('email');
  }, [setFocus]);

  const onSubmit = async (data: LoginFormData) => {
    try {
      console.log('🚀 Login attempt for:', data.email);
      authDebugger.info(`Starting login attempt for: ${data.email}`);
      
      // Print auth state before login
      authDebugger.printAuthState();
      
      await login(data.email, data.password);
      
      console.log('✅ Login successful, verifying state...');
      
      // Verify authentication state after login
      setTimeout(() => {
        authDebugger.printAuthState();
        const currentState = useAuthStore.getState();
        const hasToken = localStorage.getItem('accessToken');
        
        if (currentState.isAuthenticated && hasToken) {
          console.log('✅ Login verification successful, navigating to:', from);
          authDebugger.info(`Login verification successful, navigating to: ${from}`);
          navigate(from, { replace: true });
        } else {
          console.error('❌ Login verification failed!', {
            storeAuthenticated: currentState.isAuthenticated,
            hasToken: !!hasToken
          });
          authDebugger.error('Login verification failed', {
            storeAuthenticated: currentState.isAuthenticated,
            hasToken: !!hasToken
          });
          toast.error('Giriş doğrulaması başarısız. Lütfen tekrar deneyin.');
        }
      }, 150);
      
      toast.success('Başarıyla giriş yapıldı!');
    } catch (error: any) {
      console.error('❌ Login failed:', error);
      authDebugger.trackLoginAttempt(data.email, false, error);
      // Error is already handled in the store and shown via toast
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-100 flex items-center justify-center p-4">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-30">
        <div className="absolute top-10 left-10 w-72 h-72 bg-primary-200 rounded-full mix-blend-multiply filter blur-xl animate-float" />
        <div className="absolute top-10 right-10 w-72 h-72 bg-info-200 rounded-full mix-blend-multiply filter blur-xl animate-float" style={{ animationDelay: '2s' }} />
        <div className="absolute -bottom-8 left-20 w-72 h-72 bg-success-200 rounded-full mix-blend-multiply filter blur-xl animate-float" style={{ animationDelay: '4s' }} />
      </div>

      <div className="w-full max-w-md relative">
        {/* Logo and Brand */}
        <div className="text-center mb-8 animate-fade-in">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary-500 to-primary-600 rounded-2xl shadow-primary mb-4">
            <span className="text-white font-bold text-xl">SD</span>
          </div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Welcome back
          </h1>
          <p className="text-body text-gray-600 mt-2">
            Sign in to your SmartDesk account
          </p>
        </div>

        {/* Login Form */}
        <Card className="animate-slide-in-up" style={{ animationDelay: '0.1s' }}>
          {/* Demo Credentials */}
          <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div className="flex items-center justify-between mb-3">
              <div>
                <p className="text-sm font-medium text-blue-900">Demo Credentials</p>
                <p className="text-xs text-blue-600">🚀 Click to auto-fill or register new user!</p>
              </div>
              <Button
                type="button"
                variant="primary"
                size="sm"
                onClick={() => {
                  window.location.href = '/register';
                }}
              >
                Register Now
              </Button>
            </div>
            <div className="grid grid-cols-1 gap-2">
              <Button
                type="button"
                variant="secondary"
                size="sm"
                className="text-left justify-start"
                onClick={() => {
                  setValue('email', 'admin@erdemir.com.tr');
                  setValue('password', 'password123');
                }}
              >
                👑 Admin: admin@erdemir.com.tr
              </Button>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                className="text-left justify-start"
                onClick={() => {
                  setValue('email', 'mehmet.yilmaz@erdemir.com.tr');
                  setValue('password', 'password123');
                }}
              >
                👔 Manager: mehmet.yilmaz@erdemir.com.tr
              </Button>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                className="text-left justify-start"
                onClick={() => {
                  setValue('email', 'ali.kaya@erdemir.com.tr');
                  setValue('password', 'password123');
                }}
              >
                🎧 Agent: ali.kaya@erdemir.com.tr
              </Button>
            </div>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Email Field */}
            <div className="space-y-2">
              <Input
                id="email"
                type="email"
                label="Email address"
                placeholder="Enter your email"
                autoComplete="email"
                leftIcon={<Mail className="w-4 h-4" />}
                error={errors.email?.message}
                {...register('email', {
                  required: 'Email is required',
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Please enter a valid email address',
                  },
                })}
              />
            </div>

            {/* Password Field */}
            <div className="space-y-2">
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                label="Password"
                placeholder="Enter your password"
                autoComplete="current-password"
                leftIcon={<Lock className="w-4 h-4" />}
                rightIcon={
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="p-1 hover:bg-gray-100 rounded transition-colors duration-200"
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                }
                error={errors.password?.message}
                {...register('password', {
                  required: 'Password is required',
                  minLength: {
                    value: 6,
                    message: 'Password must be at least 6 characters',
                  },
                })}
              />
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  className="w-4 h-4 text-primary-600 bg-gray-100 border-gray-300 rounded focus:ring-primary-500 focus:ring-2"
                  {...register('rememberMe')}
                />
                <span className="text-sm text-gray-700">Remember me</span>
              </label>
              
              <Link
                to="/forgot-password"
                className="text-sm text-primary-600 hover:text-primary-700 font-medium transition-colors duration-200"
              >
                Forgot password?
              </Link>
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="primary"
              size="lg"
              fullWidth
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              {isLoading ? 'Signing in...' : 'Sign in'}
            </Button>
          </form>

          {/* Divider */}
          <div className="my-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-white text-gray-500">
                  Don't have an account?
                </span>
              </div>
            </div>
          </div>

          {/* Register Link */}
          <div className="text-center">
            <Link
              to="/register"
              className="inline-flex items-center gap-2 text-primary-600 hover:text-primary-700 font-medium transition-colors duration-200"
            >
              Create new account
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </Card>

        {/* Demo Credentials */}
        {process.env.NODE_ENV === 'development' && (
          <Card className="mt-6 bg-info-50 border-info-200 animate-slide-in-up" style={{ animationDelay: '0.2s' }}>
            <div className="text-center">
              <h3 className="text-sm font-medium text-info-800 mb-2">
                Demo Credentials
              </h3>
              <div className="space-y-1 text-xs text-info-700">
                <p><strong>Admin:</strong> admin@erdemir.com.tr / password123</p>
                <p><strong>Manager:</strong> mehmet.yilmaz@erdemir.com.tr / password123</p>
                <p><strong>Agent:</strong> ali.kaya@erdemir.com.tr / password123</p>
              </div>
            </div>
          </Card>
        )}

        {/* Footer */}
        <div className="text-center mt-8 text-xs text-gray-500 animate-fade-in" style={{ animationDelay: '0.3s' }}>
          <p>
            By signing in, you agree to our{' '}
            <Link to="/terms" className="text-primary-600 hover:text-primary-700">
              Terms of Service
            </Link>{' '}
            and{' '}
            <Link to="/privacy" className="text-primary-600 hover:text-primary-700">
              Privacy Policy
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;