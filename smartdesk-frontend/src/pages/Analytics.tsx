import React from 'react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { BarChart3 } from 'lucide-react';

const Analytics: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Analytics
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Insights and performance metrics for your support team
          </p>
        </div>
        
        <Button 
          variant="primary"
          leftIcon={<BarChart3 className="w-4 h-4" />}
        >
          Generate Report
        </Button>
      </div>

      <Card className="p-16 text-center">
        <div className="animate-bounce-gentle">
          <div className="w-16 h-16 bg-info-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <BarChart3 className="w-8 h-8 text-info-600" />
          </div>
        </div>
        
        <h2 className="text-title-3 text-gray-900 font-semibold mb-2">
          Advanced Analytics
        </h2>
        <p className="text-body text-gray-600 mb-6 max-w-md mx-auto">
          Detailed analytics dashboard with performance metrics, trends analysis, 
          and custom reporting capabilities.
        </p>
        
        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Button variant="info">
            View Reports
          </Button>
          <Button variant="outline">
            Export Data
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default Analytics;