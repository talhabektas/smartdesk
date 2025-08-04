import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { toast } from 'react-hot-toast';
import { authDebugger } from '../utils/authDebugger';

// API Base Configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8067/api/v1';

// Debug API configuration at startup
console.log('🔧 API Configuration:', {
  REACT_APP_API_URL: process.env.REACT_APP_API_URL,
  API_BASE_URL,
  NODE_ENV: process.env.NODE_ENV
});

// Create axios instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token management
class TokenManager {
  private static instance: TokenManager;
  private accessToken: string | null = null;
  private refreshToken: string | null = null;
  private isRefreshing: boolean = false;
  private refreshPromise: Promise<any> | null = null;

  static getInstance(): TokenManager {
    if (!TokenManager.instance) {
      TokenManager.instance = new TokenManager();
    }
    return TokenManager.instance;
  }

  setTokens(accessToken: string, refreshToken: string): void {
    console.log('🔑 TokenManager: Setting new tokens', {
      accessTokenLength: accessToken?.length || 0,
      refreshTokenLength: refreshToken?.length || 0,
      timestamp: new Date().toISOString()
    });
    
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    
    // Force immediate storage and verification
    setTimeout(() => {
      const storedToken = localStorage.getItem('accessToken');
      console.log('✅ Token storage verified:', {
        stored: storedToken ? 'Yes' : 'No',
        matches: storedToken === accessToken ? 'Yes' : 'No'
      });
    }, 10);
  }

  getAccessToken(): string | null {
    // Always check localStorage for the most recent token
    const storedToken = localStorage.getItem('accessToken');
    if (storedToken !== this.accessToken) {
      console.log('🔄 TokenManager: Updating cached token from localStorage');
      this.accessToken = storedToken;
    }
    
    return this.accessToken;
  }

  getRefreshToken(): string | null {
    // Always check localStorage for the most recent token
    const storedToken = localStorage.getItem('refreshToken');
    if (storedToken !== this.refreshToken) {
      console.log('🔄 TokenManager: Updating cached refresh token from localStorage');
      this.refreshToken = storedToken;
    }
    
    return this.refreshToken;
  }

  clearTokens(): void {
    console.log('🗑️ TokenManager: Clearing all tokens');
    this.accessToken = null;
    this.refreshToken = null;
    this.isRefreshing = false;
    this.refreshPromise = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  isTokenExpired(token: string): boolean {
    if (!token) {
      console.log('⚠️ TokenManager: No token provided for expiry check');
      return true;
    }
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      const isExpired = payload.exp < currentTime;
      const timeLeft = payload.exp - currentTime;
      
      console.log('🕒 TokenManager: Token expiry check', {
        currentTime: new Date(currentTime * 1000).toISOString(),
        expiresAt: new Date(payload.exp * 1000).toISOString(),
        timeLeftSeconds: Math.round(timeLeft),
        isExpired
      });
      
      return isExpired;
    } catch (error) {
      console.error('❌ TokenManager: Error checking token expiry:', error);
      return true;
    }
  }

