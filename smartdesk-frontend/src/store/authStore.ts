import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, Company, UserRole } from '../types';
import { authService } from '../services/authService';
import { webSocketService } from '../services/websocket';
import { toast } from 'react-hot-toast';

interface AuthState {
  // State
  user: User | null;
  company: Company | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (userData: any) => Promise<void>;
  logout: () => Promise<void>;
  getCurrentUser: () => Promise<void>;
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
  requestPasswordReset: (email: string) => Promise<void>;
  resetPassword: (token: string, newPassword: string) => Promise<void>;
  verifyEmail: (token: string) => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
  
  // Helper methods
  hasRole: (role: UserRole) => boolean;
  hasPermission: (role: UserRole) => boolean;
  canAccessRoute: (requiredRole?: UserRole) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      company: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Login action
      login: async (email: string, password: string) => {
        console.log('🚀 AuthStore: Starting login for:', email);
        set({ isLoading: true, error: null });
        
        try {
          const response = await authService.login({ email, password });
          
          console.log('🚀 AuthStore: Login response received, updating state');
          
          // Set state synchronously after successful login
          set({
            user: response.user,
            company: response.user.company || null,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          // Force persistence by manually triggering storage
          const persistedState = {
            user: response.user,
            company: response.user.company || null,
            isAuthenticated: true,
          };
          
          // Ensure Zustand persistence happens immediately
          localStorage.setItem('auth-storage', JSON.stringify({
            state: persistedState,
            version: 1
          }));

          // TODO: Connect to WebSocket when backend WebSocket is implemented
          // if (response.user.id) {
          //   webSocketService.connect(response.user.id);
          // }

          // Verification after a small delay
          setTimeout(() => {
            const currentState = get();
            const tokenCheck = localStorage.getItem('accessToken');
            const userCheck = localStorage.getItem('user');
            const authStorageCheck = localStorage.getItem('auth-storage');
            
            console.log('✅ Login state verification:', {
              user: currentState.user?.email || 'None',
              isAuthenticated: currentState.isAuthenticated,
              hasToken: tokenCheck ? 'Yes' : 'No',
              hasUser: userCheck ? 'Yes' : 'No',
              hasAuthStorage: authStorageCheck ? 'Yes' : 'No',
              storeUserEmail: currentState.user?.email,
              tokenLength: tokenCheck?.length || 0
            });
            
            if (!tokenCheck || !currentState.isAuthenticated) {
              console.error('❌ Critical: Login verification failed!');
            }
          }, 100);

          console.log('✅ Login successful, state updated');
          toast.success(`Hoş geldiniz, ${response.user.firstName}!`);
        } catch (error: any) {
          console.error('❌ AuthStore: Login failed:', error);
          const errorMessage = error.response?.data?.message || 'Giriş yapılırken bir hata oluştu';
          set({
            isLoading: false,
            error: errorMessage,
            isAuthenticated: false,
            user: null,
            company: null,
          });
          throw error;
        }
      },

      // Register action
      register: async (userData: any) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authService.register(userData);
          
          set({
            user: response.user,
            company: response.user.company || null,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          // TODO: Connect to WebSocket when backend WebSocket is implemented
          // if (response.user.id) {
          //   webSocketService.connect(response.user.id);
          // }

          toast.success('Kayıt başarılı! Hoş geldiniz!');
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Kayıt olurken bir hata oluştu';
          set({
            isLoading: false,
            error: errorMessage,
            isAuthenticated: false,
            user: null,
            company: null,
          });
          throw error;
        }
      },

      // Logout action
      logout: async () => {
        set({ isLoading: true });
        
        try {
          await authService.logout();
          
          // Disconnect WebSocket
          webSocketService.disconnect();
          
          set({
            user: null,
            company: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });

          toast.success('Başarıyla çıkış yapıldı');
        } catch (error: any) {
          // Even if logout fails on server, clear local state
          webSocketService.disconnect();
          set({
            user: null,
            company: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
          
          console.error('Logout error:', error);
        }
      },

      // Get current user
      getCurrentUser: async () => {
        set({ isLoading: true, error: null });
        
        try {
          const user = await authService.getCurrentUser();
          
          set({
            user,
            company: user.company || null,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          // Ensure WebSocket connection
          if (user.id && !webSocketService.isConnected()) {
            webSocketService.connect(user.id);
          }
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Kullanıcı bilgileri alınamadı';
          set({
            isLoading: false,
            error: errorMessage,
            isAuthenticated: false,
            user: null,
            company: null,
          });
          
          // Disconnect WebSocket on auth failure
          webSocketService.disconnect();
          throw error;
        }
      },

      // Change password
      changePassword: async (currentPassword: string, newPassword: string) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.changePassword({ currentPassword, newPassword });
          
          set({ isLoading: false, error: null });
          toast.success('Şifre başarıyla değiştirildi');
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Şifre değiştirilemedi';
          set({ isLoading: false, error: errorMessage });
          throw error;
        }
      },

      // Request password reset
      requestPasswordReset: async (email: string) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.requestPasswordReset(email);
          
          set({ isLoading: false, error: null });
          toast.success('Şifre sıfırlama bağlantısı email adresinize gönderildi');
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Şifre sıfırlama isteği gönderilemedi';
          set({ isLoading: false, error: errorMessage });
          throw error;
        }
      },

      // Reset password
      resetPassword: async (token: string, newPassword: string) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.resetPassword(token, newPassword);
          
          set({ isLoading: false, error: null });
          toast.success('Şifre başarıyla sıfırlandı');
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Şifre sıfırlanamadı';
          set({ isLoading: false, error: errorMessage });
          throw error;
        }
      },

