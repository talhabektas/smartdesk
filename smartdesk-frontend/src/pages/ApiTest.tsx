import React, { useState } from 'react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { api } from '../services/api';
import { useAuthStore } from '../store/authStore';
import { toast } from 'react-hot-toast';
import { 
  CheckCircle, 
  XCircle, 
  Clock, 
  Globe,
  Shield,
  User,
  RefreshCw,
} from 'lucide-react';

interface TestResult {
  name: string;
  status: 'success' | 'error' | 'pending';
  message: string;
  response?: any;
  duration?: number;
}

const ApiTest: React.FC = () => {
  const { user } = useAuthStore();
  const [results, setResults] = useState<TestResult[]>([]);
  const [isRunning, setIsRunning] = useState(false);

  const endpoints = [
    {
      name: 'Test Hello',
      url: '/v1/test/hello',
      method: 'GET',
      requiresAuth: false,
    },
    {
      name: 'Test Status',
      url: '/v1/test/status',
      method: 'GET',
      requiresAuth: false,
    },
    {
      name: 'Auth Me',
      url: '/v1/auth/me',
      method: 'GET',
      requiresAuth: true,
    },
    {
      name: 'Actuator Health',
      url: '/actuator/health',
      method: 'GET',
      requiresAuth: false,
    },
    {
      name: 'Actuator Mappings',
      url: '/actuator/mappings',
      method: 'GET',
      requiresAuth: false,
    },
  ];

  const testEndpoint = async (endpoint: any): Promise<TestResult> => {
    const startTime = Date.now();
    
    try {
      let response;
      if (endpoint.method === 'GET') {
        response = await api.get(endpoint.url);
      } else {
        response = await api.post(endpoint.url, {});
      }
      
      const duration = Date.now() - startTime;
      
      return {
        name: endpoint.name,
        status: 'success',
        message: 'Success',
        response,
        duration,
      };
    } catch (error: any) {
      const duration = Date.now() - startTime;
      
      return {
        name: endpoint.name,
        status: 'error',
        message: error.response?.data?.message || error.message || 'Unknown error',
        response: error.response?.data,
        duration,
      };
    }
  };

  const runAllTests = async () => {
    setIsRunning(true);
    setResults([]);
    
    const testResults: TestResult[] = [];
    
    for (const endpoint of endpoints) {
      // Add pending result
      const pendingResult: TestResult = {
        name: endpoint.name,
        status: 'pending',
        message: 'Testing...',
      };
      
      testResults.push(pendingResult);
      setResults([...testResults]);
      
      // Run test
      const result = await testEndpoint(endpoint);
      
      // Update result
      testResults[testResults.length - 1] = result;
      setResults([...testResults]);
      
      // Small delay between tests
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    setIsRunning(false);
    
    const successCount = testResults.filter(r => r.status === 'success').length;
    const totalCount = testResults.length;
    
    if (successCount === totalCount) {
      toast.success(`All ${totalCount} tests passed!`);
    } else {
      toast.error(`${successCount}/${totalCount} tests passed`);
    }
  };

  const clearResults = () => {
    setResults([]);
  };

  const getStatusIcon = (status: TestResult['status']) => {
    switch (status) {
      case 'success':
        return <CheckCircle className="w-5 h-5 text-success-500" />;
      case 'error':
        return <XCircle className="w-5 h-5 text-error-500" />;
      case 'pending':
        return <Clock className="w-5 h-5 text-warning-500 animate-spin" />;
      default:
        return null;
    }
  };

  const getStatusBadge = (status: TestResult['status']) => {
    switch (status) {
      case 'success':
        return <Badge variant="success">Success</Badge>;
      case 'error':
        return <Badge variant="danger">Error</Badge>;
      case 'pending':
        return <Badge variant="warning">Testing...</Badge>;
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            API Test Dashboard
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Test backend API endpoints and connectivity
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button 
            variant="secondary" 
            onClick={clearResults}
            disabled={isRunning}
          >
            Clear Results
          </Button>
          <Button 
            variant="primary" 
            onClick={runAllTests}
            disabled={isRunning}
            leftIcon={isRunning ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Globe className="w-4 h-4" />}
          >
            {isRunning ? 'Running Tests...' : 'Run All Tests'}
          </Button>
        </div>
      </div>

      {/* User Info */}
      {user && (
        <Card>
          <div className="flex items-center gap-4">
            <User className="w-8 h-8 text-gray-400" />
            <div>
              <h3 className="font-medium text-gray-900">
                {user.firstName} {user.lastName}
              </h3>
              <p className="text-sm text-gray-600">
                {user.email} • {user.role}
              </p>
            </div>
            <div className="ml-auto">
              <Badge variant="success">Authenticated</Badge>
            </div>
          </div>
        </Card>
      )}

      {/* Endpoints List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Available Endpoints</h2>
          <div className="space-y-3">
            {endpoints.map((endpoint, index) => (
              <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">{endpoint.name}</span>
                    {endpoint.requiresAuth && (
                      <Shield className="w-4 h-4 text-warning-500" />
                    )}
                  </div>
                  <p className="text-sm text-gray-600">
                    {endpoint.method} {endpoint.url}
                  </p>
                </div>
                <Badge variant="secondary">{endpoint.method}</Badge>
              </div>
            ))}
          </div>
        </Card>

        {/* Test Results */}
        <Card>
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Test Results</h2>
          {results.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <Globe className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>No tests run yet. Click "Run All Tests" to begin.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {results.map((result, index) => (
                <div key={index} className="p-3 border border-gray-200 rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(result.status)}
                      <span className="font-medium text-gray-900">{result.name}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      {result.duration && (
                        <span className="text-xs text-gray-500">{result.duration}ms</span>
                      )}
                      {getStatusBadge(result.status)}
                    </div>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{result.message}</p>
                  
                  {result.response && (
                    <details className="text-xs">
                      <summary className="cursor-pointer text-primary-600 hover:text-primary-700">
                        View Response
                      </summary>
                      <pre className="mt-2 p-2 bg-gray-100 rounded overflow-x-auto">
                        {JSON.stringify(result.response, null, 2)}
                      </pre>
                    </details>
                  )}
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* Summary */}
      {results.length > 0 && (
        <Card>
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Test Summary</h3>
          <div className="grid grid-cols-3 gap-4">
            <div className="text-center p-4 bg-success-50 rounded-lg">
              <p className="text-2xl font-bold text-success-900">
                {results.filter(r => r.status === 'success').length}
              </p>
              <p className="text-sm text-success-600">Passed</p>
            </div>
            <div className="text-center p-4 bg-error-50 rounded-lg">
              <p className="text-2xl font-bold text-error-900">
                {results.filter(r => r.status === 'error').length}
              </p>
              <p className="text-sm text-error-600">Failed</p>
            </div>
            <div className="text-center p-4 bg-info-50 rounded-lg">
              <p className="text-2xl font-bold text-info-900">
                {results.length}
              </p>
              <p className="text-sm text-info-600">Total</p>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
};

export default ApiTest;