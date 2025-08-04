import React from 'react';
import { ChatMessage } from '../../types';

interface ChatMessageItemProps {
    message: ChatMessage;
    isOwnMessage: boolean;
}

const ChatMessageItem: React.FC<ChatMessageItemProps> = ({ message, isOwnMessage }) => {
    const formatTime = (dateString: string) => {
        return new Date(dateString).toLocaleTimeString('tr-TR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className={`flex ${isOwnMessage ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${isOwnMessage
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-900'
                }`}>
                <div className="text-sm">
                    {!isOwnMessage && (
                        <div className="font-medium text-xs mb-1 text-gray-600">
                            {message.senderName}
                        </div>
                    )}
                    <div className="break-words">
                        {message.content}
                    </div>
                    <div className={`text-xs mt-1 ${isOwnMessage ? 'text-blue-100' : 'text-gray-500'
                        }`}>
                        {formatTime(message.createdAt)}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ChatMessageItem; 