      // Verify email
      verifyEmail: async (token: string) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.verifyEmail(token);
          
          // Update user's email verification status
          const currentUser = get().user;
          if (currentUser) {
            set({
              user: { ...currentUser, emailVerified: true },
              isLoading: false,
              error: null,
            });
          }
          
          toast.success('Email adresiniz başarıyla doğrulandı');
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Email doğrulanamadı';
          set({ isLoading: false, error: errorMessage });
          throw error;
        }
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },

      // Set loading
      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },

      // Check if user has specific role
      hasRole: (role: UserRole) => {
        const { user } = get();
        return user?.role === role;
      },

      // Check if user has permission (role hierarchy)
      hasPermission: (role: UserRole) => {
        const { user } = get();
        if (!user) return false;

        const roleHierarchy = {
          [UserRole.SUPER_ADMIN]: 4,
          [UserRole.MANAGER]: 3,
          [UserRole.AGENT]: 2,
          [UserRole.CUSTOMER]: 1,
        };

        const userLevel = roleHierarchy[user.role] || 0;
        const requiredLevel = roleHierarchy[role] || 0;

        return userLevel >= requiredLevel;
      },

      // Check if user can access a route
      canAccessRoute: (requiredRole?: UserRole) => {
        const { isAuthenticated, user } = get();
        
        if (!isAuthenticated || !user) {
          return false;
        }

        if (!requiredRole) {
          return true; // No specific role required
        }

        return get().hasPermission(requiredRole);
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        company: state.company,
        isAuthenticated: state.isAuthenticated,
      }),
      // Add version to handle state migrations
      version: 1,
      // Prevent hydration conflicts
      skipHydration: false,
    }
  )
);

// Initialize auth state on app start
export const initializeAuth = async () => {
  const { isAuthenticated, getCurrentUser, logout, setLoading } = useAuthStore.getState();
  
  console.log('🚀 InitializeAuth called:', {
    storeIsAuthenticated: isAuthenticated,
    serviceIsAuthenticated: authService.isAuthenticated(),
    hasToken: localStorage.getItem('accessToken') ? 'Yes' : 'No',
    hasUser: localStorage.getItem('user') ? 'Yes' : 'No',
    hasAuthStorage: localStorage.getItem('auth-storage') ? 'Yes' : 'No',
    timestamp: new Date().toISOString()
  });
  
  // Check if we have a valid token in localStorage first
  const hasValidToken = authService.isAuthenticated();
  const hasStoredUser = localStorage.getItem('user');
  const hasStoredAuthState = localStorage.getItem('auth-storage');
  
  if (hasValidToken && hasStoredUser) {
    console.log('✅ Valid token and stored user found, using cached data');
    try {
      setLoading(true);
      
      // Use stored user data to avoid unnecessary API calls during initialization
      const storedUser = JSON.parse(hasStoredUser);
      
      // Also restore from Zustand persistence if available
      if (hasStoredAuthState) {
        try {
          const authStorage = JSON.parse(hasStoredAuthState);
          if (authStorage.state && authStorage.state.user) {
            console.log('🔄 Restoring from Zustand persistence');
            useAuthStore.setState({
              user: authStorage.state.user,
              company: authStorage.state.company || null,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });
          }
        } catch (authStorageError) {
          console.warn('Failed to parse auth storage, using fallback:', authStorageError);
          // Fallback to stored user
          useAuthStore.setState({
            user: storedUser,
            company: storedUser.company || null,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        }
      } else {
        // Use stored user as fallback
        useAuthStore.setState({
          user: storedUser,
          company: storedUser.company || null,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
      }
      
      console.log('✅ Auth state restored from cache');
      
      // Optional: Verify with backend in background (non-blocking)
      getCurrentUser().catch(error => {
        console.warn('Background user verification failed:', error);
        // Only logout if token is definitely invalid (401) and we're not on login page
        if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
          console.log('⚠️ Background verification failed with 401, logging out');
          logout();
        }
      });
      
    } catch (error) {
      console.error('❌ Failed to use stored user data:', error);
      await logout();
    } finally {
      setLoading(false);
    }
  } else if (hasValidToken) {
    console.log('✅ Valid token found, getting current user from API');
    try {
      setLoading(true);
      await getCurrentUser();
    } catch (error) {
      console.error('❌ Failed to get current user:', error);
      // Only logout if we're not on the login page
      if (!window.location.pathname.includes('/login')) {
        await logout();
      }
    } finally {
      setLoading(false);
    }
  } else if (isAuthenticated) {
    console.log('⚠️ Store shows authenticated but no valid token, logging out');
    // Clear inconsistent state
    await logout();
  } else {
    console.log('ℹ️ Not authenticated, no action needed');
    setLoading(false);
  }
};

export default useAuthStore;