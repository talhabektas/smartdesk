import { api, tokenManager } from './api';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  TokenValidationResponse,
  ChangePasswordRequest,
  User,
} from '@/types';
import { authDebugger } from '../utils/authDebugger';

class AuthService {
  private static instance: AuthService;

  static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  // Login user
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    console.log('🚀 AuthService: Starting login process for:', credentials.email);
    
    try {
      const response = await api.post<AuthResponse>('/auth/login', credentials);
      
      console.log('🚀 RAW BACKEND RESPONSE ANALYSIS:');
      console.log('Response type:', typeof response);
      console.log('Response keys:', Object.keys(response || {}));
      console.log('Full response:', JSON.stringify(response, null, 2));
      
      // Check all possible token field variations
      const rawResponse = response as any;
      console.log('TOKEN FIELD ANALYSIS:');
      console.log('response.accessToken:', rawResponse.accessToken);
      console.log('response.access_token:', rawResponse.access_token);
      console.log('response.token:', rawResponse.token);
      console.log('response.jwt:', rawResponse.jwt);
      console.log('response.authToken:', rawResponse.authToken);
      console.log('response.bearerToken:', rawResponse.bearerToken);
      
      // Check user field
      console.log('USER FIELD ANALYSIS:');
      console.log('response.user:', rawResponse.user);
      console.log('response.data:', rawResponse.data);
      
      // Try to extract tokens from any possible location
      const accessToken = rawResponse.accessToken || 
                         rawResponse.access_token || 
                         rawResponse.token || 
                         rawResponse.jwt || 
                         rawResponse.authToken ||
                         rawResponse.bearerToken ||
                         rawResponse.data?.accessToken ||
                         rawResponse.data?.access_token ||
                         rawResponse.data?.token;
                         
      const refreshToken = rawResponse.refreshToken || 
                          rawResponse.refresh_token || 
                          rawResponse.data?.refreshToken ||
                          rawResponse.data?.refresh_token;
      
      const user = rawResponse.user || rawResponse.data?.user || rawResponse.data;
      
      console.log('EXTRACTED VALUES:');
      console.log('accessToken:', accessToken ? `Found (${accessToken.substring(0, 20)}...)` : 'NOT FOUND');
      console.log('refreshToken:', refreshToken ? `Found (${refreshToken.substring(0, 20)}...)` : 'NOT FOUND');
      console.log('user:', user?.email || 'NOT FOUND');
      
      if (!accessToken) {
        console.error('❌ CRITICAL: No access token found in response!');
        console.error('Available fields:', Object.keys(rawResponse));
        throw new Error('Login response missing access token');
      }
      
      const normalizedResponse: AuthResponse = {
        accessToken,
        refreshToken: refreshToken || 'dummy-refresh', // Fallback if no refresh token
        tokenType: rawResponse.tokenType || rawResponse.token_type || 'Bearer',
        expiresIn: rawResponse.expiresIn || rawResponse.expires_in || 3600,
        user: user
      };
      
      // Store tokens with immediate verification
      console.log('💾 Storing tokens...');
      tokenManager.setTokens(normalizedResponse.accessToken, normalizedResponse.refreshToken);
      
      // Store user data
      console.log('💾 Storing user data...');
      localStorage.setItem('user', JSON.stringify(normalizedResponse.user));
      
      // Immediate verification
      setTimeout(() => {
        const storedToken = localStorage.getItem('accessToken');
        const storedUser = localStorage.getItem('user');
        console.log('✅ Storage verification:', {
          hasStoredToken: storedToken ? 'Yes' : 'No',
          hasStoredUser: storedUser ? 'Yes' : 'No',
          tokenLength: storedToken?.length || 0
        });
      }, 10);
      
      console.log('✅ AuthService: Login successful for:', credentials.email);
      return normalizedResponse;
      
    } catch (error: any) {
      console.error('❌ AuthService: Login failed for:', credentials.email);
      console.error('Error details:', error);
      throw error;
    }
  }

  // Register new user
  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', userData);
    
    // CRITICAL FIX: Handle backend's snake_case response format
    const rawResponse = response as any;
    const normalizedResponse: AuthResponse = {
      accessToken: response.accessToken || rawResponse.access_token,
      refreshToken: response.refreshToken || rawResponse.refresh_token,
      tokenType: response.tokenType || rawResponse.token_type || 'Bearer',
      expiresIn: response.expiresIn || rawResponse.expires_in || 3600,
      user: response.user
    };
    
    // Store tokens
    tokenManager.setTokens(normalizedResponse.accessToken, normalizedResponse.refreshToken);
    
    // Store user data
    localStorage.setItem('user', JSON.stringify(normalizedResponse.user));
    
    return normalizedResponse;
  }

  // Refresh access token
  async refreshToken(): Promise<AuthResponse> {
    const refreshToken = tokenManager.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await api.post<AuthResponse>('/auth/refresh', {
      refreshToken,
    });

    // CRITICAL FIX: Handle backend's snake_case response format
    const rawResponse = response as any;
    const normalizedResponse: AuthResponse = {
      accessToken: response.accessToken || rawResponse.access_token,
      refreshToken: response.refreshToken || rawResponse.refresh_token,
      tokenType: response.tokenType || rawResponse.token_type || 'Bearer',
      expiresIn: response.expiresIn || rawResponse.expires_in || 3600,
      user: response.user
    };

    // Update stored tokens
    tokenManager.setTokens(normalizedResponse.accessToken, normalizedResponse.refreshToken);
    
    return normalizedResponse;
  }

  // Logout user
  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      // Continue with logout even if API call fails
      console.warn('Logout API call failed:', error);
    } finally {
      // Clear all stored data
      tokenManager.clearTokens();
      localStorage.removeItem('user');
    }
  }

  // Validate current token
  async validateToken(): Promise<TokenValidationResponse> {
    return await api.post<TokenValidationResponse>('/auth/validate-token');
  }

  // Get current user info
  async getCurrentUser(): Promise<User> {
    console.log('👤 AuthService: Getting current user info');
    
    // First try to get user from localStorage
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const user = JSON.parse(storedUser);
        console.log('✅ Using stored user data:', {
          email: user.email,
          role: user.role,
          id: user.id
        });
        return user;
      } catch (error) {
        console.error('❌ Failed to parse stored user data:', error);
        localStorage.removeItem('user'); // Remove corrupted data
      }
    }

    // If no stored user, try API call
    console.log('🌐 Fetching user from API...');
    try {
      const response = await api.get<{user: User}>('/auth/me');
      
      console.log('✅ User fetched from /auth/me:', response.user.email);
      
      // Update stored user data
      localStorage.setItem('user', JSON.stringify(response.user));
      
      return response.user;
    } catch (error) {
      console.error('❌ Failed to get user from /auth/me:', error);
      // If API fails, try alternative endpoint
      try {
        const response = await api.get<User>('/users/me');
        console.log('✅ User fetched from /users/me:', response.email);
        localStorage.setItem('user', JSON.stringify(response));
        return response;
      } catch (fallbackError) {
        console.error('❌ Failed to get user from both endpoints:', fallbackError);
        authDebugger.error('Failed to get user from all endpoints', fallbackError);
        throw fallbackError;
      }
    }
  }

  // Change password
  async changePassword(passwordData: ChangePasswordRequest): Promise<void> {
    await api.post('/auth/change-password', passwordData);
  }

  // Request password reset
  async requestPasswordReset(email: string): Promise<void> {
    await api.post('/auth/forgot-password', { email });
  }

  // Reset password with token
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/auth/reset-password', {
      token,
      newPassword,
    });
  }

  // Verify email with token
  async verifyEmail(token: string): Promise<void> {
    await api.post(`/auth/verify-email?token=${token}`);
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    const token = tokenManager.getAccessToken();
    const isValid = token !== null && !tokenManager.isTokenExpired(token);
    
    console.log('🔍 AuthService: Authentication check:', {
      hasToken: token ? 'Yes' : 'No',
      tokenLength: token?.length || 0,
      isExpired: token ? tokenManager.isTokenExpired(token) : 'N/A',
      isAuthenticated: isValid,
      timestamp: new Date().toISOString()
    });
    
    return isValid;
  }

  // Get stored user data
  getCurrentUserFromStorage(): User | null {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        return JSON.parse(userData);
      } catch {
        return null;
      }
    }
    return null;
  }

  // Check if user has specific role
  hasRole(requiredRole: string): boolean {
    const user = this.getCurrentUserFromStorage();
    return user?.role === requiredRole;
  }

  // Check if user has permission (role hierarchy)
  hasPermission(requiredRole: string): boolean {
    const user = this.getCurrentUserFromStorage();
    if (!user) return false;

    const roleHierarchy = {
      'SUPER_ADMIN': 4,
      'MANAGER': 3,
      'AGENT': 2,
      'CUSTOMER': 1,
    };

    const userLevel = roleHierarchy[user.role as keyof typeof roleHierarchy] || 0;
    const requiredLevel = roleHierarchy[requiredRole as keyof typeof roleHierarchy] || 0;

    return userLevel >= requiredLevel;
  }

  // Get user's company ID
  getUserCompanyId(): number | null {
    const user = this.getCurrentUserFromStorage();
    return user?.company?.id || null;
  }

  // Get user's department ID
  getUserDepartmentId(): number | null {
    const user = this.getCurrentUserFromStorage();
    return user?.department?.id || null;
  }

  // Check auth health
  async checkAuthHealth(): Promise<boolean> {
    try {
      await api.get('/auth/health');
      return true;
    } catch {
      return false;
    }
  }

  // Auto-refresh token if needed
  async ensureValidToken(): Promise<boolean> {
    const token = tokenManager.getAccessToken();
    
    if (!token) {
      return false;
    }

    // Check if token expires within 5 minutes
    if (this.isTokenExpiringSoon(token)) {
      try {
        await this.refreshToken();
        return true;
      } catch {
        return false;
      }
    }

    return true;
  }

  // Check if token expires soon
  private isTokenExpiringSoon(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      const timeUntilExpiry = payload.exp - currentTime;
      
      // Return true if expires within 5 minutes (300 seconds)
      return timeUntilExpiry < 300;
    } catch {
      return true;
    }
  }
}

export const authService = AuthService.getInstance();
export default authService;