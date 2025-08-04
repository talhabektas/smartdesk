// Authentication Debugging Utility
// This utility helps trace authentication flow and identify issues

export class AuthDebugger {
  private static instance: AuthDebugger;
  private logs: Array<{ timestamp: string; level: string; message: string; data?: any }> = [];

  static getInstance(): AuthDebugger {
    if (!AuthDebugger.instance) {
      AuthDebugger.instance = new AuthDebugger();
    }
    return AuthDebugger.instance;
  }

  private log(level: 'INFO' | 'WARN' | 'ERROR', message: string, data?: any) {
    const entry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      data
    };
    
    this.logs.push(entry);
    
    // Keep only last 100 entries
    if (this.logs.length > 100) {
      this.logs = this.logs.slice(-100);
    }

    // Console output with emoji for better visibility
    const emoji = level === 'ERROR' ? '❌' : level === 'WARN' ? '⚠️' : '📝';
    console.log(`${emoji} [AuthDebugger] ${message}`, data || '');
  }

  info(message: string, data?: any) {
    this.log('INFO', message, data);
  }

  warn(message: string, data?: any) {
    this.log('WARN', message, data);
  }

  error(message: string, data?: any) {
    this.log('ERROR', message, data);
  }

  // Get full authentication state snapshot
  getAuthSnapshot(): any {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const user = localStorage.getItem('user');
    const authStorage = localStorage.getItem('auth-storage');

    let tokenPayload = null;
    if (accessToken) {
      try {
        tokenPayload = JSON.parse(atob(accessToken.split('.')[1]));
      } catch (e) {
        tokenPayload = 'Invalid token format';
      }
    }

    return {
      timestamp: new Date().toISOString(),
      localStorage: {
        hasAccessToken: !!accessToken,
        hasRefreshToken: !!refreshToken,
        hasUser: !!user,
        hasAuthStorage: !!authStorage,
        accessTokenLength: accessToken?.length || 0,
        refreshTokenLength: refreshToken?.length || 0
      },
      tokenInfo: tokenPayload ? {
        subject: tokenPayload.sub,
        email: tokenPayload.email,
        role: tokenPayload.role,
        issuedAt: new Date(tokenPayload.iat * 1000).toISOString(),
        expiresAt: new Date(tokenPayload.exp * 1000).toISOString(),
        timeUntilExpiry: tokenPayload.exp - (Date.now() / 1000)
      } : null,
      userInfo: user ? JSON.parse(user) : null,
      logs: this.logs.slice(-10) // Last 10 log entries
    };
  }

  // Print detailed auth state to console
  printAuthState() {
    const snapshot = this.getAuthSnapshot();
    console.group('🔍 Authentication State Snapshot');
    console.log('📅 Timestamp:', snapshot.timestamp);
    console.log('💾 LocalStorage:', snapshot.localStorage);
    console.log('🎫 Token Info:', snapshot.tokenInfo);
    console.log('👤 User Info:', snapshot.userInfo);
    console.log('📋 Recent Logs:', snapshot.logs);
    console.groupEnd();
  }

  // Track API request with auth header
  trackApiRequest(url: string, hasAuthHeader: boolean, tokenPreview?: string) {
    this.info(`API Request: ${url}`, {
      hasAuthHeader,
      tokenPreview: tokenPreview ? `${tokenPreview.substring(0, 20)}...` : 'None'
    });
  }

  // Track login attempt
  trackLoginAttempt(email: string, success: boolean, error?: any) {
    if (success) {
      this.info(`Login successful for: ${email}`);
    } else {
      this.error(`Login failed for: ${email}`, error);
    }
  }

  // Track token refresh
  trackTokenRefresh(success: boolean, error?: any) {
    if (success) {
      this.info('Token refresh successful');
    } else {
      this.error('Token refresh failed', error);
    }
  }

  // Clear logs
  clearLogs() {
    this.logs = [];
    this.info('Auth debugger logs cleared');
  }

  // Export logs for debugging
  exportLogs(): string {
    return JSON.stringify({
      exportTimestamp: new Date().toISOString(),
      authSnapshot: this.getAuthSnapshot(),
      allLogs: this.logs
    }, null, 2);
  }

  // Add to window for development debugging
  attachToWindow() {
    if (typeof window !== 'undefined') {
      (window as any).authDebugger = {
        printState: () => this.printAuthState(),
        snapshot: () => this.getAuthSnapshot(),
        exportLogs: () => this.exportLogs(),
        clearLogs: () => this.clearLogs()
      };
      console.log('🛠️ Auth debugger attached to window.authDebugger');
    }
  }
}

export const authDebugger = AuthDebugger.getInstance();

// Auto-attach in development
if (process.env.NODE_ENV === 'development') {
  authDebugger.attachToWindow();
}

export default authDebugger;