  // Check if token expires within the next 5 minutes
  isTokenExpiringSoon(token: string): boolean {
    if (!token) return true;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      const timeUntilExpiry = payload.exp - currentTime;
      
      // Return true if expires within 5 minutes (300 seconds)
      const expiringSoon = timeUntilExpiry < 300;
      
      if (expiringSoon) {
        console.log('⚠️ TokenManager: Token expires soon', {
          timeLeftSeconds: Math.round(timeUntilExpiry)
        });
      }
      
      return expiringSoon;
    } catch {
      return true;
    }
  }

  // Prevent multiple concurrent refresh attempts
  async refreshTokens(): Promise<any> {
    if (this.isRefreshing && this.refreshPromise) {
      console.log('🔄 TokenManager: Waiting for existing refresh operation');
      return this.refreshPromise;
    }

    this.isRefreshing = true;
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken || this.isTokenExpired(refreshToken)) {
      console.log('❌ TokenManager: No valid refresh token available');
      this.isRefreshing = false;
      throw new Error('No valid refresh token');
    }

    console.log('🔄 TokenManager: Starting token refresh...');
    this.refreshPromise = axios.post(`${API_BASE_URL}/auth/refresh`, {
      refreshToken: refreshToken,
    });

    try {
      const response = await this.refreshPromise;
      
      // CRITICAL FIX: Handle backend's snake_case response format
      const rawData = response.data;
      const accessToken = rawData.accessToken || rawData.access_token;
      const newRefreshToken = rawData.refreshToken || rawData.refresh_token;
      
      console.log('🔄 TokenManager: Normalizing refresh response:', {
        hasAccessTokenCamel: rawData.accessToken ? 'Yes' : 'No',
        hasRefreshTokenCamel: rawData.refreshToken ? 'Yes' : 'No',
        hasAccessTokenSnake: rawData.access_token ? 'Yes' : 'No',
        hasRefreshTokenSnake: rawData.refresh_token ? 'Yes' : 'No',
        finalAccessTokenLength: accessToken?.length || 0,
        finalRefreshTokenLength: newRefreshToken?.length || 0
      });
      
      if (!accessToken || !newRefreshToken) {
        throw new Error('Invalid refresh response: missing tokens');
      }
      
      this.setTokens(accessToken, newRefreshToken);
      console.log('✅ TokenManager: Token refresh successful');
      
      // Return normalized response
      return {
        accessToken,
        refreshToken: newRefreshToken,
        tokenType: rawData.tokenType || rawData.token_type || 'Bearer',
        expiresIn: rawData.expiresIn || rawData.expires_in || 3600,
        user: rawData.user
      };
    } catch (error) {
      console.error('❌ TokenManager: Token refresh failed:', error);
      this.clearTokens();
      throw error;
    } finally {
      this.isRefreshing = false;
      this.refreshPromise = null;
    }
  }
}

