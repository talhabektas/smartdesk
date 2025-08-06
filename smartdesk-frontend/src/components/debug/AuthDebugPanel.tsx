import React, { useState, useEffect } from 'react';
import { useAuthStore } from '../../store/authStore';
import { authDebugger } from '../../utils/authDebugger';
import { api } from '../../services/api';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const AuthDebugPanel: React.FC = () => {
  const { user, isAuthenticated } = useAuthStore();
  const [debugInfo, setDebugInfo] = useState<any>(null);
  const [testResults, setTestResults] = useState<any[]>([]);
  const [isCollapsed, setIsCollapsed] = useState(false);

  useEffect(() => {
    const updateDebugInfo = () => {
      setDebugInfo(authDebugger.getAuthSnapshot());
    };

    updateDebugInfo();
    const interval = setInterval(updateDebugInfo, 1000);
    return () => clearInterval(interval);
  }, []);

  const runApiTest = async (endpoint: string, description: string) => {
    const testId = Date.now();
    const testResult = {
      id: testId,
      endpoint,
      description,
      timestamp: new Date().toISOString(),
      status: 'running',
      error: null,
      hasAuthHeader: false,
      responseStatus: null
    };

    setTestResults(prev => [...prev, testResult]);

    try {
      console.log(`🧪 Testing API endpoint: ${endpoint}`);
      const response = await api.get(endpoint);
      
      setTestResults(prev => prev.map(test => 
        test.id === testId 
          ? { ...test, status: 'success', responseStatus: 200, hasAuthHeader: true }
          : test
      ));
    } catch (error: any) {
      console.error(`❌ API test failed for ${endpoint}:`, error);
      
      setTestResults(prev => prev.map(test => 
        test.id === testId 
          ? { 
              ...test, 
              status: 'failed', 
              error: error.message,
              responseStatus: error.response?.status,
              hasAuthHeader: !!error.config?.headers?.Authorization
            }
          : test
      ));
    }
  };

  const clearTests = () => {
    setTestResults([]);
  };

  const exportDebugInfo = () => {
    const debugData = {
      timestamp: new Date().toISOString(),
      authSnapshot: debugInfo,
      testResults,
      logs: authDebugger.exportLogs()
    };
    
    const blob = new Blob([JSON.stringify(debugData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `auth-debug-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  if (process.env.NODE_ENV !== 'development') {
    return null;
  }

  return (
    <div className={`fixed bottom-4 z-50 transition-all duration-300 ease-in-out ${
      isCollapsed ? 'right-0' : 'right-4'
    }`}>
      {/* Toggle Button */}
      <button
        onClick={() => setIsCollapsed(!isCollapsed)}
        className={`absolute top-4 bg-gray-900 text-white p-2 rounded-l-md shadow-lg hover:bg-gray-800 transition-all duration-200 z-10 ${
          isCollapsed ? 'left-0' : '-left-10'
        }`}
        style={{ transform: isCollapsed ? 'translateX(-100%)' : 'none' }}
      >
        {isCollapsed ? <ChevronLeft className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
      </button>

      {/* Debug Panel */}
      <Card className={`transition-all duration-300 ease-in-out overflow-hidden bg-gray-900 text-white text-xs ${
        isCollapsed 
          ? 'w-0 opacity-0 pointer-events-none' 
          : 'w-96 max-h-96 overflow-y-auto opacity-100'
      }`}>
        <div className="p-4">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-sm">🔍 Auth Debug Panel</h3>
            <div className="flex gap-2">
              <Button size="sm" variant="secondary" onClick={exportDebugInfo}>
                Export
              </Button>
              <Button size="sm" variant="secondary" onClick={() => authDebugger.clearLogs()}>
                Clear
              </Button>
            </div>
          </div>

          <div className="space-y-3">
            {/* Auth Status */}
            <div>
              <div className="font-semibold mb-1">Auth Status:</div>
              <div className={`px-2 py-1 rounded text-xs ${isAuthenticated ? 'bg-green-600' : 'bg-red-600'}`}>
                {isAuthenticated ? '✅ Authenticated' : '❌ Not Authenticated'}
              </div>
              {user && (
                <div className="mt-1 text-gray-300">
                  User: {user.email} ({user.role})
                </div>
              )}
            </div>

            {/* Token Info */}
            {debugInfo && (
              <div>
                <div className="font-semibold mb-1">Token Info:</div>
                <div className="bg-gray-800 p-2 rounded text-xs">
                  <div>Has Token: {debugInfo.localStorage.hasAccessToken ? '✅' : '❌'}</div>
                  <div>Token Length: {debugInfo.localStorage.accessTokenLength}</div>
                  {debugInfo.tokenInfo && (
                    <>
                      <div>Expires: {new Date(debugInfo.tokenInfo.expiresAt).toLocaleTimeString()}</div>
                      <div>Time Left: {Math.round(debugInfo.tokenInfo.timeUntilExpiry)}s</div>
                    </>
                  )}
                </div>
              </div>
            )}

            {/* API Tests */}
            <div>
              <div className="font-semibold mb-1">API Tests:</div>
              <div className="flex flex-wrap gap-1 mb-2">
                <Button 
                  size="sm" 
                  variant="primary" 
                  onClick={() => runApiTest('/auth/me', 'Get Current User')}
                  disabled={!isAuthenticated}
                >
                  Test /auth/me
                </Button>
                <Button 
                  size="sm" 
                  variant="primary" 
                  onClick={() => runApiTest('/users', 'Get Users')}
                  disabled={!isAuthenticated}
                >
                  Test /users
                </Button>
                <Button 
                  size="sm" 
                  variant="secondary" 
                  onClick={clearTests}
                >
                  Clear
                </Button>
              </div>
              
              <div className="bg-gray-800 p-2 rounded max-h-32 overflow-y-auto">
                {testResults.length === 0 ? (
                  <div className="text-gray-400">No tests run yet</div>
                ) : (
                  testResults.slice(-5).map(test => (
                    <div key={test.id} className="mb-1 text-xs">
                      <span className={
                        test.status === 'success' ? 'text-green-400' : 
                        test.status === 'failed' ? 'text-red-400' : 'text-yellow-400'
                      }>
                        {test.status === 'success' ? '✅' : test.status === 'failed' ? '❌' : '⏳'}
                      </span>
                      <span className="ml-1">{test.description}</span>
                      {test.responseStatus && (
                        <span className="ml-1 text-gray-400">({test.responseStatus})</span>
                      )}
                      {test.error && (
                        <div className="text-red-300 ml-4 truncate">{test.error}</div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Recent Logs */}
            <div>
              <div className="font-semibold mb-1">Recent Logs:</div>
              <div className="bg-gray-800 p-2 rounded max-h-24 overflow-y-auto">
                {debugInfo?.logs?.slice(-3).map((log: any, index: number) => (
                  <div key={index} className="text-xs mb-1">
                    <span className={
                      log.level === 'ERROR' ? 'text-red-400' : 
                      log.level === 'WARN' ? 'text-yellow-400' : 'text-gray-300'
                    }>
                      {log.level === 'ERROR' ? '❌' : log.level === 'WARN' ? '⚠️' : 'ℹ️'}
                    </span>
                    <span className="ml-1 truncate">{log.message}</span>
                  </div>
                )) || <div className="text-gray-400">No logs available</div>}
              </div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default AuthDebugPanel;