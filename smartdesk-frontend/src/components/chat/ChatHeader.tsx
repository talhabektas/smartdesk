import React from 'react';
import { X, MoreVertical } from 'lucide-react';

interface ChatHeaderProps {
    title: string;
    subtitle?: string;
    onClose?: () => void;
    onMenuClick?: () => void;
}

const ChatHeader: React.FC<ChatHeaderProps> = ({
    title,
    subtitle,
    onClose,
    onMenuClick
}) => {
    return (
        <div className="flex items-center justify-between p-4 border-b border-gray-200 bg-white">
            <div className="flex-1 min-w-0">
                <h3 className="text-lg font-semibold text-gray-900 truncate">
                    {title}
                </h3>
                {subtitle && (
                    <p className="text-sm text-gray-500 truncate">
                        {subtitle}
                    </p>
                )}
            </div>

            <div className="flex items-center space-x-2">
                {onMenuClick && (
                    <button
                        onClick={onMenuClick}
                        className="p-2 text-gray-500 hover:text-gray-700 rounded-lg hover:bg-gray-100"
                        title="Menü"
                    >
                        <MoreVertical className="w-5 h-5" />
                    </button>
                )}

                {onClose && (
                    <button
                        onClick={onClose}
                        className="p-2 text-gray-500 hover:text-gray-700 rounded-lg hover:bg-gray-100"
                        title="Kapat"
                    >
                        <X className="w-5 h-5" />
                    </button>
                )}
            </div>
        </div>
    );
};

export default ChatHeader;