export const tokenManager = TokenManager.getInstance();

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  async (config) => {
    const token = tokenManager.getAccessToken();
    
    console.log('🔍 Request interceptor triggered:', {
      url: config.url,
      baseURL: config.baseURL,
      fullURL: `${config.baseURL}${config.url}`,
      method: config.method?.toUpperCase(),
      hasToken: token ? 'Yes' : 'No',
      tokenLength: token?.length || 0
    });
    
    if (token) {
      // Check if token is expired or expiring soon
      if (tokenManager.isTokenExpired(token)) {
        console.log('⚠️ Token expired, attempting refresh before request');
        try {
          await tokenManager.refreshTokens();
          const newToken = tokenManager.getAccessToken();
          if (newToken) {
            config.headers.Authorization = `Bearer ${newToken}`;
            console.log('✅ Using refreshed token for request');
          } else {
            console.log('❌ No token after refresh, request will be unauthorized');
          }
        } catch (error) {
          console.log('❌ Token refresh failed, clearing tokens');
          tokenManager.clearTokens();
        }
      } else if (tokenManager.isTokenExpiringSoon(token)) {
        console.log('⚠️ Token expires soon, refreshing in background');
        // Refresh in background but use current token for this request
        tokenManager.refreshTokens().catch(error => {
          console.warn('Background token refresh failed:', error);
        });
        config.headers.Authorization = `Bearer ${token}`;
      } else {
        // Token is valid, use it
        config.headers.Authorization = `Bearer ${token}`;
        console.log('✅ Using valid token for request:', {
          tokenPreview: `${token.substring(0, 20)}...`,
          hasAuthHeader: 'Yes'
        });
      }
    } else {
      console.log('ℹ️ No token available for request:', config.url);
    }
    
    // Final verification
    const hasAuthHeader = config.headers.Authorization ? 'Yes' : 'No';
    const authHeader = config.headers.Authorization;
    const authHeaderStr = authHeader ? String(authHeader) : '';
    
    console.log('📤 Final request config:', {
      url: config.url,
      hasAuthHeader,
      authHeaderPreview: authHeaderStr ? `${authHeaderStr.substring(0, 20)}...` : 'None'
    });
    
    // Track in auth debugger
    authDebugger.trackApiRequest(
      config.url || 'unknown', 
      hasAuthHeader === 'Yes', 
      authHeaderStr || undefined
    );
    
    return config;
  },
  (error) => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - Handle token refresh and errors
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('✅ API Response:', {
      url: response.config.url,
      status: response.status,
      hasAuth: response.config.headers.Authorization ? 'Yes' : 'No'
    });
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    console.log('❌ API Error:', {
      url: originalRequest?.url,
      status: error.response?.status,
      message: (error.response?.data as any)?.message
    });

    // Token expired, try to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      console.log('🔄 401 error detected, attempting token refresh');
      originalRequest._retry = true;

      try {
        console.log('🔄 Starting token refresh process...');
        const refreshResponse = await tokenManager.refreshTokens();
        const newToken = tokenManager.getAccessToken();
        
        if (newToken) {
          console.log('✅ Token refreshed successfully, retrying original request');
          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        } else {
          throw new Error('No token after refresh');
        }
      } catch (refreshError) {
        console.error('❌ Token refresh failed:', refreshError);
        // Only redirect to login if we're not already on the login page
        if (!window.location.pathname.includes('/login')) {
          console.log('🔄 Redirecting to login page');
          tokenManager.clearTokens();
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors (only show toasts for non-auth errors)
    if (error.response?.status === 401) {
      // Don't show toast for 401 errors as they're handled above
      console.log('ℹ️ 401 error handled by refresh logic');
    } else if (error.response?.status && error.response.status >= 500) {
      toast.error('Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin.');
    } else if (error.response?.status === 403) {
      toast.error('Bu işlem için yetkiniz bulunmuyor.');
    } else if (error.response?.status === 404) {
      toast.error('İstenen kaynak bulunamadı.');
    } else if (error.response?.data && typeof error.response.data === 'object' && 'message' in error.response.data) {
      // Only show error toast if it's not an auth-related error
      const message = (error.response.data as any).message;
      if (!message.toLowerCase().includes('unauthorized') && !message.toLowerCase().includes('authentication')) {
        toast.error(message);
      }
    }

    return Promise.reject(error);
  }
);

// Generic API methods
export const api = {
  // GET request
  get: async <T = any>(url: string, config?: any): Promise<T> => {
    const response = await apiClient.get<T>(url, config);
    return response.data;
  },

  // POST request
  post: async <T = any>(url: string, data?: any, config?: any): Promise<T> => {
    const response = await apiClient.post<T>(url, data, config);
    return response.data;
  },

  // PUT request
  put: async <T = any>(url: string, data?: any, config?: any): Promise<T> => {
    const response = await apiClient.put<T>(url, data, config);
    return response.data;
  },

  // PATCH request
  patch: async <T = any>(url: string, data?: any, config?: any): Promise<T> => {
    const response = await apiClient.patch<T>(url, data, config);
    return response.data;
  },

  // DELETE request
  delete: async <T = any>(url: string, config?: any): Promise<T> => {
    const response = await apiClient.delete<T>(url, config);
    return response.data;
  },

  // File upload
  upload: async <T = any>(url: string, file: File, onProgress?: (progress: number) => void): Promise<T> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<T>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    });

    return response.data;
  },

  // Download file
  download: async (url: string, fileName?: string): Promise<void> => {
    const response = await apiClient.get(url, {
      responseType: 'blob',
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  },
};

// Helper functions for common patterns
export const handleApiError = (error: any, defaultMessage: string = 'Bir hata oluştu') => {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  return defaultMessage;
};

export const createFormData = (data: Record<string, any>): FormData => {
  const formData = new FormData();
  Object.entries(data).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      if (value instanceof File) {
        formData.append(key, value);
      } else {
        formData.append(key, String(value));
      }
    }
  });
  return formData;
};

// Export configured axios instance for direct use if needed
export { apiClient as axiosInstance };
export default api;