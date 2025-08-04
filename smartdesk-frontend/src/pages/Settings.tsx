import React from 'react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Settings as SettingsIcon } from 'lucide-react';

const Settings: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Settings
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Configure system preferences and user settings
          </p>
        </div>
        
        <Button 
          variant="primary"
          leftIcon={<SettingsIcon className="w-4 h-4" />}
        >
          Save Changes
        </Button>
      </div>

      <Card className="p-16 text-center">
        <div className="animate-apple-spin">
          <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <SettingsIcon className="w-8 h-8 text-gray-600" />
          </div>
        </div>
        
        <h2 className="text-title-3 text-gray-900 font-semibold mb-2">
          System Configuration
        </h2>
        <p className="text-body text-gray-600 mb-6 max-w-md mx-auto">
          Comprehensive settings panel for system configuration, user preferences, 
          integrations, and security settings.
        </p>
        
        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Button variant="secondary">
            User Preferences
          </Button>
          <Button variant="outline">
            System Settings
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default Settings;